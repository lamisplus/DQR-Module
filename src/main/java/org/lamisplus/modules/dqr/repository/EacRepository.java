package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.EacDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.lamisplus.modules.dqr.util.DQRQueries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EacRepository extends JpaRepository<DQA, Long> {

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, \n" +
            "p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM patient_person p \n" +
            "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON (p1.person_uuid)\n" +
            "p1.person_uuid,\n" +
            "p1.visit_date AS last_visit_date,\n" +
            "p1.next_appointment AS last_next_appointment,\n" +
            "r.duration,\n" +
            "CASE\n" +
            "WHEN t.hiv_status = 'KNOWN_DEATH' THEN 'Dead'\n" +
            "WHEN t.hiv_status IN ('ART_TRANSFER_OUT', 'ART Transfer Out') THEN 'Transferred Out'\n" +
            "WHEN AGE(NOW(), (p1.visit_date + r.duration * INTERVAL '1 DAY')) <= INTERVAL '28 DAYS' THEN 'Active'\n" +
            "WHEN AGE(NOW(), (p1.visit_date + r.duration * INTERVAL '1 DAY')) > INTERVAL '28 DAYS' THEN 'IIT'\n" +
            "END AS status\n" +
            "FROM (\n" +
            "SELECT \n" +
            "person_uuid,\n" +
            "MAX(visit_date) AS max_visit_date\n" +
            "FROM hiv_art_pharmacy\n" +
            "GROUP BY person_uuid\n" +
            ") AS max_dates\n" +
            "JOIN hiv_art_pharmacy p1 ON max_dates.person_uuid = p1.person_uuid AND max_dates.max_visit_date = p1.visit_date\n" +
            "CROSS JOIN LATERAL (\n" +
            "SELECT\n" +
            "reg->>'regimenName' AS regimenName,\n" +
            "CAST ((reg->>'duration') AS INTEGER) AS duration\n" +
            "FROM jsonb_array_elements(p1.extra->'regimens') AS reg\n" +
            " ) AS r\n" +
            "JOIN (\n" +
            "SELECT\n" +
            "person_id,\n" +
            "hiv_status,\n" +
            "ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC) AS rn\n" +
            "FROM hiv_status_tracker\n" +
            " ) AS t ON p1.person_uuid = t.person_id AND t.rn = 1\n" +
            " JOIN hiv_regimen hr ON r.regimenName = hr.description\n" +
            "JOIN hiv_regimen_type hrt ON hr.regimen_type_id = hrt.id AND hrt.id IN (1, 2, 3, 4, 14) AND p1.archived != 1\n" +
            ") ph ON p.uuid = ph.person_uuid"+
            "\tLEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, refill_period\n" +
            "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  where archived !=1\n" +
            "GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status) ca ON p.uuid = ca.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "\t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "           ELSE NULL END AS clastViralLoad, \n" +
            "\tlr.result_reported AS lastViralLoad,\n" +
            "\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "FROM laboratory_order lo\n" +
            "LEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "GROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "ON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "LEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "LEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "LEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "INNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "LEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "LEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "WHERE  lo.archived=0 ) vl ON  e.person_uuid = vl.person_uuid\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "\tselect DISTINCT ON (ha.person_uuid) ha.person_uuid, ha.status,   ha.last_viral_load, ha.date_of_last_viral_load, MAX(has.eac_session_date) as visit_date from hiv_eac ha\n" +
            "\tLEFT JOIN hiv_eac_session has ON ha.visit_id = has.visit_id AND ha.person_uuid = has.person_uuid\n" +
            "\tWHERE ha.status = 'COMPLETED'\n" +
            "\tGROUP BY ha.person_uuid,  ha.status, ha.last_viral_load, ha.date_of_last_viral_load, has.eac_session_date\n" +
            "\tORDER BY ha.person_uuid, has.eac_session_date\n" +
            ") eac1 ON e.person_uuid = eac1.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "( select DISTINCT ON (person_uuid) person_uuid, MIN(eac_session_date) AS eac_commenced from hiv_eac_session\n" +
            " GROUP BY person_uuid, eac_session_date ORDER BY  person_uuid, eac_session_date ASC ) eac ON e.person_uuid = eac.person_uuid \n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  LEFT JOIN hiv_eac ha ON e.person_uuid = ha.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND pharm.visit_date  >= NOW() - INTERVAL '6 MONTH' AND pharm.visit_date <= NOW()\n" +
            "  AND vl.clastViralLoad > 1000 AND eac.eac_commenced IS NULL\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, ca.visit_date, p.facility_id, \n" +
            "  pharm.visit_date, ha.status, vl.clastViralLoad, eac.eac_commenced, vl.dateSampleCollected,\n" +
            "  eac1.status, eac1.last_viral_load, eac1.date_of_last_viral_load, eac1.visit_date\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getEligibleWithNoVlResult(Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, \n" +
            "p.date_of_birth AS dateOfBirth, ph.status FROM patient_person p \n" +
            "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON (p1.person_uuid)\n" +
            "p1.person_uuid,\n" +
            "p1.visit_date AS last_visit_date,\n" +
            "p1.next_appointment AS last_next_appointment,\n" +
            "r.duration,\n" +
            "CASE\n" +
            "WHEN t.hiv_status = 'KNOWN_DEATH' THEN 'Dead'\n" +
            "WHEN t.hiv_status IN ('ART_TRANSFER_OUT', 'ART Transfer Out') THEN 'Transferred Out'\n" +
            "WHEN AGE(NOW(), (p1.visit_date + r.duration * INTERVAL '1 DAY')) <= INTERVAL '28 DAYS' THEN 'Active'\n" +
            "WHEN AGE(NOW(), (p1.visit_date + r.duration * INTERVAL '1 DAY')) > INTERVAL '28 DAYS' THEN 'IIT'\n" +
            "END AS status\n" +
            "FROM (\n" +
            "SELECT \n" +
            "person_uuid,\n" +
            "MAX(visit_date) AS max_visit_date\n" +
            "FROM hiv_art_pharmacy\n" +
            "GROUP BY person_uuid\n" +
            ") AS max_dates\n" +
            "JOIN hiv_art_pharmacy p1 ON max_dates.person_uuid = p1.person_uuid AND max_dates.max_visit_date = p1.visit_date\n" +
            "CROSS JOIN LATERAL (\n" +
            "SELECT\n" +
            "reg->>'regimenName' AS regimenName,\n" +
            "CAST ((reg->>'duration') AS INTEGER) AS duration\n" +
            "FROM jsonb_array_elements(p1.extra->'regimens') AS reg\n" +
            " ) AS r\n" +
            "JOIN (\n" +
            "SELECT\n" +
            "person_id,\n" +
            "hiv_status,\n" +
            "ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC) AS rn\n" +
            "FROM hiv_status_tracker\n" +
            " ) AS t ON p1.person_uuid = t.person_id AND t.rn = 1\n" +
            " JOIN hiv_regimen hr ON r.regimenName = hr.description\n" +
            "JOIN hiv_regimen_type hrt ON hr.regimen_type_id = hrt.id AND hrt.id IN (1, 2, 3, 4, 14) AND p1.archived != 1\n" +
            ") ph ON p.uuid = ph.person_uuid" +
            "\tLEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, refill_period\n" +
            "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  where archived !=1\n" +
            "GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status) ca ON p.uuid = ca.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "\t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "           ELSE NULL END AS clastViralLoad, \n" +
            "\tlr.result_reported AS lastViralLoad,\n" +
            "\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "FROM laboratory_order lo\n" +
            "LEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "GROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "ON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "LEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "LEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "LEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "INNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "LEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "LEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "WHERE  lo.archived=0 ) vl ON  e.person_uuid = vl.person_uuid\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "\tselect DISTINCT ON (ha.person_uuid) ha.person_uuid, ha.status,   ha.last_viral_load, ha.date_of_last_viral_load, MAX(has.eac_session_date) as visit_date from hiv_eac ha\n" +
            "\tLEFT JOIN hiv_eac_session has ON ha.visit_id = has.visit_id AND ha.person_uuid = has.person_uuid\n" +
            "\tWHERE ha.status = 'COMPLETED'\n" +
            "\tGROUP BY ha.person_uuid,  ha.status, ha.last_viral_load, ha.date_of_last_viral_load, has.eac_session_date\n" +
            "\tORDER BY ha.person_uuid, has.eac_session_date\n" +
            ") eac1 ON e.person_uuid = eac1.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "( select DISTINCT ON (person_uuid) person_uuid, MIN(eac_session_date) AS eac_commenced from hiv_eac_session\n" +
            " GROUP BY person_uuid, eac_session_date ORDER BY  person_uuid, eac_session_date ASC ) eac ON e.person_uuid = eac.person_uuid \n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  LEFT JOIN hiv_eac ha ON e.person_uuid = ha.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND eac1.status is NOT NULL AND vl.dateSampleCollected IS  NULL\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, ca.visit_date, p.facility_id, \n" +
            "  pharm.visit_date, ha.status, vl.clastViralLoad, eac.eac_commenced, vl.dateSampleCollected,\n" +
            "  eac1.status, eac1.last_viral_load, eac1.date_of_last_viral_load, eac1.visit_date\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getOnEacNotCompleted(Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, \n" +
            "p.date_of_birth AS dateOfBirth, ph.status \n" +
            "FROM patient_person p \n" +
            "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON (p1.person_uuid)\n" +
            "p1.person_uuid,\n" +
            "p1.visit_date AS last_visit_date,\n" +
            "p1.next_appointment AS last_next_appointment,\n" +
            "r.duration,\n" +
            "CASE\n" +
            "WHEN t.hiv_status = 'KNOWN_DEATH' THEN 'Dead'\n" +
            "WHEN t.hiv_status IN ('ART_TRANSFER_OUT', 'ART Transfer Out') THEN 'Transferred Out'\n" +
            "WHEN AGE(NOW(), (p1.visit_date + r.duration * INTERVAL '1 DAY')) <= INTERVAL '28 DAYS' THEN 'Active'\n" +
            "WHEN AGE(NOW(), (p1.visit_date + r.duration * INTERVAL '1 DAY')) > INTERVAL '28 DAYS' THEN 'IIT'\n" +
            "END AS status\n" +
            "FROM (\n" +
            "SELECT \n" +
            "person_uuid,\n" +
            "MAX(visit_date) AS max_visit_date\n" +
            "FROM hiv_art_pharmacy\n" +
            "GROUP BY person_uuid\n" +
            ") AS max_dates\n" +
            "JOIN hiv_art_pharmacy p1 ON max_dates.person_uuid = p1.person_uuid AND max_dates.max_visit_date = p1.visit_date\n" +
            "CROSS JOIN LATERAL (\n" +
            "SELECT\n" +
            "reg->>'regimenName' AS regimenName,\n" +
            "CAST ((reg->>'duration') AS INTEGER) AS duration\n" +
            "FROM jsonb_array_elements(p1.extra->'regimens') AS reg\n" +
            " ) AS r\n" +
            "JOIN (\n" +
            "SELECT\n" +
            "person_id,\n" +
            "hiv_status,\n" +
            "ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC) AS rn\n" +
            "FROM hiv_status_tracker\n" +
            " ) AS t ON p1.person_uuid = t.person_id AND t.rn = 1\n" +
            " JOIN hiv_regimen hr ON r.regimenName = hr.description\n" +
            "JOIN hiv_regimen_type hrt ON hr.regimen_type_id = hrt.id AND hrt.id IN (1, 2, 3, 4, 14) AND p1.archived != 1\n" +
            ") ph ON p.uuid = ph.person_uuid" +
            "\tLEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, refill_period\n" +
            "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  where archived !=1\n" +
            "GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status) ca ON p.uuid = ca.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "\t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "           ELSE NULL END AS clastViralLoad, \n" +
            "\tlr.result_reported AS lastViralLoad,\n" +
            "\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "FROM laboratory_order lo\n" +
            "LEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "GROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "ON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "LEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "LEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "LEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "INNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "LEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "LEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "WHERE  lo.archived=0 ) vl ON  e.person_uuid = vl.person_uuid\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "\tselect DISTINCT ON (ha.person_uuid) ha.person_uuid, ha.status,   ha.last_viral_load, ha.date_of_last_viral_load, MAX(has.eac_session_date) as visit_date from hiv_eac ha\n" +
            "\tLEFT JOIN hiv_eac_session has ON ha.visit_id = has.visit_id AND ha.person_uuid = has.person_uuid\n" +
            "\tWHERE ha.status = 'COMPLETED'\n" +
            "\tGROUP BY ha.person_uuid,  ha.status, ha.last_viral_load, ha.date_of_last_viral_load, has.eac_session_date\n" +
            "\tORDER BY ha.person_uuid, has.eac_session_date\n" +
            ") eac1 ON e.person_uuid = eac1.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "( select DISTINCT ON (person_uuid) person_uuid, MIN(eac_session_date) AS eac_commenced from hiv_eac_session\n" +
            " GROUP BY person_uuid, eac_session_date ORDER BY  person_uuid, eac_session_date ASC ) eac ON e.person_uuid = eac.person_uuid \n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  LEFT JOIN hiv_eac ha ON e.person_uuid = ha.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND eac1.status is NOT NULL AND eac1.visit_date IS NOT NULL AND eac1.date_of_last_viral_load IS NULL\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, ca.visit_date, p.facility_id, \n" +
            "  pharm.visit_date, ha.status, vl.clastViralLoad, eac.eac_commenced, vl.dateSampleCollected,\n" +
            "  eac1.status, eac1.last_viral_load, eac1.date_of_last_viral_load, eac1.visit_date\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getNoPostEacVl (Long facilityId);



    @Query(value = DQRQueries.DataConsistency.EAC_SUMMARY_QUERIES, nativeQuery = true)
    List<EacDTOProjection> getEacSummary(Long facilityId);

}
