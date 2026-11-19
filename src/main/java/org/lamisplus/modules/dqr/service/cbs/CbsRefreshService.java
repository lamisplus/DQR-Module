package org.lamisplus.modules.dqr.service.cbs;

import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.cbs.CbsSettings;
import org.lamisplus.modules.dqr.domain.cbs.PatientAssessment;
import org.lamisplus.modules.dqr.domain.cbs.PatientSource;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsRefreshRun;
import org.lamisplus.modules.dqr.repository.cbs.CbsClinicalRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsRefreshRunRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Rebuilds a facility's CBS data from the clinical tables.
 * Runs one at a time on its own thread so a big facility can't hold up web requests,
 * and two refreshes of the same facility can never overlap.
 */
@Slf4j
@Service
public class CbsRefreshService {

    private final CbsSourceLoader sourceLoader;
    private final CbsEngine engine;
    private final CbsStore store;
    private final CbsSettingsService settings;
    private final CbsRefreshRunRepository runRepository;
    private final CbsClinicalRepository clinicalRepository;
    private final TransactionTemplate tx;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cbs-refresh");
        t.setDaemon(true);
        return t;
    });
    private final Set<Long> running = ConcurrentHashMap.newKeySet();

    public CbsRefreshService(CbsSourceLoader sourceLoader, CbsEngine engine, CbsStore store, CbsSettingsService settings,
                             CbsRefreshRunRepository runRepository, CbsClinicalRepository clinicalRepository,
                             @Qualifier("cbsTransactionTemplate") TransactionTemplate tx) {
        this.sourceLoader = sourceLoader;
        this.engine = engine;
        this.store = store;
        this.settings = settings;
        this.runRepository = runRepository;
        this.clinicalRepository = clinicalRepository;
        this.tx = tx;
    }

    @PostConstruct
    public void init() {
        try {
            runRepository.failInterruptedRuns();
        } catch (Exception e) {
            // the tables may not exist yet on the very first start
            log.debug("CBS refresh table not ready: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    public boolean isRunning(Long facilityId) {
        return running.contains(facilityId);
    }

    /** Queues a refresh unless one is already running for the facility. Returns false when skipped. */
    public boolean requestRefresh(Long facilityId, String user) {
        if (!running.add(facilityId)) {
            return false;
        }
        executor.submit(() -> {
            try {
                refresh(facilityId, user);
            } finally {
                running.remove(facilityId);
            }
        });
        return true;
    }

    // 2am by default. Override with dqr.cbs.refresh-cron in the yml if a facility needs another time.
    @Scheduled(cron = "${dqr.cbs.refresh-cron:0 0 2 * * *}")
    public void nightlyRefresh() {
        List<Long> facilities = clinicalRepository.facilitiesWithHivPatients();
        log.info("CBS nightly refresh for {} facilities", facilities.size());
        facilities.forEach(f -> requestRefresh(f, CbsStore.SYSTEM_USER));
    }

    private void refresh(Long facilityId, String user) {
        LocalDate today = LocalDate.now();
        CbsRefreshRun run = new CbsRefreshRun();
        run.setFacilityId(facilityId);
        run.setAsOfDate(today);
        run.setStatus("RUNNING");
        run.setTriggeredBy(user);
        run.setStartedAt(LocalDateTime.now());
        run = runRepository.save(run);
        long started = System.currentTimeMillis();
        try {
            CbsSettings cfg = settings.current();
            Map<String, PatientSource> sources = sourceLoader.loadFacility(facilityId);
            Map<String, PatientAssessment> assessments = new HashMap<>(sources.size() * 2);
            for (PatientSource s : sources.values()) {
                assessments.put(s.getPersonUuid(), engine.assess(s, today, today, cfg));
            }
            // CBS-DQ-24: records with no patient have no timeline, so their findings ride along
            // under a key that can't clash with a person uuid. They have no patient summary row.
            assessments.put("~unlinked-records", engine.unlinkedRecords(sourceLoader.loadOrphanRecords(facilityId)));
            long runId = run.getId();
            int[] counts = tx.execute(status -> store.saveFacility(runId, facilityId, today, cfg, sources.values(), assessments));
            run.setPatientsProcessed(sources.size());
            run.setFindingsActive(counts[0]);
            run.setFindingsNew(counts[1]);
            run.setFindingsCleared(counts[2]);
            run.setEventsCreated(counts[3]);
            finish(run, "COMPLETED", null);
            log.info("CBS refresh facility {}: {} patients, {} findings ({} new, {} cleared) in {} ms",
                    facilityId, sources.size(), counts[0], counts[1], counts[2], System.currentTimeMillis() - started);
        } catch (Exception e) {
            log.error("CBS refresh failed for facility {}", facilityId, e);
            finish(run, "FAILED", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void finish(CbsRefreshRun run, String status, String error) {
        run.setStatus(status);
        run.setErrorMessage(error);
        run.setFinishedAt(LocalDateTime.now());
        runRepository.save(run);
    }
}
