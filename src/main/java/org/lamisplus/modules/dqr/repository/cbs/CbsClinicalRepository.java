package org.lamisplus.modules.dqr.repository.cbs;

import org.lamisplus.modules.dqr.domain.cbs.projection.CbsSourceRows;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Read-only queries on tables owned by other modules (Patient, HIV, HTS, Laboratory, Core).
 * Bound to the DQA entity the same way the other DQR repositories are, since DQR does not
 * map those tables as entities.
 *
 * personUuid is optional: null loads the whole facility, a value loads one patient.
 * Every query skips archived rows and stays inside the facility.
 * Sources follow the Data_Element_Map sheet of the BA mapping workbook.
 */
@Repository
public interface CbsClinicalRepository extends JpaRepository<DQA, Long> {

    // HTS final result date: dateOfFinalHivTestDone, an older misspelt key, then the visit date.
    // The regex guard keeps one badly typed date from failing the whole refresh.
    String HTS_DATE = "COALESCE(" +
            "CASE WHEN h.observation->>'dateOfFinalHivTestDone' ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}' " +
            "     THEN CAST(SUBSTRING(h.observation->>'dateOfFinalHivTestDone', 1, 10) AS DATE) END, " +
            "CASE WHEN h.observation->>'dateoffinalHivTestResult' ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}' " +
            "     THEN CAST(SUBSTRING(h.observation->>'dateoffinalHivTestResult', 1, 10) AS DATE) END, " +
            "h.date_of_visit)";

