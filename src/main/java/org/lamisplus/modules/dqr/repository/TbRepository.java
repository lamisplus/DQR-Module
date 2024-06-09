package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.PatientDTOProjection;
import org.lamisplus.modules.dqr.domain.TbSummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.lamisplus.modules.dqr.util.DQRQueries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TbRepository extends JpaRepository<DQA, Long> {



    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "\t\tFROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' AND tpt.data->'tbIptScreening'->>'fever' IS NULL\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientNoDocumentedTbScreening (Long facilityId);



    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND tpt.data->'tbIptScreening'->>'fever' IS NULL\n" +
            "\t\tAND (tpt.data->'tbIptScreening'->>'outcome' IS NULL)\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientNoTbScreeningOutCome (Long facilityId);


    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND tpt.data->'tbIptScreening'->>'outcome' IS NULL\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientTbStatusLastVisit (Long facilityId);

    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND tpt.data->'tbIptScreening'->>'outcome' ILIKE '%Presumptive TB case%' \n" +
            "\tAND (tpt.data->'tbIptScreening'->>'completionDate' IS NULL OR tpt.data->'tbIptScreening'->>'completionDate' = '')\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPresumptiveNoTbSample (Long facilityId);


    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND (tpt.data->'tbIptScreening'->>'outcome' ILIKE '%Presumptive TB case%' \n" +
            "\tAND (tpt.data->'tbIptScreening'->>'completionDate'  IS NOT NULL OR tpt.data->'tbIptScreening'->>'completionDate' <> '') AND \n" +
            "\ttpt.data->'tbIptScreening'->>'treatementType' IS NULL)\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPresumptiveNoSampleType (Long facilityId);


    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND tpt.data->'tbIptScreening'->>'tbTreatment' = 'Yes' \n" +
            "\t\tAND (tpt.data->'tbIptScreening'->>'outcome' IS NULL\n" +
            "\t\t\t OR tpt.data->'tbIptScreening'->>'outcome' = '')\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientOnTbWithNoOutcome (Long facilityId);



    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND tpt.data->'tbIptScreening'->>'eligibleForTPT' = 'Yes' \n" +
            "\t\tAND (tpt.data->'tptMonitoring'->> 'date' IS NULL OR\n" +
            "\ttpt.data->'tptMonitoring'->> 'date' = '')\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientEligibleForIptNoDateIptStarted (Long facilityId);


    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()\n" +
            "\t\tAND ipt.iptCompletionDate IS NULL\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientIptStartedWithNoCompletionDate (Long facilityId);


    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()\n" +
            "\t\tAND ipt.iptCompletionStatus IS NULL \n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientIptStartedWithoutCompletedStatus (Long facilityId);


    @Query(value = "SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth, ph.status\n" +
            "    FROM\n" +
            "        patient_person p\n" +
            "    INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
            "\tLEFT JOIN \n" +
            "\t(\n" +
            "\tSELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
            "\tGROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
            "\t) hap ON p.uuid = hap.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT DISTINCT ON (person_uuid)\n" +
            "            person_uuid,\n" +
            "            MAX(date_of_observation) AS date_of_observation,\n" +
            "           data\n" +
            "\t\tFROM\n" +
            "            hiv_observation\n" +
            "        WHERE\n" +
            "            type = 'Chronic Care'\n" +
            "        GROUP BY\n" +
            "            person_uuid,\n" +
            "            date_of_observation,\n" +
            "            type,\n" +
            "            data\n" +
            "    ) AS tpt ON e.person_uuid = tpt.person_uuid\n" +
            "    LEFT JOIN (\n" +
            "        SELECT\n" +
            "            TRUE AS commenced,\n" +
            "            hac.person_uuid\n" +
            "        FROM\n" +
            "            hiv_art_clinical hac\n" +
            "        WHERE\n" +
            "            hac.archived = 0\n" +
            "            AND hac.is_commencement IS TRUE\n" +
            "        GROUP BY\n" +
            "            hac.person_uuid\n" +
            "    ) ca ON p.uuid = ca.person_uuid\n" +
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
            "\tLEFT JOIN (\n" +
            "\tSELECT\n" +
            " DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
            "ipt_type.regimen_name AS iptType,\n" +
            "hap.visit_date AS dateOfIptStart,\n" +
            "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
            "(\n" +
            "    CASE\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
            "   WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
            "      AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
            "         ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
            "         END\n" +
            "          ) AS iptCompletionDate\n" +
            "        FROM\n" +
            "       hiv_art_pharmacy hap\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT person_uuid,\n" +
            "            MAX(visit_date) AS MAXDATE\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy\n" +
            "           WHERE\n" +
            "        (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
            "      AND archived = 0\n" +
            "           GROUP BY\n" +
            "        person_uuid\n" +
            "           ORDER BY\n" +
            "        MAXDATE ASC\n" +
            "       ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
            "           AND max_ipt.person_uuid = hap.person_uuid\n" +
            "           INNER JOIN (\n" +
            "           SELECT\n" +
            "        DISTINCT h.person_uuid,\n" +
            "            h.visit_date,\n" +
            "            CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
            "            CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
            "            hrt.description\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy h,\n" +
            "        jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
            "       RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
            "       RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
            "           WHERE\n" +
            "       hrt.id IN (15)\n" +
            "       ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
            "           AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
            "           LEFT JOIN (\n" +
            "           SELECT\n" +
            "        hap.person_uuid,\n" +
            "        hap.visit_date,\n" +
            "       TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
            "           FROM\n" +
            "        hiv_art_pharmacy hap\n" +
            "       INNER JOIN (\n" +
            "       SELECT\n" +
            "           DISTINCT person_uuid,\n" +
            "        MAX(visit_date) AS MAXDATE\n" +
            "       FROM\n" +
            "           hiv_art_pharmacy\n" +
            "       WHERE\n" +
            "        ipt ->> 'dateCompleted' IS NOT NULL\n" +
            "       GROUP BY\n" +
            "           person_uuid\n" +
            "       ORDER BY\n" +
            "           MAXDATE ASC\n" +
            "        ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
            "       AND complete_ipt.person_uuid = hap.person_uuid\n" +
            "       ) complete ON complete.person_uuid = hap.person_uuid\n" +
            "        WHERE\n" +
            "           hap.archived = 0\n" +
            "           AND hap.visit_date < CAST (NOW() AS DATE)\n" +
            "        GROUP BY\n" +
            "       hap.person_uuid,\n" +
            "       ipt_type.regimen_name,\n" +
            "       hap.ipt,\n" +
            "       hap.visit_date\n" +
            "\t) ipt ON e.person_uuid = ipt.personuuid80\n" +
            "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
            "    WHERE\n" +
            "        p.archived = 0\n" +
            "        AND p.facility_id = ?1 AND status = 'Active' \n" +
            "\t\tAND ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()\n" +
            "\t\tAND ipt.iptType IS NULL \n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth, ph.status,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC", nativeQuery = true)
    List<PatientDTOProjection> getPatientIptStartedWithoutTptType (Long facilityId);


    @Query(value = DQRQueries.TBQueries.TB_SUMMARY_QUERY, nativeQuery = true)
    List<TbSummaryDTOProjection> getTbSummary(Long facilityId);

}
