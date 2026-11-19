package org.lamisplus.modules.dqr.repository.cbs;

import org.lamisplus.modules.dqr.domain.cbs.entity.CbsRefreshRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CbsRefreshRunRepository extends JpaRepository<CbsRefreshRun, Long> {

    Optional<CbsRefreshRun> findFirstByFacilityIdAndStatusOrderByFinishedAtDesc(Long facilityId, String status);

    Optional<CbsRefreshRun> findFirstByFacilityIdOrderByStartedAtDesc(Long facilityId);

    // A run left as RUNNING by a server restart would otherwise look busy forever
    @Modifying
    @Transactional
    @Query("UPDATE CbsRefreshRun r SET r.status = 'FAILED', r.finishedAt = CURRENT_TIMESTAMP, " +
            "r.errorMessage = 'Interrupted by a server restart' WHERE r.status = 'RUNNING'")
    int failInterruptedRuns();
}
