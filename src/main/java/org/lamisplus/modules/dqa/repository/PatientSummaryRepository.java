package org.lamisplus.modules.dqa.repository;

import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PatientSummaryRepository extends JpaRepository<DQA, Long> {

    @Query(value = "SELECT count(dateOfBirth) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(dateOfBirth) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as t",nativeQuery = true)
    List<PatientSummaryDTOProjection> getWithDobSumm (Long facilityId);

    @Query(value = "SELECT count(age) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(age) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "   p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as t", nativeQuery = true)
    List<PatientSummaryDTOProjection> getWithAgeSum (Long facilityId);


    @Query(value = "SELECT count(sex) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(sex) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "   p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as t", nativeQuery = true)
    List<PatientSummaryDTOProjection> getWithSexSum (Long facilityId);

    @Query(value = "SELECT count(marital_status) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(marital_status) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "   p.date_of_birth AS dateOfBirth, p.marital_status AS marital_status\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as t", nativeQuery = true)
    List <PatientSummaryDTOProjection> getWithMaritalStaSum (Long facilityId);

    @Query(value = "SELECT count(education) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(education) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "   p.date_of_birth AS dateOfBirth, p.education AS education\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as t\n" +
            "  \n", nativeQuery = true)
    List <PatientSummaryDTOProjection> getWithEducationSumm (Long facilityId);

    @Query(value = "SELECT count(employment) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(employment) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "   p.date_of_birth AS dateOfBirth, p.employment_status AS employment\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as t", nativeQuery = true)
    List <PatientSummaryDTOProjection> getWithOccupSumm (Long facilityId);

    @Query(value = "SELECT count(address) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(address) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "   p.date_of_birth AS dateOfBirth, p.address AS address\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as t", nativeQuery = true)
    List <PatientSummaryDTOProjection> getWithAddressSumm (Long facilityId);

    @Query(value = "SELECT count(patientId) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(patientId) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
            "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "   p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as t", nativeQuery = true)
    List <PatientSummaryDTOProjection> getWithIdentifier (Long facilityId);
}
