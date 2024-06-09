package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.ClientVerificationDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.lamisplus.modules.dqr.util.DQRQueries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientVerificationRepository extends JpaRepository<DQA, Long> {


    @Query(value = "SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   INNER JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT CAST(sample.date_sample_collected AS DATE ) as dateOfViralLoadSampleCollection, patient_uuid as person_uuid1  FROM (\n" +
            "     SELECT lt.viral_load_indication, sm.facility_id,sm.date_sample_collected, sm.patient_uuid, sm.archived, ROW_NUMBER () OVER (PARTITION BY sm.patient_uuid ORDER BY date_sample_collected DESC) as rnkk\n" +
            "     FROM public.laboratory_sample  sm\n" +
            "  INNER JOIN public.laboratory_test lt ON lt.id = sm.test_id\n" +
            "     WHERE lt.lab_test_id=16\n" +
            "       AND  lt.viral_load_indication !=719\n" +
            "       AND date_sample_collected IS NOT null\n" +
            " )as sample\n" +
            "         WHERE sample.rnkk = 1\n" +
            "           AND (sample.archived is null OR sample.archived = 0)\n" +
            ") sampleCol ON ca.person_uuid = sampleCol.person_uuid1\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active' AND\n" +
            "  CAST(DATE_PART('year', AGE(NOW(), ca.visit_date)) * 12 + DATE_PART('month', AGE(NOW(), ca.visit_date)) AS INTEGER ) >= 6 \n" +
            "  AND sampleCol.dateOfViralLoadSampleCollection IS NULL", nativeQuery = true)
    List<PatientDTOProjection> getVlPrior (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   INNER JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\t\n" +
            "INNER JOIN (\n" +
            "\tSELECT DISTINCT ON (hap.person_uuid) hap.person_uuid,hac.pregnancy_status ,hac.next_appointment, hac.tb_status ,\n" +
            "hap.next_appointment , hap.extra IS ,hap.refill_period FROM hiv_art_clinical hac\n" +
            "LEFT JOIN HIV_ART_PHARMACY hap ON hap.person_uuid = hac.person_uuid\n" +
            "WHERE hap.archived = 0 AND hac.archived = 0 AND (hac.pregnancy_status IS NULL OR hac.next_appointment IS NULL OR hac.tb_status IS NULL OR\n" +
            "hap.next_appointment IS NULL OR hap.extra IS NULL OR hap.refill_period IS NULL)\n" +
            "\t) incomplete ON ca.person_uuid = incomplete.person_uuid\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active'", nativeQuery = true)
    List<PatientDTOProjection> getIncompleteEncounter (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   INNER JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT person_uuid, visit_date, previousPickUpDrugDate,\n" +
            "CAST(DATE_PART('year', AGE(visit_date, previousPickUpDrugDate)) * 12 + DATE_PART('month', AGE(visit_date, previousPickUpDrugDate)) AS INTEGER ) AS monthApart\n" +
            "FROM (\n" +
            "select person_uuid, visit_date,\n" +
            "ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY visit_date DESC),\n" +
            "LEAD(visit_date) OVER (PARTITION BY person_uuid ORDER BY visit_date DESC) AS previousPickUpDrugDate\n" +
            "from hiv_art_pharmacy\n" +
            ") pharm where\n" +
            "row_number = 1\n" +
            ") pickUp ON ca.person_uuid = pickUP.person_uuid\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active' AND pickUp.monthApart >= 12", nativeQuery = true)
    List<PatientDTOProjection> getLastPickMoreThanOneYear (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   INNER JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\t\n" +
            "INNER JOIN (\n" +
            "SELECT\n" +
            "\tDISTINCT ON (person_uuid)\n" +
            "    person_uuid,\n" +
            "    visit_date,\n" +
            "    next_appointment\n" +
            "FROM\n" +
            "    HIV_ART_CLINICAL\n" +
            "WHERE\n" +
            "    archived = 0\n" +
            "    AND (person_uuid, visit_date, next_appointment) IN (\n" +
            "        SELECT\n" +
            "            person_uuid,\n" +
            "            visit_date,\n" +
            "            next_appointment\n" +
            "        FROM\n" +
            "            HIV_ART_CLINICAL\n" +
            "        WHERE\n" +
            "            archived = 0\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            visit_date,\n" +
            "            next_appointment\n" +
            "        HAVING\n" +
            "            COUNT(*) > 1\n" +
            "    )\n" +
            ") sameClinical ON ca.person_uuid = sameClinical.person_uuid\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active'", nativeQuery = true)
    List<PatientDTOProjection> getDuplicateClinicVisit (Long facilityId);

    @Query(value = " SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   INNER JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
            "\t LEFT JOIN (\n" +
            " SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, enrollment_date, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
            "  WHERE archived = 0 AND recapture = 0  GROUP BY person_uuid, recapture, enrollment_date) b ON  b.person_uuid = ca.person_uuid\n" +
            "LEFT JOIN (\n" +
            " SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, enrollment_date, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
            "  WHERE archived = 0 AND recapture != 0  GROUP BY person_uuid, recapture, enrollment_date) recap ON  recap.person_uuid = ca.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT * FROM (\n" +
            "SELECT \n" +
            "\tDISTINCT ON (person_uuid)\n" +
            "\tperson_uuid, visit_date,next_appointment, tb_status,\n" +
            "\tCAST(DATE_PART('year', AGE(now(), visit_date)) * 12 + DATE_PART('month', AGE(now(), visit_date)) AS INTEGER ) AS monthTillDate,\t\n" +
            "ROW_NUMBER() OVER ( PARTITION BY person_uuid ORDER BY visit_date DESC)\n" +
            "FROM HIV_ART_CLINICAL \n" +
            "\tWHERE archived = 0\n" +
            "\t) visit where row_number = 1\n" +
            ") lastVisit ON ca.person_uuid = lastVisit.person_uuid\t\n" +
            "  LEFT JOIN (\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active' AND\n" +
            "(lastVisit.visit_date > recap.enrollment_date) AND b.enrollment_date IS NOT NULL;\t", nativeQuery = true)
    List<PatientDTOProjection> getRecentClinicEncounterNoRecapture (Long facilityId);

    @Query(value ="SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "\t\tFROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "INNER JOIN (\n" +
            "SELECT * FROM (\n" +
            "SELECT \n" +
            "\tDISTINCT ON (person_uuid)\n" +
            "\tperson_uuid, visit_date,next_appointment, tb_status,\n" +
            "\tCAST(DATE_PART('year', AGE(now(), visit_date)) * 12 + DATE_PART('month', AGE(now(), visit_date)) AS INTEGER ) AS monthTillDate,\t\n" +
            "ROW_NUMBER() OVER ( PARTITION BY person_uuid ORDER BY visit_date DESC)\n" +
            "FROM HIV_ART_CLINICAL \n" +
            "\tWHERE archived = 0\n" +
            "\t) visit where row_number = 1 AND visit. monthTillDate >= 15\n" +
            ") lastVisit ON p.uuid = lastVisit.person_uuid\t\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active'\n" +
            "GROUP BY e.id, p.id, p.hospital_number, p.date_of_birth, ph.status", nativeQuery = true)
    List<PatientDTOProjection> getClinicGreaterThanOneFiveMonthYear (Long facilityId);

    @Query(value = "SELECT patientId, hospitalNumber, sex, age, dateOfBirth, status \n" +
            "FROM \n" +
            "\t(\n" +
            " SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, sameDemographics.person_uuid, ph.status\t\n" +
            "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   INNER JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN (\n" +
            "SELECT\n" +
            "\tDISTINCT ON (p1.HOSPITAL_NUMBER)\n" +
            "    p1.HOSPITAL_NUMBER,\n" +
            "    INITCAP(p1.sex) AS sex,\n" +
            "    p1.date_of_birth,\n" +
            "\tp1.uuid,\n" +
            "\tca1.person_uuid,\n" +
            "    ca1.visit_date,\n" +
            "    p2.HOSPITAL_NUMBER AS matching_hospital_number,\n" +
            "    INITCAP(p2.sex) AS matching_sex,\n" +
            "    p2.date_of_birth AS matching_date_of_birth,\n" +
            "    ca2.visit_date AS matching_visit_date\n" +
            "FROM\n" +
            "    PATIENT_PERSON p1\n" +
            "JOIN\n" +
            "    PATIENT_PERSON p2 ON p1.HOSPITAL_NUMBER = p2.HOSPITAL_NUMBER\n" +
            "                      AND INITCAP(p1.sex) = INITCAP(p2.sex)\n" +
            "                      AND p1.date_of_birth = p2.date_of_birth\n" +
            "                      AND p1.uuid <> p2.uuid  -- Exclude the same row\n" +
            "INNER JOIN (\n" +
            "    SELECT TRUE AS commenced, hac1.person_uuid, hac1.visit_date\n" +
            "    FROM hiv_art_clinical hac1\n" +
            "    WHERE hac1.archived = 0 AND hac1.is_commencement IS TRUE\n" +
            "    GROUP BY hac1.person_uuid, hac1.visit_date\n" +
            ") ca1 ON ca1.person_uuid = p1.uuid\n" +
            "INNER JOIN (\n" +
            "    SELECT TRUE AS commenced, hac2.person_uuid, hac2.visit_date\n" +
            "    FROM hiv_art_clinical hac2\n" +
            "    WHERE hac2.archived = 0 AND hac2.is_commencement IS TRUE\n" +
            "    GROUP BY hac2.person_uuid, hac2.visit_date\n" +
            ") ca2 ON ca2.person_uuid = p2.uuid\n" +
            ") sameDemographics ON ca.person_uuid = sameDemographics.uuid\n" +
            "  LEFT JOIN (\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active'\n" +
            "\t\tAND sameDemographics.person_uuid IS NOT NULL\n" +
            "   GROUP BY e.id, p.id, p.hospital_number, p.date_of_birth, sameDemographics.person_uuid, ph.status ) duplicateDemographics", nativeQuery = true)
    List<PatientDTOProjection> getDuplicateDemo (Long facilityId);

    @Query(value = "SELECT patientId, hospitalNumber, sex, age, dateOfBirth, status \n" +
            "FROM \n" +
            "\t(\n" +
            " SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, recap.person_uuid AS biometric, ph.status\t\n" +
            "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   INNER JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN (\n" +
            " SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, enrollment_date, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
            "  WHERE archived = 0 AND recapture != 0  GROUP BY person_uuid, recapture, enrollment_date) recap ON  recap.person_uuid = ca.person_uuid\n" +
            "  LEFT JOIN (\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active'\n" +
            "\t\tAND recap.person_uuid IS NULL\n" +
            "--    AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
            "   GROUP BY e.id, p.id, p.hospital_number, p.date_of_birth, recap.person_uuid, ph.status ) noRecaptureBiometric", nativeQuery = true)
    List<PatientDTOProjection> getNoRecaptureBiometric (Long facilityId);

    @Query(value = "SELECT patientId, hospitalNumber, sex, age, dateOfBirth, status \n" +
            "FROM \n" +
            "\t(\n" +
            " SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, b.person_uuid AS biometric, ph.status\t\n" +
            "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   INNER JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN (\n" +
            " SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, enrollment_date, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
            "  WHERE archived = 0 AND recapture = 0  GROUP BY person_uuid, recapture, enrollment_date) b ON  b.person_uuid = ca.person_uuid\n" +
            "  LEFT JOIN (\n" +
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
            ") ph ON p.uuid = ph.person_uuid\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND status = 'Active'\n" +
            "\t\tAND b.person_uuid IS NULL\n" +
            "--    AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
            "   GROUP BY e.id, p.id, p.hospital_number, p.date_of_birth, b.person_uuid, ph.status ) noBiometric\t", nativeQuery = true)
    List<PatientDTOProjection> getNoBaselineBiometric (Long facilityId);

    @Query(value = DQRQueries.ClientVerificationQueries.CLIENT_VERIFICATION, nativeQuery = true)
    List<ClientVerificationDTOProjection> getClientVerificationSummary(Long facilityId);
}
