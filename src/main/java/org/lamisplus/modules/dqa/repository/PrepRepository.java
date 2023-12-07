package org.lamisplus.modules.dqa.repository;

import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PrepSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrepRepository extends JpaRepository<DQA, Long> {


    @Query(value = "SELECT DISTINCT ON (person_uuid)  client_code, date_visit,  prep_offered, prep_accepted, hiv_test_result FROM \n" +
            "hts_client where hiv_test_result is not null AND hiv_test_result = 'Negative' AND prep_offered = false", nativeQuery = true)
    List<PatientDTOProjection> getOfferedPrep(Long facilityId);


    @Query(value = "SELECT DISTINCT ON (person_uuid)  client_code, date_visit,  prep_offered, prep_accepted, hiv_test_result FROM \n" +
            "hts_client where hiv_test_result is not null AND hiv_test_result = 'Negative' AND prep_offered = false\n" +
            "AND prep_accepted = false", nativeQuery = true)
    List<PatientDTOProjection> getAcceptedOffer (Long facilityId);
    @Query(value = "SELECT DISTINCT ON (hc.person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  hc.prep_offered, hc.prep_accepted, hc.hiv_test_result, pe.status, pe.unique_id FROM \n" +
            " hts_client hc LEFT JOIN prep_enrollment pe ON hc.person_uuid = pe.person_uuid where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
            " AND hc.prep_accepted = true AND pe.status is null", nativeQuery = true)
    List<PatientDTOProjection> getPrepInitiated(Long facilityId);

    @Query(value = "SELECT DISTINCT ON (hc.person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  hc.prep_offered, hc.prep_accepted, hc.hiv_test_result, pc.urinalysis_result FROM \n" +
            "hts_client hc LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
            "AND hc.prep_accepted = true AND pc.urinalysis_result is null OR pc.urinalysis_result = ''", nativeQuery = true)
    List<PatientDTOProjection> getPrepInitiatedAndUrinalysis(Long facilityId);


//    @Query(value = "SELECT DISTINCT ON (hc.person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  hc.prep_offered, hc.prep_accepted, hc.hiv_test_result,\n" +
//            "pc.encounter_date AS commence_date, pe.date_enrolled,\n" +
//            "pc.urinalysis_result, (TRANSLATE((pc.urinalysis->'testDate')::varchar, '\",[,]', ' ')::VARCHAR(100))::DATE AS test_date FROM \n" +
//            "hts_client hc \n" +
//            "LEFT JOIN prep_enrollment pe ON hc.person_uuid = pe.person_uuid\n" +
//            "LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
//            "AND hc.prep_accepted = true AND pc.is_commencement is true AND pe.date_enrolled < pc.encounter_date", nativeQuery = true)
//    List<PatientDTOProjection> getCurrentUrinalysisGreaterThanDateEnrolled(Long facilityId);

//    @Query(value = "", nativeQuery = true)
//    List<PatientDTOProjection> getCurrentUrinalysisGreaterThanHivStatus(Long facilityId);

    @Query(value = "vSELECT DISTINCT ON (hc.person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  hc.prep_offered, hc.prep_accepted, hc.hiv_test_result,\n" +
            "pc.encounter_date AS commence_date, pe.date_enrolled,\n" +
            "pc.urinalysis_result, (TRANSLATE((pc.urinalysis->'testDate')::varchar, '\",[,]', ' ')::VARCHAR(100))::DATE AS test_date FROM \n" +
            "hts_client hc \n" +
            "LEFT JOIN prep_enrollment pe ON hc.person_uuid = pe.person_uuid\n" +
            "LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
            "AND hc.prep_accepted = true AND pc.is_commencement is true AND pe.date_enrolled < pc.encounter_date", nativeQuery = true)
    List<PatientDTOProjection> getDateRegisterLessThanDateCommenced(Long facilityId);

    // summary
    @Query(value = "WITH prepSummary AS (\n" +
            "\tSELECT DISTINCT ON (person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  (CASE WHEN hc.prep_offered = true  THEN 1 ELSE null END) AS prep_offered, \n" +
            "\thc.prep_accepted, hc.prep_offered = hc.prep_accepted AS offAndAccpt,\n" +
            "\t hc.hiv_test_result, pe.status, pe.unique_id, pe.date_enrolled, pe.date_started, \n" +
            "\t(CASE WHEN pe.date_enrolled IS NULL  THEN pe.date_started ELSE pe.date_enrolled END) AS date_enrolled_prep,\n" +
            "\tpc. urinalysis, (TRANSLATE((pc. urinalysis->'testDate')::varchar, '\",[,]', ' ')::VARCHAR(100))::DATE AS UrinalysisDate,\n" +
            "\t(CASE WHEN (TRANSLATE((pc. urinalysis->'testDate')::varchar, '\",[,]', ' ')::VARCHAR(100))::DATE > (CASE WHEN pe.date_enrolled IS NULL  THEN pe.date_started ELSE pe.date_enrolled END)\n" +
            "\t THEN 1 ELSE NULL END) AS urinaGreaterthanenrollDate,\n" +
            "\thc.date_visit AS status_date,\n" +
            "\t(CASE WHEN (TRANSLATE((pc. urinalysis->'testDate')::varchar, '\",[,]', ' ')::VARCHAR(100))::DATE > hc.date_visit\n" +
            "\t THEN 1 ELSE NULL END) AS urinaGreaterthanStatusDate,\n" +
            "\tiscommenced.encounter_date,\n" +
            "\t(CASE WHEN (CASE WHEN pe.date_enrolled IS NULL  THEN pe.date_started ELSE pe.date_enrolled END) < iscommenced.encounter_date\n" +
            "\t THEN 1 ELSE NULL END) AS enrollDateLessThanCommenced\n" +
            "\tFROM\n" +
            "\thts_client hc  LEFT JOIN prep_enrollment pe ON hc.person_uuid = pe.person_uuid\n" +
            "\tLEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tselect DISTINCT ON (person_uuid) pc.person_uuid, pc.is_commencement, pc.encounter_date from prep_enrollment pe JOIN prep_clinic pc on pe.uuid = pc.prep_enrollment_uuid\n" +
            "\t\twhere is_commencement = true\n" +
            "\t) iscommenced ON pe.person_uuid = iscommenced.person_uuid\n" +
            "\twhere hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND pe.facility_id = ?1\n" +
            ")\n" +
            "SELECT \n" +
            "  COUNT(prep_offered) AS pOfferredNumerator,\n" +
            "  COUNT(person_uuid) AS pOfferedDenominator,\n" +
            "  ROUND((CAST(COUNT(prep_offered) AS DECIMAL) / COUNT(person_uuid)) * 100, 2) AS pOfferredPerformance,\n" +
            "  COUNT(prep_accepted) AS pAccepetedNumerator,\n" +
            "  COUNT(prep_offered) AS pAcceptedDenominator,\n" +
            "  ROUND((CAST(COUNT(prep_offered) AS DECIMAL) / COUNT(prep_offered)) * 100, 2) AS pAcceptedPerformance,\n" +
            "  COUNT(date_enrolled_prep) AS pEnrollPrepdNumerator,\n" +
            "  COUNT(offAndAccpt) AS pEnrollDenominator,\n" +
            "  ROUND((CAST(COUNT(date_enrolled_prep) AS DECIMAL) / COUNT(offAndAccpt)) * 100, 2) AS pEnrollPerformance,\n" +
            "  COUNT(urinalysis) AS pEnrolledPrepUrinaNumerator,\n" +
            "  COUNT(date_enrolled_prep) AS pEnrolledPrepUrinaDenominator,\n" +
            "  ROUND((CAST(COUNT(urinalysis) AS DECIMAL) / COUNT(date_enrolled_prep)) * 100, 2) AS pEnrolledPrepUrinaPerformance,\n" +
            "  COUNT(urinaGreaterthanenrollDate) AS pUrinaGreaterEnrollNumerator,\n" +
            "  COUNT(urinalysis) AS pUrinaGreaterEnrollDenominator,\n" +
            "  ROUND((CAST(COUNT(urinaGreaterthanenrollDate) AS DECIMAL) / COUNT(urinalysis)) * 100, 2) AS pUrinaGreaterEnrollPerformance,\n" +
            "  COUNT(urinaGreaterthanStatusDate) AS pUrinaGreaterStatusDateNumerator,\n" +
            "  COUNT(urinalysis) AS pUrinaGreaterStatusDateDenominator,\n" +
            "  ROUND((CAST(COUNT(urinaGreaterthanStatusDate) AS DECIMAL) / COUNT(urinalysis)) * 100, 2) AS pUrinaGreaterStatusDatePerformance,\n" +
            "  COUNT(enrollDateLessThanCommenced) AS commencedNumerator,\n" +
            "  COUNT(encounter_date) AS commencedDenominator,\n" +
            "  ROUND((CAST(COUNT(enrollDateLessThanCommenced) AS DECIMAL) / COUNT(encounter_date)) * 100, 2) AS commencedPerformance\n" +
            "FROM\n" +
            "\tprepSummary", nativeQuery = true)
    List<PrepSummaryDTOProjection> getPrepSummary(Long facilityId);


}
