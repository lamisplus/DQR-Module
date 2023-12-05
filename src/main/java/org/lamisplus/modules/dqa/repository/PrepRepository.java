package org.lamisplus.modules.dqa.repository;

import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
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

    @Query(value = "SELECT SUM(CASE WHEN prep_offered = true THEN 1 ELSE 0 END) AS numerator, count(person_uuid) as denominator, ROUND((CAST(SUM(CASE WHEN prep_offered = true THEN 1 ELSE 0 END) AS DECIMAL) / count(person_uuid)) * 100, 2) AS performance FROM (\t\n" +
            "SELECT DISTINCT ON (person_uuid) person_uuid, client_code, date_visit,  prep_offered, prep_accepted, hiv_test_result FROM \n" +
            "hts_client where hiv_test_result is not null AND hiv_test_result = 'Negative'\n" +
            "\t) prep", nativeQuery = true)
    List<PatientSummaryDTOProjection> getNegativeNotEnrolledOnPrepSumm (Long facilityId);


    @Query(value = "SELECT SUM(CASE WHEN prep_accepted = true THEN 1 ELSE 0 END) AS numerator, count(person_uuid) as denominator, ROUND((CAST(SUM(CASE WHEN prep_accepted = true THEN 1 ELSE 0 END) AS DECIMAL) / count(person_uuid)) * 100, 2) AS performance FROM (\t\n" +
            "SELECT DISTINCT ON (person_uuid) person_uuid,  client_code, date_visit,  prep_offered, prep_accepted, hiv_test_result FROM \n" +
            "hts_client where hiv_test_result is not null AND hiv_test_result = 'Negative' AND prep_offered = true\n" +
            "AND prep_accepted = true\n" +
            "\t) prep", nativeQuery = true)
    List<PatientSummaryDTOProjection> getNegativeOfferedPrepSumm (Long facilityId);


    @Query(value = "SELECT count(status) AS numerator, count(person_uuid) as denominator, ROUND((CAST(count(status) AS DECIMAL) / count(person_uuid)) * 100, 2) AS performance FROM (\t\n" +
            "SELECT DISTINCT ON (hc.person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  hc.prep_offered, hc.prep_accepted, hc.hiv_test_result, pe.status, pe.unique_id, pe.date_started FROM \n" +
            "hts_client hc LEFT JOIN prep_enrollment pe ON hc.person_uuid = pe.person_uuid where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
            "AND hc.prep_accepted = true ) prep_enrolled", nativeQuery = true)
    List<PatientSummaryDTOProjection> getInitiatedPrepSumm (Long facilityId);


    @Query(value = "SELECT SUM(CASE WHEN urinalysis_result != '' THEN 1 ELSE 0 END) AS numerator , count(person_uuid) as denominator, ROUND((CAST(SUM(CASE WHEN urinalysis_result != '' THEN 1 ELSE 0 END) AS DECIMAL) / count(person_uuid)) * 100, 2) AS performance FROM (\t\n" +
            "SELECT DISTINCT ON (hc.person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  hc.prep_offered, hc.prep_accepted, hc.hiv_test_result, pc.urinalysis_result FROM \n" +
            "hts_client hc LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND hc.prep_offered = true\n" +
            "AND hc.prep_accepted = true\n" +
            "\t) urinalysis", nativeQuery = true)
    List<PatientSummaryDTOProjection> getInitiatedWithUrinalysisPrepSumm (Long facilityId);


//    @Query(value = "", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getCurrentUrinalysisGreaterThanHivStatusSumm (Long facilityId);
//
//
//    @Query(value = "", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getDateRegisterLessThanDateCommencedSumm (Long facilityId);
//
//    @Query(value = "", nativeQuery = true)
//   List<PatientSummaryDTOProjection> getCurrentUrinalysisGreaterThanDateEnrolledSumm(Long facilityId);


}
