package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.BiometricSummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BiometricRepository extends JpaRepository<DQA, Long> {


    @Query(value = "WITH PatientBiometrics AS (\n" +
            "SELECT  e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "p.date_of_birth AS dateOfBirth, b.person_uuid AS person_uuid1, b.biometric_valid_captured AS validcapture,\n" +
            "b.person_uuid AS personId, bb.recapture, bb.person_uuid, bb.biometric_valid_captured AS validrecap\n" +
            "   FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   LEFT JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN (\n" +
            " SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
            "  WHERE archived != 1 GROUP BY person_uuid, recapture) b ON e.person_uuid = b.person_uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
            " WHERE archived != 1 AND recapture != 0 GROUP BY person_uuid, recapture) bb ON e.person_uuid = bb.person_uuid\n" +
            "   LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "   WHERE p.archived=0 AND p.facility_id= 1722 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
            "   GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured,bb.biometric_valid_captured, b.person_uuid,\n" +
            "\tbb.recapture, bb.person_uuid\n" +
            "   ORDER BY p.id DESC\n" +
            ")\n" +
            "SELECT\n" +
            "    COUNT(person_uuid1) AS captureNumerator,\n" +
            "    COUNT(hospitalNumber) AS captureDenominator,\n" +
            "    ROUND((CAST(COUNT(person_uuid1) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS capturePerformance,\n" +
            "\tCOUNT(validcapture) AS validcapNumerator,\n" +
            "    COUNT(hospitalNumber) AS validcapDenominator,\n" +
            "    ROUND((CAST(COUNT(validcapture) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS validcapPerformance,\n" +
            "\tCOUNT(recapture) AS recapNumerator,\n" +
            "    COUNT(person_uuid1) AS recapDenominator,\n" +
            "    ROUND((CAST(COUNT(recapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS recapPerformance,\n" +
            "\tCOUNT(validrecap) AS validRecapNumerator,\n" +
            "    COUNT(recapture) AS validRecapDenominator,\n" +
            "    ROUND((CAST(COUNT(validrecap) AS DECIMAL) / COUNT(recapture)) * 100, 2) AS validRecapPerformance\n" +
            "FROM\n" +
            "    PatientBiometrics", nativeQuery = true)
    List<BiometricSummaryDTOProjection> getBiometricSummary (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN (\n" +
            "\tSELECT person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') NOT LIKE '46%') AS biometric_valid_captured FROM biometric\n" +
            " WHERE archived != 1 GROUP BY person_uuid) b ON p.uuid = b.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND b.person_uuid is null AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured, b.person_uuid\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsNotCaptured(Long facilityId);



    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN (\n" +
            "\tSELECT person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') NOT LIKE '46%') AS biometric_valid_captured FROM biometric\n" +
            " WHERE archived != 1 GROUP BY person_uuid) b ON p.uuid = b.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND b.biometric_valid_captured !=0 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsValidCaptured(Long facilityId);




    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN (\n" +
            "\tSELECT person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
            " WHERE archived != 1 AND recapture != 0 GROUP BY person_uuid, recapture) b ON p.uuid = b.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND b.person_uuid is null AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured, b.person_uuid\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsNotRecaptured (Long facilityId);



    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN (\n" +
            "\tSELECT person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') NOT LIKE '46%') AS biometric_valid_captured FROM biometric\n" +
            " WHERE archived != 1 AND recapture != 0 GROUP BY person_uuid) b ON p.uuid = b.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND b.biometric_valid_captured !=0 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsValidRecaptured (Long facilityId);


}
