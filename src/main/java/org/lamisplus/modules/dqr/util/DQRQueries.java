package org.lamisplus.modules.dqr.util;

public class DQRQueries {

    public static class DataConsistency {

        public static final String CLINICALS_SUMMARY_QUERIES = "WITH dataConsistence AS (\n" +
                "    SELECT\n" +
                "        e.unique_id AS patientId,\n" +
                "        p.hospital_number AS hospitalNumber,\n" +
                "        INITCAP(p.sex) AS sex,\n" +
                "        p.date_of_birth AS dateOfBirth,\n" +
                "        tri.body_weight AS adultweight,\n" +
                "        tri.visit_date AS visit_date,\n" +
                "        e.target_group_id AS target_group,\n" +
                "        e.entry_point_id AS entryPoint,\n" +
                "        e.date_confirmed_hiv AS hiv_confirm_date,\n" +
                "        (CASE WHEN lasClinic.lastvisit >= e.date_confirmed_hiv THEN 1 ELSE null END) AS lGreaterConf,\n" +
                "        pharm.visit_date AS lastPickUp,\n" +
                "        (CASE WHEN pharm.visit_date > p.date_of_birth THEN 1 ELSE null END) AS lstPickGreaterDOb,\n" +
                "        transfer.hiv_status,\n" +
                "        transfer.status_date,\n" +
                "        (CASE WHEN e.date_started < transfer.status_date THEN 1 ELSE null END) AS ArtGreaterTrans,\n" +
                "        (CASE WHEN e.date_started = lasClinic.lastvisit THEN 1 ELSE null END) AS ArtEqClinicD,\n" +
                "        (CASE WHEN e.date_started = pharm.visit_date THEN 1 ELSE null END) AS ArtEqDrugPickupD,\n" +
                "        (CASE WHEN pharm.visit_date >= transfer.status_date THEN 1 ELSE null END) AS DrugPickHigherThanTrans,\n" +
                "        (CASE WHEN pharm.visit_date <= CURRENT_DATE THEN 1 ELSE null END) AS DrugPickLessToday,\n" +
                "        (CASE WHEN lasClinic.lastvisit <= CURRENT_DATE THEN 1 ELSE null END) AS clinicPickLessToday,\n" +
                "        (CASE WHEN e.date_started <= CURRENT_DATE THEN 1 ELSE null END) AS artDateLessToday,\n" +
                "        (CASE WHEN lasClinic.lastvisit > transfer.status_date THEN 1 ELSE null END) AS clinicGreaterThanTrans,\n" +
                "        (CASE WHEN vl.dateOfLastViralLoad > vl.dateSampleCollected THEN 1 ELSE NULL END) AS vlSample,\n" +
                "        CASE WHEN sex = 'Female' AND EXTRACT(YEAR FROM AGE(NOW(), date_of_birth)) > 12 THEN 1 ELSE NULL END AS activeFemaleAdult,\n" +
                "        CASE WHEN lasClinic.pregnancy_status IS NOT NULL AND EXTRACT(YEAR FROM AGE(NOW(), date_of_birth)) > 12 AND INITCAP(p.sex) = 'Female' THEN 1 ELSE NULL END AS adultPre,\n" +
                "        CASE WHEN tri.body_weight < 61 AND EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) BETWEEN 0 AND 14 THEN 1 ELSE NULL END AS peadweight,\n" +
                "        CASE WHEN EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) BETWEEN 0 AND 14 THEN 1 ELSE NULL END AS peadcURR,\n" +
                "\t    st.status\n" +
                "\tFROM\n" +
                "        patient_person p\n" +
                "    INNER JOIN\n" +
                "        hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT\n" +
                "            TRUE AS commenced,\n" +
                "            hac.person_uuid,\n" +
                "            hac.visit_date,\n" +
                "            hac.pregnancy_status\n" +
                "        FROM\n" +
                "            hiv_art_clinical hac\n" +
                "        WHERE\n" +
                "            hac.archived = 0 AND hac.is_commencement IS TRUE\n" +
                "        GROUP BY\n" +
                "            hac.person_uuid, hac.visit_date, hac.pregnancy_status\n" +
                "    ) ca ON p.uuid = ca.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (person_uuid)\n" +
                "            person_uuid, visit_date, body_weight\n" +
                "        FROM (\n" +
                "            SELECT\n" +
                "                ht.person_uuid,\n" +
                "                MAX(ht.visit_date) AS visit_date,\n" +
                "                tr.body_weight\n" +
                "            FROM\n" +
                "                hiv_art_clinical ht\n" +
                "            JOIN\n" +
                "                triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid\n" +
                "            GROUP BY\n" +
                "                ht.person_uuid, tr.body_weight\n" +
                "            ORDER BY\n" +
                "                ht.person_uuid DESC\n" +
                "        ) fi\n" +
                "        ORDER BY\n" +
                "            person_uuid DESC\n" +
                "    ) tri ON tri.person_uuid = p.uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (person_uuid)\n" +
                "            person_uuid,\n" +
                "            lastVisit,\n" +
                "            pregnancy_status\n" +
                "        FROM (\n" +
                "            SELECT\n" +
                "                hacc.person_uuid,\n" +
                "                MAX(hacc.visit_date) AS lastVisit,\n" +
                "                pregnancy_status\n" +
                "            FROM\n" +
                "                hiv_art_clinical hacc\n" +
                "            JOIN\n" +
                "                patient_person p2 ON hacc.person_uuid = p2.uuid\n" +
                "            WHERE\n" +
                "                hacc.archived = 0\n" +
                "            GROUP BY\n" +
                "                person_uuid, pregnancy_status\n" +
                "            ORDER BY\n" +
                "                person_uuid DESC\n" +
                "        ) lClinicVisit\n" +
                "        ORDER BY\n" +
                "            person_uuid DESC\n" +
                "    ) lasClinic ON p.uuid = lasClinic.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (person_id)\n" +
                "            person_id,\n" +
                "            MAX(status_date) AS status_date,\n" +
                "            hiv_status\n" +
                "        FROM\n" +
                "            hiv_status_tracker\n" +
                "        WHERE\n" +
                "            hiv_status = 'ART_TRANSFER_IN'\n" +
                "        GROUP BY\n" +
                "            person_id, hiv_status\n" +
                "    ) transfer ON p.uuid = transfer.person_id\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (lo.patient_uuid)\n" +
                "            lo.patient_uuid AS person_uuid,\n" +
                "            ls.date_sample_collected AS dateSampleCollected,\n" +
                "            lr.result_reported AS lastViralLoad,\n" +
                "            lr.date_result_reported AS dateOfLastViralLoad\n" +
                "        FROM\n" +
                "            laboratory_order lo\n" +
                "        LEFT JOIN (\n" +
                "            SELECT\n" +
                "                patient_uuid,\n" +
                "                MAX(order_date) AS MAXDATE\n" +
                "            FROM\n" +
                "                laboratory_order\n" +
                "            GROUP BY\n" +
                "                patient_uuid\n" +
                "            ORDER BY\n" +
                "                MAXDATE ASC\n" +
                "        ) AS current_lo ON current_lo.patient_uuid = lo.patient_uuid AND current_lo.MAXDATE = lo.order_date\n" +
                "        LEFT JOIN laboratory_test lt ON lt.lab_order_id = lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
                "        LEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id = lt.viral_load_indication\n" +
                "        LEFT JOIN laboratory_labtest ll ON ll.id = lt.lab_test_id\n" +
                "        LEFT JOIN laboratory_sample ls ON ls.test_id = lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
                "        LEFT JOIN laboratory_result lr ON lr.test_id = lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
                "        WHERE\n" +
                "            lo.archived = 0 AND lr.date_result_reported IS NOT NULL\n" +
                "    ) vl ON e.person_uuid = vl.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (person_uuid)\n" +
                "            person_uuid,\n" +
                "            visit_date,\n" +
                "            refill_period,\n" +
                "            regimen\n" +
                "        FROM (\n" +
                "            SELECT\n" +
                "                person_uuid,\n" +
                "                refill_period,\n" +
                "                MAX(visit_date) AS visit_date,\n" +
                "                extra->'regimens'->0->>'name' AS regimen\n" +
                "            FROM\n" +
                "                hiv_art_pharmacy\n" +
                "            GROUP BY\n" +
                "                refill_period, person_uuid, extra\n" +
                "            ORDER BY\n" +
                "                person_uuid DESC\n" +
                "        ) fi\n" +
                "        ORDER BY\n" +
                "            person_uuid DESC\n" +
                "    ) pharm ON pharm.person_uuid = p.uuid\n" +
                "\tLEFT JOIN (\n" +
                "\t   SELECT personUuid, status FROM (\n" +
                "\tSELECT\n" +
                " DISTINCT ON (pharmacy.person_uuid) pharmacy.person_uuid AS personUuid,\n" +
                "(\n" +
                "    CASE\n" +
                "        WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "        WHEN(\n" +
                "        stat.status_date > pharmacy.maxdate\n" +
                "    AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %' )\n" +
                ")THEN stat.hiv_status\n" +
                "        ELSE pharmacy.status\n" +
                "        END\n" +
                "    ) AS status,\n" +
                "\n" +
                "stat.cause_of_death, stat.va_cause_of_death\n" +
                "\n" +
                "         FROM\n" +
                " (\n" +
                "     SELECT\n" +
                "         (\n" +
                " CASE\n" +
                "     WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "     ELSE 'Active'\n" +
                "     END\n" +
                " ) status,\n" +
                "         (\n" +
                " CASE\n" +
                "     WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "     ELSE hp.visit_date\n" +
                "     END\n" +
                " ) AS visit_date,\n" +
                "         hp.person_uuid, MAXDATE\n" +
                "     FROM\n" +
                "         hiv_art_pharmacy hp\n" +
                " INNER JOIN (\n" +
                "         SELECT hap.person_uuid, hap.visit_date AS  MAXDATE, ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "           FROM public.hiv_art_pharmacy hap \n" +
                "                    INNER JOIN public.hiv_art_pharmacy_regimens pr \n" +
                "                    ON pr.art_pharmacy_id = hap.id \n" +
                "            INNER JOIN hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "            INNER JOIN public.hiv_regimen r on r.id = pr.regimens_id \n" +
                "            INNER JOIN public.hiv_regimen_type rt on rt.id = r.regimen_type_id \n" +
                "            WHERE r.regimen_type_id in (1,2,3,4,14) \n" +
                "            AND hap.archived = 0                \n" +
                "             ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid \n" +
                "      AND MAX.rnkkk3 = 1\n" +
                "     WHERE\n" +
                " hp.archived = 0\n" +
                " ) pharmacy\n" +
                "\n" +
                "     LEFT JOIN (\n" +
                "     SELECT\n" +
                "         hst.hiv_status,\n" +
                "         hst.person_id,\n" +
                "\t\t hst.status_date,\n" +
                "\t\t hst.cause_of_death,\n" +
                "\t\t hst.va_cause_of_death\n" +
                "     FROM\n" +
                "         (\n" +
                " SELECT * FROM (SELECT DISTINCT (person_id) person_id, status_date, cause_of_death,va_cause_of_death,\n" +
                "        hiv_status, ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "    FROM hiv_status_tracker WHERE archived=0 )s\n" +
                " WHERE s.row_number=1\n" +
                "         ) hst\n" +
                " INNER JOIN hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                " ) stat ON stat.person_id = pharmacy.person_uuid --AND pharmacy.status = 'Active' \n" +
                ") -- st where status = 'Active'\n" +
                "\t\t\n" +
                "\t)st ON st.personUuid = e.person_uuid\n" +
                "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
                "    WHERE\n" +
                "        p.archived = 0 AND p.facility_id = ?1 AND st.status = 'Active'\n" +
                "    GROUP BY\n" +
                "        e.id, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.visit_date,\n" +
                "        e.target_group_id, e.entry_point_id, e.date_confirmed_hiv, p.sex, p.id,\n" +
                "        transfer.hiv_status, transfer.status_date, lasClinic.lastvisit, pharm.visit_date,\n" +
                "        vl.dateOfLastViralLoad, vl.dateSampleCollected,\n" +
                "        lasClinic.pregnancy_status,\n" +
                "\t    st.status\n" +
                "    ORDER BY\n" +
                "        p.id DESC\n" +
                ")\n" +
                "SELECT\n" +
                "    COUNT(target_group) AS targNumerator,\n" +
                "    COUNT(hospitalNumber) AS targDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(target_group) AS targVariance,\n" +
                "    ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS targPerformance,\n" +
                "    COUNT(entrypoint) AS entryNumerator,\n" +
                "    COUNT(hospitalNumber) AS entryDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(entrypoint) AS entryVariance,\n" +
                "    ROUND((CAST(COUNT(entrypoint) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS entryPerformance,\n" +
                "    COUNT(adultweight) AS adultWeightNumerator,\n" +
                "    COUNT(hospitalNumber) AS adultWeightDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(adultweight) AS adultWeightVariance,\n" +
                "    ROUND((CAST(COUNT(adultweight) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS adultWeightPerformance,\n" +
                "    COUNT(peadweight) AS peadWeightNumerator,\n" +
                "    COUNT(peadcURR) AS peadWeightDenominator,\n" +
                "    COUNT(peadcURR) - COUNT(peadweight) AS peadWeightVariance,\n" +
                "    ROUND((CAST(COUNT(peadweight) AS DECIMAL) / COUNT(peadcURR)) * 100, 2) AS peadWeightPerformance,\n" +
                "    COUNT(adultPre) AS pregNumerator,\n" +
                "    COUNT(activeFemaleAdult) AS pregDenominator,\n" +
                "    COUNT(activeFemaleAdult) - COUNT(adultPre) AS pregVariance,\n" +
                "    ROUND((CAST(COUNT(adultPre) AS DECIMAL) / COUNT(activeFemaleAdult)) * 100, 2) AS pregPerformance,\n" +
                "    COUNT(ArtEqClinicD) AS artEqClinicNumerator,\n" +
                "    COUNT(hospitalNumber) AS artEqClinicDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(ArtEqClinicD) AS artEqClinicVariance,\n" +
                "    ROUND((CAST(COUNT(ArtEqClinicD) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artEqClinicPerformance,\n" +
                "    COUNT(ArtEqDrugPickupD) AS artEqLastPickupNumerator,\n" +
                "    COUNT(hospitalNumber) AS artEqLastPickupDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(ArtEqDrugPickupD) AS artEqLastPickupVariance,\n" +
                "    ROUND((CAST(COUNT(ArtEqDrugPickupD) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artEqLastPickupPerformance,\n" +
                "    COUNT(lGreaterConf) AS lGreaterConfNumerator,\n" +
                "    COUNT(hospitalNumber) AS lGreaterConfDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(lGreaterConf) AS lGreaterConfVariance,\n" +
                "    ROUND((CAST(COUNT(lGreaterConf) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lGreaterConfPerformance,\n" +
                "    COUNT(ArtGreaterTrans) AS artGreaterTransNumerator,\n" +
                "    COUNT(hiv_status) AS ArtGreaterTransDenominator,\n" +
                "    COUNT(hiv_status) - COUNT(ArtGreaterTrans) AS ArtGreaterTransVariance,\n" +
                "    ROUND((CAST(COUNT(ArtGreaterTrans) AS DECIMAL) / COUNT(hiv_status)) * 100, 2) AS ArtGreaterTransPerformance,\n" +
                "    COUNT(lstPickGreaterDOb) AS lstPickGreaterDObNumerator,\n" +
                "    COUNT(hospitalNumber) AS lstPickGreaterDObDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(lstPickGreaterDOb) AS lstPickGreaterDObVariance,\n" +
                "    ROUND((CAST(COUNT(lstPickGreaterDOb) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lstPickGreaterDObPerformance,\n" +
                "    COUNT(DrugPickHigherThanTrans) AS lDrugPickHighNumerator,\n" +
                "    COUNT(hospitalNumber) AS lDrugPickHighDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(DrugPickHigherThanTrans) AS lDrugPickHighVariance,\n" +
                "    ROUND((CAST(COUNT(DrugPickHigherThanTrans) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lDrugPickHighPerformance,\n" +
                "    COUNT(DrugPickLessToday) AS lDrugPickHighTodayNumerator,\n" +
                "    COUNT(hospitalNumber) AS lDrugPickHighTodayDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(DrugPickLessToday) AS lDrugPickHighTodayVariance,\n" +
                "    ROUND((CAST(COUNT(DrugPickLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lDrugPickHighTodayPerformance,\n" +
                "    COUNT(clinicPickLessToday) AS clinicPickLessTodayNumerator,\n" +
                "    COUNT(hospitalNumber) AS clinicPickLessTodayDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(clinicPickLessToday) AS clinicPickLessTodayVariance,\n" +
                "    ROUND((CAST(COUNT(clinicPickLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS clinicPickLessTodayPerformance,\n" +
                "    COUNT(artDateLessToday) AS artDateLessTodayNumerator,\n" +
                "    COUNT(hospitalNumber) AS artDateLessTodayDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(artDateLessToday) AS artDateLessTodayVariance,\n" +
                "    ROUND((CAST(COUNT(artDateLessToday) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS artDateLessTodayPerformance,\n" +
                "    COUNT(vlSample) AS vlNumerator,\n" +
                "    COUNT(hospitalNumber) AS vlDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(vlSample) AS vlVariance,\n" +
                "    ROUND((CAST(COUNT(vlSample) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS vlPerformance\n" +
                "FROM\n" +
                "    dataConsistence;\n";


