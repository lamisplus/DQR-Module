package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.HtsSummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.lamisplus.modules.dqr.util.DQRQueries;
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


    @Query(value = DQRQueries.DataConsistency.HTS_SUMMARY_QUERIES, nativeQuery = true)
    List<HtsSummaryDTOProjection> getHtsSummary (Long facilityId);

}
