package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.lamisplus.modules.dqr.domain.entity.ValiditySummaryDTOProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataValidityRepository extends JpaRepository<DQA, Long> {


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN (SELECT person_uuid, max(visit_date) as lastPS from hiv_art_clinical where archived=0 \n" +
            "  group by person_uuid, visit_date ORDER BY lastPS DESC LIMIT 1) lasPreg ON p.uuid = lasPreg.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND EXTRACT(YEAR FROM p.date_of_birth) < 1920\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithDateLessThan1920(Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN (SELECT person_uuid, max(visit_date) as lastPS from hiv_art_clinical where archived=0 \n" +
            "  group by person_uuid, visit_date ORDER BY lastPS DESC LIMIT 1) lasPreg ON p.uuid = lasPreg.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) NOT BETWEEN 0 AND 90\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithAgeBetweenZeroAndNinety(Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN (SELECT person_uuid, max(visit_date) as lastPS from hiv_art_clinical where archived=0 \n" +
            "  group by person_uuid, visit_date ORDER BY lastPS DESC LIMIT 1) lasPreg ON p.uuid = lasPreg.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND EXTRACT(YEAR FROM e.date_started) < 1985\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithArtStartLessThan1985(Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN (SELECT person_uuid, max(visit_date) as lastPS from hiv_art_clinical where archived=0 \n" +
            "  group by person_uuid, visit_date ORDER BY lastPS DESC LIMIT 1) lasPreg ON p.uuid = lasPreg.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND EXTRACT(YEAR FROM e.date_confirmed_hiv) NOT BETWEEN 1985 AND EXTRACT(YEAR FROM CURRENT_DATE)\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithhivConfirmDateLessThan1985(Long facilityId);

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
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND b.biometric_valid_captured !=0\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithValidBiometric (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth\n" +
            "  FROM patient_person p \n" +
            "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  \t  LEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, refill_period, extra\n" +
            "FROM ( select person_uuid, refill_period,  extra from hiv_art_pharmacy\n" +
            "\t  where archived = 0 AND refill_period NOT BETWEEN 14 AND 180\n" +
            "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND pharm.refill_period IS NOT NULL\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, pharm.refill_period, p.facility_id,  pharm.extra\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithArvRefillPeriodBetweennFourteenAndOneHundredAndEight (Long facilityId);

    @Query(value = " SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, \n" +
            "  p.date_of_birth AS dateOfBirth, vl.dateOfLastViralLoad\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN (\n" +
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
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN (SELECT person_uuid, max(visit_date) as lastPS from hiv_art_clinical where archived=0 \n" +
            "  group by person_uuid, visit_date ORDER BY lastPS DESC LIMIT 1) lasPreg ON p.uuid = lasPreg.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 AND EXTRACT(YEAR FROM vl.dateOfLastViralLoad) < 1985\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, vl.dateOfLastViralLoad\n" +
            "  ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithViralLoadDateLessThan1985 (Long facilityId);

// SUMMARY

    @Query(value = "\n" +
            "WITH validitySummary AS (\n" +
            "  SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            ",p.date_of_birth AS dateOfBirth, b.biometric_valid_captured AS validBio, pharm.refill_period,\n" +
            "\tvl.dateOfLastViralLoad, CASE WHEN EXTRACT(YEAR FROM e.date_confirmed_hiv) BETWEEN 1985 AND EXTRACT(YEAR FROM NOW())\n" +
            "     THEN EXTRACT(YEAR FROM e.date_confirmed_hiv) ELSE NULL END AS confirmed, e.date_confirmed_hiv AS hivConfirm,\n" +
            "CASE WHEN EXTRACT(YEAR FROM e.date_started) BETWEEN 1985 AND EXTRACT(YEAR FROM NOW())\n" +
            "     THEN EXTRACT(YEAR FROM e.date_started) ELSE NULL END AS start_date, e.date_started,\n" +
            "CASE WHEN CAST(EXTRACT(YEAR from AGE(NOW(), p.date_of_birth)) AS INTEGER) BETWEEN 0 AND 90\n" +
            "     THEN EXTRACT(YEAR FROM p.date_of_birth) ELSE NULL END AS ageInitiated,\n" +
            "\tCASE WHEN EXTRACT(YEAR FROM p.date_of_birth) > 1920 THEN 1 ELSE NULL END AS normalDob\n" +
            "\n" +
            "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            " LEFT JOIN (\n" +
            "\tSELECT person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured FROM biometric\n" +
            " WHERE archived != 1 GROUP BY person_uuid) b ON p.uuid = b.person_uuid\n" +
            "LEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, refill_period, extra\n" +
            "FROM ( select person_uuid, refill_period,  extra from hiv_art_pharmacy\n" +
            "\t  where archived = 0 AND refill_period  BETWEEN 14 AND 180\n" +
            "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "LEFT JOIN (\n" +
            "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
            "\t\tbac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
            "-- \t\tCASE WHEN lr.result_reported ~ E'^\\\\d+(\\\\.\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
            "--            ELSE NULL END AS lastViralLoad, \n" +
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
            "WHERE  lo.archived=0 AND\n" +
            "\tlr.date_result_reported IS NOT NULL \n" +
            "\t\t   AND\n" +
            "\t\t\t\t    EXTRACT(YEAR FROM lr.date_result_reported) BETWEEN 1985 AND EXTRACT(YEAR FROM NOW())\n" +
            ") vl ON e.person_uuid = vl.person_uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1 \n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured,\n" +
            "  pharm.refill_period, vl.dateOfLastViralLoad, e.date_confirmed_hiv\n" +
            "  ORDER BY p.id DESC   \n" +
            "\t) \n" +
            "SELECT\n" +
            "  COUNT(validBio) AS bioNumerator,\n" +
            "  COUNT(hospitalNumber) AS bioDenominator,\n" +
            "  ROUND((CAST(COUNT(validBio) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS bioPerformance,\n" +
            "  COUNT(refill_period) AS regimenNumerator,\n" +
            "  COUNT(hospitalNumber) AS regimenDenominator,\n" +
            "  ROUND((CAST(COUNT(refill_period) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS regimenPerformance,\n" +
            "  COUNT(dateOfLastViralLoad) AS vlDateNumerator,\n" +
            "  COUNT(hospitalNumber) AS vlDateDenominator,\n" +
            "  ROUND((CAST(COUNT(dateOfLastViralLoad) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS vlDatePerformance,\n" +
            "  COUNT(confirmed) AS hivDateNumerator,\n" +
            "  COUNT(hivConfirm) AS hivDateDenominator,\n" +
            "  ROUND((CAST(COUNT(confirmed) AS DECIMAL) / COUNT(hivConfirm)) * 100, 2) AS hivDatePerformance,\n" +
            "  COUNT(start_date) AS startDateNumerator,\n" +
            "  COUNT(date_started) AS startDateDenominator,\n" +
            "  ROUND((CAST(COUNT(start_date) AS DECIMAL) / COUNT(date_started)) * 100, 2) AS startDatePerformance,\n" +
            "  COUNT(ageInitiated) AS ageInitiatedNumerator,\n" +
            "  COUNT(hospitalNumber) AS ageInitiatedDenominator,\n" +
            "  ROUND((CAST(COUNT(ageInitiated) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS ageInitiatedPerformance,\n" +
            "  COUNT(normalDob) AS normalDobNumerator,\n" +
            "  COUNT(hospitalNumber) AS normalDobDenominator,\n" +
            "  ROUND((CAST(COUNT(normalDob) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS normalDobPerformance\n" +
            "FROM\n" +
            "    validitySummary\n", nativeQuery = true)
    List<ValiditySummaryDTOProjection> getPatientValiditySummary (Long facilityId);

}
