package org.lamisplus.modules.starter.repository;

import org.lamisplus.modules.starter.domain.PatientDTOProjection;
import org.lamisplus.modules.starter.domain.PatientDto;
import org.lamisplus.modules.starter.domain.entity.Starter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientDemoRepository extends JpaRepository<Starter, Long> {

    @Query(value = "", nativeQuery = true)
    List<PatientDTOProjection> getPatient(Long facilityId);

    @Query(value ="SELECT count(dateOfBirth) FROM (\n" +
            "SELECT p.id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ", CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "   p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= 1722\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "  ORDER BY p.id DESC)as totalScore" , nativeQuery = true)
    List<PatientDTOProjection> getPatientDobCount(Long facilityId);
}
