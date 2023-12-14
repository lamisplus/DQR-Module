package org.lamisplus.modules.dqr.repository;

import org.lamisplus.modules.dqr.domain.TbSummaryDTOProjection;
import org.lamisplus.modules.dqr.domain.entity.DQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TbRepository extends JpaRepository<DQA, Long> {


    @Query(value = "WITH tbSummary AS (\n" +
            "    SELECT\n" +
            "        e.unique_id AS patientId,\n" +
            "        p.hospital_number AS hospitalNumber,\n" +
            "        INITCAP(p.sex) AS sex,\n" +
            "        CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
            "        p.date_of_birth AS dateOfBirth,\n" +
            "        tpt.date_of_observation,\n" +
            "        tpt.data->'tptMonitoring'->> 'date' AS tptDate,\n" +
            "        tpt.data->'tbIptScreening'->>'fever' AS doctb,\n" +
            "        tpt.data->'tbIptScreening'->>'outcome' AS tboutcome,\n" +
            "        tpt.data->'tbIptScreening'->>'tbTreatment' AS tbtreat,\n" +
            "        tpt.data->'tbIptScreening'->>'completionDate' AS sampleDate,\n" +
            "        tpt.data->'tbIptScreening'->>'treatementType' AS treatmenttype,\n" +
            "        tpt.data->'tbIptScreening'->>'eligibleForTPT' AS tptEligible,\n" +
            "        tpt.data->'tbIptScreening'->>'completionDate' AS tptcompletionDate,\n" +
            "\t\ttpt.data->'tbIptScreening'->>'treatmentOutcome' AS tbstatus,\n" +
            "\t\t(CASE WHEN \n" +
            "    tpt.data->'tbIptScreening'->>'outcome' IS NOT NULL AND tpt.data->'tbIptScreening'->>'outcome' <> '' AND\n" +
            "    tpt.data->'tbIptScreening'->>'fever' IS NOT NULL AND tpt.data->'tbIptScreening'->>'fever' <> ''\n" +
            "\tTHEN 1 ELSE NULL END) AS completeAnddoc,\n" +
            "\t(CASE WHEN tpt.data->'tbIptScreening'->>'outcome' ILIKE '%Presumptive TB case%' THEN 1 ELSE NULL END) AS presumptivetb,\n" +
            "\t(CASE WHEN  (tpt.data->'tbIptScreening'->>'outcome' ILIKE '%Presumptive TB case%' \n" +
            "\tAND (tpt.data->'tbIptScreening'->>'completionDate'  IS NOT NULL OR tpt.data->'tbIptScreening'->>'completionDate' <> '')) THEN 1 ELSE NULL END) AS prsmptivecollection,\n" +
            "\t(CASE WHEN  (tpt.data->'tbIptScreening'->>'outcome' ILIKE '%Presumptive TB case%' \n" +
            "\tAND (tpt.data->'tbIptScreening'->>'completionDate'  IS NOT NULL OR tpt.data->'tbIptScreening'->>'completionDate' <> '') AND \n" +
            "\ttpt.data->'tbIptScreening'->>'treatementType' IS NOT NULL) THEN 1 ELSE NULL END) AS prsmptivectionsamp,\n" +
            "\t(CASE WHEN tpt.data->'tbIptScreening'->>'tbTreatment' = 'Yes' THEN 1 ELSE NULL END) AS tbtreatyes,\n" +
            "\t(CASE WHEN tpt.data->'tbIptScreening'->>'tbTreatment' = 'Yes' AND (tpt.data->'tbIptScreening'->>'outcome' IS NOT NULL OR tpt.data->'tbIptScreening'->>'outcome' <>'') THEN 1 ELSE NULL END) AS tbtreatwithoutcome,\n" +
            "\t(CASE WHEN tpt.data->'tbIptScreening'->>'eligibleForTPT' = 'Yes' THEN 1 ELSE NULL END) AS eligibeipt,\n" +
            "\t(CASE WHEN tpt.data->'tbIptScreening'->>'eligibleForTPT' = 'Yes' AND (tpt.data->'tptMonitoring'->> 'date' IS NOT NULL OR\n" +
            "\ttpt.data->'tptMonitoring'->> 'date' <> '') THEN 1 ELSE NULL END) AS iptStartDate,\n" +
            "\t(CASE WHEN hap.lastV >= NOW() - INTERVAL '6 MONTH' AND hap.lastV <= NOW() THEN 1 ELSE null END) AS hadVl6month,\n" +
            "\tipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate,\n" +
            "\t(CASE WHEN tpt.data->'tbIptScreening'->>'eligibleForTPT' = 'Yes' AND ipt.dateOfIptStart IS NOT NULL THEN 1 ELSE NULL END) AS iptEliStart,\n" +
            "\t(CASE WHEN ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW() THEN 1 ELSE null END) AS ipt6month,\n" +
            "\t(CASE WHEN (ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()) AND ipt.iptCompletionDate IS NOT NULL THEN 1 ELSE null END) AS ipt6monthCompl,\n" +
            "\tipt.iptCompletionStatus,\n" +
            "\t(CASE WHEN (ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()) AND ipt.iptCompletionStatus IS NOT NULL THEN 1 ELSE null END) AS iptStatus,\n" +
            "\t(CASE WHEN (ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()) AND ipt.iptType IS NOT NULL THEN 1 ELSE null END) AS iptTypeStatus\n" +
            "\n" +
            "\n" +
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
            "        AND p.facility_id = ?1\n" +
            "    GROUP BY\n" +
            "        e.id,\n" +
            "        ca.commenced,\n" +
            "        p.id,\n" +
            "        pc.display,\n" +
            "        p.hospital_number,\n" +
            "        p.date_of_birth,\n" +
            "        tpt.data,\n" +
            "        tpt.date_of_observation,\n" +
            "\t\thap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus\n" +
            "    ORDER BY\n" +
            "        p.id DESC\n" +
            ")\n" +
            "SELECT\n" +
            "    COUNT(doctb) AS tbScreenNumerator,\n" +
            "    COUNT(hospitalNumber) AS tbScreenDenominator,\n" +
            "    ROUND((CAST(COUNT(doctb) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS tbScreenPerformance,\n" +
            "\tCOUNT(completeAnddoc) AS docAndCompletedNumerator,\n" +
            "    COUNT(doctb) AS docAndCompletedDenominator,\n" +
            "    ROUND((CAST(COUNT(completeAnddoc) AS DECIMAL) / COUNT(doctb)) * 100, 2) AS docAndCompletedPerformance,\n" +
            "\tCOUNT(tboutcome) AS tbstatusNumerator,\n" +
            "    COUNT(hospitalNumber) AS tbstatusDenominator,\n" +
            "    ROUND((CAST(COUNT(tboutcome) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS tbstatusPerformance,\n" +
            "\tCOUNT(prsmptivecollection) AS preSampleNumerator,\n" +
            "    COUNT(tboutcome) AS preSampleDenominator,\n" +
            "    ROUND((CAST(COUNT(prsmptivecollection) AS DECIMAL) / COUNT(tboutcome)) * 100, 2) AS preSamplePerformance,\n" +
            "\tCOUNT(prsmptivectionsamp) AS preSampleTypeNumerator,\n" +
            "    COUNT(prsmptivecollection) AS preSampleTypeDenominator,\n" +
            "    ROUND((CAST(COUNT(prsmptivectionsamp) AS DECIMAL) / COUNT(prsmptivecollection)) * 100, 2) AS preSampleTypePerformance,\n" +
            "\tCOUNT(iptStartDate) AS tptstartNumerator,\n" +
            "    COUNT(eligibeipt) AS tptstartDenominator,\n" +
            "    ROUND((CAST(COUNT(iptStartDate) AS DECIMAL) / COUNT(eligibeipt)) * 100, 2) AS tptstartPerformance,\n" +
            "\tCOUNT(iptEliStart) AS iptEliStartNumerator,\n" +
            "    COUNT(eligibeipt) AS iptEliStartDenominator,\n" +
            "    ROUND((CAST(COUNT(iptEliStart) AS DECIMAL) / COUNT(eligibeipt)) * 100, 2) AS iptEliStartPerformance,\n" +
            "\tCOUNT(ipt6monthCompl) AS ipt6monthComplNumerator,\n" +
            "    COUNT(ipt6month) AS ipt6monthComplDenominator,\n" +
            "    ROUND((CAST(COUNT(ipt6monthCompl) AS DECIMAL) / COUNT(ipt6month)) * 100, 2) AS ipt6monthComplPerformance,\n" +
            "\tCOUNT(iptStatus) AS iptComplStatususNumerator,\n" +
            "    COUNT(ipt6month) AS iptComplStatususDenominator,\n" +
            "    ROUND((CAST(COUNT(iptStatus) AS DECIMAL) / COUNT(ipt6month)) * 100, 2) AS iptComplStatususPerformance,\n" +
            "\tCOUNT(iptTypeStatus) AS iptTypeStatusNumerator,\n" +
            "    COUNT(ipt6month) AS iptTypeStatusDenominator,\n" +
            "    ROUND((CAST(COUNT(iptTypeStatus) AS DECIMAL) / COUNT(ipt6month)) * 100, 2) AS iptTypeStatusPerformance\n" +
            "FROM\n" +
            "    tbSummary;\n" +
            "\t\n" +
            "\t\n", nativeQuery = true)
    List<TbSummaryDTOProjection> getTbSummary(Long facilityId);

}
