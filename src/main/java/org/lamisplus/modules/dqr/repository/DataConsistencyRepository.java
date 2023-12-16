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

    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, \n" +
            "CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND e.date_started > NOW()\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientStartDateGreaterThanToday (Long facilityId);


    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "  p.date_of_birth AS dateOfBirth\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "LEFT JOIN\n" +
            "  hiv_art_clinical hac ON hac.person_uuid = e.person_uuid\n" +
            "LEFT JOIN\n" +
            "  base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "WHERE\n" +
            "  p.archived = 0 AND p.facility_id = ?1\n" +
            "GROUP BY\n" +
            "  e.unique_id, p.hospital_number, p.sex, p.date_of_birth, e.person_uuid, p.id, e.date_started\n" +
            "HAVING\n" +
            " e.date_started > MAX(hac.visit_date)\n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getPatientClinicDateGreaterThanToday (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "\t  LEFT JOIN \n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "\tORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND e.date_started > pharm.visit_date\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, pharm.visit_date\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientArtDateGreaterThanClinicDay (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "\t  LEFT JOIN \n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "\tORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND e.date_confirmed_hiv > pharm.visit_date\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, pharm.visit_date\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientLastPickUpGreaterThanConfirmDate (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "      , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
            "         p.date_of_birth AS dateOfBirth\n" +
            "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "        LEFT JOIN\n" +
            "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "\t\tLEFT JOIN\n" +
            "\t\t(SELECT DISTINCT (person_id)\n" +
            "\t\tperson_id, MAX(status_date) AS status_date, hiv_status FROM hiv_status_tracker where hiv_status = 'ART_TRANSFER_IN'\n" +
            "\t\tGROUP BY person_id, hiv_status ) transfer ON p.uuid = transfer.person_id\n" +
            "        LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "        WHERE p.archived=0 AND p.facility_id= ?1 AND transfer.status_date < e.date_started\n" +
            "        GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, transfer.status_date\n" +
            "        ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientStartDateGreaterThanTransferIn (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
            "\t  LEFT JOIN \n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "\tORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "      LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "      WHERE p.archived=0 AND p.facility_id= ?1 AND p.date_of_birth > pharm.visit_date\n" +
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, pharm.visit_date\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientDobGreaterThanLastPick (Long facilityId);


    @Query(value = "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
            "    , CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age, \n" +
            "       p.date_of_birth AS dateOfBirth, pharm.visit_date, transfer.status_date\n" +
            "      FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "      LEFT JOIN\n" +
            "      (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "      GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
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
            "      GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, pharm.visit_date, transfer.status_date\n" +
            "      ORDER BY p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientLastPickUpGreaterThanTransferInDate (Long facilityId);


    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "  p.date_of_birth AS dateOfBirth\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
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
            "  e.unique_id, p.hospital_number, p.sex, p.date_of_birth, p.id\n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getPatientLastPickUpGreaterThanToday (Long facilityId);


    @Query(value = "SELECT\n" +
            "  e.unique_id AS patientId,\n" +
            "  p.hospital_number AS hospitalNumber,\n" +
            "  INITCAP(p.sex) AS sex,\n" +
            "  CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "  p.date_of_birth AS dateOfBirth, p.uuid\n" +
            "FROM\n" +
            "  patient_person p\n" +
            "INNER JOIN\n" +
            "  hiv_enrollment e ON p.uuid = e.person_uuid\n" +
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
            "  e.unique_id, p.hospital_number, p.sex, p.date_of_birth, p.id\n" +
            "ORDER BY\n" +
            "  p.id DESC;", nativeQuery = true)
    List<PatientDTOProjection> getPatientLastClinicGreaterThanToday (Long facilityId);


//    @Query(value = "", nativeQuery = true)
//    List<PatientDTOProjection> getPatientLastClinicGreaterThanToday (Long facilityId);



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
    @Query(value = "WITH dataConsistence AS (\n" +
            "SELECT e.unique_id AS patientId , p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,p.date_of_birth AS dateOfBirth, tri.body_weight AS adultweight, \n" +
            "\tpeadtri.body_weight AS peadweight, tri.visit_date AS visit_date, e.target_group_id as target_group, e.entry_point_id AS entryPoint,\n" +
            "\tlasPreg.pregnancy_status,e.date_confirmed_hiv AS hiv_confirm_date, (CASE WHEN lasClinic.lastvisit >= e.date_confirmed_hiv THEN 1 ELSE null END) AS lGreaterConf,\n" +
            "\tpharm.visit_date AS lastPickUp,(CASE WHEN pharm.visit_date >  p.date_of_birth THEN 1 ELSE null END) AS lstPickGreaterDOb,\n" +
            "  \ttransfer.hiv_status, transfer.status_date,(CASE WHEN e.date_started < transfer.status_date THEN 1 ELSE null END)  AS ArtGreaterTrans,\n" +
            "\t(CASE WHEN e.date_started = lasClinic.lastvisit  THEN 1 ELSE null END) ArtEqClinicD, (CASE WHEN e.date_started = pharm.visit_date  THEN 1 ELSE null END) ArtEqDrugPickupD,\n" +
            "\t(CASE WHEN pharm.visit_date >= transfer.status_date THEN 1 ELSE null END)  AS DrugPickHigherThanTrans,\n" +
            "\t(CASE WHEN pharm.visit_date <= CAST(now() AS DATE) THEN 1 ELSE null END)  AS DrugPickLessToday,\n" +
            "\t(CASE WHEN lasClinic.lastvisit <= CAST(now() AS DATE) THEN 1 ELSE null END)  AS clinicPickLessToday,\n" +
            "\t(CASE WHEN e.date_started <= CAST(now() AS DATE) THEN 1 ELSE null END)  AS artDateLessToday\n" +
            "  FROM patient_person p\n" +
            "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
            "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
            "  LEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, body_weight\n" +
            "FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
            "    FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid \n" +
            "    where tr.body_weight < 121\n" +
            "GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
            "LEFT JOIN\n" +
            "  (SELECT DISTINCT ON (person_uuid)\n" +
            "    person_uuid, visit_date, body_weight\n" +
            "FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
            "    FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid \n" +
            "\t  JOIN patient_person pp ON tr.person_uuid = pp.uuid\n" +
            "    where tr.body_weight < 61 AND CAST (EXTRACT(YEAR from AGE(NOW(), pp.date_of_birth)) AS INTEGER) BETWEEN 0 AND 14\n" +
            "GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
            "    person_uuid DESC ) peadtri ON peadtri.person_uuid = p.uuid\t\n" +
            "\tLEFT JOIN (SELECT DISTINCT ON (person_uuid)\n" +
            "\t\t\t   person_uuid, lastPs, pregnancy_status\n" +
            "\t\t\t   FROM\n" +
            "\t\t(SELECT hacc.person_uuid, max(hacc.visit_date) as lastPS, hacc.pregnancy_status from hiv_art_clinical hacc JOIN patient_person p2\n" +
            "\t\t\t ON hacc.person_uuid = p2.uuid\t\t\t \n" +
            "\t\t\t where hacc.archived=0 AND CAST (EXTRACT(YEAR from AGE(NOW(), p2.date_of_birth)) AS INTEGER) >12  AND INITCAP(p2.sex) = 'Female'\n" +
            "  group by person_uuid, visit_date, pregnancy_status ORDER BY person_uuid DESC ) ppre ORDER BY\n" +
            "    person_uuid DESC )\t\t\t   \n" +
            "\t\t\t   lasPreg ON p.uuid = lasPreg.person_uuid\n" +
            "\tLEFT JOIN (SELECT DISTINCT ON (person_uuid)\n" +
            "\t\t\t   person_uuid, lastVisit\n" +
            "\t\t\t   FROM\n" +
            "\t\t(SELECT hacc.person_uuid, MAX(hacc.visit_date) as lastVisit from hiv_art_clinical hacc JOIN patient_person p2\n" +
            "\t\t\t ON hacc.person_uuid = p2.uuid\t\t\t \n" +
            "\t\t\t where hacc.archived=0 \n" +
            "  group by person_uuid ORDER BY person_uuid DESC ) lClinicVisit ORDER BY\n" +
            "    person_uuid DESC )\t\t\t   \n" +
            "\t\t\t   lasClinic ON p.uuid = lasClinic.person_uuid\n" +
            "\tLEFT JOIN\n" +
            "\t(SELECT DISTINCT (person_id)\n" +
            "person_id, MAX(status_date) AS status_date, hiv_status FROM hiv_status_tracker where hiv_status = 'ART_TRANSFER_IN'\n" +
            "GROUP BY person_id, hiv_status ) transfer ON p.uuid = transfer.person_id\n" +
            "\tLEFT JOIN \n" +
            "   (SELECT DISTINCT ON (person_uuid)\n" +
            "     person_uuid, visit_date, refill_period, regimen\n" +
            " FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
            "-- \t   where person_uuid = '5ee6adc1-8f13-4214-9d87-481462a1937f'\n" +
            " GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
            "-- \twhere person_uuid = '5ee6adc1-8f13-4214-9d87-481462a1937f'\n" +
            "\tORDER BY\n" +
            "     person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
            "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
            "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
            "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.visit_date,\n" +
            "\tpeadtri.body_weight, e.target_group_id, e.entry_point_id, lasPreg.pregnancy_status, e.date_confirmed_hiv, \n" +
            "\ttransfer.hiv_status, transfer.status_date,lasclinic.lastvisit, pharm.visit_date\n" +
            "  ORDER BY p.id DESC )\n" +
            "  SELECT \n" +
            "  COUNT(target_group) AS targNumerator,\n" +
            "  COUNT(hospitalNumber) AS targDenominator,\n" +
            "  ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS targPerformance,\n" +
            "  COUNT(entrypoint) AS entryNumerator,\n" +
            "  COUNT(hospitalNumber) AS entryDenominator,\n" +
            "  ROUND((CAST(COUNT(entrypoint) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS entryPerformance,\n" +
            "  COUNT(adultweight) AS adultWeightNumerator,\n" +
            "  COUNT(hospitalNumber) AS adultWeightDenominator,\n" +
            "  ROUND((CAST(COUNT(adultweight) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS adultWeightPerformance,\n" +
            "  COUNT(peadweight) AS peadWeightNumerator,\n" +
            "  COUNT(hospitalNumber) AS peadWeightDenominator,\n" +
            "  ROUND((CAST(COUNT(peadweight) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS peadWeightPerformance,\n" +
            "  COUNT(pregnancy_status) AS pregNumerator,\n" +
            "  COUNT(hospitalNumber) AS pregDenominator,\n" +
            "  ROUND((CAST(COUNT(pregnancy_status) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS pregPerformance,\n" +
            "  COUNT(ArtEqClinicD) AS artEqClinicNumerator,\n" +
            "  COUNT(hospitalNumber) AS artEqClinicDenominator,\n" +
            "  ROUND((CAST(COUNT(ArtEqClinicD) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artEqClinicPerformance,\n" +
            "  COUNT(ArtEqDrugPickupD) AS artEqLastPickupNumerator,\n" +
            "  COUNT(hospitalNumber) AS artEqLastPickupDenominator,\n" +
            "  ROUND((CAST(COUNT(ArtEqDrugPickupD) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artEqLastPickupPerformance,\n" +
            "  COUNT(lGreaterConf) AS lGreaterConfNumerator,\n" +
            "  COUNT(hospitalNumber) AS lGreaterConfDenominator,\n" +
            "  ROUND((CAST(COUNT(lGreaterConf) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lGreaterConfPerformance,\n" +
            "  COUNT(ArtGreaterTrans) AS ArtGreaterTransNumerator,\n" +
            "  COUNT(hiv_status) AS ArtGreaterTransDenominator,\n" +
            "  ROUND((CAST(COUNT(ArtGreaterTrans) AS DECIMAL) / COUNT(hiv_status)) * 100, 2) AS ArtGreaterTransPerformance,\n" +
            "  COUNT(lstPickGreaterDOb) AS lstPickGreaterDObNumerator,\n" +
            "  COUNT(hospitalNumber) AS lstPickGreaterDObDenominator,\n" +
            "  ROUND((CAST(COUNT(lstPickGreaterDOb) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lstPickGreaterDObPerformance,\n" +
            "  COUNT(DrugPickHigherThanTrans) AS lDrugPickHighNumerator,\n" +
            "  COUNT(hospitalNumber) AS lDrugPickHighDenominator,\n" +
            "  ROUND((CAST(COUNT(DrugPickHigherThanTrans) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lDrugPickHighPerformance,\n" +
            "  COUNT(DrugPickLessToday) AS lDrugPickHighTodayNumerator,\n" +
            "  COUNT(hospitalNumber) AS lDrugPickHighTodayDenominator,\n" +
            "  ROUND((CAST(COUNT(DrugPickLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lDrugPickHighTodayPerformance,\n" +
            "  COUNT(clinicPickLessToday) AS clinicPickLessTodayNumerator,\n" +
            "  COUNT(hospitalNumber) AS clinicPickLessTodayDenominator,\n" +
            "  ROUND((CAST(COUNT(clinicPickLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS clinicPickLessTodayPerformance,\n" +
            "  COUNT(artDateLessToday) AS artDateLessTodayNumerator,\n" +
            "  COUNT(hospitalNumber) AS artDateLessTodayDenominator,\n" +
            "  ROUND((CAST(COUNT(artDateLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artDateLessTodayPerformance\n" +
            "  FROM\n" +
            "   \tdataConsistence\n" +
            "  \n" +
            "  \n" +
            "\t", nativeQuery = true)
    List<ClinicalConsistencyDTOProjection> getClinicalConsistencySummary (Long facilityId);





}
