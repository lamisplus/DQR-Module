package org.lamisplus.modules.dqr.repository.cbs;

import org.lamisplus.modules.dqr.domain.cbs.entity.CbsIssue;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CbsIssueRepository extends JpaRepository<CbsIssue, Long> {

    Optional<CbsIssue> findByIdAndFacilityId(Long id, Long facilityId);

    String CLOSED = "('RESOLVED', 'APPROVED_EXCEPTION')";

    @Query(nativeQuery = true, value = "SELECT " +
            "COUNT(*) FILTER (WHERE status IN ('OPEN', 'ACKNOWLEDGED')) AS open, " +
            "COUNT(*) FILTER (WHERE status IN ('UNDER_REVIEW', 'CORRECTED', 'EXCEPTION_REQUESTED')) AS underReview, " +
            "COUNT(*) FILTER (WHERE due_date < CURRENT_DATE AND status NOT IN " + CLOSED + ") AS overdue, " +
            "COUNT(*) FILTER (WHERE status = 'RESOLVED') AS resolved, " +
            "COUNT(*) FILTER (WHERE status = 'APPROVED_EXCEPTION') AS approvedExceptions, " +
            "COUNT(*) FILTER (WHERE assigned_to IS NULL AND status NOT IN " + CLOSED + ") AS unassigned, " +
            "COUNT(*) FILTER (WHERE assigned_to IS NOT NULL AND status NOT IN " + CLOSED + ") AS assigned, " +
            "COUNT(*) FILTER (WHERE escalated AND status NOT IN " + CLOSED + ") AS escalated " +
            "FROM cbs_issue WHERE facility_id = :facilityId")
    CbsViews.IssueSummary summary(@Param("facilityId") Long facilityId);

    String ISSUE_COLUMNS = "SELECT i.id AS id, i.person_uuid AS personUuid, COALESCE(s.patient_id, i.person_uuid) AS patientId, " +
            "s.hospital_number AS hospitalNumber, f.rule_code AS ruleCode, f.category AS category, " +
            "f.finding_type AS findingType, f.severity AS severity, f.description AS description, i.status AS status, " +
            "i.assigned_to AS assignedTo, i.due_date AS dueDate, i.escalated AS escalated, i.created_at AS createdAt, " +
            "i.resolved_at AS resolvedAt, f.event_date AS eventDate, (i.due_date < CURRENT_DATE AND i.status NOT IN " + CLOSED + ") AS overdue";

    // status also accepts the card groups OPEN_GROUP, REVIEW_GROUP and UNRESOLVED
    String SEARCH_WHERE = CbsSqlParts.ISSUE_JOINS + "WHERE i.facility_id = :facilityId " +
            "AND (CAST(:status AS VARCHAR) IS NULL " +
            "     OR (CAST(:status AS VARCHAR) = 'OPEN_GROUP' AND i.status IN ('OPEN', 'ACKNOWLEDGED')) " +
            "     OR (CAST(:status AS VARCHAR) = 'REVIEW_GROUP' AND i.status IN ('UNDER_REVIEW', 'CORRECTED', 'EXCEPTION_REQUESTED')) " +
            "     OR (CAST(:status AS VARCHAR) = 'UNRESOLVED' AND i.status NOT IN " + CLOSED + ") " +
            "     OR i.status = CAST(:status AS VARCHAR)) " +
            "AND (CAST(:assignedTo AS VARCHAR) IS NULL OR (CAST(:assignedTo AS VARCHAR) = 'UNASSIGNED' AND i.assigned_to IS NULL) OR i.assigned_to = CAST(:assignedTo AS VARCHAR)) " +
            "AND (CAST(:severity AS VARCHAR) IS NULL OR f.severity = CAST(:severity AS VARCHAR)) " +
            "AND (CAST(:category AS VARCHAR) IS NULL OR f.category = CAST(:category AS VARCHAR)) " +
            "AND (CAST(:flag AS VARCHAR) IS NULL " +
            "     OR (CAST(:flag AS VARCHAR) = 'overdue' AND i.due_date < CURRENT_DATE AND i.status NOT IN " + CLOSED + ") " +
            "     OR (CAST(:flag AS VARCHAR) = 'escalated' AND i.escalated AND i.status NOT IN " + CLOSED + ")) " +
            "AND (CAST(:search AS VARCHAR) IS NULL OR s.patient_id ILIKE CAST(:search AS VARCHAR) OR s.hospital_number ILIKE CAST(:search AS VARCHAR) " +
            "     OR CAST(i.id AS VARCHAR) = CAST(:issueNumber AS VARCHAR)) " +
            // the date range is on the event date of the problem behind the issue (both ends included)
            "AND (CAST(CAST(:fromDate AS VARCHAR) AS DATE) IS NULL OR f.event_date >= CAST(CAST(:fromDate AS VARCHAR) AS DATE)) " +
            "AND (CAST(CAST(:toDate AS VARCHAR) AS DATE) IS NULL OR f.event_date <= CAST(CAST(:toDate AS VARCHAR) AS DATE))";

    @Query(nativeQuery = true, value = ISSUE_COLUMNS + SEARCH_WHERE, countQuery = "SELECT COUNT(*)" + SEARCH_WHERE)
    Page<CbsViews.IssueRow> search(@Param("facilityId") Long facilityId, @Param("status") String status,
                                   @Param("assignedTo") String assignedTo, @Param("severity") String severity,
                                   @Param("category") String category, @Param("flag") String flag,
                                   @Param("search") String search, @Param("issueNumber") String issueNumber,
                                   @Param("fromDate") String fromDate, @Param("toDate") String toDate, Pageable pageable);

    @Query(nativeQuery = true, value = ISSUE_COLUMNS + ", i.updated_at AS updatedAt, f.id AS findingId, " +
            "f.rule_version AS ruleVersion, f.event_type AS eventType, CAST(f.evidence AS TEXT) AS evidence, " +
            "f.active AS active, f.first_detected_at AS dateIdentified, f.last_detected_at AS lastDetected, " +
            "f.cleared_at AS clearedAt" + CbsSqlParts.ISSUE_JOINS + "WHERE i.facility_id = :facilityId AND i.id = :id")
    Optional<CbsViews.IssueDetail> detail(@Param("facilityId") Long facilityId, @Param("id") Long id);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM cbs_issue WHERE facility_id = :facilityId AND status = CAST(:status AS VARCHAR) " +
            "AND (CAST(CAST(:fromDate AS VARCHAR) AS DATE) IS NULL OR resolved_at >= CAST(CAST(:fromDate AS VARCHAR) AS DATE)) " +
            "AND (CAST(CAST(:toDate AS VARCHAR) AS DATE) IS NULL OR resolved_at < CAST(CAST(:toDate AS VARCHAR) AS DATE) + 1)")
    long countClosedBetween(@Param("facilityId") Long facilityId, @Param("status") String status,
                            @Param("fromDate") String fromDate, @Param("toDate") String toDate);

    @Query(nativeQuery = true, value = "SELECT status AS name, COUNT(*) AS count FROM cbs_issue WHERE facility_id = :facilityId GROUP BY 1 ORDER BY 2 DESC")
    java.util.List<CbsViews.NameCount> countByStatus(@Param("facilityId") Long facilityId);
}
