package org.lamisplus.modules.dqa.repository;

import org.lamisplus.modules.dqa.domain.ClinicalSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ClinicalSummaryRepository extends JpaRepository<DQA, Long> {

    @Query(value = "WITH PatientClinic AS (\n" +
            "\tSELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,p.date_of_birth AS dateOfBirth, \n" +
            "\tpharm.refill_period as refillMonth, pharm.visit_date AS drug_visit_date, pharm.regimen, e.date_started as start_date,\n" +
            "\te.date_confirmed_hiv AS hiv_confirm_date, e.target_group_id as target_group, e.entry_point_id AS entryPoint,\n" +
            "\tca.visit_date as commence_date,  e.time_hiv_diagnosis as hivDiagnose, CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "\tca.pregnancy_status as pregStatus, tri.body_weight as weight, tri.visit_date AS visit_date\n" +
            "   FROM patient_person p\n" +
            "   INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "   LEFT JOIN\n" +
            "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "   GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "   LEFT JOIN\n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi ORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "\tLEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, body_weight\n" +
            "FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
            "    FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid\n" +
            "    GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
            "   LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "   WHERE p.archived=0 AND p.facility_id= 1722\n" +
            "   GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, \n" +
            "\tpharm.refill_period, p.facility_id, pharm.visit_date, e.date_started, e.date_confirmed_hiv, e.target_group_id,\n" +
            "\te.time_hiv_diagnosis,ca.pregnancy_status, tri.body_weight, tri.visit_date, pharm.regimen\n" +
            "   ORDER BY p.id DESC )\n" +
            "      SELECT\n" +
            "    COUNT(refillMonth) AS refillMonthNumerator,\n" +
            "    COUNT(hospitalNumber) AS refillMonthDenominator,\n" +
            "    ROUND((CAST(COUNT(refillMonth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS refillMonthPerformance,\n" +
            "\tCOUNT(regimen) AS regimenNumerator,\n" +
            "    COUNT(hospitalNumber) AS regimenDenominator,\n" +
            "    ROUND((CAST(COUNT(regimen) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS regimenPerformance,\n" +
            "\tCOUNT(start_date) AS startDateNumerator,\n" +
            "    COUNT(hospitalNumber) AS startDateDenominator,\n" +
            "    ROUND((CAST(COUNT(start_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS startDatePerformance,\n" +
            "\tCOUNT(hiv_confirm_date) AS confirmDateNumerator,\n" +
            "    COUNT(hospitalNumber) AS confirmDateDenominator,\n" +
            "    ROUND((CAST(COUNT(hiv_confirm_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS confirmDatePerformance,\n" +
            "\tCOUNT(target_group) AS targNumerator,\n" +
            "    COUNT(hospitalNumber) AS targDenominator,\n" +
            "    ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS targPerformance,\n" +
            "\tCOUNT(entryPoint) AS entryNumerator,\n" +
            "    COUNT(hospitalNumber) AS entryDenominator,\n" +
            "    ROUND((CAST(COUNT(entryPoint) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS entryPerformance,\n" +
            "\tCOUNT(commence_date) AS commencedNumerator,\n" +
            "    COUNT(hospitalNumber) AS commencedDenominator,\n" +
            "    ROUND((CAST(COUNT(commence_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS commencedPerformance,\n" +
            "\tCOUNT(start_date) AS enrolledDateNumerator,\n" +
            "    COUNT(hospitalNumber) AS enrolledDateDenominator,\n" +
            "    ROUND((CAST(COUNT(start_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS enrolledDatePerformance,\n" +
            "\tCOUNT(hivDiagnose) AS diagnoseNumerator,\n" +
            "    COUNT(hospitalNumber) AS diagnoseDenominator,\n" +
            "    ROUND((CAST(COUNT(hivDiagnose) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS diagnosePerformance,\n" +
            "\tCOUNT(pregStatus) AS pregNumerator,\n" +
            "    COUNT(hospitalNumber) AS pregDenominator,\n" +
            "    ROUND((CAST(COUNT(pregStatus) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS pregPerformance,\n" +
            "\tCOUNT(weight) AS weightNumerator,\n" +
            "    COUNT(hospitalNumber) AS weightDenominator,\n" +
            "    ROUND((CAST(COUNT(weight) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS weightPerformance,\n" +
            "\tCOUNT(visit_date) AS lastVisitNumerator,\n" +
            "    COUNT(hospitalNumber) AS lastVisitDenominator,\n" +
            "    ROUND((CAST(COUNT(visit_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lastVisitPerformance,\n" +
            "\tCOUNT(age) AS ageNumerator,\n" +
            "    COUNT(hospitalNumber) AS ageDenominator,\n" +
            "    ROUND((CAST(COUNT(age) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS agePerformance,\n" +
            "\tCOUNT(drug_visit_date) AS lastPickNumerator,\n" +
            "    COUNT(hospitalNumber) AS lastPickDenominator,\n" +
            "    ROUND((CAST(COUNT(drug_visit_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lastPickPerformance\n" +
            "   FROM\n" +
            "   \tPatientClinic", nativeQuery = true)
    List<ClinicalSummaryDTOProjection> getClinicalSummary (Long facilityId);

//
//
//    @Query(value = "SELECT count(refillMonth) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(refillMonth) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
//            "  SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "  ,p.date_of_birth AS dateOfBirth, pharm.refill_period as refillMonth, pharm.visit_date AS visit_date\n" +
//            "    FROM patient_person p\n" +
//            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "    LEFT JOIN\n" +
//            "    (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "    GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
//            "    LEFT JOIN\n" +
//            "    (SELECT DISTINCT ON (person_uuid)\n" +
//            "      person_uuid, visit_date, refill_period\n" +
//            "  FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
//            "  GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
//            "      person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
//            "    LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "    WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "    GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, pharm.refill_period, p.facility_id, pharm.visit_date\n" +
//            "    ORDER BY p.id DESC) dd ", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getArvMnthSum (Long facilityId);
//
//
//    @Query(value = "SELECT count(start_date) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(start_date) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
//            "\tSELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
//            "       p.date_of_birth AS dateOfBirth,e.date_started as start_date\n" +
//            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "      LEFT JOIN\n" +
//            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
//            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "      WHERE p.archived=0 AND p.facility_id= ?1 \n" +
//            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
//            "      ORDER BY p.id DESC ) artcount\n" +
//            "  ", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getStartDateSumm (Long facilityId);
//
//    @Query(value = "SELECT count(hiv_confirm_date) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(hiv_confirm_date) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
//            "\t  SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "      , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
//            "         p.date_of_birth AS dateOfBirth, e.date_confirmed_hiv AS hiv_confirm_date\n" +
//            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "        LEFT JOIN\n" +
//            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
//            "        LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "        WHERE p.archived=0 AND p.facility_id= ?1 \n" +
//            "        GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
//            "        ORDER BY p.id DESC ) hivCount", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getHivConfirmTestDateSumm (Long facilityId);
//
//    @Query(value = "SELECT count(target_group) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(target_group) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
//            "\tSELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "   , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
//            "      p.date_of_birth AS dateOfBirth, e.target_group_id as target_group\n" +
//            "     FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "     LEFT JOIN\n" +
//            "     (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "     GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
//            "     LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "     WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "     GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
//            "     ORDER BY p.id DESC ) tarCount", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getTargetGroupSumm (Long facilityId);
//
//    @Query(value = "SELECT count(entryPoint) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(entryPoint) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
//            "\t SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
//            "       p.date_of_birth AS dateOfBirth, e.entry_point_id AS entryPoint\n" +
//            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "      LEFT JOIN\n" +
//            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
//            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "      WHERE p.archived=0 AND p.facility_id= ?1 \n" +
//            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
//            "      ORDER BY p.id DESC ) entryP", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getEntryPointSumm (Long facilityId);
//
//    @Query(value = "SELECT count(commence_date) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(commence_date) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
//            "\t  SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "     , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
//            "        p.date_of_birth AS dateOfBirth, ca.visit_date as commence_date\n" +
//            "       FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "       LEFT JOIN\n" +
//            "       (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "       GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
//            "       LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "       WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "       GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date\n" +
//            "       ORDER BY p.id DESC ) commence ", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getCommenceDateSumm (Long facilityId);
//
//    @Query(value = "SELECT count(start_date) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(start_date) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
//            "\t   SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "      , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
//            "         p.date_of_birth AS dateOfBirth, e.date_started AS start_date\n" +
//            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "        LEFT JOIN\n" +
//            "        (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "        GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
//            "        LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "        WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "        GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date\n" +
//            "        ORDER BY p.id DESC ) startDate", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getHivEnrollSumm (Long facilityId);
//
//
//    @Query(value = "SELECT count(hivDiagnose) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(hivDiagnose) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
//            "\t\tSELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "       , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
//            "          p.date_of_birth AS dateOfBirth, e.time_hiv_diagnosis as hivDiagnose\n" +
//            "         FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "         LEFT JOIN\n" +
//            "         (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "         GROUP BY hac.person_uuid, hac.visit_date)ca ON p.uuid = ca.person_uuid\n" +
//            "         LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "         WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "         GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date\n" +
//            "         ORDER BY p.id DESC ) diagnose", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getDateDiagnoseHivSumm (Long facilityId);
//
//
//    @Query(value = "SELECT count(pregStatus) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(pregStatus) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
//            "\t\t SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "          ,p.date_of_birth AS dateOfBirth, ca.pregnancy_status as pregStatus\n" +
//            "            FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "            LEFT JOIN\n" +
//            "            (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "            GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
//            "            LEFT JOIN (SELECT person_uuid, max(visit_date) as lastPS from hiv_art_clinical where archived=0 \n" +
//            "            group by person_uuid, visit_date ORDER BY lastPS DESC LIMIT 1) lasPreg ON p.uuid = lasPreg.person_uuid\n" +
//            "            LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "            WHERE p.archived=0 AND p.facility_id= ?1 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) >12  AND INITCAP(p.sex) = 'Female'\n" +
//            "            GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status\n" +
//            "            ORDER BY p.id DESC ) prgSumm", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getPregStatusSumm (Long facilityId);
//
//    @Query(value = "SELECT count(weight) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(weight) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
//            "  SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "  ,p.date_of_birth AS dateOfBirth, tri.body_weight as weight, tri.visit_date AS visit_date\n" +
//            "    FROM patient_person p\n" +
//            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "    LEFT JOIN\n" +
//            "    (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "    GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
//            "    LEFT JOIN\n" +
//            "    (SELECT DISTINCT ON (person_uuid)\n" +
//            "      person_uuid, visit_date, body_weight\n" +
//            "  FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
//            "      FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid\n" +
//            "      GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
//            "      person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
//            "    LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "    WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "    GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.visit_date\n" +
//            "    ORDER BY p.id DESC) dd", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getWeightLastVisitSumm (Long facilityId);
//
//    @Query(value = "SELECT count(visit_date) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(visit_date) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
//            "    SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "    ,p.date_of_birth AS dateOfBirth, pharm.refill_period as refillMonth, pharm.visit_date AS visit_date\n" +
//            "      FROM patient_person p \n" +
//            "      INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "      LEFT JOIN\n" +
//            "      (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "      GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
//            "      LEFT JOIN\n" +
//            "      (SELECT DISTINCT ON (person_uuid)\n" +
//            "        person_uuid, visit_date, refill_period\n" +
//            "    FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
//            "    GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
//            "        person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
//            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "      WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, pharm.refill_period, p.facility_id, pharm.visit_date\n" +
//            "      ORDER BY p.id DESC) dd ", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getClinicLastVisitSumm (Long facilityId);
//
//    @Query(value = "SELECT count(age) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(age) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
//            "      SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "      , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
//            "         p.date_of_birth AS dateOfBirth\n" +
//            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "        LEFT JOIN\n" +
//            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
//            "        LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "        WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "        GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
//            "        ORDER BY p.id DESC)as t", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getClientAgeSumm (Long facilityId);
//
//    @Query(value = "SELECT count(visit_date) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(visit_date) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\n" +
//            "    SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
//            "    ,p.date_of_birth AS dateOfBirth, pharm.refill_period as refillMonth, pharm.visit_date AS visit_date\n" +
//            "      FROM patient_person p\n" +
//            "      INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
//            "      LEFT JOIN\n" +
//            "      (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
//            "      GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
//            "      LEFT JOIN\n" +
//            "      (SELECT DISTINCT ON (person_uuid)\n" +
//            "        person_uuid, visit_date, refill_period\n" +
//            "    FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
//            "    GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
//            "        person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
//            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
//            "      WHERE p.archived=0 AND p.facility_id= ?1\n" +
//            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, pharm.refill_period, p.facility_id, pharm.visit_date\n" +
//            "      ORDER BY p.id DESC) dd ", nativeQuery = true)
//    List<PatientSummaryDTOProjection> getClientWithNoLastVisitDateSumm (Long facilityId);



}
