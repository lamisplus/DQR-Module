package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.PharmacySummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PharmacyRepository extends JpaRepository<DQA, Long> {

    @Query(value = "SELECT patientId, hospitalNumber, sex, dateOfBirth, status FROM (\n" +
            "SELECT patientId, hospitalNumber, sex, dateOfBirth, status FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth, ph.status, pharm.refill_period as refillMonth, pharm.visit_date AS visit_date, pharm.extra AS regimen\n" +
            "  FROM patient_person p \n" +
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
            ") ph ON p.uuid = ph.person_uuid " +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  \t  LEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, refill_period, extra\n" +
            "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra from hiv_art_pharmacy\n" +
            "\t  where archived = 0\n" +
            "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 \n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, ca.visit_date, pharm.refill_period, p.facility_id, pharm.visit_date, pharm.extra\n" +
            "  ORDER BY p.id DESC) dd\n" +
            "\t\t  where regimen is null\n" +
            "\t) ee", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithoutRegimen(Long facilityId);


    @Query(value = "SELECT patientId, hospitalNumber, sex, dateOfBirth, status FROM (\n" +
            "SELECT patientId, hospitalNumber, sex, dateOfBirth, status FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth, ph.status, pharm.refill_period as refillMonth, pharm.visit_date AS visit_date\n" +
            "  FROM patient_person p \n" +
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
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  \t  LEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, refill_period\n" +
            "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
            "\t  where archived = 0\n" +
            "GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 \n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ph.status, ca.visit_date, pharm.refill_period, p.facility_id, pharm.visit_date\n" +
            "  ORDER BY p.id DESC) dd\n" +
            "\t\t  where refillMonth is null\n" +
            "\t) ee", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithoutDrugRefillDuration(Long facilityId);



    @Query(value = "With pharmacySummary AS (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth, pharm.refill_period as refillMonth, pharm.visit_date AS visit_date, pharm.extra AS regimen\n" +
            "  FROM patient_person p \n" +
            "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, refill_period, extra\n" +
            "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra from hiv_art_pharmacy\n" +
            "where archived = 0\n" +
            "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, pharm.refill_period, p.facility_id, pharm.visit_date, pharm.extra\n" +
            "  ORDER BY p.id DESC )\n" +
            "\tSELECT\n" +
            "    COUNT(refillMonth) AS refillNumerator,\n" +
            "    COUNT(hospitalNumber) AS refillDenominator,\n" +
            "    ROUND((CAST(COUNT(refillMonth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS refillPerformance,\n" +
            "    COUNT(regimen) AS regimenNumerator,\n" +
            "    COUNT(hospitalNumber) AS regimenDenominator,\n" +
            "    ROUND((CAST(COUNT(regimen) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS regimenPerformance\n" +
            "FROM pharmacySummary", nativeQuery = true)
    List<PharmacySummaryDTOProjection> getPharmacySummary(Long facilityId);

}