        public static final String PHARMACY_SUMMARY_QUERIES = "With pharmacySummary AS (\n" +
                "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
                ",p.date_of_birth AS dateOfBirth, pharm.refill_period as refillMonth, pharm.visit_date AS visit_date, pharm.extra AS regimen\n" +
                "  FROM patient_person p \n" +
                "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "  LEFT JOIN\n" +
                "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
                "  LEFT JOIN\n" +
                "  (SELECT DISTINCT ON (person_uuid)\n" +
                "    person_uuid, visit_date, refill_period, extra\n" +
                "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra from hiv_art_pharmacy\n" +
                "where archived = 0\n" +
                "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi ORDER BY\n" +
                "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
                "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
                "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
                "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, pharm.refill_period, p.facility_id, pharm.visit_date, pharm.extra\n" +
                "  ORDER BY p.id DESC )\n" +
                "SELECT\n" +
                "    COUNT(refillMonth) AS refillNumerator,\n" +
                "    COUNT(hospitalNumber) AS refillDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(refillMonth) AS refillVariance,\n" +
                "    ROUND((CAST(COUNT(refillMonth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS refillPerformance,\n" +
                "    COUNT(regimen) AS regimenNumerator,\n" +
                "    COUNT(hospitalNumber) AS regimenDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(regimen) AS regimenVariance,\n" +
                "    ROUND((CAST(COUNT(regimen) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS regimenPerformance\n" +
                "FROM pharmacySummary";


