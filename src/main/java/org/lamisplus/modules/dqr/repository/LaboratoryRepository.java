package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.LaboratoryDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaboratoryRepository extends JpaRepository<DQA, Long> {

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "\t\tCAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth,ph.status\n" +
            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
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
            ") ph ON p.uuid = ph.person_uuid "+
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "-- \t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "--            ELSE NULL END AS lastViralLoad, \n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
            "\t\t\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\t\t\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "\t\tFROM laboratory_order lo\n" +
            "\t\tLEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "\t\tGROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "\t\tON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "\t\tLEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "\t\tLEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "\t\tINNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "\t\tWHERE  lo.archived=0\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\tLEFT JOIN(\n" +
            "\t\tselect DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
            "\t\tfrom laboratory_sample\n" +
            "\t\tWHERE test_id = 1\n" +
            "\t\tGROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
            "\tcd4 ON e.person_uuid = cd4.patient_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "  \t\t(SELECT DISTINCT ON (person_uuid)\n" +
            "    \tperson_uuid, visit_date, refill_period\n" +
            "\t\tFROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  \twhere archived !=1\n" +
            "\t\tGROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    \tperson_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND pharm.visit_date >= NOW() - INTERVAL '1 YEAR' AND pharm.visit_date <= NOW()\n" +
            "\t\tAND vl.lastViralLoad IS NULL\n" +
            "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth,ph.status, vl.dateSampleCollected, \n" +
            "\t\tvl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getEligibleWithNoVlResult (Long facilityId);



    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "\t\tCAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth,ph.status\n" +
            "\t\t FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
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
            ") ph ON p.uuid = ph.person_uuid "+
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "-- \t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "--            ELSE NULL END AS lastViralLoad, \n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
            "\t\t\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\t\t\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "\t\tFROM laboratory_order lo\n" +
            "\t\tLEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "\t\tGROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "\t\tON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "\t\tLEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "\t\tLEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "\t\tINNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "\t\tWHERE  lo.archived=0\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\tLEFT JOIN(\n" +
            "\t\tselect DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
            "\t\tfrom laboratory_sample\n" +
            "\t\tWHERE test_id = 1\n" +
            "\t\tGROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
            "\tcd4 ON e.person_uuid = cd4.patient_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "  \t\t(SELECT DISTINCT ON (person_uuid)\n" +
            "    \tperson_uuid, visit_date, refill_period\n" +
            "\t\tFROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  \twhere archived !=1\n" +
            "\t\tGROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    \tperson_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND vl.dateOfLastViralLoad IS NULL AND vl.dateSampleCollected IS NULL\n" +
            "\t\tAND pharm.visit_date >= NOW() - INTERVAL '1 YEAR' AND pharm.visit_date <= NOW()\n" +
            "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth,ph.status, vl.dateSampleCollected, \n" +
            "\t\tvl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getActiveNoVl (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "\t\tCAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth,ph.status\n" +
            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN (\n" +
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
            ") ph ON p.uuid = ph.person_uuid "+
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "-- \t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "--            ELSE NULL END AS lastViralLoad, \n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
            "\t\t\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\t\t\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "\t\tFROM laboratory_order lo\n" +
            "\t\tLEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "\t\tGROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "\t\tON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "\t\tLEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "\t\tLEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "\t\tINNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "\t\tWHERE  lo.archived=0\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\tLEFT JOIN(\n" +
            "\t\tselect DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
            "\t\tfrom laboratory_sample\n" +
            "\t\tWHERE test_id = 1\n" +
            "\t\tGROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
            "\tcd4 ON e.person_uuid = cd4.patient_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "  \t\t(SELECT DISTINCT ON (person_uuid)\n" +
            "    \tperson_uuid, visit_date, refill_period\n" +
            "\t\tFROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  \twhere archived !=1\n" +
            "\t\tGROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    \tperson_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND vl.dateOfLastViralLoad IS NOT NULL AND vl.pcrDate IS NULL\n" +
            "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth,ph.status, vl.dateSampleCollected, \n" +
            "\t\tvl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getHadVlNoPcrDate (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "\t\tCAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth,ph.status\t\n" +
            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "-- \t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "--            ELSE NULL END AS lastViralLoad, \n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
            "\t\t\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\t\t\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "\t\tFROM laboratory_order lo\n" +
            "\t\tLEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "\t\tGROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "\t\tON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "\t\tLEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "\t\tLEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "\t\tINNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "\t\tWHERE  lo.archived=0\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\tLEFT JOIN(\n" +
            "\t\tselect DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
            "\t\tfrom laboratory_sample\n" +
            "\t\tWHERE test_id = 1\n" +
            "\t\tGROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
            "\tcd4 ON e.person_uuid = cd4.patient_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "  \t\t(SELECT DISTINCT ON (person_uuid)\n" +
            "    \tperson_uuid, visit_date, refill_period\n" +
            "\t\tFROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  \twhere archived !=1\n" +
            "\t\tGROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    \tperson_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND vl.dateOfLastViralLoad IS NOT NULL AND vl.viralLoadType IS NULL\n" +
            "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth,ph.status, vl.dateSampleCollected, \n" +
            "\t\tvl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getHadNoVlIndicator (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "\t\tCAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth,ph.status\n" +
            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
            "\t\t\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\t\t\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "\t\tFROM laboratory_order lo\n" +
            "\t\tLEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "\t\tGROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "\t\tON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "\t\tLEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "\t\tLEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "\t\tINNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "\t\tWHERE  lo.archived=0\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\tLEFT JOIN(\n" +
            "\t\tselect DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
            "\t\tfrom laboratory_sample\n" +
            "\t\tWHERE test_id = 1\n" +
            "\t\tGROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
            "\tcd4 ON e.person_uuid = cd4.patient_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "  \t\t(SELECT DISTINCT ON (person_uuid)\n" +
            "    \tperson_uuid, visit_date, refill_period\n" +
            "\t\tFROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  \twhere archived !=1\n" +
            "\t\tGROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    \tperson_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND vl.dateSampleCollected > vl.dateOfLastViralLoad\n" +
            "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth,ph.status, vl.dateSampleCollected, \n" +
            "\t\tvl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getVlSampleDateGreaterThanVlReportDate (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "\t\tCAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth,ph.status\n" +
            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
            "\t\t\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\t\t\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "\t\tFROM laboratory_order lo\n" +
            "\t\tLEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "\t\tGROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "\t\tON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "\t\tLEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "\t\tLEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "\t\tINNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "\t\tWHERE  lo.archived=0\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\tLEFT JOIN(\n" +
            "\t\tselect DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
            "\t\tfrom laboratory_sample\n" +
            "\t\tWHERE test_id = 1\n" +
            "\t\tGROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
            "\tcd4 ON e.person_uuid = cd4.patient_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "  \t\t(SELECT DISTINCT ON (person_uuid)\n" +
            "    \tperson_uuid, visit_date, refill_period\n" +
            "\t\tFROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  \twhere archived !=1\n" +
            "\t\tGROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    \tperson_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND vl.dateOfLastViralLoad >= NOW() - INTERVAL '1 YEAR' AND vl.dateOfLastViralLoad <= NOW()\n" +
            "\t\tAND cd4.cd4date IS NULL\n" +
            "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth,ph.status , vl.dateSampleCollected, \n" +
            "\t\tvl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getVlWithinOneYearWithCD4 (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "\t\tCAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth,ph.status\n" +
            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "-- \t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "--            ELSE NULL END AS lastViralLoad, \n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
            "\t\t\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\t\t\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "\t\tFROM laboratory_order lo\n" +
            "\t\tLEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "\t\tGROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "\t\tON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "\t\tLEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "\t\tLEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "\t\tINNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "\t\tWHERE  lo.archived=0\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\tLEFT JOIN(\n" +
            "\t\tselect DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
            "\t\tfrom laboratory_sample\n" +
            "\t\tWHERE test_id = 1\n" +
            "\t\tGROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
            "\tcd4 ON e.person_uuid = cd4.patient_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "  \t\t(SELECT DISTINCT ON (person_uuid)\n" +
            "    \tperson_uuid, visit_date, refill_period\n" +
            "\t\tFROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  \twhere archived !=1\n" +
            "\t\tGROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    \tperson_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND cd4.cd4date IS NULL\n" +
            "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth,ph.status, vl.dateSampleCollected, \n" +
            "\t\tvl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getWithinOneYearWithNoCD4 (Long facilityId);


    @Query(value = "WITH vlSummary AS ( \t\t\n" +
            "\t\tSELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "\t\tCAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth, vl.dateSampleCollected, vl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date,\n" +
            "\t\t (CASE WHEN pharm.visit_date >= NOW() - INTERVAL '1 YEAR' AND pharm.visit_date <= NOW() THEN 1 ELSE null END) AS eligibleVl1year,\n" +
            "(CASE WHEN vl.dateOfLastViralLoad >= NOW() - INTERVAL '1 YEAR' AND vl.dateOfLastViralLoad <= NOW() THEN 1 ELSE null END) AS hadvl1year,\n" +
            "\t(CASE WHEN vl.dateOfLastViralLoad IS NOT NULL AND vl.dateSampleCollected IS NOT NULL THEN 1 ELSE NULL END) AS hadvlAndSampleDate,\n" +
            "\t(CASE WHEN vl.dateOfLastViralLoad IS NOT NULL AND vl.pcrDate IS NOT NULL THEN 1 ELSE NULL END) AS hadvlAndpcrDate,\n" +
            "\t(CASE WHEN vl.viralLoadType IS NOT NULL AND vl.dateSampleCollected IS NOT NULL THEN 1 ELSE NULL END) AS hadVlIndicator,\n" +
            "\t(CASE WHEN vl.dateOfLastViralLoad > vl.dateSampleCollected THEN 1 ELSE NULL END) AS vlDateGsDate,\n" +
            "\t(CASE WHEN cd4.cd4date >= NOW() - INTERVAL '1 YEAR' AND cd4.cd4date <= NOW() THEN 1 ELSE null END) AS hadcd4vl1year\n" +
            "\n" +
            "\t\n" +
            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "\t\tLEFT JOIN (\n" +
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "-- \t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "--            ELSE NULL END AS lastViralLoad, \n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
            "\t\t\tlr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
            "\t\t\tlr.date_result_reported as dateOfLastViralLoad\n" +
            "\t\tFROM laboratory_order lo\n" +
            "\t\tLEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "\t\tGROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "\t\tON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "\t\tLEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "\t\tLEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "\t\tINNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "\t\tLEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "\t\tWHERE  lo.archived=0\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\tLEFT JOIN(\n" +
            "\t\tselect DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
            "\t\tfrom laboratory_sample\n" +
            "\t\tWHERE test_id = 1\n" +
            "\t\tGROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
            "\tcd4 ON e.person_uuid = cd4.patient_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "  \t\t(SELECT DISTINCT ON (person_uuid)\n" +
            "    \tperson_uuid, visit_date, refill_period\n" +
            "\t\tFROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  \twhere archived !=1\n" +
            "\t\tGROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    \tperson_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 \n" +
            "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth, vl.dateSampleCollected, \n" +
            "\t\tvl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
            "        ORDER BY p.id DESC )\n" +
            "\t\tSELECT \n" +
            "  COUNT(hadvl1year) AS eligibleVlNumerator,\n" +
            "  COUNT(eligibleVl1year) AS eligibleVlDenominator,\n" +
            "  ROUND((CAST(COUNT(hadvl1year) AS DECIMAL) / COUNT(eligibleVl1year)) * 100, 2) AS eligibleVlPerformance,\n" +
            "  COUNT(hadvlAndSampleDate) AS hadVlNumerator,\n" +
            "  COUNT(dateOfLastViralLoad) AS hadVlDenominator,\n" +
            "  ROUND((CAST(COUNT(hadvlAndSampleDate) AS DECIMAL) / COUNT(dateOfLastViralLoad)) * 100, 2) AS hadVlPerformance,\n" +
            "  COUNT(hadvlAndpcrDate) AS hadPcrDateNumerator,\n" +
            "  COUNT(dateSampleCollected) AS hadPcrDateDenominator,\n" +
            "  ROUND((CAST(COUNT(hadvlAndpcrDate) AS DECIMAL) / COUNT(dateSampleCollected)) * 100, 2) AS hadPcrDatePerformance,\n" +
            "  COUNT(hadVlIndicator) AS  hadIndicatorNumerator,\n" +
            "  COUNT(dateSampleCollected) AS  hadIndicatorDenominator,\n" +
            "  ROUND((CAST(COUNT(hadVlIndicator) AS DECIMAL) / COUNT(dateSampleCollected)) * 100, 2) AS hadIndicatorPerformance,\n" +
            "  COUNT(vlDateGsDate) AS vlDateGsDateNumerator,\n" +
            "  COUNT(dateOfLastViralLoad) AS vlDateGsDateDenominator,\n" +
            "  ROUND((CAST(COUNT(vlDateGsDate) AS DECIMAL) / COUNT(dateOfLastViralLoad)) * 100, 2) AS vlDateGsDatePerformance,\n" +
            "  COUNT(hadcd4vl1year) AS treatmentCd4Numerator,\n" +
            "  COUNT(eligibleVl1year) AS treatmentCd4Denominator,\n" +
            "  ROUND((CAST(COUNT(hadcd4vl1year) AS DECIMAL) / COUNT(eligibleVl1year)) * 100, 2) AS treatmentCd4Performance,\n" +
            "  COUNT(hadcd4vl1year) AS cd4WithinYearNumerator,\n" +
            "  COUNT(hadcd4vl1year) AS cd4WithinYearDenominator,\n" +
            "  ROUND((CAST(COUNT(hadcd4vl1year) AS DECIMAL) / COUNT(eligibleVl1year)) * 100, 2) AS cd4WithinYearPerformance\n" +
            "  FROM \n" +
            "\tvlSummary\t", nativeQuery = true)
    List<LaboratoryDTOProjection> getLaboratorySummary(Long facilityId);

}
