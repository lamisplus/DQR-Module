package org.lamisplus.modules.dqr.repository.cbs;

import org.lamisplus.modules.dqr.domain.cbs.entity.CbsStoredFinding;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CbsFindingRepository extends JpaRepository<CbsStoredFinding, Long> {

    String FINDING_COLUMNS = "SELECT f.id AS findingId, i.id AS issueId, f.person_uuid AS personUuid, " +
            "COALESCE(s.patient_id, f.person_uuid) AS patientId, s.hospital_number AS hospitalNumber, f.rule_code AS ruleCode, " +
            "f.category AS category, f.finding_type AS findingType, f.severity AS severity, f.event_type AS eventType, " +
            "f.description AS description, CAST(f.evidence AS TEXT) AS evidence, f.active AS active, " +
            "f.event_date AS eventDate, f.first_detected_at AS dateIdentified, f.last_detected_at AS lastDetected, f.cleared_at AS clearedAt, " +
            "i.status AS issueStatus, i.assigned_to AS assignedTo";

    String UNRESOLVED_BASE = CbsSqlParts.FINDING_JOINS + "WHERE f.facility_id = :facilityId AND f.active AND " + CbsSqlParts.UNRESOLVED;

    // issueStatus: null/UNRESOLVED = still open, ALL = everything, otherwise one issue status.
    // fromDate/toDate are on the finding's event date (both ends included).
    String SEARCH_WHERE = CbsSqlParts.FINDING_JOINS + "WHERE f.facility_id = :facilityId" + CbsSqlParts.VIEW_FILTER +
            "AND (CASE WHEN CAST(:issueStatus AS VARCHAR) IS NULL OR CAST(:issueStatus AS VARCHAR) = 'UNRESOLVED' THEN f.active AND " + CbsSqlParts.UNRESOLVED +
            "          WHEN CAST(:issueStatus AS VARCHAR) = 'ALL' THEN TRUE ELSE i.status = CAST(:issueStatus AS VARCHAR) END) " +
            "AND (CAST(:category AS VARCHAR) IS NULL OR f.category = CAST(:category AS VARCHAR)) " +
            "AND (CAST(:severity AS VARCHAR) IS NULL OR f.severity = CAST(:severity AS VARCHAR)) " +
            "AND (CAST(:ruleCode AS VARCHAR) IS NULL OR f.rule_code = CAST(:ruleCode AS VARCHAR)) " +
            "AND (CAST(:search AS VARCHAR) IS NULL OR s.patient_id ILIKE CAST(:search AS VARCHAR) OR s.hospital_number ILIKE CAST(:search AS VARCHAR)) " +
            "AND (CAST(CAST(:fromDate AS VARCHAR) AS DATE) IS NULL OR f.event_date >= CAST(CAST(:fromDate AS VARCHAR) AS DATE)) " +
            "AND (CAST(CAST(:toDate AS VARCHAR) AS DATE) IS NULL OR f.event_date <= CAST(CAST(:toDate AS VARCHAR) AS DATE))";

    @Query(nativeQuery = true, value = FINDING_COLUMNS + SEARCH_WHERE +
            " ORDER BY CASE f.severity WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END, f.first_detected_at DESC, f.id",
            countQuery = "SELECT COUNT(*)" + SEARCH_WHERE)
    Page<CbsViews.FindingRow> search(@Param("facilityId") Long facilityId, @Param("view") String view,
                                     @Param("issueStatus") String issueStatus, @Param("category") String category,
                                     @Param("severity") String severity, @Param("ruleCode") String ruleCode,
                                     @Param("search") String search, @Param("fromDate") String fromDate,
                                     @Param("toDate") String toDate, Pageable pageable);

    @Query(nativeQuery = true, value = FINDING_COLUMNS + UNRESOLVED_BASE + "AND f.person_uuid = CAST(:personUuid AS VARCHAR) " +
            "ORDER BY CASE f.severity WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END, f.first_detected_at")
    List<CbsViews.FindingRow> unresolvedForPatient(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) AS \"all\", " +
            "COUNT(*) FILTER (WHERE f.finding_type IN ('VALIDATION_ERROR', 'POSSIBLE_DISCREPANCY') AND f.rule_code <> 'CBS-DQ-04') AS validation, " +
            "COUNT(*) FILTER (WHERE f.finding_type = 'MISSING_EXPECTED_EVENT') AS gaps, " +
            "COUNT(*) FILTER (WHERE f.rule_code = 'CBS-DQ-04') AS status" + UNRESOLVED_BASE)
    CbsViews.FindingCounts unresolvedCounts(@Param("facilityId") Long facilityId);

    @Query(nativeQuery = true, value = "SELECT f.category AS name, COUNT(*) AS count" + UNRESOLVED_BASE + "GROUP BY 1 ORDER BY 2 DESC")
    List<CbsViews.NameCount> unresolvedByCategory(@Param("facilityId") Long facilityId);

    @Query(nativeQuery = true, value = "SELECT f.severity AS name, COUNT(*) AS count" + UNRESOLVED_BASE + "GROUP BY 1 ORDER BY 2 DESC")
    List<CbsViews.NameCount> unresolvedBySeverity(@Param("facilityId") Long facilityId);

    @Query(nativeQuery = true, value = "SELECT f.rule_code AS name, COUNT(*) AS count" + UNRESOLVED_BASE + "GROUP BY 1 ORDER BY 1")
    List<CbsViews.NameCount> unresolvedByRule(@Param("facilityId") Long facilityId);

    @Query(nativeQuery = true, value = "SELECT f.category AS name, f.severity AS severity, COUNT(*) AS unresolved, " +
            "COUNT(DISTINCT f.person_uuid) AS patients" + UNRESOLVED_BASE + "GROUP BY 1, 2 ORDER BY 3 DESC")
    List<CbsViews.CategoryRow> unresolvedByCategoryAndSeverity(@Param("facilityId") Long facilityId);

    @Query(nativeQuery = true, value = "SELECT COUNT(*)" + UNRESOLVED_BASE + "AND f.finding_type = 'MISSING_EXPECTED_EVENT'")
    long countUnresolvedGaps(@Param("facilityId") Long facilityId);

    // Overview cards for an "Event date from / to" range. Same meaning as the unfiltered cards
    // (which come from cbs_patient_summary), limited to findings whose event date is in the range.
    @Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT f.person_uuid) FILTER (WHERE s.person_uuid IS NOT NULL) AS recordsWithFlags, " +
            "COUNT(*) FILTER (WHERE s.person_uuid IS NOT NULL AND f.finding_type = 'MISSING_EXPECTED_EVENT') AS eventGaps, " +
            "COUNT(DISTINCT f.person_uuid) FILTER (WHERE s.person_uuid IS NOT NULL AND f.rule_code = 'CBS-DQ-04') AS statusDiscrepancies, " +
            "COUNT(*) FILTER (WHERE f.severity = 'High') AS highPriorityUnresolved" + UNRESOLVED_BASE +
            "AND (CAST(CAST(:fromDate AS VARCHAR) AS DATE) IS NULL OR f.event_date >= CAST(CAST(:fromDate AS VARCHAR) AS DATE)) " +
            "AND (CAST(CAST(:toDate AS VARCHAR) AS DATE) IS NULL OR f.event_date <= CAST(CAST(:toDate AS VARCHAR) AS DATE))")
    CbsViews.PeriodCounts unresolvedCountsForEventDates(@Param("facilityId") Long facilityId,
                                                            @Param("fromDate") String fromDate, @Param("toDate") String toDate);

    @Query(nativeQuery = true, value = "SELECT COUNT(*)" + UNRESOLVED_BASE + "AND f.severity = 'High'")
    long countUnresolvedHighSeverity(@Param("facilityId") Long facilityId);
}