        public static final String LABORATORY_SUMMARY_QUERIES = "WITH vlSummary AS ( \n" +
                "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
                "CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
                "         p.date_of_birth AS dateOfBirth, vl.dateSampleCollected, vl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date,\n" +
                " (CASE WHEN pharm.visit_date >= NOW() - INTERVAL '1 YEAR' AND pharm.visit_date <= NOW() THEN 1 ELSE null END) AS eligibleVl1year,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad >= NOW() - INTERVAL '1 YEAR' AND vl.dateOfLastViralLoad <= NOW() THEN 1 ELSE null END) AS hadvl1year,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad IS NOT NULL AND vl.dateSampleCollected IS NOT NULL THEN 1 ELSE NULL END) AS hadvlAndSampleDate,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad IS NOT NULL AND vl.pcrDate IS NOT NULL THEN 1 ELSE NULL END) AS hadvlAndpcrDate,\n" +
                "(CASE WHEN vl.viralLoadType IS NOT NULL AND vl.dateSampleCollected IS NOT NULL THEN 1 ELSE NULL END) AS hadVlIndicator,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad > vl.dateSampleCollected THEN 1 ELSE NULL END) AS vlDateGsDate,\n" +
                "(CASE WHEN cd4.cd4date >= NOW() - INTERVAL '1 YEAR' AND cd4.cd4date <= NOW() THEN 1 ELSE null END) AS hadcd4vl1year\n" +
                "\n" +
                "\n" +
                "        FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "        LEFT JOIN\n" +
                "        (SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "        GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
                "bac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
                "-- CASE WHEN lr.result_reported ~ E'^\\\\\\\\d+(\\\\\\\\.\\\\\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
                "--            ELSE NULL END AS lastViralLoad, \n" +
                "lr.result_reported AS lastViralLoad,\n" +
                "lr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
                "lr.date_result_reported as dateOfLastViralLoad\n" +
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
                "WHERE  lo.archived=0\n" +
                ") vl ON e.person_uuid = vl.person_uuid\n" +
                "LEFT JOIN(\n" +
                "select DISTINCT ON (patient_uuid) patient_uuid, MAX(date_sample_collected) AS cd4date \n" +
                "from laboratory_sample\n" +
                "WHERE test_id = 1\n" +
                "GROUP BY patient_uuid, date_sample_collected ORDER BY patient_uuid, date_sample_collected DESC )\n" +
                "cd4 ON e.person_uuid = cd4.patient_uuid\n" +
                "LEFT JOIN\n" +
                "  (SELECT DISTINCT ON (person_uuid)\n" +
                "    person_uuid, visit_date, refill_period\n" +
                "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
                "  where archived !=1\n" +
                "GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
                "    person_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
                "        WHERE p.archived=0 AND p.facility_id= ?1 \n" +
                "        GROUP BY e.id, ca.commenced, p.id, p.hospital_number, p.date_of_birth, vl.dateSampleCollected, \n" +
                "vl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date\n" +
                "        ORDER BY p.id DESC )\n" +
                "SELECT \n" +
                "  COUNT(hadvl1year) AS eligibleVlNumerator,\n" +
                "  COUNT(eligibleVl1year) AS eligibleVlDenominator,\n" +
                "  COUNT(eligibleVl1year) - COUNT(hadvl1year) AS eligibleVlVariance,\n" +
                "  ROUND((CAST(COUNT(hadvl1year) AS DECIMAL) / COUNT(eligibleVl1year)) * 100, 2) AS eligibleVlPerformance,\n" +
                "  COUNT(hadvlAndSampleDate) AS hadVlNumerator,\n" +
                "  COUNT(dateOfLastViralLoad) AS hadVlDenominator,\n" +
                "  COUNT(dateOfLastViralLoad) - COUNT(hadvlAndSampleDate) AS hadVlVariance,\n" +
                "  ROUND((CAST(COUNT(hadvlAndSampleDate) AS DECIMAL) / COUNT(dateOfLastViralLoad)) * 100, 2) AS hadVlPerformance,\n" +
                "  COUNT(hadvlAndpcrDate) AS hadPcrDateNumerator,\n" +
                "  COUNT(dateSampleCollected) AS hadPcrDateDenominator,\n" +
                "  COUNT(dateSampleCollected) - COUNT(hadvlAndpcrDate) AS hadPcrDateVariance,\n" +
                "  ROUND((CAST(COUNT(hadvlAndpcrDate) AS DECIMAL) / COUNT(dateSampleCollected)) * 100, 2) AS hadPcrDatePerformance,\n" +
                "  COUNT(hadVlIndicator) AS  hadIndicatorNumerator,\n" +
                "  COUNT(dateSampleCollected) AS  hadIndicatorDenominator,\n" +
                "  COUNT(dateSampleCollected) - COUNT(hadVlIndicator) AS hadIndicatorVariance,\n" +
                "  ROUND((CAST(COUNT(hadVlIndicator) AS DECIMAL) / COUNT(dateSampleCollected)) * 100, 2) AS hadIndicatorPerformance,\n" +
                "  COUNT(vlDateGsDate) AS vlDateGsDateNumerator,\n" +
                "  COUNT(dateOfLastViralLoad) AS vlDateGsDateDenominator,\n" +
                "  COUNT(dateOfLastViralLoad) - COUNT(vlDateGsDate) AS vlDateGsDateVariance,\n" +
                "  ROUND((CAST(COUNT(vlDateGsDate) AS DECIMAL) / COUNT(dateOfLastViralLoad)) * 100, 2) AS vlDateGsDatePerformance,\n" +
                "  COUNT(hadcd4vl1year) AS treatmentCd4Numerator,\n" +
                "  COUNT(eligibleVl1year) AS treatmentCd4Denominator,\n" +
                "  COUNT(eligibleVl1year) - COUNT(hadcd4vl1year) AS treatmentCd4Variance,\n" +
                "  ROUND((CAST(COUNT(hadcd4vl1year) AS DECIMAL) / COUNT(eligibleVl1year)) * 100, 2) AS treatmentCd4Performance,\n" +
                "  COUNT(hadcd4vl1year) AS cd4WithinYearNumerator,\n" +
                "  COUNT(eligibleVl1year) AS cd4WithinYearDenominator,\n" +
                "  COUNT(eligibleVl1year) - COUNT(hadcd4vl1year) AS cd4WithinYearVariance,\n" +
                "  ROUND((CAST(COUNT(hadcd4vl1year) AS DECIMAL) / COUNT(eligibleVl1year)) * 100, 2) AS cd4WithinYearPerformance\n" +
                "  FROM \n" +
                "vlSummary";

        public static final String EAC_SUMMARY_QUERIES = "WITH eacSummary AS (\n" +
                "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex, \n" +
                "p.date_of_birth AS dateOfBirth, e.date_started,e.person_uuid,\n" +
                "(CASE WHEN e.date_started >= NOW() - INTERVAL '1 YEAR' AND e.date_started <= NOW() THEN 1 ELSE null END) AS hadVl1year,\n" +
                "(CASE WHEN ((pharm.visit_date  >= NOW() - INTERVAL '6 MONTH' AND pharm.visit_date <= NOW()) AND CAST(vl.clastViralLoad AS NUMERIC) > 1000) THEN 1 ELSE null END) AS sixmVl,\n" +
                "(CASE WHEN ((pharm.visit_date  >= NOW() - INTERVAL '6 MONTH' AND pharm.visit_date <= NOW()) AND eac.eac_commenced IS NOT NULL AND CAST(vl.clastViralLoad AS NUMERIC) > 1000) THEN 1 ELSE null END) AS hadeac,\n" +
                "pharm.visit_date,\n" +
                "vl.dateSampleCollected,\n" +
                "eac1.status , eac1.last_viral_load, eac1.date_of_last_viral_load, eac1.visit_date AS eac_completion,\n" +
                "(CASE WHEN eac1.status is NOT NULL AND vl.dateSampleCollected IS NOT NULL THEN 1 ELSE NULL END) AS compleacdate,\n" +
                "(CASE WHEN eac1.status is NOT NULL AND eac1.visit_date IS NOT NULL THEN 1 ELSE NULL END) AS cmpleac,\n" +
                "(CASE WHEN eac1.status is NOT NULL AND eac1.visit_date IS NOT NULL AND eac1.date_of_last_viral_load IS NOT NULL THEN 1 ELSE NULL END) AS postEac\n" +
                "  FROM patient_person p \n" +
                "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "LEFT JOIN\n" +
                "  (SELECT DISTINCT ON (person_uuid)\n" +
                "    person_uuid, visit_date, refill_period\n" +
                "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date from hiv_art_pharmacy\n" +
                "  where archived !=1\n" +
                "GROUP BY refill_period, person_uuid ORDER BY person_uuid DESC ) fi ORDER BY\n" +
                "    person_uuid DESC ) pharm ON e.person_uuid = pharm.person_uuid\n" +
                "  LEFT JOIN\n" +
                "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status) ca ON p.uuid = ca.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
                "bac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
                "CASE WHEN lr.result_reported ~ E'^\\\\\\\\d+(\\\\\\\\.\\\\\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
                "           ELSE NULL END AS clastViralLoad, \n" +
                "lr.result_reported AS lastViralLoad,\n" +
                "lr.date_sample_received_at_pcr_lab AS pcrDate,\n" +
                "lr.date_result_reported as dateOfLastViralLoad\n" +
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
                "WHERE  lo.archived=0 ) vl ON  e.person_uuid = vl.person_uuid\n" +
                "LEFT JOIN\n" +
                "(\n" +
                "select DISTINCT ON (ha.person_uuid) ha.person_uuid, ha.status,   ha.last_viral_load, ha.date_of_last_viral_load, MAX(has.eac_session_date) as visit_date from hiv_eac ha\n" +
                "LEFT JOIN hiv_eac_session has ON ha.visit_id = has.visit_id AND ha.person_uuid = has.person_uuid\n" +
                "WHERE ha.status = 'COMPLETED'\n" +
                "GROUP BY ha.person_uuid,  ha.status, ha.last_viral_load, ha.date_of_last_viral_load, has.eac_session_date\n" +
                "ORDER BY ha.person_uuid, has.eac_session_date\n" +
                ") eac1 ON e.person_uuid = eac1.person_uuid\n" +
                "LEFT JOIN \n" +
                "( select DISTINCT ON (person_uuid) person_uuid, MIN(eac_session_date) AS eac_commenced from hiv_eac_session\n" +
                " GROUP BY person_uuid, eac_session_date ORDER BY  person_uuid, eac_session_date ASC ) eac ON e.person_uuid = eac.person_uuid \n" +
                "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
                "  LEFT JOIN hiv_eac ha ON e.person_uuid = ha.person_uuid\n" +
                "  WHERE p.archived=0 AND p.facility_id= ?1 \n" +
                "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, p.facility_id, \n" +
                "  pharm.visit_date, ha.status, vl.clastViralLoad, eac.eac_commenced, vl.dateSampleCollected,\n" +
                "  eac1.status, eac1.last_viral_load, eac1.date_of_last_viral_load, eac1.visit_date\n" +
                "  ORDER BY p.id DESC )\n" +
                "  SELECT \n" +
                "  COUNT(hadeac) AS eacCommencedNumerator,\n" +
                "  COUNT(sixmVl) AS eacCommencedDenominator,\n" +
                "  COUNT(sixmVl) - COUNT(hadeac) AS eacCommencedVariance,\n" +
                "  ROUND((CAST(COUNT(hadeac) AS DECIMAL) / COUNT(sixmVl)) * 100, 2) AS eacCommencedPerformance,\n" +
                "  COUNT(compleacdate) AS eacComDateNumerator,\n" +
                "  COUNT(eac_completion) AS eacComDateDenominator,\n" +
                "  COUNT(eac_completion) - COUNT(compleacdate) AS eacComDateVariance,\n" +
                "  ROUND((CAST(COUNT(compleacdate) AS DECIMAL) / COUNT(eac_completion)) * 100, 2) AS eacComDatePerformance,\n" +
                "  COUNT(postEac) AS postEacNumerator,\n" +
                "  COUNT(cmpleac) AS postEacDenominator,\n" +
                "  COUNT(cmpleac) - COUNT(postEac) AS postEacVariance,\n" +
                "  ROUND((CAST(COUNT(postEac) AS DECIMAL) / COUNT(cmpleac)) * 100, 2) AS postEacPerformance\n" +
                "  FROM\n" +
                "  eacSummary";


