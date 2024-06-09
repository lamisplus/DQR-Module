package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.PrepSummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.lamisplus.modules.dqr.util.DQRQueries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrepRepository extends JpaRepository<DQA, Long> {


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)  hc.client_code AS patientId, hc.client_code AS hospitalNumber, \n" +
            "hc.extra->>'gender' AS sex, CAST(hc.extra->>'age' AS INTEGER) AS age, CAST(hc.extra->>'date_of_birth' AS DATE) AS dateOfBirth, \n" +
            "hc.hiv_test_result AS status FROM hts_client hc LEFT JOIN prep_enrollment pe ON pe.person_uuid = hc.person_uuid\n" +
            "where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = false AND hc.facility_id = ?1", nativeQuery = true)
    List<PatientDTOProjection> getOfferedPrep(Long facilityId);


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)  hc.client_code AS patientId, hc.client_code AS hospitalNumber, \n" +
            "hc.extra->>'gender' AS sex, CAST(hc.extra->>'age' AS INTEGER) AS age, CAST(hc.extra->>'date_of_birth' AS DATE) AS dateOfBirth, \n" +
            "hc.hiv_test_result AS status FROM hts_client hc LEFT JOIN prep_enrollment pe ON pe.person_uuid = hc.person_uuid\n" +
            "where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = false \n" +
            "AND prep_accepted IS NULL\n" +
            "AND hc.facility_id = ?1\n", nativeQuery = true)
    List<PatientDTOProjection> getAcceptedOffer (Long facilityId);

    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)  hc.client_code AS patientId, hc.client_code AS hospitalNumber, \n" +
            "hc.extra->>'gender' AS sex, CAST(hc.extra->>'age' AS INTEGER) AS age, CAST(hc.extra->>'date_of_birth' AS DATE) AS dateOfBirth, \n" +
            "hc.hiv_test_result AS status FROM hts_client hc LEFT JOIN prep_enrollment pe ON pe.person_uuid = hc.person_uuid\n" +
            "where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
            "AND hc.prep_accepted = true AND pe.status is null\n" +
            "AND hc.facility_id = ?1", nativeQuery = true)
    List<PatientDTOProjection> getPrepInitiated(Long facilityId);

    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)  hc.client_code AS patientId, hc.client_code AS hospitalNumber, \n" +
            "hc.extra->>'gender' AS sex, CAST(hc.extra->>'age' AS INTEGER) AS age, CAST(hc.extra->>'date_of_birth' AS DATE) AS dateOfBirth, \n" +
            "hc.hiv_test_result AS status FROM hts_client hc LEFT JOIN prep_enrollment pe ON pe.person_uuid = hc.person_uuid\n" +
            "LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid\n" +
            "where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
            "AND hc.prep_accepted = true AND pc.urinalysis_result is null OR pc.urinalysis_result = ''\n" +
            "AND hc.facility_id = ?1", nativeQuery = true)
    List<PatientDTOProjection> getPrepInitiatedAndUrinalysis(Long facilityId);



    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)  hc.client_code AS patientId, hc.client_code AS hospitalNumber, \n" +
            "hc.extra->>'gender' AS sex, CAST(hc.extra->>'age' AS INTEGER) AS age, CAST(hc.extra->>'date_of_birth' AS DATE) AS dateOfBirth, \n" +
            "hc.hiv_test_result AS status FROM hts_client hc LEFT JOIN prep_enrollment pe ON pe.person_uuid = hc.person_uuid\n" +
            "LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid\n" +
            "where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
            "AND hc.prep_accepted = true AND pc.is_commencement is true AND pe.date_enrolled < pc.encounter_date\n" +
            "AND hc.facility_id = ?1", nativeQuery = true)
    List<PatientDTOProjection> getDateRegisterLessThanDateCommenced(Long facilityId);

    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)\n" +
            "       hc.client_code AS patientId,\n" +
            "       hc.client_code AS hospitalNumber,\n" +
            "       hc.extra->>'gender' AS sex,\n" +
            "       CAST(hc.extra->>'age' AS INTEGER) AS age,\n" +
            "       CAST(hc.extra->>'date_of_birth' AS DATE) AS dateOfBirth,\n" +
            "       hc.hiv_test_result AS status\n" +
            "FROM hts_client hc\n" +
            "LEFT JOIN prep_enrollment pe ON pe.person_uuid = hc.person_uuid\n" +
            "LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid\n" +
            "WHERE hc.hiv_test_result IS NOT NULL\n" +
            "  AND hc.hiv_test_result = 'Negative'\n" +
            "  AND hc.prep_offered = TRUE\n" +
            "  AND hc.prep_accepted = TRUE\n" +
            "  AND (pc.urinalysis_result IS NOT NULL OR pc.urinalysis_result = '')\n" +
            "  AND hc.facility_id = ?1\n" +
            "GROUP BY hc.person_uuid, pc.urinalysis, hc.client_code, pc.urinalysis_result, hc.hiv_test_result, hc.extra, pe.date_enrolled\n" +
            "HAVING pe.date_enrolled > CAST(MAX(pc.urinalysis->> 'testDate') AS DATE);\n", nativeQuery = true)
    List<PatientDTOProjection> getUrinalysisGreaterThanDateEnroll(Long facilityId);


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)\n" +
            "       hc.client_code AS patientId,\n" +
            "       hc.client_code AS hospitalNumber,\n" +
            "       hc.extra->>'gender' AS sex,\n" +
            "       CAST(hc.extra->>'age' AS INTEGER) AS age,\n" +
            "       CAST(hc.extra->>'date_of_birth' AS DATE) AS dateOfBirth,\n" +
            "       hc.hiv_test_result AS status\n" +
            "FROM hts_client hc\n" +
            "LEFT JOIN prep_enrollment pe ON pe.person_uuid = hc.person_uuid\n" +
            "LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid\n" +
            "WHERE hc.hiv_test_result IS NOT NULL\n" +
            "  AND hc.hiv_test_result = 'Negative'\n" +
            "  AND hc.prep_offered = TRUE\n" +
            "  AND hc.prep_accepted = TRUE\n" +
            "  AND (pc.urinalysis_result IS NOT NULL OR pc.urinalysis_result = '')\n" +
            "  AND hc.facility_id = ?1\n" +
            "  AND hc.archived != 1\n" +
            "  AND pc.archived != 1\n" +
            "  AND pe.archived != 1\n" +
            "GROUP BY hc.person_uuid, pc.urinalysis, hc.client_code, pc.urinalysis_result, hc.hiv_test_result,\n" +
            "hc.extra, pe.date_enrolled, pc.encounter_date\n" +
            "HAVING pc.encounter_date > CAST(MAX(pc.urinalysis->> 'testDate') AS DATE)", nativeQuery = true)
    List<PatientDTOProjection> getUrinalysisGreaterThanStatusDate(Long facilityId);

    // summary
    @Query(value = DQRQueries.DataConsistency.PREP_SUMMARY_QUERIES, nativeQuery = true)
    List<PrepSummaryDTOProjection> getPrepSummary(Long facilityId);


}