    // Everyone enrolled in HIV care plus HTS positives, so unlinked positives are assessed too
    @Query(nativeQuery = true, value = "SELECT p.uuid AS uuid, p.hospital_number AS hospitalNumber, p.sex AS sex, " +
            "p.date_of_birth AS dateOfBirth, p.is_date_of_birth_estimated AS dobEstimated FROM patient_person p " +
            "WHERE p.archived = 0 AND p.facility_id = :facilityId " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR p.uuid = CAST(:personUuid AS VARCHAR)) " +
            "AND (EXISTS (SELECT 1 FROM hiv_enrollment_commencement e WHERE e.person_uuid = p.uuid AND e.archived = 0 AND e.facility_id = :facilityId) " +
            "  OR EXISTS (SELECT 1 FROM hts_encounter h WHERE h.patient_uuid = p.uuid AND h.archived IS NOT TRUE " +
            "             AND h.facility_id = :facilityId AND h.observation->>'finalHivTestResult' ILIKE 'positive'))")
    List<CbsSourceRows.Person> persons(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // hiv_enrollment is no longer written to; enrolment and ART commencement now share
    // hiv_enrollment_commencement. Columns are aliased to the old names so the loader is unchanged.
    // Earliest enrolment first; the loader keeps only the first one per person
    @Query(nativeQuery = true, value = "SELECT e.uuid AS uuid, e.person_uuid AS personUuid, e.unique_id AS uniqueId, " +
            "e.date_confirmed_hiv_test AS dateConfirmedHiv, e.date_enrolled_in_hiv_care AS dateOfRegistration, " +
            "e.date_art_started AS dateStarted, c.display AS statusAtRegistration, e.created_date AS createdDate " +
            "FROM hiv_enrollment_commencement e LEFT JOIN base_application_codeset c ON c.id = e.status_at_registration_id " +
            "WHERE e.archived = 0 AND e.facility_id = :facilityId " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR e.person_uuid = CAST(:personUuid AS VARCHAR)) " +
            "ORDER BY e.date_enrolled_in_hiv_care NULLS LAST, e.id")
    List<CbsSourceRows.Enrollment> enrollments(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // The commencement part of the same form. hiv_art_clinical stopped getting commencement
    // rows when this table came in, and its old ones were copied here, so this is the only source.
    // Regimen and CD4 come along for CBS-DQ-16 and EV09.
    @Query(nativeQuery = true, value = "SELECT uuid AS uuid, person_uuid AS personUuid, date_art_started AS recordDate, " +
            "created_date AS createdDate, regimen_id AS regimenId, regimen_line_id AS regimenLineId, " +
            "COALESCE(CAST(cd4_at_art_start AS VARCHAR), CAST(cd4_percentage AS VARCHAR) || '%', NULLIF(TRIM(cd4_lf_id), '')) AS cd4AtStart, " +
            "NULLIF(TRIM(clinical_stage_id), '') AS clinicalStage FROM hiv_enrollment_commencement " +
            "WHERE archived = 0 AND facility_id = :facilityId AND is_commencement IS TRUE AND date_art_started IS NOT NULL " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR person_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.Commencement> commencements(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    @Query(nativeQuery = true, value = "SELECT uuid AS uuid, person_uuid AS personUuid, visit_date AS visitDate, " +
            "created_date AS createdDate, is_commencement AS isCommencement FROM hiv_art_clinical " +
            "WHERE archived = 0 AND facility_id = :facilityId " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR person_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.ClinicVisit> clinicVisits(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // EV01 source per the workbook: hts_encounter final result (Positive, and Negative for CBS-DQ-09)
    @Query(nativeQuery = true, value = "SELECT h.uuid AS uuid, h.patient_uuid AS personUuid, " + HTS_DATE + " AS recordDate, " +
            "h.date_created AS createdDate, h.observation->>'finalHivTestResult' AS result FROM hts_encounter h " +
            "WHERE h.archived IS NOT TRUE AND h.facility_id = :facilityId " +
            "AND h.observation->>'finalHivTestResult' IN ('Positive', 'Negative') " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR h.patient_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.HtsResult> htsResults(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // regimenLine: highest ART line dispensed at the visit (1st, 2nd or 3rd; adult or paediatric).
    // Regimen types 1/3 are 1st line, 2/4 2nd line, 14/16 3rd line; others (CTX, IPT...) are not ART lines.
    @Query(nativeQuery = true, value = "SELECT p.uuid AS uuid, p.person_uuid AS personUuid, p.visit_date AS visitDate, " +
            "p.refill_period AS refillPeriod, p.created_date AS createdDate, " +
            "(SELECT MAX(CASE r.regimen_type_id WHEN 1 THEN 1 WHEN 3 THEN 1 WHEN 2 THEN 2 WHEN 4 THEN 2 WHEN 14 THEN 3 WHEN 16 THEN 3 END) " +
            "   FROM jsonb_array_elements(CASE WHEN jsonb_typeof(p.extra->'regimens') = 'array' THEN p.extra->'regimens' ELSE '[]' END) x " +
            "   JOIN hiv_regimen r ON CAST(r.id AS VARCHAR) = x->>'regimenId') AS regimenLine " +
            "FROM hiv_art_pharmacy p WHERE p.archived = 0 AND p.facility_id = :facilityId " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR p.person_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.Refill> refills(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // person_id on the status tracker holds the person uuid, despite the name
    @Query(nativeQuery = true, value = "SELECT uuid AS uuid, person_id AS personUuid, hiv_status AS hivStatus, " +
            "status_date AS statusDate, cause_of_death AS causeOfDeath FROM hiv_status_tracker " +
            "WHERE archived = 0 AND facility_id = :facilityId " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR person_id = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.TrackerStatus> trackerStatuses(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    @Query(nativeQuery = true, value = "SELECT uuid AS uuid, person_uuid AS personUuid, " +
            "COALESCE(date_of_observation, CAST(date AS DATE)) AS trackingDate, date_of_death AS dateOfDeath, " +
            "cause_of_death AS causeOfDeath, COALESCE(date_return_to_care, date_patient_returned) AS dateReturned " +
            "FROM hiv_patient_tracker WHERE archived = 0 AND facility_id = :facilityId " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR person_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.PatientTracking> patientTrackings(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // EV04 also counts the transfer-in form
    @Query(nativeQuery = true, value = "SELECT uuid AS uuid, person_uuid AS personUuid, COALESCE(date_of_visit, received_date) AS recordDate, " +
            "created_date AS createdDate FROM hiv_patient_transfer_in WHERE archived = 0 AND facility_id = :facilityId " +
            "AND COALESCE(date_of_visit, received_date) IS NOT NULL " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR person_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.DatedRecord> transferInForms(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // Viral load (lab test 16) and CD4 (1 = CD4, 50 = Visitect CD4) orders with their earliest
    // accepted sample and latest result. Rejected samples don't count (EV10 exclusion).
    @Query(nativeQuery = true, value = "SELECT COALESCE(r.uuid, t.uuid) AS uuid, t.patient_uuid AS personUuid, t.lab_test_id AS labTestId, " +
            "t.viral_load_indication AS indication, s.sample_date AS sampleDate, s.sample_number AS sampleNumber, " +
            "CAST(COALESCE(r.date_result_reported, r.date_assayed) AS DATE) AS resultDate, r.result_reported AS resultReported " +
            "FROM laboratory_test t " +
            "LEFT JOIN (SELECT DISTINCT ON (test_id) test_id, CAST(date_sample_collected AS DATE) AS sample_date, sample_number " +
            "           FROM laboratory_sample WHERE archived = 0 AND facility_id = :facilityId " +
            "           AND COALESCE(sample_accepted, '') NOT ILIKE 'no%' AND COALESCE(sample_accepted, '') NOT ILIKE 'reject%' " +
            "           ORDER BY test_id, date_sample_collected NULLS LAST) s ON s.test_id = t.id " +
            "LEFT JOIN (SELECT DISTINCT ON (test_id) test_id, uuid, date_result_reported, date_assayed, result_reported " +
            "           FROM laboratory_result WHERE archived = 0 AND facility_id = :facilityId " +
            "           ORDER BY test_id, date_result_reported DESC NULLS LAST) r ON r.test_id = t.id " +
            "WHERE t.archived = 0 AND t.facility_id = :facilityId AND t.lab_test_id IN (16, 1, 50) " +
            "AND (s.sample_date IS NOT NULL OR r.uuid IS NOT NULL) " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR t.patient_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.LabTest> labTests(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    @Query(nativeQuery = true, value = "SELECT uuid AS uuid, person_uuid AS personUuid, eac_session_date AS recordDate, " +
            "created_date AS createdDate FROM hiv_eac_session WHERE archived = 0 AND facility_id = :facilityId " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR person_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.DatedRecord> eacSessions(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // Completed EAC episodes (EV13 / CBS-DQ-21). "FIRST EAC COMPLETED" only means session one is done.
    // The completion date is the last session of the episode.
    @Query(nativeQuery = true, value = "SELECT e.uuid AS uuid, e.person_uuid AS personUuid, " +
            "(SELECT MAX(s.eac_session_date) FROM hiv_eac_session s WHERE s.eac_id = e.uuid AND s.archived = 0) AS recordDate, " +
            "e.created_date AS createdDate FROM hiv_eac e WHERE e.archived = 0 AND e.facility_id = :facilityId " +
            "AND (UPPER(TRIM(e.status)) = 'COMPLETED' OR e.status ILIKE 'THIRD%COMPLETED' OR e.status ILIKE 'FOURTH%COMPLETED') " +
            "AND (CAST(:personUuid AS VARCHAR) IS NULL OR e.person_uuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.DatedRecord> completedEacs(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // CBS-DQ-23: hospital numbers and ART unique IDs used by more than one patient in the facility
    @Query(nativeQuery = true, value = "SELECT * FROM (" +
            "SELECT p.uuid AS personUuid, 'Hospital number' AS idType, TRIM(p.hospital_number) AS idValue, CAST(g.n - 1 AS INTEGER) AS otherPatients " +
            "FROM patient_person p JOIN (SELECT UPPER(TRIM(hospital_number)) AS k, COUNT(*) AS n FROM patient_person " +
            "  WHERE archived = 0 AND facility_id = :facilityId AND NULLIF(TRIM(hospital_number), '') IS NOT NULL " +
            "  GROUP BY 1 HAVING COUNT(*) > 1) g ON g.k = UPPER(TRIM(p.hospital_number)) " +
            "WHERE p.archived = 0 AND p.facility_id = :facilityId " +
            "UNION " +
            "SELECT e.person_uuid, 'ART unique ID', TRIM(e.unique_id), CAST(g.n - 1 AS INTEGER) " +
            "FROM hiv_enrollment_commencement e JOIN (SELECT UPPER(TRIM(unique_id)) AS k, COUNT(DISTINCT person_uuid) AS n " +
            "  FROM hiv_enrollment_commencement WHERE archived = 0 AND facility_id = :facilityId AND NULLIF(TRIM(unique_id), '') IS NOT NULL " +
            "  GROUP BY 1 HAVING COUNT(DISTINCT person_uuid) > 1) g ON g.k = UPPER(TRIM(e.unique_id)) " +
            "WHERE e.archived = 0 AND e.facility_id = :facilityId) d " +
            "WHERE (CAST(:personUuid AS VARCHAR) IS NULL OR d.personUuid = CAST(:personUuid AS VARCHAR))")
    List<CbsSourceRows.DuplicateId> duplicateIds(@Param("facilityId") Long facilityId, @Param("personUuid") String personUuid);

    // CBS-DQ-24: clinical records whose patient key is empty or matches no active patient
    @Query(nativeQuery = true, value = "SELECT * FROM (" +
            "SELECT 'hiv_enrollment_commencement' AS sourceTable, x.uuid AS uuid, x.person_uuid AS personUuid, " +
            "  COALESCE(x.date_enrolled_in_hiv_care, x.date_art_started) AS recordDate FROM hiv_enrollment_commencement x " +
            "  WHERE x.archived = 0 AND x.facility_id = :facilityId " +
            "UNION ALL SELECT 'hiv_art_pharmacy', x.uuid, x.person_uuid, x.visit_date FROM hiv_art_pharmacy x " +
            "  WHERE x.archived = 0 AND x.facility_id = :facilityId " +
            "UNION ALL SELECT 'hiv_art_clinical', x.uuid, x.person_uuid, x.visit_date FROM hiv_art_clinical x " +
            "  WHERE x.archived = 0 AND x.facility_id = :facilityId " +
            "UNION ALL SELECT 'hiv_status_tracker', x.uuid, x.person_id, x.status_date FROM hiv_status_tracker x " +
            "  WHERE x.archived = 0 AND x.facility_id = :facilityId " +
            "UNION ALL SELECT 'hiv_eac_session', x.uuid, x.person_uuid, x.eac_session_date FROM hiv_eac_session x " +
            "  WHERE x.archived = 0 AND x.facility_id = :facilityId " +
            "UNION ALL SELECT 'laboratory_test', x.uuid, x.patient_uuid, CAST(x.date_created AS DATE) FROM laboratory_test x " +
            "  WHERE x.archived = 0 AND x.facility_id = :facilityId AND x.lab_test_id IN (16, 1, 50) " +
            "UNION ALL SELECT 'hts_encounter', x.uuid, x.patient_uuid, x.date_of_visit FROM hts_encounter x " +
            "  WHERE x.archived IS NOT TRUE AND x.facility_id = :facilityId " +
            // encounters with no final result are unfinished forms, not events
            "  AND x.observation->>'finalHivTestResult' IN ('Positive', 'Negative') " +
            ") o WHERE NOT EXISTS (SELECT 1 FROM patient_person p WHERE p.uuid = o.personUuid AND p.archived = 0)")
    List<CbsSourceRows.OrphanRecord> orphanRecords(@Param("facilityId") Long facilityId);

    @Query(nativeQuery = true, value = "SELECT DISTINCT facility_id FROM hiv_enrollment_commencement WHERE archived = 0 AND facility_id IS NOT NULL")
    List<Long> facilitiesWithHivPatients();

    @Query(nativeQuery = true, value = "SELECT name FROM base_organisation_unit WHERE id = :facilityId")
    String facilityName(@Param("facilityId") Long facilityId);

    // Users who can see the facility are the only ones an issue may be assigned to
    @Query(nativeQuery = true, value = "SELECT DISTINCT u.user_name AS userName, " +
            "TRIM(CONCAT(u.first_name, ' ', u.last_name)) AS fullName FROM base_application_user u " +
            "JOIN base_application_user_organisation_unit uo ON uo.application_user_id = u.id " +
            "WHERE u.archived = 0 AND uo.archived = 0 AND uo.organisation_unit_id = :facilityId ORDER BY 1")
    List<CbsSourceRows.User> assignableUsers(@Param("facilityId") Long facilityId);
}
