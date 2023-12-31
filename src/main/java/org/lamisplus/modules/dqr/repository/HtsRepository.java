package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.HtsSummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HtsRepository extends JpaRepository<DQA, Long> {


    @Query(value = "SELECT \n" +
            "    client_code AS patientId,\n" +
            "    client_code AS hospitalNumber,\n" +
            "    extra->> 'gender' AS sex,\n" +
            "    extra->> 'date_of_birth' AS dateOfBirth,\n" +
            "    CAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((extra->>'date_of_birth') AS DATE))) AS INTEGER) AS age,\n" +
            "\thiv_test_result AS status\n" +
            "FROM \n" +
            "    hts_client\n" +
            "WHERE \n" +
            "    CAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((extra->>'date_of_birth') AS DATE))) AS INTEGER) > 15\n" +
            "    AND person_uuid IS NOT NULL \n" +
            "\tAND hiv_test_result = 'Positive'\n" +
            "    AND RECENCY IS NULL \n" +
            "    AND facility_id = ?1 \n" +
            "    AND archived != 1;", nativeQuery = true)
    List<PatientDTOProjection> getHtsPosNoRecency(Long facilityId);


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)\n" +
            "    \thc.client_code AS patientId,\n" +
            "    \thc.client_code AS hospitalNumber,\n" +
            "    \thc.extra->> 'gender' AS sex,\n" +
            "    \thc.extra->> 'date_of_birth' AS dateOfBirth,\n" +
            "    \tCAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((hc.extra->>'date_of_birth') AS DATE))) AS INTEGER) AS age,\n" +
            "\t\thc.hiv_test_result AS status\n" +
            "\tFROM\n" +
            "       \thts_client hc\n" +
            "       LEFT JOIN hts_index_elicitation hie ON hc.uuid = hie.hts_client_uuid\n" +
            "       LEFT JOIN laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
            "       LEFT JOIN laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
            "WHERE \n" +
            "       hc.facility_id = ?1 \n" +
            "       AND hc.hiv_test_result = 'Positive'\n" +
            "       AND recency->>'rencencyInterpretation' ILIKE '%Recent%' ESCAPE ' '\n" +
            "       AND lo.order_date IS NULL\n" +
            "\t   AND lo.archived != 1 \n" +
            "\t   AND lr.archived != 1\n" +
            "\t   AND hc.archived != 1", nativeQuery = true)
    List<PatientDTOProjection> getHtsPosRecentNoVlSampleDate (Long facilityId);


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)\n" +
            "    \thc.client_code AS patientId,\n" +
            "    \thc.client_code AS hospitalNumber,\n" +
            "    \thc.extra->> 'gender' AS sex,\n" +
            "    \thc.extra->> 'date_of_birth' AS dateOfBirth,\n" +
            "    \tCAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((hc.extra->>'date_of_birth') AS DATE))) AS INTEGER) AS age,\n" +
            "\t\thc.hiv_test_result AS status\n" +
            "\tFROM\n" +
            "       \thts_client hc\n" +
            "       LEFT JOIN hts_index_elicitation hie ON hc.uuid = hie.hts_client_uuid\n" +
            "       LEFT JOIN laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
            "       LEFT JOIN laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
            "WHERE \n" +
            "       hc.facility_id = ?1 \n" +
            "       AND hc.hiv_test_result = 'Positive'\n" +
            "       AND recency->>'rencencyInterpretation' ILIKE '%Recent%' ESCAPE ' '\n" +
            "       AND lo.order_date IS NULL\n" +
            "\t   AND lo.archived != 1 \n" +
            "\t   AND lr.archived != 1\n" +
            "\t   AND hc.archived != 1\n" +
            "\t   AND lr.date_result_reported IS NULL;\n", nativeQuery = true)
    List<PatientDTOProjection> getHtsPosRecentNoVlSampleAndNoResultDate(Long facilityId);


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)\n" +
            "    \thc.client_code AS patientId,\n" +
            "    \thc.client_code AS hospitalNumber,\n" +
            "    \thc.extra->> 'gender' AS sex,\n" +
            "    \thc.extra->> 'date_of_birth' AS dateOfBirth,\n" +
            "    \tCAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((hc.extra->>'date_of_birth') AS DATE))) AS INTEGER) AS age,\n" +
            "\t\thc.hiv_test_result AS status\n" +
            "\tFROM\n" +
            "       \thts_client hc\n" +
            "       LEFT JOIN hts_index_elicitation hie ON hc.uuid = hie.hts_client_uuid\n" +
            "       LEFT JOIN laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
            "       LEFT JOIN laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
            "WHERE \n" +
            "       hc.facility_id = ?1 \n" +
            "       AND hc.hiv_test_result = 'Positive'\n" +
            "       AND recency->>'rencencyInterpretation' ILIKE '%Recent%' ESCAPE ' '\n" +
            "       AND lo.order_date IS NULL\n" +
            "\t   AND lo.archived != 1 \n" +
            "\t   AND lr.archived != 1\n" +
            "\t   AND hc.archived != 1\n" +
            "\t   AND lr.date_result_reported IS NULL\n" +
            "\t   AND CAST(lr.date_result_reported AS DATE) > CAST(lo.order_date AS DATE);\n", nativeQuery = true)
    List<PatientDTOProjection> getHtsPosRecentVlResultDateGreaterThanReportDate(Long facilityId);


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)\n" +
            "    \thc.client_code AS patientId,\n" +
            "    \thc.client_code AS hospitalNumber,\n" +
            "    \thc.extra->> 'gender' AS sex,\n" +
            "    \thc.extra->> 'date_of_birth' AS dateOfBirth,\n" +
            "    \tCAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((hc.extra->>'date_of_birth') AS DATE))) AS INTEGER) AS age,\n" +
            "\t\thc.hiv_test_result AS status\n" +
            "\t\t\n" +
            "\tFROM\n" +
            "       \thts_client hc\n" +
            "       LEFT JOIN hts_index_elicitation hie ON hc.uuid = hie.hts_client_uuid\n" +
            "       LEFT JOIN laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
            "       LEFT JOIN laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
            "WHERE \n" +
            "       hc.facility_id = ?1 \n" +
            "       AND hc.hiv_test_result = 'Positive'\n" +
            "       AND hc.recency IS NOT NULL\n" +
            "\t   AND hc.RECENCY->>'rencencyId' IS NOT NULL \n" +
            "\t   AND hc.RECENCY->>'rencencyId' != ''\n" +
            "\t   AND CAST(hc.RECENCY->>'optOutRTRITestDate' AS DATE) < hc.date_visit\n" +
            "\t   AND lo.archived != 1 \n" +
            "\t   AND lr.archived != 1\n" +
            "\t   AND hc.archived != 1", nativeQuery = true)
    List<PatientDTOProjection> getHtsPosRecencyDateLessThanStatusDate(Long facilityId);


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)\n" +
            "    \thc.client_code AS patientId,\n" +
            "    \thc.client_code AS hospitalNumber,\n" +
            "    \thc.extra->> 'gender' AS sex,\n" +
            "    \thc.extra->> 'date_of_birth' AS dateOfBirth,\n" +
            "    \tCAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((hc.extra->>'date_of_birth') AS DATE))) AS INTEGER) AS age,\n" +
            "\t\thc.hiv_test_result AS status\n" +
            "\tFROM\n" +
            "       \thts_client hc\n" +
            "       LEFT JOIN hts_index_elicitation hie ON hc.uuid = hie.hts_client_uuid\n" +
            "       LEFT JOIN laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
            "       LEFT JOIN laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
            "WHERE \n" +
            "       hc.facility_id = ?1 \n" +
            "       AND hc.hiv_test_result = 'Positive'\n" +
            "       AND hie.hts_client_uuid is null\n" +
            "\t   AND hc.archived != 1", nativeQuery = true)
    List<PatientDTOProjection> getHtsPosNoElicitation (Long facilityId);


    @Query(value = "SELECT DISTINCT ON (hc.person_uuid)\n" +
            "    \thc.client_code AS patientId,\n" +
            "    \thc.client_code AS hospitalNumber,\n" +
            "    \thc.extra->> 'gender' AS sex,\n" +
            "    \thc.extra->> 'date_of_birth' AS dateOfBirth,\n" +
            "    \tCAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((hc.extra->>'date_of_birth') AS DATE))) AS INTEGER) AS age,\n" +
            "\t\thc.hiv_test_result AS status\n" +
            "\tFROM\n" +
            "       \thts_client hc\n" +
            "       LEFT JOIN hts_index_elicitation hie ON hc.uuid = hie.hts_client_uuid\n" +
            "       LEFT JOIN laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
            "       LEFT JOIN laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
            "WHERE \n" +
            "       hc.facility_id = ?1 \n" +
            "\t   AND hc.archived != 1\n" +
            "\t   AND hc.index_client = true \n" +
            "\t   AND hc.testing_setting is null", nativeQuery = true)
    List<PatientDTOProjection> getHtsNoTestSettings (Long facilityId);



    @Query(value = "SELECT \n" +
            "    \thc.client_code AS patientId,\n" +
            "    \thc.client_code AS hospitalNumber,\n" +
            "    \thc.extra->> 'gender' AS sex,\n" +
            "    \thc.extra->> 'date_of_birth' AS dateOfBirth,\n" +
            "    \tCAST(EXTRACT(YEAR FROM AGE(NOW(), CAST((hc.extra->>'date_of_birth') AS DATE))) AS INTEGER) AS age,\n" +
            "\t\thc.hiv_test_result AS status\n" +
            "\tFROM\n" +
            "       \thts_client hc\n" +
            "WHERE \n" +
            "       hc.facility_id = ?1 \n" +
            "\t   AND hc.archived != 1\n" +
            "\t   AND hc.target_group IS NULL", nativeQuery = true)
    List<PatientDTOProjection> getHtsNoTargetGroup (Long facilityId);


    @Query(value = "WITH htsSummary AS (\n" +
            "\tSELECT person_uuid, client_code, date_visit, target_group, hts_client_uuid, hasindex, testing_setting, gender,date_of_birth, age, hiv_test_result, poscount, adultPos,\n" +
            "\t (CASE WHEN adultPos =1 AND recency IS NOT NULL  THEN 1 ELSE NULL END) AS adPosRec,\n" +
            "\t rita, order_date,\n" +
            "\t(CASE WHEN rita ILIKE '%Recent%' ESCAPE ' ' THEN 1 ELSE NULL END) AS recentInfection,\n" +
            "\t(CASE WHEN (rita ILIKE '%Recent%' ESCAPE ' '  AND order_date IS NOT NULL) THEN 1 ELSE NULL END) AS recentwitVL,\n" +
            "\t(CASE WHEN (rita ILIKE '%Recent%' ESCAPE ' '  AND result_reported IS NOT NULL AND order_date IS NOT NULL) THEN 1 ELSE NULL END) AS recentwitVlResl,\n" +
            "\t(CASE WHEN resultdate > order_date THEN 1 ELSE NULL END) AS rsGreaterThan, resultdate, recency, recencydate,\n" +
            "\t(CASE WHEN (recencydate >= date_visit) AND recencydate IS NOT NULL AND date_visit IS NOT NULL THEN 1 ELSE NULL END) AS dateconfirm,\n" +
            "\t(CASE WHEN testing_setting IS NOT NULL AND hasindex IS NOT NULL  THEN 1 ELSE NULL END) AS settings\n" +
            "\tFROM \n" +
            "\t(\n" +
            "select DISTINCT ON (person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  hc.target_group,  hie.hts_client_uuid, (CASE WHEN hc.index_client = 'true' THEN 1 ELSE null END) hasIndex,\n" +
            "hc.testing_setting,\n" +
            "CAST((hc.extra->>'gender') AS VARCHAR(100)) AS gender, CAST((hc.extra->>'date_of_birth') AS DATE) AS date_of_birth, \n" +
            "CAST(EXTRACT(YEAR FROM AGE(NOW(), CAST(hc.extra->>'date_of_birth' AS DATE))) AS INTEGER) AS age\n" +
            ", hc.hiv_test_result,\n" +
            "(CASE WHEN hc.hiv_test_result = 'Positive' THEN 1 ELSE null END) AS posCount,\n" +
            "(CASE WHEN CAST(EXTRACT(YEAR FROM AGE(NOW(), CAST(hc.extra->>'date_of_birth' AS DATE))) AS INTEGER) > 15 THEN 1 ELSE null END) AS adultPos,\n" +
            "recency->>'rencencyId' As recency,\n" +
            "recency->>'rencencyInterpretation' AS RIta,CAST(lo.order_date AS DATE),\n" +
            "lr.result_reported, CAST(lr.date_result_reported AS DATE) AS resultdate, (CASE   WHEN recency->>'optOutRTRITestDate' IS NOT NULL   AND recency->>'optOutRTRITestDate' <> '' \n" +
            "  THEN CAST(recency->>'optOutRTRITestDate' AS DATE) \n" +
            "  ELSE NULL \n" +
            "END) AS recencydate\n" +
            "from hts_client hc\n" +
            "LEFT JOIN hts_index_elicitation hie on hc.uuid = hie.hts_client_uuid\n" +
            "LEFT JOIN laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
            "LEFT JOIN laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
            "\n" +
            "\twhere hc.facility_id = ?1 ) pro\n" +
            ")\n" +
            "SELECT \n" +
            "  COUNT(adPosRec) AS totalPosNumerator,\n" +
            "  COUNT(adultpos) AS totalPosDenominator,\n" +
            "--   ROUND((CAST(COUNT(prep_offered) AS DECIMAL) / COUNT(person_uuid)) * 100, 2) AS pOfferredPerformance,\n" +
            "  COUNT(recentwitVL) AS withVLNumerator,\n" +
            "  COUNT(recentInfection) AS withVLDenominator,\n" +
            "  ROUND((CAST(COUNT(recentwitVL) AS DECIMAL) / COUNT(recentInfection)) * 100, 2) AS withVLPerformance,\n" +
            "  COUNT(recentwitVlResl) AS withVlResNumerator,\n" +
            "  COUNT(recentwitVL) AS withVlResDenominator,\n" +
            "  ROUND((CAST(COUNT(recentwitVlResl) AS DECIMAL) / COUNT(recentwitVL)) * 100, 2) AS withVlResPerformance,\n" +
            "  COUNT(rsGreaterThan) AS rsGreaterNumerator,\n" +
            "  COUNT(resultdate) AS rsGreaterDenominator,\n" +
            "  ROUND((CAST(COUNT(rsGreaterThan) AS DECIMAL) / COUNT(resultdate)) * 100, 2) AS rsGreaterPerformance,\n" +
            "  COUNT(dateconfirm) AS recencyNumerator,\n" +
            "  COUNT(recency) AS recencyDenominator,\n" +
            "  ROUND((CAST(COUNT(dateconfirm) AS DECIMAL) / COUNT(recency)) * 100, 2) AS recencyPerformance,\n" +
            "  COUNT(hts_client_uuid) AS elicitedNumerator,\n" +
            "  COUNT(hasindex) AS elicitedDenominator,\n" +
            "  ROUND((CAST(COUNT(dateconfirm) AS DECIMAL) / COUNT(hasindex)) * 100, 2) AS elicitedPerformance,\n" +
            "  COUNT(settings) AS settingsNumerator,\n" +
            "  COUNT(hasindex) AS settingsDenominator,\n" +
            "  ROUND((CAST(COUNT(settings) AS DECIMAL) / COUNT(hasindex)) * 100, 2) AS settingsPerformance,\n" +
            "  COUNT(target_group) AS targNumerator,\n" +
            "  COUNT(person_uuid) AS targDenominator,\n" +
            "  ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(person_uuid)) * 100, 2) AS targPerformance\n" +
            "  FROM\t\n" +
            "  \thtsSummary", nativeQuery = true)
    List<HtsSummaryDTOProjection> getHtsSummary (Long facilityId);

}
