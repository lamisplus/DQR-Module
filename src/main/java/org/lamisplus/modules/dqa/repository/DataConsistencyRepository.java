package org.lamisplus.modules.dqa.repository;

import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.PatientSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DataConsistencyRepository extends JpaRepository<DQA, Long> {


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
            "      p.date_of_birth AS dateOfBirth\n" +
            "     FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "     LEFT JOIN\n" +
            "     (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "     GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "     LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "     WHERE p.archived=0 AND p.facility_id= ?1 AND e.target_group_id is null\n" +
            "     GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "     ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithoutTargetGroup(Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, p.date_of_birth AS dateOfBirth\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND e.entry_point_id is null\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithoutCareEntryPoint(Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,p.date_of_birth AS dateOfBirth\n" +
            "    FROM patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "    LEFT JOIN\n" +
            "    (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "    GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "    LEFT JOIN\n" +
            "    (SELECT DISTINCT ON (person_uuid)\n" +
            "      person_uuid, visit_date, body_weight\n" +
            "  FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
            "      FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid\n" +
            "      GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
            "      person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
            "    LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "    WHERE p.archived=0 AND p.facility_id= ?1 AND tri.body_weight > 121\n" +
            "    GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.visit_date\n" +
            "    ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientsWithAbornormalWeightLastVisit (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, p.date_of_birth AS dateOfBirth\n" +
            "    FROM patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "    LEFT JOIN\n" +
            "    (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "    GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "    LEFT JOIN\n" +
            "    (SELECT DISTINCT ON (person_uuid)\n" +
            "      person_uuid, captu, body_weight\n" +
            "  FROM ( SELECT he.person_uuid,  tr.body_weight, MAX(DATE(CAST(tr.capture_date AS timestamp))) as captu, tr.capture_date As capture_date\n" +
            "      FROM hiv_enrollment he LEFT JOIN triage_vital_sign tr ON he.person_uuid = tr.person_uuid\n" +
            "      GROUP BY he.person_uuid, tr.body_weight, tr.capture_date ORDER BY tr.capture_date DESC ) fi \n" +
            "\t ORDER BY\n" +
            "      person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
            "    LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "    WHERE p.archived=0 AND p.facility_id= ?1 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) BETWEEN 0 AND 14 AND tri.body_weight > 61\n" +
            "    GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.captu, p.uuid\n" +
            "    ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPeadiatricWeightLastVisit (Long facilityId);

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, p.date_of_birth AS dateOfBirth\n" +
            "            FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "            LEFT JOIN\n" +
            "            (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "            GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "            LEFT JOIN (SELECT person_uuid, max(visit_date) as lastPS from hiv_art_clinical where archived=0 \n" +
            "            group by person_uuid, visit_date ORDER BY lastPS DESC LIMIT 1) lasPreg ON p.uuid = lasPreg.person_uuid\n" +
            "            LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "            WHERE p.archived=0 AND p.facility_id= ?1 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) >12  AND INITCAP(p.sex) = 'Female' AND ca.pregnancy_status IS NULL\n" +
            "            GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status\n" +
            "            ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getFemalePatientsWithoutPregStatusLastVisit (Long facilityId);

//                          Summary Of Data Consistency
    @Query(value = "SELECT count(target_group) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(target_group) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
            "\tSELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "   , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "      p.date_of_birth AS dateOfBirth, e.target_group_id as target_group\n" +
            "     FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "     LEFT JOIN\n" +
            "     (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "     GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "     LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "     WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "     GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "     ORDER BY p.id DESC ) tarCount", nativeQuery = true)
    List<PatientSummaryDTOProjection> getTargetGroupSumm (Long facilityId);

    @Query(value = "SELECT count(entryPoint) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(entryPoint) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
            "\t SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "       p.date_of_birth AS dateOfBirth, e.entry_point_id AS entryPoint\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 \n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "      ORDER BY p.id DESC ) entryP", nativeQuery = true)
    List<PatientSummaryDTOProjection> getCareEntryPointSumm (Long facilityId);

    @Query(value = "SELECT count(weight) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(weight) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
            "  SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "  ,p.date_of_birth AS dateOfBirth, tri.body_weight as weight, tri.visit_date AS visit_date\n" +
            "    FROM patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "    LEFT JOIN\n" +
            "    (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "    GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "    LEFT JOIN\n" +
            "    (SELECT DISTINCT ON (person_uuid)\n" +
            "      person_uuid, visit_date, body_weight\n" +
            "  FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
            "      FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid\n" +
            "      where tr.body_weight > 121\n" +
            "\t\tGROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
            "      person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
            "    LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "    WHERE p.archived=0 AND p.facility_id= ?1 \n" +
            "    GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.visit_date\n" +
            "    ORDER BY p.id DESC) dd ", nativeQuery = true)
    List<PatientSummaryDTOProjection> getAbnormalWeightGreaterThan121Summ (Long facilityId);

    @Query(value = "SELECT count(weight) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(weight) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
            "\tSELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "  ,p.date_of_birth AS dateOfBirth, tri.body_weight as weight, tri.captu AS visit_date, CAST (EXTRACT(YEAR from AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age, p.uuid\n" +
            "    FROM patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "    LEFT JOIN\n" +
            "    (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "    GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "    LEFT JOIN\n" +
            "    (SELECT DISTINCT ON (person_uuid)\n" +
            "      person_uuid, captu, body_weight\n" +
            "  FROM ( SELECT he.person_uuid,  tr.body_weight, MAX(DATE(CAST(tr.capture_date AS timestamp))) as captu, tr.capture_date As capture_date\n" +
            "      FROM hiv_enrollment he LEFT JOIN triage_vital_sign tr ON he.person_uuid = tr.person_uuid\n" +
            "\t\twhere tr.archived != 1  AND tr.body_weight > 61\n" +
            "      GROUP BY he.person_uuid, tr.body_weight, tr.capture_date ORDER BY tr.capture_date DESC ) fi \n" +
            "\t ORDER BY\n" +
            "      person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
            "    LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "    WHERE p.archived=0 AND p.facility_id= ?1 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) BETWEEN 0 AND 14 \n" +
            "    GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.captu, p.uuid\n" +
            "    ORDER BY p.id DESC ) ss", nativeQuery = true)
    List<PatientSummaryDTOProjection> getPaedAbnormalWeightGreaterThan61Summ (Long facilityId);

    @Query(value = "SELECT count(pregStatus) AS numerator, count(hospitalNumber) as denominator, ROUND((CAST(count(pregStatus) AS DECIMAL) / count(hospitalNumber)) * 100, 2) AS performance FROM (\t\n" +
            "\t\t SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, p.date_of_birth AS dateOfBirth, ca.pregnancy_status as pregStatus\n" +
            "            FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "            LEFT JOIN\n" +
            "            (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "            GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "            LEFT JOIN (SELECT person_uuid, max(visit_date) as lastPS from hiv_art_clinical where archived=0 \n" +
            "            group by person_uuid, visit_date ORDER BY lastPS DESC LIMIT 1) lasPreg ON p.uuid = lasPreg.person_uuid\n" +
            "            LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "            WHERE p.archived=0 AND p.facility_id= ?1 AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) >12  AND INITCAP(p.sex) = 'Female'\n" +
            "            GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status\n" +
            "            ORDER BY p.id DESC ) prgSumm", nativeQuery = true)
    List<PatientSummaryDTOProjection> getFemalePregStatusSumm (Long facilityId);




}
