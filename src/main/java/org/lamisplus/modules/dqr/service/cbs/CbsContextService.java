package org.lamisplus.modules.dqr.service.cbs;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsContextDto;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsRefreshResponse;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsRefreshRun;
import org.lamisplus.modules.dqr.repository.cbs.CbsClinicalRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsRefreshRunRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** The facility and data-freshness header shown on every CBS screen, and starting a refresh. */
@Service
@RequiredArgsConstructor
public class CbsContextService {

    private final CbsAccess access;
    private final CbsRefreshService refreshService;
    private final CbsRefreshRunRepository runRepository;
    private final CbsClinicalRepository clinicalRepository;
    private final CbsSettingsService settings;

    public CbsContextDto current() {
        CbsContextDto out = new CbsContextDto();
        fill(out, access.facilityId());
        return out;
    }

    public CbsRefreshResponse startRefresh() {
        Long facilityId = access.facilityId();
        CbsRefreshResponse out = new CbsRefreshResponse();
        out.setStarted(refreshService.requestRefresh(facilityId, access.userName()));
        fill(out, facilityId);
        return out;
    }

    /** Fills the context fields of any screen response that carries them at the top level. */
    public void fill(CbsContextDto out, Long facilityId) {
        CbsRefreshRun lastSuccess = runRepository.findFirstByFacilityIdAndStatusOrderByFinishedAtDesc(facilityId, "COMPLETED").orElse(null);
        CbsRefreshRun latest = runRepository.findFirstByFacilityIdOrderByStartedAtDesc(facilityId).orElse(null);
        out.setFacilityId(facilityId);
        out.setFacilityName(clinicalRepository.facilityName(facilityId));
        out.setLastSuccessfulRefresh(lastSuccess);
        out.setLatestRun(latest);
        out.setProcessing(refreshService.isRunning(facilityId));
        out.setAsOfDate(lastSuccess == null ? null : lastSuccess.getAsOfDate());
        out.setStale(lastSuccess != null
                && ChronoUnit.HOURS.between(lastSuccess.getFinishedAt(), LocalDateTime.now()) > settings.current().getStaleHours());
        out.setLatestRunFailed(latest != null && "FAILED".equals(latest.getStatus()));
    }
}
