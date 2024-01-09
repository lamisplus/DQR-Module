package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.ClinicalConsistencyDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DataConsistencyRepository extends JpaRepository<DQA, Long> {


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "      p.date_of_birth AS dateOfBirth, ph.status\n" +
            "     FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "     LEFT JOIN\n" +
            "     (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "     GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            ") ph ON p.uuid = ph.person_uuid " +
            "     LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "     WHERE p.archived=0 AND p.facility_id= ?1 AND e.target_group_id is null\n" +
            "     GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status\n" +
            "     ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithoutTargetGroup(Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, p.date_of_birth AS dateOfBirth, ph.status\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND e.entry_point_id is null\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithoutCareEntryPoint(Long facilityId);


    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  p.date_of_birth AS dateOfBirth,\n" +
            "  CASE\n" +
            "    WHEN t.hiv_status = 'KNOWN_DEATH' THEN 'Dead'\n" +
            "    WHEN t.hiv_status IN ('ART_TRANSFER_OUT', 'ART Transfer Out') THEN 'Transferred Out'\n" +
            "    WHEN AGE(NOW(), ph.last_visit_date) <= INTERVAL '28 DAYS' THEN 'Active'\n" +
            "    WHEN AGE(NOW(), ph.last_visit_date) > INTERVAL '28 DAYS' THEN 'IIT'\n" +
            "  END AS status\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            " LEFT JOIN\n" +
            "    (SELECT DISTINCT ON (person_uuid)\n" +
            "      person_uuid, visit_date, body_weight\n" +
            "  FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
            "      FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid\n" +
            "      GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
            "      person_uuid DESC ) tri ON tri.person_uuid = e.person_uuid\n" +
            "LEFT JOIN\n" +
            "  (\n" +
            "    SELECT\n" +
            "      person_id,\n" +
            "      hiv_status,\n" +
            "      ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC) AS rn\n" +
            "    FROM\n" +
            "      hiv_status_tracker\n" +
            "  ) AS t ON p.uuid = t.person_id AND t.rn = 1\n" +
            "LEFT JOIN\n" +
            "  (\n" +
            "    SELECT\n" +
            "      p1.person_uuid,\n" +
            "      MAX(p1.visit_date) AS last_visit_date,\n" +
            "      MAX(next_appointment) AS last_next_appointment,\n" +
            "      MAX(r.duration) AS duration\n" +
            "    FROM\n" +
            "      hiv_art_pharmacy p1\n" +
            "    CROSS JOIN LATERAL (\n" +
            "      SELECT\n" +
            "        reg->>'regimenName' AS regimenName,\n" +
            "        CAST((reg->>'duration') AS INTEGER) AS duration\n" +
            "      FROM\n" +
            "        jsonb_array_elements(p1.extra->'regimens') AS reg\n" +
            "    ) AS r\n" +
            "    WHERE\n" +
            "      p1.archived != 1\n" +
            "    GROUP BY\n" +
            "      p1.person_uuid\n" +
            "  ) AS ph ON p.uuid = ph.person_uuid\n" +
            "WHERE\n" +
            "  p.archived = 0\n" +
            "  AND p.facility_id = ?1\n" +
            "  AND (tri.body_weight > 121 OR tri.body_weight IS NULL)\n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithAbornormalWeightLastVisit (Long facilityId);

    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  p.date_of_birth AS dateOfBirth,\n" +
            "  CASE\n" +
            "    WHEN t.hiv_status = 'KNOWN_DEATH' THEN 'Dead'\n" +
            "    WHEN t.hiv_status IN ('ART_TRANSFER_OUT', 'ART Transfer Out') THEN 'Transferred Out'\n" +
            "    WHEN AGE(NOW(), ph.last_visit_date) <= INTERVAL '28 DAYS' THEN 'Active'\n" +
            "    WHEN AGE(NOW(), ph.last_visit_date) > INTERVAL '28 DAYS' THEN 'IIT'\n" +
            "  END AS status\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            " LEFT JOIN\n" +
            "    (SELECT DISTINCT ON (person_uuid)\n" +
            "      person_uuid, visit_date, body_weight\n" +
            "  FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
            "      FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid\n" +
            "      GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
            "      person_uuid DESC ) tri ON tri.person_uuid = e.person_uuid\n" +
            "LEFT JOIN\n" +
            "  (\n" +
            "    SELECT\n" +
            "      person_id,\n" +
            "      hiv_status,\n" +
            "      ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC) AS rn\n" +
            "    FROM\n" +
            "      hiv_status_tracker\n" +
            "  ) AS t ON p.uuid = t.person_id AND t.rn = 1\n" +
            "LEFT JOIN\n" +
            "  (\n" +
            "    SELECT\n" +
            "      p1.person_uuid,\n" +
            "      MAX(p1.visit_date) AS last_visit_date,\n" +
            "      MAX(next_appointment) AS last_next_appointment,\n" +
            "      MAX(r.duration) AS duration\n" +
            "    FROM\n" +
            "      hiv_art_pharmacy p1\n" +
            "    CROSS JOIN LATERAL (\n" +
            "      SELECT\n" +
            "        reg->>'regimenName' AS regimenName,\n" +
            "        CAST((reg->>'duration') AS INTEGER) AS duration\n" +
            "      FROM\n" +
            "        jsonb_array_elements(p1.extra->'regimens') AS reg\n" +
            "    ) AS r\n" +
            "    WHERE\n" +
            "      p1.archived != 1\n" +
            "    GROUP BY\n" +
            "      p1.person_uuid\n" +
            "  ) AS ph ON p.uuid = ph.person_uuid\n" +
            "WHERE\n" +
            "  p.archived = 0\n" +
            "  AND p.facility_id = ?1\n" +
            "  AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) BETWEEN 0 AND 14 AND (tri.body_weight > 61 OR tri.body_weight IS NULL) \n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getPeadiatricWeightLastVisit (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, \n" +
            "CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth, ph.status\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND e.date_started > NOW()\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientStartDateGreaterThanToday (Long facilityId);


    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "  p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
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
            "LEFT JOIN\n" +
            "  hiv_art_clinical hac ON hac.person_uuid = e.person_uuid\n" +
            "LEFT JOIN\n" +
            "  base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "WHERE\n" +
            "  p.archived = 0 AND p.facility_id = ?1\n" +
            "GROUP BY\n" +
            "  e.unique_id, p.hospital_number, p.sex, p.date_of_birth, ph.status, e.person_uuid, p.id, e.date_started\n" +
            "HAVING\n" +
            " e.date_started > MAX(hac.visit_date)\n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getPatientClinicDateGreaterThanToday (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth, ph.status\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            " LEFT JOIN \n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "\tORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND e.date_started > pharm.visit_date\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, pharm.visit_date\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientArtDateGreaterThanClinicDay (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth, ph.status\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "\t  LEFT JOIN \n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "\tORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND e.date_confirmed_hiv > pharm.visit_date\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, pharm.visit_date\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientLastPickUpGreaterThanConfirmDate (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "      , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth, ph.status\n" +
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
            "\t\tLEFT JOIN\n" +
            "\t\t(SELECT DISTINCT (person_id)\n" +
            "\t\tperson_id, MAX(status_date) AS status_date, hiv_status FROM hiv_status_tracker where hiv_status = 'ART_TRANSFER_IN'\n" +
            "\t\tGROUP BY person_id, hiv_status ) transfer ON p.uuid = transfer.person_id\n" +
            "        LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND transfer.status_date < e.date_started\n" +
            "        GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, transfer.status_date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientStartDateGreaterThanTransferIn (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth, ph.status\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "\t  LEFT JOIN \n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "\tORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND p.date_of_birth > pharm.visit_date\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, pharm.visit_date\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientDobGreaterThanLastPick (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth, ph.status\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "\t  LEFT JOIN\n" +
            "\t(SELECT DISTINCT (person_id)\n" +
            "\tperson_id, MAX(status_date) AS status_date, hiv_status FROM hiv_status_tracker where hiv_status = 'ART_TRANSFER_IN'\n" +
            "\tGROUP BY person_id, hiv_status ) transfer ON p.uuid = transfer.person_id\n" +
            "\t  LEFT JOIN \n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "\tORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND pharm.visit_date < transfer.status_date\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, pharm.visit_date, transfer.status_date\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientLastPickUpGreaterThanTransferInDate (Long facilityId);


    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "  p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
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
            "LEFT JOIN\n" +
            "  hiv_art_clinical hac ON hac.person_uuid = e.person_uuid AND hac.archived = 0 AND hac.is_commencement IS TRUE\n" +
            "LEFT JOIN (\n" +
            "  SELECT\n" +
            "    person_uuid,\n" +
            "    MAX(visit_date) AS visit_date,\n" +
            "    MAX(refill_period) AS refill_period,\n" +
            "    MAX(extra->'regimens'->0->>'name') AS regimen\n" +
            "  FROM\n" +
            "    hiv_art_pharmacy\n" +
            "  WHERE\n" +
            "    archived = 0\n" +
            "  GROUP BY\n" +
            "    person_uuid\n" +
            ") pharm ON pharm.person_uuid = p.uuid\n" +
            "LEFT JOIN\n" +
            "  base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "WHERE\n" +
            "  p.archived = 0\n" +
            "  AND p.facility_id = ?1\n" +
            "  AND COALESCE(pharm.visit_date, NOW()) > NOW()\n" +
            "GROUP BY\n" +
            "  e.unique_id, p.hospital_number, p.sex, p.date_of_birth, ph.status, p.id\n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getPatientLastPickUpGreaterThanToday (Long facilityId);


    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "  p.date_of_birth AS dateOfBirth, ph.status\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
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
            "LEFT JOIN\n" +
            "  hiv_art_clinical hac ON hac.person_uuid = e.person_uuid AND hac.archived = 0 AND hac.is_commencement IS TRUE\n" +
            "LEFT JOIN (\n" +
            "  SELECT\n" +
            "    person_uuid,\n" +
            "    MAX(visit_date) AS visit_date,\n" +
            "    MAX(refill_period) AS refill_period,\n" +
            "    MAX(extra->'regimens'->0->>'name') AS regimen\n" +
            "  FROM\n" +
            "    hiv_art_pharmacy\n" +
            "  WHERE\n" +
            "    archived = 0\n" +
            "  GROUP BY\n" +
            "    person_uuid\n" +
            ") pharm ON pharm.person_uuid = p.uuid\n" +
            "LEFT JOIN\n" +
            "  base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "WHERE\n" +
            "  p.archived = 0\n" +
            "  AND p.facility_id = ?1\n" +
            "  AND pharm.visit_date > NOW()\n" +
            "GROUP BY\n" +
            "  e.unique_id, p.hospital_number, p.sex, p.date_of_birth, ph.status, p.id\n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getPatientLastClinicGreaterThanToday (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "      , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth, ph.status\n" +
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
            "\t\tSELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ls.date_sample_collected as dateSampleCollected,\n" +
            "\t\t-- \t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "\t\t--            ELSE NULL END AS lastViralLoad, \n" +
            "\t\t\tlr.result_reported AS lastViralLoad,\n" +
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
            "\t\tWHERE  lo.archived=0 AND\n" +
            "\t\t\tlr.date_result_reported IS NOT NULL\n" +
            "\t\t) vl ON e.person_uuid = vl.person_uuid\n" +
            "\t    LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND vl.dateSampleCollected > vl.dateOfLastViralLoad\n" +
            "        GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientVlSampleDateGreaterThanResultDate (Long facilityId);



    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  p.date_of_birth AS dateOfBirth,\n" +
            "  CASE\n" +
            "    WHEN t.hiv_status = 'KNOWN_DEATH' THEN 'Dead'\n" +
            "    WHEN t.hiv_status IN ('ART_TRANSFER_OUT', 'ART Transfer Out') THEN 'Transferred Out'\n" +
            "    WHEN AGE(NOW(), ph.last_visit_date) <= INTERVAL '28 DAYS' THEN 'Active'\n" +
            "    WHEN AGE(NOW(), ph.last_visit_date) > INTERVAL '28 DAYS' THEN 'IIT'\n" +
            "  END AS status\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN (\n" +
            "select DISTINCT ON (h1.person_uuid) h1.person_uuid, MAX(h1.visit_date) AS visit_date,\n" +
            "h1.pregnancy_status\n" +
            "from hiv_art_clinical h1\n" +
            "GROUP BY h1.person_uuid, h1.visit_date, h1.pregnancy_status\n" +
            "ORDER BY h1.person_uuid, h1.visit_date DESC \n" +
            ") preg ON e.person_uuid = preg.person_uuid\n" +
            "LEFT JOIN\n" +
            "  (\n" +
            "    SELECT\n" +
            "      person_id,\n" +
            "      hiv_status,\n" +
            "      ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC) AS rn\n" +
            "    FROM\n" +
            "      hiv_status_tracker\n" +
            "  ) AS t ON p.uuid = t.person_id AND t.rn = 1\n" +
            "LEFT JOIN\n" +
            "  (\n" +
            "    SELECT\n" +
            "      p1.person_uuid,\n" +
            "      MAX(p1.visit_date) AS last_visit_date,\n" +
            "      MAX(next_appointment) AS last_next_appointment,\n" +
            "      MAX(r.duration) AS duration\n" +
            "    FROM\n" +
            "      hiv_art_pharmacy p1\n" +
            "    CROSS JOIN LATERAL (\n" +
            "      SELECT\n" +
            "        reg->>'regimenName' AS regimenName,\n" +
            "        CAST((reg->>'duration') AS INTEGER) AS duration\n" +
            "      FROM\n" +
            "        jsonb_array_elements(p1.extra->'regimens') AS reg\n" +
            "    ) AS r\n" +
            "    WHERE\n" +
            "      p1.archived != 1\n" +
            "    GROUP BY\n" +
            "      p1.person_uuid\n" +
            "  ) AS ph ON p.uuid = ph.person_uuid\n" +
            "WHERE\n" +
            "  p.archived = 0\n" +
            "  AND p.facility_id = ?1\n" +
            "  AND CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) > 12\n" +
            "  AND INITCAP(p.sex) = 'Female' AND preg.pregnancy_status is null\n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getFemalePatientsWithoutPregStatusLastVisit (Long facilityId);

//                          Summary Of Data Consistency
    @Query(value = "WITH dataConsistence AS (\n" +
            "SELECT e.unique_id AS patientId , p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,p.date_of_birth AS dateOfBirth, tri.body_weight AS adultweight, \n" +
            "tri.visit_date AS visit_date, e.target_group_id as target_group, e.entry_point_id AS entryPoint,\n" +
            "e.date_confirmed_hiv AS hiv_confirm_date, (CASE WHEN lasClinic.lastvisit >= e.date_confirmed_hiv THEN 1 ELSE null END) AS lGreaterConf,\n" +
            "pharm.visit_date AS lastPickUp,(CASE WHEN pharm.visit_date >  p.date_of_birth THEN 1 ELSE null END) AS lstPickGreaterDOb,\n" +
            "transfer.hiv_status, transfer.status_date,(CASE WHEN e.date_started < transfer.status_date THEN 1 ELSE null END)  AS ArtGreaterTrans,\n" +
            "(CASE WHEN e.date_started = lasClinic.lastvisit  THEN 1 ELSE null END) ArtEqClinicD, (CASE WHEN e.date_started = pharm.visit_date  THEN 1 ELSE null END) ArtEqDrugPickupD,\n" +
            "(CASE WHEN pharm.visit_date >= transfer.status_date THEN 1 ELSE null END)  AS DrugPickHigherThanTrans,\n" +
            "(CASE WHEN pharm.visit_date <= CAST(now() AS DATE) THEN 1 ELSE null END)  AS DrugPickLessToday,\n" +
            "(CASE WHEN lasClinic.lastvisit <= CAST(now() AS DATE) THEN 1 ELSE null END)  AS clinicPickLessToday,\n" +
            "(CASE WHEN e.date_started <= CAST(now() AS DATE) THEN 1 ELSE null END)  AS artDateLessToday,\n" +
            "(CASE WHEN lasClinic.lastvisit > transfer.status_date  THEN 1 ELSE null END)  AS clinicGreaterThanTrans,\n" +
            "(CASE WHEN vl.dateOfLastViralLoad > vl.dateSampleCollected THEN 1 ELSE NULL END) AS vlSample,\n" +
            "CASE WHEN SEX = 'Female' AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 THEN 1 ELSE NULL END AS activeFemaleAdult,\n" +
            "CASE WHEN lasClinic.pregnancy_status IS NOT NULL AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 AND INITCAP(p.sex) = 'Female' THEN 1 ELSE NULL END AS adultPre,\n" +
            "CASE WHEN tri.body_weight < 61 AND CAST (EXTRACT(YEAR from AGE(NOW(), p.date_of_birth)) AS INTEGER) BETWEEN 0 AND 14 THEN 1 ELSE NULL END AS peadweight,\n" +
            "CASE WHEN CAST (EXTRACT(YEAR from AGE(NOW(), p.date_of_birth)) AS INTEGER) BETWEEN 0 AND 14 THEN 1 ELSE NULL END AS peadcURR\n" +
            "\n" +
            " FROM patient_person p\n" +
            " INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            " LEFT JOIN\n" +
            " (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            " GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN\n" +
            " (SELECT DISTINCT ON (person_uuid)\n" +
            "   person_uuid, visit_date, body_weight\n" +
            "FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
            "   FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid \n" +
            "GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
            "   person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
            "LEFT JOIN (SELECT DISTINCT ON (person_uuid)\n" +
            "person_uuid, lastVisit,pregnancy_status\n" +
            "  FROM\n" +
            "(SELECT hacc.person_uuid, MAX(hacc.visit_date) as lastVisit, pregnancy_status from hiv_art_clinical hacc JOIN patient_person p2\n" +
            "ON hacc.person_uuid = p2.uuid\n" +
            "where hacc.archived=0 \n" +
            " group by person_uuid, pregnancy_status ORDER BY person_uuid DESC ) lClinicVisit ORDER BY\n" +
            "   person_uuid DESC ) \n" +
            " lasClinic ON p.uuid = lasClinic.person_uuid\n" +
            "LEFT JOIN\n" +
            "(SELECT DISTINCT (person_id)\n" +
            "person_id, MAX(status_date) AS status_date, hiv_status FROM hiv_status_tracker where hiv_status = 'ART_TRANSFER_IN'\n" +
            "GROUP BY person_id, hiv_status ) transfer ON p.uuid = transfer.person_id\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ls.date_sample_collected as dateSampleCollected,\n" +
            "lr.result_reported AS lastViralLoad,\n" +
            "lr.date_result_reported as dateOfLastViralLoad\n" +
            "FROM laboratory_order lo\n" +
            "LEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
            "GROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
            "ON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
            "LEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
            "LEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
            "LEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
            "-- INNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
            "LEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
            "LEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
            "WHERE  lo.archived=0 AND\n" +
            "lr.date_result_reported IS NOT NULL\n" +
            ") vl ON e.person_uuid = vl.person_uuid\n" +
            "LEFT JOIN \n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, refill_period, regimen\n" +
            "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "ORDER BY\n" +
            "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            " LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            " WHERE p.archived=0 AND p.facility_id= ?1\n" +
            " GROUP BY e.id, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.visit_date, \n" +
            "e.target_group_id, e.entry_point_id,  e.date_confirmed_hiv, p.sex, p.id,\n" +
            "transfer.hiv_status, transfer.status_date,lasclinic.lastvisit, pharm.visit_date, \n" +
            "\tvl.dateOfLastViralLoad,vl.dateSampleCollected,  \n" +
            "\tlasClinic.pregnancy_status\n" +
            " ORDER BY p.id DESC )\n" +
            " SELECT \n" +
            " COUNT(target_group) AS targNumerator,\n" +
            " COUNT(hospitalNumber) AS targDenominator,\n" +
            " ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS targPerformance,\n" +
            " COUNT(entrypoint) AS entryNumerator,\n" +
            " COUNT(hospitalNumber) AS entryDenominator,\n" +
            " ROUND((CAST(COUNT(entrypoint) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS entryPerformance,\n" +
            " COUNT(adultweight) AS adultWeightNumerator,\n" +
            " COUNT(hospitalNumber) AS adultWeightDenominator,\n" +
            " ROUND((CAST(COUNT(adultweight) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS adultWeightPerformance,\n" +
            " COUNT(peadweight) AS peadWeightNumerator,\n" +
            " COUNT(peadcURR) AS peadWeightDenominator,\n" +
            " ROUND((CAST(COUNT(peadweight) AS DECIMAL) / COUNT(peadcURR)) * 100, 2) AS peadWeightPerformance,\n" +
            " COUNT(adultPre) AS pregNumerator,\n" +
            " COUNT(activeFemaleAdult) AS pregDenominator,\n" +
            " ROUND((CAST(COUNT(adultPre) AS DECIMAL) / COUNT(activeFemaleAdult)) * 100, 2) AS pregPerformance,\n" +
            " COUNT(ArtEqClinicD) AS artEqClinicNumerator,\n" +
            " COUNT(hospitalNumber) AS artEqClinicDenominator,\n" +
            " ROUND((CAST(COUNT(ArtEqClinicD) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artEqClinicPerformance,\n" +
            " COUNT(ArtEqDrugPickupD) AS artEqLastPickupNumerator,\n" +
            " COUNT(hospitalNumber) AS artEqLastPickupDenominator,\n" +
            " ROUND((CAST(COUNT(ArtEqDrugPickupD) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artEqLastPickupPerformance,\n" +
            " COUNT(lGreaterConf) AS lGreaterConfNumerator,\n" +
            " COUNT(hospitalNumber) AS lGreaterConfDenominator,\n" +
            " ROUND((CAST(COUNT(lGreaterConf) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lGreaterConfPerformance,\n" +
            " COUNT(ArtGreaterTrans) AS artGreaterTransNumerator,\n" +
            " COUNT(hiv_status) AS ArtGreaterTransDenominator,\n" +
            " ROUND((CAST(COUNT(ArtGreaterTrans) AS DECIMAL) / COUNT(hiv_status)) * 100, 2) AS ArtGreaterTransPerformance,\n" +
            " COUNT(lstPickGreaterDOb) AS lstPickGreaterDObNumerator,\n" +
            " COUNT(hospitalNumber) AS lstPickGreaterDObDenominator,\n" +
            " ROUND((CAST(COUNT(lstPickGreaterDOb) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lstPickGreaterDObPerformance,\n" +
            " COUNT(DrugPickHigherThanTrans) AS lDrugPickHighNumerator,\n" +
            " COUNT(hospitalNumber) AS lDrugPickHighDenominator,\n" +
            " ROUND((CAST(COUNT(DrugPickHigherThanTrans) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lDrugPickHighPerformance,\n" +
            " COUNT(DrugPickLessToday) AS lDrugPickHighTodayNumerator,\n" +
            " COUNT(hospitalNumber) AS lDrugPickHighTodayDenominator,\n" +
            " ROUND((CAST(COUNT(DrugPickLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lDrugPickHighTodayPerformance,\n" +
            " COUNT(clinicPickLessToday) AS clinicPickLessTodayNumerator,\n" +
            " COUNT(hospitalNumber) AS clinicPickLessTodayDenominator,\n" +
            " ROUND((CAST(COUNT(clinicPickLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS clinicPickLessTodayPerformance,\n" +
            " COUNT(artDateLessToday) AS artDateLessTodayNumerator,\n" +
            " COUNT(hospitalNumber) AS artDateLessTodayDenominator,\n" +
            " ROUND((CAST(COUNT(artDateLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artDateLessTodayPerformance,\n" +
            " COUNT(vlSample) AS vlNumerator,\n" +
            " COUNT(hospitalNumber) AS vlDenominator,\n" +
            " ROUND((CAST(COUNT(vlSample) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS vlPerformance\n" +
            " FROM\n" +
            "  dataConsistence", nativeQuery = true)
    List<ClinicalConsistencyDTOProjection> getClinicalConsistencySummary (Long facilityId);

}