        public static final String HTS_SUMMARY_QUERIES = "WITH htsSummary AS (\n" +
                "SELECT person_uuid, client_code, date_visit, target_group, hts_client_uuid, hasindex, testing_setting, gender,date_of_birth, age, hiv_test_result, poscount, adultPos,\n" +
                " (CASE WHEN adultPos =1 AND recency IS NOT NULL  THEN 1 ELSE NULL END) AS adPosRec,\n" +
                " rita, order_date,\n" +
                "(CASE WHEN rita ILIKE '%Recent%' ESCAPE ' ' THEN 1 ELSE NULL END) AS recentInfection,\n" +
                "(CASE WHEN (rita ILIKE '%Recent%' ESCAPE ' '  AND order_date IS NOT NULL) THEN 1 ELSE NULL END) AS recentwitVL,\n" +
                "(CASE WHEN (rita ILIKE '%Recent%' ESCAPE ' '  AND result_reported IS NOT NULL AND order_date IS NOT NULL) THEN 1 ELSE NULL END) AS recentwitVlResl,\n" +
                "(CASE WHEN resultdate > order_date THEN 1 ELSE NULL END) AS rsGreaterThan, resultdate, recency, recencydate,\n" +
                "(CASE WHEN (recencydate >= date_visit) AND recencydate IS NOT NULL AND date_visit IS NOT NULL THEN 1 ELSE NULL END) AS dateconfirm,\n" +
                "(CASE WHEN testing_setting IS NOT NULL AND hasindex IS NOT NULL  THEN 1 ELSE NULL END) AS settings\n" +
                "FROM \n" +
                "(\n" +
                "select DISTINCT ON (person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  hc.target_group,  hie.hts_client_uuid, (CASE WHEN hc.index_client = 'true' THEN 1 ELSE null END) hasIndex,\n" +
                "hc.testing_setting,\n" +
                "CAST((hc.extra->>'gender') AS VARCHAR(100)) AS gender, CAST((hc.extra->>'date_of_birth') AS DATE) AS date_of_birth, \n" +
                "CAST(EXTRACT(YEAR FROM AGE(NOW(), CAST(hc.extra->>'date_of_birth' AS DATE))) AS INTEGER) AS age\n" +
                ", hc.hiv_test_result,\n" +
                "(CASE WHEN hc.hiv_test_result = 'Positive' THEN 1 ELSE null END) AS posCount,\n" +
                "(CASE WHEN CAST(EXTRACT(YEAR FROM AGE(NOW(), CAST(hc.extra->>'date_of_birth' AS DATE))) AS INTEGER) > 15 THEN 1 ELSE null END) AS adultPos,\n" +
                "recency->>'rencencyId' As recency,\n" +
                "recency->>'rencencyInterpretation' AS RIta,CAST(lo.order_date AS DATE),\n" +
                "lr.result_reported, CAST(lr.date_result_reported AS DATE) AS resultdate, (CASE   WHEN recency->>'optOutRTRITestDate' IS NOT NULL   AND recency->>'optOutRTRITestDate' <> '' \n" +
                "  THEN CAST(recency->>'optOutRTRITestDate' AS DATE) \n" +
                "  ELSE NULL \n" +
                "END) AS recencydate\n" +
                "from hts_client hc\n" +
                "LEFT JOIN hts_index_elicitation hie on hc.uuid = hie.hts_client_uuid\n" +
                "LEFT JOIN laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
                "LEFT JOIN laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
                "where hc.facility_id = ?1 ) pro\n" +
                ")\n" +
                "SELECT \n" +
                "  COUNT(adPosRec) AS totalPosNumerator,\n" +
                "  COUNT(adultpos) AS totalPosDenominator,\n" +
                "  COUNT(adultpos) - COUNT(adPosRec) AS totalPosVariance,\n" +
                "--   ROUND((CAST(COUNT(prep_offered) AS DECIMAL) / COUNT(person_uuid)) * 100, 2) AS pOfferredPerformance,\n" +
                "  COUNT(recentwitVL) AS withVLNumerator,\n" +
                "  COUNT(recentInfection) AS withVLDenominator,\n" +
                "  COUNT(recentInfection) - COUNT(recentwitVL) AS withVLVariance, \n" +
                "  ROUND((CAST(COUNT(recentwitVL) AS DECIMAL) / COUNT(recentInfection)) * 100, 2) AS withVLPerformance,\n" +
                "  COUNT(recentwitVlResl) AS withVlResNumerator,\n" +
                "  COUNT(recentwitVL) AS withVlResDenominator,\n" +
                "  COUNT(recentwitVL) - COUNT(recentwitVlResl) AS withVlResVariance,\n" +
                "  ROUND((CAST(COUNT(recentwitVlResl) AS DECIMAL) / COUNT(recentwitVL)) * 100, 2) AS withVlResPerformance,\n" +
                "  COUNT(rsGreaterThan) AS rsGreaterNumerator,\n" +
                "  COUNT(resultdate) AS rsGreaterDenominator,\n" +
                "  COUNT(resultdate) - COUNT(rsGreaterThan) AS rsGreaterVariance,\n" +
                "  ROUND((CAST(COUNT(rsGreaterThan) AS DECIMAL) / COUNT(resultdate)) * 100, 2) AS rsGreaterPerformance,\n" +
                "  COUNT(dateconfirm) AS recencyNumerator,\n" +
                "  COUNT(recency) AS recencyDenominator,\n" +
                "  COUNT(recency) - COUNT(dateconfirm) AS recencyVariance,\n" +
                "  ROUND((CAST(COUNT(dateconfirm) AS DECIMAL) / COUNT(recency)) * 100, 2) AS recencyPerformance,\n" +
                "  COUNT(hts_client_uuid) AS elicitedNumerator,\n" +
                "  COUNT(hasindex) AS elicitedDenominator,\n" +
                "  COUNT(hasindex) - COUNT(hts_client_uuid) AS elicitedVariance,\n" +
                "  ROUND((CAST(COUNT(dateconfirm) AS DECIMAL) / COUNT(hasindex)) * 100, 2) AS elicitedPerformance,\n" +
                "  COUNT(settings) AS settingsNumerator,\n" +
                "  COUNT(hasindex) AS settingsDenominator,\n" +
                "  COUNT(hasindex) - COUNT(settings) AS settingsVariance,\n" +
                "  ROUND((CAST(COUNT(settings) AS DECIMAL) / COUNT(hasindex)) * 100, 2) AS settingsPerformance,\n" +
                "  COUNT(target_group) AS targNumerator,\n" +
                "  COUNT(person_uuid) AS targDenominator,\n" +
                "  COUNT(person_uuid) - COUNT(target_group) AS targVariance,\n" +
                "  ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(person_uuid)) * 100, 2) AS targPerformance\n" +
                "  FROM\n" +
                "  htsSummary";


