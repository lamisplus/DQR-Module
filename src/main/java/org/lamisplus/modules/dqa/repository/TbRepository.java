package org.lamisplus.modules.dqa.repository;

import org.lamisplus.modules.dqa.domain.PatientDTOProjection;
import org.lamisplus.modules.dqa.domain.TbSummaryDTOProjection;
import org.lamisplus.modules.dqa.domain.entity.DQA;
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
            "\t(CASE WHEN hap.lastV >= NOW() - INTERVAL '6 MONTH' AND hap.lastV <= NOW() THEN 1 ELSE null END) AS hadVl6month\n" +
            "\t\n" +
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
            "        tpt.data,hap.lastv,\n" +
            "        tpt.date_of_observation\n" +
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
            "    ROUND((CAST(COUNT(iptStartDate) AS DECIMAL) / COUNT(eligibeipt)) * 100, 2) AS tptstartPerformance\n" +
            "\t\n" +
            "FROM\n" +
            "    tbSummary;\n", nativeQuery = true)
    List<TbSummaryDTOProjection> getTbSummary(Long facilityId);

}
