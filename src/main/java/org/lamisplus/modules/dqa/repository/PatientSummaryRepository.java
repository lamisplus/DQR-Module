package org.lamisplus.modules.dqa.repository;

import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.SummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientSummaryRepository extends JpaRepository<DQA, Long> {


    @Query(value = "-- patient summaries\n" +
            "WITH PatientSummary AS (\n" +
            "    SELECT e.unique_id AS patientId, p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex, CAST(EXTRACT(YEAR FROM AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, p.marital_status, p.education,\tp.employment_status As employment,\n" +
            "\t\tp.address    FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "    LEFT JOIN ( SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac\n" +
            "            WHERE hac.archived = 0 AND hac.is_commencement IS TRUE \n" +
            "            GROUP BY hac.person_uuid ) ca ON p.uuid = ca.person_uuid\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE p.archived = 0 AND p.facility_id = ?1\n" +
            "    GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "    ORDER BY p.id DESC\n" +
            ")\n" +
            "SELECT\n" +
            "    COUNT(age) AS ageNumerator,\n" +
            "    COUNT(hospitalNumber) AS ageDenominator,\n" +
            "    ROUND((CAST(COUNT(age) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS agePerformance,\n" +
            "    COUNT(sex) AS sexNumerator,\n" +
            "    COUNT(hospitalNumber) AS sexDenominator,\n" +
            "    ROUND((CAST(COUNT(sex) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS sexPerformance,\n" +
            "\tCOUNT(dateOfBirth) AS dobNumerator,\n" +
            "    COUNT(hospitalNumber) AS dobDenominator,\n" +
            "    ROUND((CAST(COUNT(dateOfBirth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS dobPerformance,\n" +
            "\tCOUNT(marital_status) AS maritalNumerator,\n" +
            "    COUNT(hospitalNumber) AS maritalDenominator,\n" +
            "    ROUND((CAST(COUNT(marital_status) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS maritalPerformance,\n" +
            "\tCOUNT(education) AS eduNumerator,\n" +
            "    COUNT(hospitalNumber) AS eduDenominator,\n" +
            "    ROUND((CAST(COUNT(education) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS eduPerformance,\n" +
            "\tCOUNT(employment) AS employNumerator,\n" +
            "    COUNT(hospitalNumber) AS employDenominator,\n" +
            "    ROUND((CAST(COUNT(employment) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS employPerformance,\n" +
            "\tCOUNT(address) AS addressNumerator,\n" +
            "    COUNT(hospitalNumber) AS addressDenominator,\n" +
            "    ROUND((CAST(COUNT(address) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS addressPerformance,\n" +
            "\tCOUNT(patientId) AS pIdNumerator,\n" +
            "    COUNT(hospitalNumber) AS pIdDenominator,\n" +
            "    ROUND((CAST(COUNT(patientId) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS pIdPerformance\n" +
            "\t\n" +
            "FROM\n" +
            "    PatientSummary\n", nativeQuery = true)
    List <SummaryDTOProjection> patientDemoSummary (Long facilityId);
}
