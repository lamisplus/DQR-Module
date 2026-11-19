package org.lamisplus.modules.dqr.repository.cbs;

import org.lamisplus.modules.dqr.domain.cbs.entity.CbsIssueHistory;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Only save() and reads are used. History rows are never updated or deleted. */
@Repository
public interface CbsIssueHistoryRepository extends JpaRepository<CbsIssueHistory, Long> {

    boolean existsByIssueIdAndAction(Long issueId, String action);

    @Query(nativeQuery = true, value = "SELECT id AS id, action AS action, from_status AS fromStatus, to_status AS toStatus, " +
            "note AS note, action_date AS actionDate, CAST(details AS TEXT) AS details, performed_by AS performedBy, " +
            "performed_at AS performedAt FROM cbs_issue_history WHERE issue_id = :issueId ORDER BY performed_at, id")
    List<CbsViews.HistoryRow> historyFor(@Param("issueId") Long issueId);
}
