package org.lamisplus.modules.dqr.repository.cbs;

import org.lamisplus.modules.dqr.domain.cbs.entity.CbsPatientSummary;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CbsPatientSummaryRepository extends JpaRepository<CbsPatientSummary, CbsPatientSummary.Key> {

    long countByFacilityId(Long facilityId);

    long countByFacilityIdAndOpenFlagsGreaterThan(Long facilityId, Integer openFlags);

    long countByFacilityIdAndStatusDiscrepancyTrue(Long facilityId);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) AS clientsAssessed, " +
            "COUNT(*) FILTER (WHERE open_flags > 0) AS recordsWithFlags, " +
            "COALESCE(SUM(open_gaps), 0) AS eventGaps, " +
            "COUNT(*) FILTER (WHERE status_discrepancy) AS statusDiscrepancies " +
            "FROM cbs_patient_summary WHERE facility_id = :facilityId")
    CbsViews.OverviewCounts overviewCounts(@Param("facilityId") Long facilityId);

    @Query(nativeQuery = true, value = "SELECT treatment_status AS name, COUNT(*) AS count FROM cbs_patient_summary " +
            "WHERE facility_id = :facilityId GROUP BY 1 ORDER BY 2 DESC")
    List<CbsViews.NameCount> treatmentStatusBreakdown(@Param("facilityId") Long facilityId);

    // "Clients Assessed" for an "Event date from / to" range: patients in the CBS list with any
    // record dated in the range (enrolment, ART start, pickup, clinic visit, lab sample, EAC session,
    // tracking update, HTS visit). Lost-to-follow-up statuses don't count: they are written by the
    // system (ETL), not a contact with the client.
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM cbs_patient_summary s WHERE s.facility_id = :facilityId AND s.person_uuid IN (" +
            " SELECT a.person FROM (" +
            "  SELECT person_uuid AS person, date_enrolled_in_hiv_care AS d FROM hiv_enrollment_commencement WHERE archived = 0 AND facility_id = :facilityId" +
            "  UNION ALL SELECT person_uuid, date_art_started FROM hiv_enrollment_commencement WHERE archived = 0 AND facility_id = :facilityId" +
            "  UNION ALL SELECT person_uuid, visit_date FROM hiv_art_pharmacy WHERE archived = 0 AND facility_id = :facilityId" +
            "  UNION ALL SELECT person_uuid, visit_date FROM hiv_art_clinical WHERE archived = 0 AND facility_id = :facilityId" +
            "  UNION ALL SELECT patient_uuid, CAST(date_sample_collected AS DATE) FROM laboratory_sample WHERE archived = 0 AND facility_id = :facilityId" +
            "  UNION ALL SELECT person_uuid, eac_session_date FROM hiv_eac_session WHERE archived = 0 AND facility_id = :facilityId" +
            "  UNION ALL SELECT person_id, status_date FROM hiv_status_tracker WHERE archived = 0 AND facility_id = :facilityId AND hiv_status <> 'LOST_TO_FOLLOWUP'" +
            "  UNION ALL SELECT person_uuid, COALESCE(date_of_observation, CAST(date AS DATE)) FROM hiv_patient_tracker WHERE archived = 0 AND facility_id = :facilityId" +
            "  UNION ALL SELECT patient_uuid, date_of_visit FROM hts_encounter WHERE archived IS NOT TRUE AND facility_id = :facilityId" +
            " ) a WHERE a.d >= COALESCE(CAST(CAST(:fromDate AS VARCHAR) AS DATE), DATE '1900-01-01') " +
            " AND a.d <= COALESCE(CAST(CAST(:toDate AS VARCHAR) AS DATE), DATE '9999-12-31'))")
    long clientsWithActivityBetween(@Param("facilityId") Long facilityId, @Param("fromDate") String fromDate,
                                    @Param("toDate") String toDate);

    String PATIENT_WHERE = " FROM cbs_patient_summary s LEFT JOIN base_organisation_unit o ON o.id = s.facility_id " +
            "WHERE s.facility_id = :facilityId " +
            "AND (CAST(:search AS VARCHAR) IS NULL OR s.patient_id ILIKE CAST(:search AS VARCHAR) OR s.hospital_number ILIKE CAST(:search AS VARCHAR)) " +
            "AND (CAST(:status AS VARCHAR) IS NULL OR s.treatment_status = CAST(:status AS VARCHAR)) " +
            "AND (CAST(:card AS VARCHAR) IS NULL OR (CAST(:card AS VARCHAR) = 'flags' AND s.open_flags > 0) " +
            "     OR (CAST(:card AS VARCHAR) = 'gaps' AND s.open_gaps > 0) OR (CAST(:card AS VARCHAR) = 'discrepancies' AND s.status_discrepancy)) " +
            // Category, severity and the "Event date from / to" range all look at the patient's unresolved
            // findings; with a range, the "Show" card only counts findings of its own kind in that range.
            "AND (CAST(:category AS VARCHAR) IS NULL AND CAST(:severity AS VARCHAR) IS NULL " +
            "     AND CAST(:fromDate AS VARCHAR) IS NULL AND CAST(:toDate AS VARCHAR) IS NULL OR EXISTS (" +
            "     SELECT 1 FROM cbs_finding f LEFT JOIN cbs_issue i ON i.finding_id = f.id " +
            "     WHERE f.facility_id = s.facility_id AND f.person_uuid = s.person_uuid AND f.active AND " + CbsSqlParts.UNRESOLVED +
            "     AND (CAST(:category AS VARCHAR) IS NULL OR f.category = CAST(:category AS VARCHAR)) " +
            "     AND (CAST(:severity AS VARCHAR) IS NULL OR f.severity = CAST(:severity AS VARCHAR)) " +
            "     AND (CAST(CAST(:fromDate AS VARCHAR) AS DATE) IS NULL OR f.event_date >= CAST(CAST(:fromDate AS VARCHAR) AS DATE)) " +
            "     AND (CAST(CAST(:toDate AS VARCHAR) AS DATE) IS NULL OR f.event_date <= CAST(CAST(:toDate AS VARCHAR) AS DATE)) " +
            "     AND (CAST(:card AS VARCHAR) IS DISTINCT FROM 'gaps' OR f.finding_type = 'MISSING_EXPECTED_EVENT') " +
            "     AND (CAST(:card AS VARCHAR) IS DISTINCT FROM 'discrepancies' OR f.rule_code = 'CBS-DQ-04')))";

    // Sorting comes from the Pageable (whitelisted columns only, see CbsQueryService)
    @Query(nativeQuery = true, value = "SELECT s.person_uuid AS personUuid, s.patient_id AS patientId, " +
            "s.hospital_number AS hospitalNumber, s.sex AS sex, s.treatment_status AS treatmentStatus, " +
            "s.treatment_status_date AS treatmentStatusDate, s.vital_status AS vitalStatus, " +
            "s.latest_event_type AS latestEventType, s.latest_event_date AS latestEventDate, s.open_flags AS openFlags, " +
            "s.open_gaps AS openGaps, s.status_discrepancy AS statusDiscrepancy, s.as_of_date AS asOfDate, " +
            "o.name AS facilityName" + PATIENT_WHERE,
            countQuery = "SELECT COUNT(*)" + PATIENT_WHERE)
    Page<CbsViews.PatientRow> search(@Param("facilityId") Long facilityId, @Param("search") String search,
                                     @Param("status") String status, @Param("card") String card,
                                     @Param("category") String category, @Param("severity") String severity,
                                     @Param("fromDate") String fromDate, @Param("toDate") String toDate,
                                     Pageable pageable);

    // Other patients sharing an identifier are shown for review, never merged
    @Query(nativeQuery = true, value = "SELECT person_uuid AS personUuid, patient_id AS patientId, " +
            "hospital_number AS hospitalNumber, sex AS sex, date_of_birth AS dateOfBirth FROM cbs_patient_summary " +
            "WHERE facility_id = :facilityId AND person_uuid <> CAST(:personUuid AS VARCHAR) " +
            "AND (hospital_number = CAST(:hospitalNumber AS VARCHAR) OR patient_id = CAST(:patientId AS VARCHAR))")
    List<CbsViews.IdentityRow> identityConflicts(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid,
                                                 @Param("hospitalNumber") String hospitalNumber, @Param("patientId") String patientId);
}