        public static final String PREP_SUMMARY_QUERIES = "WITH prepSummary AS (\n" +
                "SELECT DISTINCT ON (person_uuid) hc.person_uuid, hc.client_code, hc.date_visit,  (CASE WHEN hc.prep_offered = true  THEN 1 ELSE null END) AS prep_offered, \n" +
                "hc.prep_accepted, hc.prep_offered = hc.prep_accepted AS offAndAccpt,\n" +
                " hc.hiv_test_result, pe.status, pe.unique_id, pe.date_enrolled, pe.date_started, \n" +
                "(CASE WHEN pe.date_enrolled IS NULL  THEN pe.date_started ELSE pe.date_enrolled END) AS date_enrolled_prep,\n" +
                "pc. urinalysis, CAST((pc. urinalysis->>'testDate') AS DATE) AS UrinalysisDate,\n" +
                "(CASE WHEN CAST((pc. urinalysis->>'testDate') AS DATE) > (CASE WHEN pe.date_enrolled IS NULL  THEN pe.date_started ELSE pe.date_enrolled END)\n" +
                " THEN 1 ELSE NULL END) AS urinaGreaterthanenrollDate,\n" +
                "hc.date_visit AS status_date,\n" +
                "(CASE WHEN CAST((pc. urinalysis->>'testDate') AS DATE) > hc.date_visit\n" +
                " THEN 1 ELSE NULL END) AS urinaGreaterthanStatusDate,\n" +
                "iscommenced.encounter_date,\n" +
                "(CASE WHEN (CASE WHEN pe.date_enrolled IS NULL  THEN pe.date_started ELSE pe.date_enrolled END) < iscommenced.encounter_date\n" +
                " THEN 1 ELSE NULL END) AS enrollDateLessThanCommenced\n" +
                "FROM\n" +
                "hts_client hc  LEFT JOIN prep_enrollment pe ON hc.person_uuid = pe.person_uuid\n" +
                "LEFT JOIN prep_clinic pc ON hc.person_uuid = pc.person_uuid\n" +
                "LEFT JOIN \n" +
                "(\n" +
                "select DISTINCT ON (person_uuid) pc.person_uuid, pc.is_commencement, pc.encounter_date from prep_enrollment pe JOIN prep_clinic pc on pe.uuid = pc.prep_enrollment_uuid\n" +
                "where is_commencement = true\n" +
                ") iscommenced ON pe.person_uuid = iscommenced.person_uuid\n" +
                "where hc.hiv_test_result is not null AND hc.hiv_test_result = 'Negative' AND pe.facility_id = ?1\n" +
                ")\n" +
                "SELECT \n" +
                "  COUNT(prep_offered) AS pOfferredNumerator,\n" +
                "  COUNT(person_uuid) AS pOfferedDenominator,\n" +
                "  COUNT(person_uuid) - COUNT(prep_offered) AS pOfferedVariance,\n" +
                "  ROUND((CAST(COUNT(prep_offered) AS DECIMAL) / COUNT(person_uuid)) * 100, 2) AS pOfferredPerformance,\n" +
                "  COUNT(prep_accepted) AS pAcceptedNumerator,\n" +
                "  COUNT(prep_offered) AS pAcceptedDenominator,\n" +
                "  COUNT(prep_offered) - COUNT(prep_accepted) AS pAcceptedVariance,\n" +
                "  ROUND((CAST(COUNT(prep_offered) AS DECIMAL) / COUNT(prep_offered)) * 100, 2) AS pAcceptedPerformance,\n" +
                "  COUNT(date_enrolled_prep) AS pEnrollNumerator,\n" +
                "  COUNT(offAndAccpt) AS pEnrollDenominator,\n" +
                "  COUNT(offAndAccpt) - COUNT(date_enrolled_prep) AS pEnrollVariance,\n" +
                "  ROUND((CAST(COUNT(date_enrolled_prep) AS DECIMAL) / COUNT(offAndAccpt)) * 100, 2) AS pEnrollPerformance,\n" +
                "  COUNT(urinalysis) AS pEnrolledPrepUrinaNumerator,\n" +
                "  COUNT(date_enrolled_prep) AS pEnrolledPrepUrinaDenominator,\n" +
                "  COUNT(date_enrolled_prep) - COUNT(urinalysis) AS pEnrolledPrepUrinaVariance,\n" +
                "  ROUND((CAST(COUNT(urinalysis) AS DECIMAL) / COUNT(date_enrolled_prep)) * 100, 2) AS pEnrolledPrepUrinaPerformance,\n" +
                "  COUNT(urinaGreaterthanenrollDate) AS pUrinaGreaterEnrollNumerator,\n" +
                "  COUNT(urinalysis) AS pUrinaGreaterEnrollDenominator,\n" +
                "  COUNT(urinalysis) - COUNT(urinaGreaterthanenrollDate) AS pUrinaGreaterEnrollVariance,\n" +
                "  ROUND((CAST(COUNT(urinaGreaterthanenrollDate) AS DECIMAL) / COUNT(urinalysis)) * 100, 2) AS pUrinaGreaterEnrollPerformance,\n" +
                "  COUNT(urinaGreaterthanStatusDate) AS pUrinaGreaterStatusDateNumerator,\n" +
                "  COUNT(urinalysis) AS pUrinaGreaterStatusDateDenominator,\n" +
                "  COUNT(urinalysis) - COUNT(urinaGreaterthanStatusDate) AS pUrinaGreaterStatusDateVariance,\n" +
                "  ROUND((CAST(COUNT(urinaGreaterthanStatusDate) AS DECIMAL) / COUNT(urinalysis)) * 100, 2) AS pUrinaGreaterStatusDatePerformance,\n" +
                "  COUNT(enrollDateLessThanCommenced) AS commencedNumerator,\n" +
                "  COUNT(encounter_date) AS commencedDenominator,\n" +
                "  COUNT(encounter_date) - COUNT(enrollDateLessThanCommenced) AS commencedVariance,\n" +
                "  ROUND((CAST(COUNT(enrollDateLessThanCommenced) AS DECIMAL) / COUNT(encounter_date)) * 100, 2) AS commencedPerformance\n" +
                "FROM\n" +
                "prepSummary";


    }

    public static class ClinicalVariables {

        public static final String CLINICAL_VARIABLE_SUMMARY_QUERIES = "WITH PatientClinic AS (\n" +
                " SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,p.date_of_birth AS dateOfBirth,\n" +
                "pharm.refill_period as refillMonth, pharm.visit_date AS drug_visit_date, pharm.regimen, e.date_started as start_date,\n" +
                "e.date_confirmed_hiv AS hiv_confirm_date, e.target_group_id as target_group, e.entry_point_id AS entryPoint,\n" +
                "ca.visit_date as commence_date,  e.time_hiv_diagnosis as hivDiagnose, CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
                "preg.pregnancy_status as pregStatus, tri.body_weight as weight, tri.visit_date AS visit_date,\n" +
                "CASE WHEN preg.pregnancy_status IS NOT NULL AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 AND INITCAP(p.sex) = 'Female' THEN 1 ELSE NULL END AS adultPre,\n" +
                "CASE WHEN CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 AND INITCAP(p.sex) = 'Female' THEN 1 ELSE NULL END AS adultAge\n" +
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
                "     person_uuid DESC ) pharm ON pharm.person_uuid = e.person_uuid\n" +
                "LEFT JOIN\n" +
                "  (SELECT DISTINCT ON (person_uuid)\n" +
                "    person_uuid, visit_date, body_weight\n" +
                "FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
                "    FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid\n" +
                "    GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
                "    person_uuid DESC ) tri ON tri.person_uuid = e.person_uuid\n" +
                "LEFT JOIN (\n" +
                "select DISTINCT ON (h1.person_uuid) h1.person_uuid, MAX(h1.visit_date) AS visit_date,\n" +
                "h1.pregnancy_status\n" +
                "from hiv_art_clinical h1\n" +
                "GROUP BY h1.person_uuid, h1.visit_date, h1.pregnancy_status\n" +
                "ORDER BY h1.person_uuid, h1.visit_date DESC \n" +
                ") preg ON e.person_uuid = preg.person_uuid \n" +
                "   LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
                "   WHERE p.archived=0 AND p.facility_id= ?1\n" +
                "   GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, \n" +
                "pharm.refill_period, p.facility_id, pharm.visit_date, e.date_started, e.date_confirmed_hiv, e.target_group_id,\n" +
                "e.time_hiv_diagnosis,preg.pregnancy_status, tri.body_weight, tri.visit_date, pharm.regimen\n" +
                "   ORDER BY p.id DESC\n" +
                ")\n" +
                "      SELECT\n" +
                "    COUNT(refillMonth) AS refillMonthNumerator,\n" +
                "    COUNT(hospitalNumber) AS refillMonthDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(refillMonth) AS refillMonthVariance,\n" +
                "    ROUND((CAST(COUNT(refillMonth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS refillMonthPerformance,\n" +
                "\tCOUNT(regimen) AS regimenNumerator,\n" +
                "    COUNT(hospitalNumber) AS regimenDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(regimen) AS regimenVariance,\n" +
                "    ROUND((CAST(COUNT(regimen) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS regimenPerformance,\n" +
                "\tCOUNT(start_date) AS startDateNumerator,\n" +
                "    COUNT(hospitalNumber) AS startDateDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(start_date) AS startDateVariance,\n" +
                "    ROUND((CAST(COUNT(start_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS startDatePerformance,\n" +
                "\tCOUNT(hiv_confirm_date) AS confirmDateNumerator,\n" +
                "    COUNT(hospitalNumber) AS confirmDateDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(hiv_confirm_date) AS confirmDateVariance,\n" +
                "    ROUND((CAST(COUNT(hiv_confirm_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS confirmDatePerformance,\n" +
                "\tCOUNT(target_group) AS targNumerator,\n" +
                "    COUNT(hospitalNumber) AS targDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(target_group) AS targVariance, \n" +
                "    ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS targPerformance,\n" +
                "\tCOUNT(entryPoint) AS entryNumerator,\n" +
                "    COUNT(hospitalNumber) AS entryDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(entryPoint) AS entryVariance,\n" +
                "    ROUND((CAST(COUNT(entryPoint) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS entryPerformance,\n" +
                "\tCOUNT(commence_date) AS commencedNumerator,\n" +
                "    COUNT(hospitalNumber) AS commencedDenominator,\n" +
                "   COUNT(hospitalNumber) - COUNT(commence_date) AS commencedVariance, " +
                "    ROUND((CAST(COUNT(commence_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS commencedPerformance,\n" +
                "\tCOUNT(start_date) AS enrolledDateNumerator,\n" +
                "    COUNT(hospitalNumber) AS enrolledDateDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(start_date) AS enrolledDateVariance,\n" +
                "    ROUND((CAST(COUNT(start_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS enrolledDatePerformance,\n" +
                "\tCOUNT(hivDiagnose) AS diagnoseNumerator,\n" +
                "    COUNT(hospitalNumber) AS diagnoseDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(hivDiagnose) AS diagnoseVariance,\n" +
                "    ROUND((CAST(COUNT(hivDiagnose) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS diagnosePerformance,\n" +
                "\tCOUNT(adultPre) AS pregNumerator,\n" +
                "    COUNT(adultAge) AS pregDenominator,\n" +
                "\tCOUNT(adultAge) - COUNT(adultPre) AS pregVariance,\n" +
                "    ROUND((CAST(COUNT(adultPre) AS DECIMAL) / COUNT(adultAge)) * 100, 2) AS pregPerformance,\n" +
                "\tCOUNT(weight) AS weightNumerator,\n" +
                "    COUNT(hospitalNumber) AS weightDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(weight) AS weightVariance,\n" +
                "    ROUND((CAST(COUNT(weight) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS weightPerformance,\n" +
                "\tCOUNT(visit_date) AS lastVisitNumerator,\n" +
                "    COUNT(hospitalNumber) AS lastVisitDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(visit_date) AS lastVisitVariance,\n" +
                "    ROUND((CAST(COUNT(visit_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lastVisitPerformance,\n" +
                "\tCOUNT(age) AS ageNumerator,\n" +
                "    COUNT(hospitalNumber) AS ageDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(age) AS ageVariance,\n" +
                "    ROUND((CAST(COUNT(age) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS agePerformance,\n" +
                "\tCOUNT(drug_visit_date) AS lastPickNumerator,\n" +
                "    COUNT(hospitalNumber) AS lastPickDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(drug_visit_date) AS lastPickVariance,\n" +
                "    ROUND((CAST(COUNT(drug_visit_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lastPickPerformance\n" +
                "   \tFROM\n" +
                "   \tPatientClinic";
    }

    public static class PatientDemographyQueries {
        public static final String DEMOGRAPHIC_SUMMARY_QUERY = "WITH PatientSummary AS (\n" +
                "    SELECT \n" +
                "        e.unique_id AS patientId, \n" +
                "        p.hospital_number AS hospitalNumber,\n" +
                "        INITCAP(p.sex) AS sex, \n" +
                "        CAST(EXTRACT(YEAR FROM AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
                "        p.date_of_birth AS dateOfBirth, \n" +
                "        p.marital_status, \n" +
                "        p.education,\n" +
                "        p.employment_status AS employment,\n" +
                "        p.address, \n" +
                "\t    st.status\n" +
                "    FROM \n" +
                "        patient_person p \n" +
                "    INNER JOIN \n" +
                "        hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "    LEFT JOIN \n" +
                "        (SELECT TRUE as commenced, hac.person_uuid \n" +
                "         FROM hiv_art_clinical hac\n" +
                "         WHERE hac.archived = 0 AND hac.is_commencement IS TRUE \n" +
                "         GROUP BY hac.person_uuid\n" +
                "        ) ca ON p.uuid = ca.person_uuid\n" +
                "    LEFT JOIN \n" +
                "        (\n" +
                "            SELECT \n" +
                "                personUuid, \n" +
                "                status \n" +
                "            FROM \n" +
                "                (\n" +
                "                    SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                        pharmacy.person_uuid AS personUuid,\n" +
                "                        (\n" +
                "                            CASE\n" +
                "                                WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                                WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                                ELSE pharmacy.status\n" +
                "                            END\n" +
                "                        ) AS status,\n" +
                "                        stat.cause_of_death, \n" +
                "                        stat.va_cause_of_death\n" +
                "                    FROM \n" +
                "                        (\n" +
                "                            SELECT\n" +
                "                                (\n" +
                "                                    CASE\n" +
                "                                        WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                        ELSE 'Active'\n" +
                "                                    END\n" +
                "                                ) status,\n" +
                "                                (\n" +
                "                                    CASE\n" +
                "                                        WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                        ELSE hp.visit_date\n" +
                "                                    END\n" +
                "                                ) AS visit_date,\n" +
                "                                hp.person_uuid, \n" +
                "                                MAXDATE\n" +
                "                            FROM \n" +
                "                                hiv_art_pharmacy hp\n" +
                "                            INNER JOIN \n" +
                "                                (\n" +
                "                                    SELECT \n" +
                "                                        hap.person_uuid, \n" +
                "                                        hap.visit_date AS MAXDATE, \n" +
                "                                        ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                    FROM \n" +
                "                                        public.hiv_art_pharmacy hap \n" +
                "                                    INNER JOIN \n" +
                "                                        public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                    INNER JOIN \n" +
                "                                        hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                    INNER JOIN \n" +
                "                                        public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                    INNER JOIN \n" +
                "                                        public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                    WHERE \n" +
                "                                        r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                        AND hap.archived = 0                \n" +
                "                                ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                            WHERE\n" +
                "                                hp.archived = 0\n" +
                "                        ) pharmacy\n" +
                "                    LEFT JOIN \n" +
                "                        (\n" +
                "                            SELECT \n" +
                "                                hst.hiv_status,\n" +
                "                                hst.person_id,\n" +
                "                                hst.status_date,\n" +
                "                                hst.cause_of_death,\n" +
                "                                hst.va_cause_of_death\n" +
                "                            FROM \n" +
                "                                (\n" +
                "                                    SELECT * FROM \n" +
                "                                        (\n" +
                "                                            SELECT \n" +
                "                                                DISTINCT (person_id) person_id, \n" +
                "                                                status_date, \n" +
                "                                                cause_of_death, \n" +
                "                                                va_cause_of_death,\n" +
                "                                                hiv_status, \n" +
                "                                                ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                            FROM \n" +
                "                                                hiv_status_tracker \n" +
                "                                            WHERE \n" +
                "                                                archived = 0 \n" +
                "                                        ) s\n" +
                "                                    WHERE \n" +
                "                                        s.row_number = 1\n" +
                "                                ) hst\n" +
                "                            INNER JOIN \n" +
                "                                hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                        ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "                ) st\n" +
                "        ) st ON st.personUuid = e.person_uuid\n" +
                "    LEFT JOIN \n" +
                "        base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
                "    WHERE \n" +
                "        p.archived = 0 AND p.facility_id = ?1 AND st.status = 'Active'\n" +
                "    GROUP BY \n" +
                "        e.id, \n" +
                "        ca.commenced, \n" +
                "        p.id, \n" +
                "        pc.display, \n" +
                "        p.hospital_number, \n" +
                "        p.date_of_birth,\n" +
                "\t    st.status\n" +
                "    ORDER BY \n" +
                "        p.id DESC\n" +
                ")\n" +
                "SELECT\n" +
                "    COUNT(age) AS ageNumerator,\n" +
                "    COUNT(hospitalNumber) AS ageDenominator,\n" +
                "    ROUND((CAST(COUNT(age) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS agePerformance,\n" +
                "    COUNT(sex) AS sexNumerator,\n" +
                "    COUNT(hospitalNumber) AS sexDenominator,\n" +
                "    ROUND((CAST(COUNT(sex) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS sexPerformance,\n" +
                "    COUNT(dateOfBirth) AS dobNumerator,\n" +
                "    COUNT(hospitalNumber) AS dobDenominator,\n" +
                "    ROUND((CAST(COUNT(dateOfBirth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS dobPerformance,\n" +
                "    COUNT(marital_status) AS maritalNumerator,\n" +
                "    COUNT(hospitalNumber) AS maritalDenominator,\n" +
                "    ROUND((CAST(COUNT(marital_status) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS maritalPerformance,\n" +
                "    COUNT(education) AS eduNumerator,\n" +
                "    COUNT(hospitalNumber) AS eduDenominator,\n" +
                "    ROUND((CAST(COUNT(education) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS eduPerformance,\n" +
                "    COUNT(employment) AS employNumerator,\n" +
                "    COUNT(hospitalNumber) AS employDenominator,\n" +
                "    ROUND((CAST(COUNT(employment) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS employPerformance,\n" +
                "    COUNT(address) AS addressNumerator,\n" +
                "    COUNT(hospitalNumber) AS addressDenominator,\n" +
                "    ROUND((CAST(COUNT(address) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS addressPerformance,\n" +
                "    COUNT(patientId) AS pIdNumerator,\n" +
                "    COUNT(hospitalNumber) AS pIdDenominator,\n" +
                "    ROUND((CAST(COUNT(patientId) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS pIdPerformance\n" +
                "FROM\n" +
                "    PatientSummary;\n";

    }

    public static class BiometricQueries {

        public static final String BIOMETRIC_SUMMARY_QUERIES = "WITH PatientBiometrics AS (\n" +
                "    SELECT  \n" +
                "        e.unique_id AS patientId,\n" +
                "        p.hospital_number AS hospitalNumber,\n" +
                "        INITCAP(p.sex) AS sex,\n" +
                "        p.date_of_birth AS dateOfBirth,\n" +
                "        b.person_uuid AS person_uuid1,\n" +
                "        b.biometric_valid_captured AS validcapture,\n" +
                "        b.person_uuid AS personId,\n" +
                "        bb.recapture,\n" +
                "        bb.person_uuid,\n" +
                "        bb.biometric_valid_captured AS validrecap,\n" +
                "\t    st.status\n" +
                "    FROM \n" +
                "        patient_person p\n" +
                "    INNER JOIN \n" +
                "        hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  \n" +
                "        FROM hiv_art_clinical hac \n" +
                "        WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "        GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status\n" +
                "    ) ca ON p.uuid = ca.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (person_uuid) \n" +
                "            person_uuid,\n" +
                "            COUNT(biometric_type) AS biometric_fingers_captured,\n" +
                "            COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured,\n" +
                "            recapture \n" +
                "        FROM biometric\n" +
                "        WHERE archived != 1 \n" +
                "        GROUP BY person_uuid, recapture\n" +
                "    ) b ON e.person_uuid = b.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (person_uuid) \n" +
                "            person_uuid,\n" +
                "            COUNT(biometric_type) AS biometric_fingers_captured,\n" +
                "            COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured,\n" +
                "            recapture \n" +
                "        FROM biometric\n" +
                "        WHERE archived != 1 AND recapture != 0 \n" +
                "        GROUP BY person_uuid, recapture\n" +
                "    ) bb ON e.person_uuid = bb.person_uuid\n" +
                "\tLEFT JOIN (\n" +
                "\tSELECT personUuid, status FROM (\n" +
                "\tSELECT\n" +
                " DISTINCT ON (pharmacy.person_uuid) pharmacy.person_uuid AS personUuid,\n" +
                "(\n" +
                "    CASE\n" +
                "        WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "        WHEN(\n" +
                "        stat.status_date > pharmacy.maxdate\n" +
                "    AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %' )\n" +
                ")THEN stat.hiv_status\n" +
                "        ELSE pharmacy.status\n" +
                "        END\n" +
                "    ) AS status,\n" +
                "\n" +
                "stat.cause_of_death, stat.va_cause_of_death\n" +
                "\n" +
                "         FROM\n" +
                " (\n" +
                "     SELECT\n" +
                "         (\n" +
                " CASE\n" +
                "     WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "     ELSE 'Active'\n" +
                "     END\n" +
                " ) status,\n" +
                "         (\n" +
                " CASE\n" +
                "     WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "     ELSE hp.visit_date\n" +
                "     END\n" +
                " ) AS visit_date,\n" +
                "         hp.person_uuid, MAXDATE\n" +
                "     FROM\n" +
                "         hiv_art_pharmacy hp\n" +
                " INNER JOIN (\n" +
                "         SELECT hap.person_uuid, hap.visit_date AS  MAXDATE, ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "           FROM public.hiv_art_pharmacy hap \n" +
                "                    INNER JOIN public.hiv_art_pharmacy_regimens pr \n" +
                "                    ON pr.art_pharmacy_id = hap.id \n" +
                "            INNER JOIN hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "            INNER JOIN public.hiv_regimen r on r.id = pr.regimens_id \n" +
                "            INNER JOIN public.hiv_regimen_type rt on rt.id = r.regimen_type_id \n" +
                "            WHERE r.regimen_type_id in (1,2,3,4,14) \n" +
                "            AND hap.archived = 0                \n" +
                "             ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid \n" +
                "      AND MAX.rnkkk3 = 1\n" +
                "     WHERE\n" +
                " hp.archived = 0\n" +
                " ) pharmacy\n" +
                "\n" +
                "     LEFT JOIN (\n" +
                "     SELECT\n" +
                "         hst.hiv_status,\n" +
                "         hst.person_id,\n" +
                "\t\t hst.status_date,\n" +
                "\t\t hst.cause_of_death,\n" +
                "\t\t hst.va_cause_of_death\n" +
                "     FROM\n" +
                "         (\n" +
                " SELECT * FROM (SELECT DISTINCT (person_id) person_id, status_date, cause_of_death,va_cause_of_death,\n" +
                "        hiv_status, ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "    FROM hiv_status_tracker WHERE archived=0 )s\n" +
                " WHERE s.row_number=1\n" +
                "         ) hst\n" +
                " INNER JOIN hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                " ) stat ON stat.person_id = pharmacy.person_uuid --AND pharmacy.status = 'Active' \n" +
                ") --st where status = 'Active'\n" +
                "\t)st ON st.personUuid = e.person_uuid\n" +
                "    LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
                "    WHERE \n" +
                "        p.archived=0 \n" +
                "        AND p.facility_id = ?1 \n" +
                "\t    AND st.status = 'Active'\n" +
                "        AND CAST(EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
                "    GROUP BY \n" +
                "        e.id, \n" +
                "        ca.commenced, \n" +
                "        p.id, \n" +
                "        pc.display, \n" +
                "        p.hospital_number, \n" +
                "        p.date_of_birth, \n" +
                "        ca.visit_date, \n" +
                "        ca.pregnancy_status, \n" +
                "        b.biometric_fingers_captured, \n" +
                "        b.biometric_valid_captured,\n" +
                "        bb.biometric_valid_captured, \n" +
                "        b.person_uuid,\n" +
                "        bb.recapture, \n" +
                "        bb.person_uuid,\n" +
                "\t    st.status\n" +
                "    ORDER BY \n" +
                "        p.id DESC\n" +
                ")\n" +
                "SELECT\n" +
                "    COUNT(person_uuid1) AS captureNumerator,\n" +
                "    COUNT(hospitalNumber) AS captureDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(person_uuid1) AS captureVariance,\n" +
                "    ROUND((CAST(COUNT(person_uuid1) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS capturePerformance,\n" +
                "    COUNT(validcapture) AS validcapNumerator,\n" +
                "    COUNT(person_uuid1) AS validcapDenominator,\n" +
                "    COUNT(person_uuid1) - COUNT(validcapture) AS validcapVariance,\n" +
                "    ROUND((CAST(COUNT(validcapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS validcapPerformance,\n" +
                "    COUNT(recapture) AS recapNumerator,\n" +
                "    COUNT(person_uuid1) AS recapDenominator,\n" +
                "    COUNT(person_uuid1) - COUNT(recapture) AS recapVariance,\n" +
                "    ROUND((CAST(COUNT(recapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS recapPerformance,\n" +
                "    COUNT(validrecap) AS validRecapNumerator,\n" +
                "    COUNT(recapture) AS validRecapDenominator,\n" +
                "    COUNT(recapture) - COUNT(validrecap) AS validRecapVariance,\n" +
                "    ROUND((CAST(COUNT(validrecap) AS DECIMAL) / COUNT(recapture)) * 100, 2) AS validRecapPerformance\n" +
                "FROM \n" +
                "    PatientBiometrics;\n";

        public static final String GET_PATIENTS_NOT_CAPTURE = "";
    }

    public static class ClientVerificationQueries {

        public static final String CLIENT_VERIFICATION = "WITH clientVerification AS (\n" +
                "    SELECT  \n" +
                "        e.unique_id AS patientId,\n" +
                "        p.hospital_number AS hospitalNumber,\n" +
                "        INITCAP(p.sex) AS sex,\n" +
                "        p.date_of_birth AS dateOfBirth,\n" +
                "        b.person_uuid AS person_uuid1,\n" +
                "        b.enrollment_date,\n" +
                "        b.biometric_valid_captured AS validcapture,\n" +
                "        recap.person_uuid AS personUuid,\n" +
                "        recap.enrollment_date,\n" +
                "        CASE WHEN (lastVisit.visit_date > recap.enrollment_date) AND b.enrollment_date IS NOT NULL THEN 1 ELSE NULL END AS clinicNoRecapture,\n" +
                "        recap.biometric_valid_captured AS recapture,\n" +
                "        lastVisit.visit_date AS lastClinicVisit,\n" +
                "        lastVisit.monthTillDate,\n" +
                "        CASE WHEN lastVisit.monthTillDate >= 15 THEN 1 ELSE NULL END AS lastClinicMonth,\n" +
                "        CASE WHEN pickUp.monthApart >= 12 THEN 1 ELSE NULL END AS pickUpOneYear,\n" +
                "        CASE WHEN b.person_uuid IS NULL THEN 1 ELSE NULL END AS noBaseline,\n" +
                "        CASE WHEN b.person_uuid IS NOT NULL AND recap.person_uuid IS NULL THEN 1 ELSE NULL END AS hasBaseLineNoRecapture,\n" +
                "        sameDemographics.uuid AS sameDemographic,\n" +
                "        sameClinical.person_uuid AS dupClinical,\n" +
                "        incompleteEncounter.person_uuid AS incomplete,\n" +
                "        sampleCol.dateOfViralLoadSampleCollection,\n" +
                "        ca.visit_date,\n" +
                "        CASE WHEN CAST(DATE_PART('year', AGE(NOW(), ca.visit_date)) * 12 + DATE_PART('month', AGE(NOW(), ca.visit_date)) AS INTEGER ) >= 6 AND sampleCol.dateOfViralLoadSampleCollection IS NULL THEN 1 ELSE NULL END AS vlPrior,\n" +
                "\t\tst.status\n" +
                "    FROM \n" +
                "        patient_person p \n" +
                "    INNER JOIN \n" +
                "        hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "    INNER JOIN (\n" +
                "        SELECT TRUE as commenced, hac.person_uuid, hac.visit_date \n" +
                "        FROM hiv_art_clinical hac \n" +
                "        WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "        GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status\n" +
                "    ) ca ON p.uuid = ca.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (person_uuid) \n" +
                "            person_uuid, \n" +
                "            COUNT(biometric_type) AS biometric_fingers_captured, \n" +
                "            enrollment_date, \n" +
                "            COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, \n" +
                "            recapture \n" +
                "        FROM biometric\n" +
                "        WHERE archived = 0 AND recapture = 0  \n" +
                "        GROUP BY person_uuid, recapture, enrollment_date\n" +
                "    ) b ON b.person_uuid = ca.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (person_uuid) \n" +
                "            person_uuid, \n" +
                "            COUNT(biometric_type) AS biometric_fingers_captured, \n" +
                "            enrollment_date, \n" +
                "            COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, \n" +
                "            recapture \n" +
                "        FROM biometric\n" +
                "        WHERE archived = 0 AND recapture != 0  \n" +
                "        GROUP BY person_uuid, recapture, enrollment_date\n" +
                "    ) recap ON recap.person_uuid = ca.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT\n" +
                "            DISTINCT ON (p1.HOSPITAL_NUMBER)\n" +
                "            p1.HOSPITAL_NUMBER,\n" +
                "            INITCAP(p1.sex) AS sex,\n" +
                "            p1.date_of_birth,\n" +
                "            p1.uuid,\n" +
                "            ca1.person_uuid,\n" +
                "            ca1.visit_date,\n" +
                "            p2.HOSPITAL_NUMBER AS matching_hospital_number,\n" +
                "            INITCAP(p2.sex) AS matching_sex,\n" +
                "            p2.date_of_birth AS matching_date_of_birth,\n" +
                "            ca2.visit_date AS matching_visit_date\n" +
                "        FROM\n" +
                "            PATIENT_PERSON p1\n" +
                "        JOIN\n" +
                "            PATIENT_PERSON p2 ON p1.HOSPITAL_NUMBER = p2.HOSPITAL_NUMBER\n" +
                "            AND INITCAP(p1.sex) = INITCAP(p2.sex)\n" +
                "            AND p1.date_of_birth = p2.date_of_birth\n" +
                "            AND p1.uuid <> p2.uuid  -- Exclude the same row\n" +
                "        INNER JOIN (\n" +
                "            SELECT TRUE AS commenced, hac1.person_uuid, hac1.visit_date\n" +
                "            FROM hiv_art_clinical hac1\n" +
                "            WHERE hac1.archived = 0 AND hac1.is_commencement IS TRUE\n" +
                "            GROUP BY hac1.person_uuid, hac1.visit_date\n" +
                "        ) ca1 ON ca1.person_uuid = p1.uuid\n" +
                "        INNER JOIN (\n" +
                "            SELECT TRUE AS commenced, hac2.person_uuid, hac2.visit_date\n" +
                "            FROM hiv_art_clinical hac2\n" +
                "            WHERE hac2.archived = 0 AND hac2.is_commencement IS TRUE\n" +
                "            GROUP BY hac2.person_uuid, hac2.visit_date\n" +
                "        ) ca2 ON ca2.person_uuid = p2.uuid\n" +
                "    ) sameDemographics ON ca.person_uuid = sameDemographics.uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT * FROM (\n" +
                "            SELECT \n" +
                "                DISTINCT ON (person_uuid)\n" +
                "                person_uuid, \n" +
                "                visit_date,\n" +
                "                next_appointment, \n" +
                "                tb_status,\n" +
                "                CAST(DATE_PART('year', AGE(now(), visit_date)) * 12 + DATE_PART('month', AGE(now(), visit_date)) AS INTEGER ) AS monthTillDate, \n" +
                "                ROW_NUMBER() OVER ( PARTITION BY person_uuid ORDER BY visit_date DESC)\n" +
                "            FROM HIV_ART_CLINICAL \n" +
                "            WHERE archived = 0\n" +
                "        ) visit \n" +
                "        WHERE row_number = 1\n" +
                "    ) lastVisit ON ca.person_uuid = lastVisit.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT \n" +
                "            person_uuid, \n" +
                "            visit_date, \n" +
                "            previousPickUpDrugDate,\n" +
                "            CAST(DATE_PART('year', AGE(visit_date, previousPickUpDrugDate)) * 12 + DATE_PART('month', AGE(visit_date, previousPickUpDrugDate)) AS INTEGER ) AS monthApart\n" +
                "        FROM (\n" +
                "            SELECT \n" +
                "                person_uuid, \n" +
                "                visit_date,\n" +
                "                ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY visit_date DESC),\n" +
                "                LEAD(visit_date) OVER (PARTITION BY person_uuid ORDER BY visit_date DESC) AS previousPickUpDrugDate\n" +
                "            FROM hiv_art_pharmacy\n" +
                "        ) pharm \n" +
                "        WHERE row_number = 1\n" +
                "    ) pickUp ON ca.person_uuid = pickUP.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT\n" +
                "            DISTINCT ON (person_uuid)\n" +
                "            person_uuid,\n" +
                "            visit_date,\n" +
                "            next_appointment\n" +
                "        FROM\n" +
                "            HIV_ART_CLINICAL\n" +
                "        WHERE\n" +
                "            archived = 0\n" +
                "            AND (person_uuid, visit_date, next_appointment) IN (\n" +
                "                SELECT\n" +
                "                    person_uuid,\n" +
                "                    visit_date,\n" +
                "                    next_appointment\n" +
                "                FROM\n" +
                "                    HIV_ART_CLINICAL\n" +
                "                WHERE\n" +
                "                    archived = 0\n" +
                "                GROUP BY\n" +
                "                    person_uuid,\n" +
                "                    visit_date,\n" +
                "                    next_appointment\n" +
                "                HAVING\n" +
                "                    COUNT(*) > 1\n" +
                "            )\n" +
                "    ) sameClinical ON ca.person_uuid = sameClinical.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "        SELECT DISTINCT ON (hap.person_uuid) \n" +
                "            hap.person_uuid,\n" +
                "            hac.pregnancy_status,\n" +
                "            hac.next_appointment, \n" +
                "            hac.tb_status,\n" +
                "            hap.next_appointment, \n" +
                "            hap.extra IS, \n" +
                "            hap.refill_period \n" +
                "        FROM \n" +
                "            hiv_art_clinical hac\n" +
                "        LEFT JOIN \n" +
                "            HIV_ART_PHARMACY hap ON hap.person_uuid = hac.person_uuid\n" +
                "        WHERE \n" +
                "            hap.archived = 0 \n" +
                "            AND hac.archived = 0 \n" +
                "            AND (hac.pregnancy_status IS NULL OR hac.next_appointment IS NULL OR hac.tb_status IS NULL OR\n" +
                "                 hap.next_appointment IS NULL OR hap.extra IS NULL OR hap.refill_period IS NULL)\n" +
                "    ) incompleteEncounter ON ca.person_uuid = incompleteEncounter.person_uuid\n" +
                "    LEFT JOIN (\n" +
                "\t\t\tSELECT \n" +
                "\t\t\t\tCAST(sample.date_sample_collected AS DATE ) as dateOfViralLoadSampleCollection, \n" +
                "\t\t\t\tpatient_uuid as person_uuid1  \n" +
                "\t\t\tFROM (\n" +
                "\t\t\t\tSELECT \n" +
                "\t\t\t\t\tlt.viral_load_indication, \n" +
                "\t\t\t\t\tsm.facility_id, \n" +
                "\t\t\t\t\tsm.date_sample_collected, \n" +
                "\t\t\t\t\tsm.patient_uuid, \n" +
                "\t\t\t\t\tsm.archived, \n" +
                "\t\t\t\t\tROW_NUMBER () OVER (PARTITION BY sm.patient_uuid ORDER BY date_sample_collected DESC) as rnkk\n" +
                "\t\t\t\tFROM \n" +
                "\t\t\t\t\tpublic.laboratory_sample sm\n" +
                "\t\t\t\tINNER JOIN \n" +
                "\t\t\t\t\tpublic.laboratory_test lt ON lt.id = sm.test_id\n" +
                "\t\t\t\tWHERE \n" +
                "\t\t\t\t\tlt.lab_test_id=16\n" +
                "\t\t\t\t\tAND lt.viral_load_indication !=719\n" +
                "\t\t\t\t\tAND date_sample_collected IS NOT null\n" +
                "\t\t\t)as sample\n" +
                "\t\t\tWHERE \n" +
                "\t\t\t\tsample.rnkk = 1\n" +
                "\t\t\t\tAND (sample.archived is null OR sample.archived = 0)\n" +
                "\t\t) sampleCol ON ca.person_uuid = sampleCol.person_uuid1\n" +
                "\t\tLEFT JOIN (\n" +
                "\t\tSELECT personUuid, status FROM (\n" +
                "\t\tSELECT\n" +
                "\t DISTINCT ON (pharmacy.person_uuid) pharmacy.person_uuid AS personUuid,\n" +
                "\t(\n" +
                "\t\tCASE\n" +
                "\t\t\tWHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "\t\t\tWHEN(\n" +
                "\t\t\tstat.status_date > pharmacy.maxdate\n" +
                "\t\tAND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %' )\n" +
                "\t)THEN stat.hiv_status\n" +
                "\t\t\tELSE pharmacy.status\n" +
                "\t\t\tEND\n" +
                "\t\t) AS status,\n" +
                "\n" +
                "\tstat.cause_of_death, stat.va_cause_of_death\n" +
                "\n" +
                "\t\t\t FROM\n" +
                "\t (\n" +
                "\t\t SELECT\n" +
                "\t\t\t (\n" +
                "\t CASE\n" +
                "\t\t WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "\t\t ELSE 'Active'\n" +
                "\t\t END\n" +
                "\t ) status,\n" +
                "\t\t\t (\n" +
                "\t CASE\n" +
                "\t\t WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "\t\t ELSE hp.visit_date\n" +
                "\t\t END\n" +
                "\t ) AS visit_date,\n" +
                "\t\t\t hp.person_uuid, MAXDATE\n" +
                "\t\t FROM\n" +
                "\t\t\t hiv_art_pharmacy hp\n" +
                "\t INNER JOIN (\n" +
                "\t\t\t SELECT hap.person_uuid, hap.visit_date AS  MAXDATE, ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "\t\t\t   FROM public.hiv_art_pharmacy hap \n" +
                "\t\t\t\t\t\tINNER JOIN public.hiv_art_pharmacy_regimens pr \n" +
                "\t\t\t\t\t\tON pr.art_pharmacy_id = hap.id \n" +
                "\t\t\t\tINNER JOIN hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "\t\t\t\tINNER JOIN public.hiv_regimen r on r.id = pr.regimens_id \n" +
                "\t\t\t\tINNER JOIN public.hiv_regimen_type rt on rt.id = r.regimen_type_id \n" +
                "\t\t\t\tWHERE r.regimen_type_id in (1,2,3,4,14) \n" +
                "\t\t\t\tAND hap.archived = 0                \n" +
                "\t\t\t\t ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid \n" +
                "\t\t  AND MAX.rnkkk3 = 1\n" +
                "\t\t WHERE\n" +
                "\t hp.archived = 0\n" +
                "\t ) pharmacy\n" +
                "\n" +
                "\t\t LEFT JOIN (\n" +
                "\t\t SELECT\n" +
                "\t\t\t hst.hiv_status,\n" +
                "\t\t\t hst.person_id,\n" +
                "\t\t\t hst.status_date,\n" +
                "\t\t\t hst.cause_of_death,\n" +
                "\t\t\t hst.va_cause_of_death\n" +
                "\t\t FROM\n" +
                "\t\t\t (\n" +
                "\t SELECT * FROM (SELECT DISTINCT (person_id) person_id, status_date, cause_of_death,va_cause_of_death,\n" +
                "\t\t\thiv_status, ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "\t\tFROM hiv_status_tracker WHERE archived=0 )s\n" +
                "\t WHERE s.row_number=1\n" +
                "\t\t\t ) hst\n" +
                "\t INNER JOIN hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "\t ) stat ON stat.person_id = pharmacy.person_uuid --AND pharmacy.status = 'Active' \n" +
                "\t) --st where status = 'Active'\n" +
                "\t)st ON st.personUuid = e.person_uuid\n" +
                "    WHERE \n" +
                "        p.archived=0 \n" +
                "        AND p.facility_id= ?1\n" +
                "\t    AND st.status = 'Active'\n" +
                "    GROUP BY \n" +
                "        e.id, \n" +
                "        p.id, \n" +
                "        p.hospital_number, \n" +
                "        p.date_of_birth, \n" +
                "        b.biometric_valid_captured, \n" +
                "        b.person_uuid, \n" +
                "        recap.biometric_valid_captured, \n" +
                "        recap.person_uuid, \n" +
                "        sameDemographics.uuid, \n" +
                "        lastVisit.visit_date,\n" +
                "        lastVisit.monthTillDate, \n" +
                "        pickUp.visit_date,\n" +
                "        pickUp.monthApart, \n" +
                "        pickUp.previousPickUpDrugDate, \n" +
                "        b.enrollment_date, \n" +
                "        recap.enrollment_date,\n" +
                "        sameClinical.person_uuid, \n" +
                "        incompleteEncounter.person_uuid, \n" +
                "        sampleCol.dateOfViralLoadSampleCollection, \n" +
                "        ca.visit_date,\n" +
                "\t\tst.status\n" +
                "    ORDER BY \n" +
                "        p.id DESC\n" +
                ")\n" +
                "SELECT\n" +
                "COUNT(noBaseline) AS noBaseLineNumerator,\n" +
                "    COUNT(hospitalNumber) AS noBaseLineDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(noBaseline) AS noBaselineVariance,\n" +
                "    ROUND((CAST(COUNT(noBaseline) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS noBaseLinePerformance,\n" +
                "\tCOUNT(hasBaseLineNoRecapture) AS hasBaseLineNoRecaptureNumerator,\n" +
                "    COUNT(person_uuid1) AS hasBaseLineNoRecaptureDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(hasBaseLineNoRecapture) AS hasBaseLineNoRecaptureVariance,\n" +
                "    ROUND((CAST(COUNT(hasBaseLineNoRecapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS hasBaseLineNoRecapturePerformance,\n" +
                "\tCOUNT(sameDemographic) AS sameDemographicsNumerator,\n" +
                "    COUNT(hospitalNumber) AS sameDemographicsDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(sameDemographic) AS sameDemographicsVariance,\n" +
                "    ROUND((CAST(COUNT(sameDemographic) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS sameDemographicsPerformance,\n" +
                "\tCOUNT(lastClinicMonth) AS clinicMoreThanOneYearNumerator,\n" +
                "    COUNT(hospitalNumber) AS clinicMoreThanOneYearDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(lastClinicMonth) AS clinicMoreThanOneYearVariance,\n" +
                "    ROUND((CAST(COUNT(lastClinicMonth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS clinicMoreThanOneYearPerformance,\n" +
                "\tCOUNT(pickUpOneYear) AS pickUpOneYearNumerator,\n" +
                "    COUNT(hospitalNumber) AS pickUpOneYearDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(pickUpOneYear) AS pickUpOneYearVariance,\n" +
                "    ROUND((CAST(COUNT(pickUpOneYear) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS pickUpOneYearPerformance,\n" +
                "\tCOUNT(clinicNoRecapture) AS clinicNoRecaptureNumerator,\n" +
                "    COUNT(person_uuid1) AS clinicNoRecaptureDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(clinicNoRecapture) AS clinicNoRecaptureVariance,\n" +
                "    ROUND((CAST(COUNT(clinicNoRecapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS clinicNoRecapturePerformance,\n" +
                "\tCOUNT(dupClinical) AS sameClinicalNumerator,\n" +
                "    COUNT(hospitalNumber) AS sameClinicalDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(dupClinical) AS sameClinicalVariance,\n" +
                "    ROUND((CAST(COUNT(dupClinical) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS sameClinicalPerformance, \n" +
                "\tCOUNT(incomplete) AS incompleteNumerator,\n" +
                "    COUNT(hospitalNumber) AS incompleteDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(incomplete) AS incompleteVariance,\n" +
                "    ROUND((CAST(COUNT(incomplete) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS incompletePerformance,\n" +
                "\tCOUNT(vlPrior) AS labNumerator,\n" +
                "    COUNT(hospitalNumber) AS labDenominator,\n" +
                "\tCOUNT(hospitalNumber) - COUNT(vlPrior) AS labVariance,\n" +
                "    ROUND((CAST(COUNT(vlPrior) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS labPerformance\n" +
                "FROM\n" +
                "    clientVerification";

    }

}
