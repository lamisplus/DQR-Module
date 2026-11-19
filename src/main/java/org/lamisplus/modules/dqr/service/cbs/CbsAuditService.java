package org.lamisplus.modules.dqr.service.cbs;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsAuditLog;
import org.lamisplus.modules.dqr.repository.cbs.CbsAuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/** Records access to patient-level CBS information and every export attempt (spec US-32 AC7). */
@Service
@RequiredArgsConstructor
public class CbsAuditService {

    private final CbsAuditLogRepository repository;

    public void record(Long facilityId, String action, String reference, Map<String, Object> details, String user) {
        CbsAuditLog log = new CbsAuditLog();
        log.setFacilityId(facilityId);
        log.setAction(action);
        log.setReference(reference);
        log.setDetails(details);
        log.setPerformedBy(user);
        log.setPerformedAt(LocalDateTime.now());
        repository.save(log);
    }
}
