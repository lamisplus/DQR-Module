package org.lamisplus.modules.dqr.util;

public class DQRQueries {

    public static class DataConsistency {

        public static final String CLINICALS_SUMMARY_QUERIES = "WITH dataConsistence AS (\n" +
                "SELECT e.unique_id AS patientId , p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,p.date_of_birth AS dateOfBirth, CASE WHEN tri.body_weight IS NOT NULL AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 AND st.status = 'Active' THEN 1 ELSE NULL END AS adultweight, \n" +
                "tri.visit_date AS visit_date, e.target_group_id as target_group, CASE WHEN e.entry_point_id IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS entryPoint,\n" +
                "e.date_confirmed_hiv AS hiv_confirm_date, (CASE WHEN lasClinic.lastvisit >= e.date_confirmed_hiv AND st.status = 'Active' THEN 1 ELSE null END) AS lGreaterConf,\n" +
                "pharm.visit_date AS lastPickUp,(CASE WHEN pharm.visit_date >  p.date_of_birth AND st.status = 'Active' THEN 1 ELSE null END) AS lstPickGreaterDOb,\n" +
                "transfer.hiv_status, transfer.status_date,(CASE WHEN e.date_started < transfer.status_date AND st.status = 'Active' THEN 1 ELSE null END)  AS ArtGreaterTrans,\n" +
                "(CASE WHEN e.date_started = lasClinic.lastvisit AND st.status = 'Active' THEN 1 ELSE null END) ArtEqClinicD, (CASE WHEN e.date_started = pharm.visit_date AND st.status = 'Active' THEN 1 ELSE null END) ArtEqDrugPickupD,\n" +
                "(CASE WHEN pharm.visit_date >= transfer.status_date AND st.status = 'Active' THEN 1 ELSE null END)  AS DrugPickHigherThanTrans,\n" +
                "(CASE WHEN pharm.visit_date <= CAST(now() AS DATE) AND st.status = 'Active' THEN 1 ELSE null END)  AS DrugPickLessToday,\n" +
                "(CASE WHEN lasClinic.lastvisit <= CAST(now() AS DATE) AND st.status = 'Active' THEN 1 ELSE null END)  AS clinicPickLessToday,\n" +
                "(CASE WHEN e.date_started <= CAST(now() AS DATE) AND st.status = 'Active' THEN 1 ELSE null END)  AS artDateLessToday,\n" +
                "(CASE WHEN lasClinic.lastvisit > transfer.status_date AND st.status = 'Active'  THEN 1 ELSE null END)  AS clinicGreaterThanTrans,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad > vl.dateSampleCollected AND st.status = 'Active' THEN 1 ELSE NULL END) AS vlSample,\n" +
                "CASE WHEN SEX = 'Female' AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 AND st.status = 'Active' THEN 1 ELSE NULL END AS activeFemaleAdult,\n" +
                "CASE WHEN lasClinic.pregnancy_status IS NOT NULL AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 AND INITCAP(p.sex) = 'Female' AND st.status = 'Active' THEN 1 ELSE NULL END AS adultPre,\n" +
                "CASE WHEN tri.body_weight < 61 AND st.status = 'Active' AND CAST (EXTRACT(YEAR from AGE(NOW(), p.date_of_birth)) AS INTEGER) BETWEEN 0 AND 14 THEN 1 ELSE NULL END AS peadweight,\n" +
                "CASE WHEN CAST (EXTRACT(YEAR from AGE(NOW(), p.date_of_birth)) AS INTEGER) BETWEEN 0 AND 14 AND st.status = 'Active' THEN 1 ELSE NULL END AS peadcURR,\n" +
                "CASE WHEN st.status = 'Active' THEN 1 ELSE NULL END AS artStatus\n" +
                "\n" +
                " FROM patient_person p\n" +
                " INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                " LEFT JOIN\n" +
                " (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                " GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
                "\n" +
                "LEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\n" +
                "\t\n" +
                " LEFT JOIN\n" +
                " (SELECT DISTINCT ON (person_uuid)\n" +
                "   person_uuid, visit_date, body_weight\n" +
                "FROM ( SELECT ht.person_uuid, MAX(ht.visit_date) AS visit_date, tr.body_weight\n" +
                "   FROM hiv_art_clinical ht JOIN triage_vital_sign tr ON ht.person_uuid = tr.person_uuid AND ht.vital_sign_uuid = tr.uuid \n" +
                "GROUP BY ht.person_uuid, tr.body_weight ORDER BY ht.person_uuid DESC ) fi ORDER BY\n" +
                "   person_uuid DESC ) tri ON tri.person_uuid = p.uuid\n" +
                "LEFT JOIN (SELECT DISTINCT ON (person_uuid)\n" +
                "person_uuid, lastVisit,pregnancy_status\n" +
                "  FROM\n" +
                "(SELECT hacc.person_uuid, MAX(hacc.visit_date) as lastVisit, pregnancy_status from hiv_art_clinical hacc JOIN patient_person p2\n" +
                "ON hacc.person_uuid = p2.uuid\n" +
                "where hacc.archived=0 \n" +
                " group by person_uuid, pregnancy_status ORDER BY person_uuid DESC ) lClinicVisit ORDER BY\n" +
                "   person_uuid DESC ) \n" +
                " lasClinic ON p.uuid = lasClinic.person_uuid\n" +
                "LEFT JOIN\n" +
                "(SELECT DISTINCT (person_id)\n" +
                "person_id, MAX(status_date) AS status_date, hiv_status FROM hiv_status_tracker where hiv_status = 'ART_TRANSFER_IN'\n" +
                "GROUP BY person_id, hiv_status ) transfer ON p.uuid = transfer.person_id\n" +
                "LEFT JOIN (\n" +
                "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ls.date_sample_collected as dateSampleCollected,\n" +
                "lr.result_reported AS lastViralLoad,\n" +
                "lr.date_result_reported as dateOfLastViralLoad\n" +
                "FROM laboratory_order lo\n" +
                "LEFT JOIN ( SELECT patient_uuid, MAX(order_date) AS MAXDATE FROM laboratory_order lo\n" +
                "GROUP BY patient_uuid ORDER BY MAXDATE ASC ) AS current_lo\n" +
                "ON current_lo.patient_uuid=lo.patient_uuid AND current_lo.MAXDATE=lo.order_date\n" +
                "LEFT JOIN laboratory_test lt ON lt.lab_order_id=lo.id AND lt.patient_uuid = lo.patient_uuid\n" +
                "LEFT JOIN base_application_codeset bac_viral_load ON bac_viral_load.id=lt.viral_load_indication\n" +
                "LEFT JOIN laboratory_labtest ll ON ll.id=lt.lab_test_id\n" +
                "-- INNER JOIN hiv_enrollment h ON h.person_uuid=current_lo.patient_uuid\n" +
                "LEFT JOIN laboratory_sample ls ON ls.test_id=lt.id AND ls.patient_uuid = lo.patient_uuid\n" +
                "LEFT JOIN laboratory_result lr ON lr.test_id=lt.id AND lr.patient_uuid = lo.patient_uuid\n" +
                "WHERE  lo.archived=0 AND\n" +
                "lr.date_result_reported IS NOT NULL\n" +
                ") vl ON e.person_uuid = vl.person_uuid\n" +
                "LEFT JOIN \n" +
                "  (SELECT DISTINCT ON (person_uuid)\n" +
                "    person_uuid, visit_date, refill_period, regimen\n" +
                "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra->'regimens'->0->>'name' AS regimen from hiv_art_pharmacy\n" +
                "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi\n" +
                "ORDER BY\n" +
                "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
                " LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
                " WHERE p.archived=0 AND p.facility_id= ?1\n" +
                " GROUP BY e.id, p.hospital_number, p.date_of_birth, ca.visit_date, tri.body_weight, p.facility_id, tri.visit_date, \n" +
                "e.target_group_id, e.entry_point_id,  e.date_confirmed_hiv, p.sex, p.id,\n" +
                "transfer.hiv_status, transfer.status_date,lasclinic.lastvisit, pharm.visit_date, \n" +
                "vl.dateOfLastViralLoad,vl.dateSampleCollected, st.status,\n" +
                "lasClinic.pregnancy_status\n" +
                " ORDER BY p.id DESC )\n" +
                " SELECT \n" +
                " COUNT(target_group) AS targNumerator,\n" +
                " COUNT(artStatus) AS targDenominator,\n" +
                "  COUNT(artStatus) -  COUNT(target_group) AS targVariance,\n" +
                " ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS targPerformance,\n" +
                " COUNT(entrypoint) AS entryNumerator,\n" +
                " COUNT(artStatus) AS entryDenominator,\n" +
                "  COUNT(artStatus) -  COUNT(entrypoint) AS entryVariance,\n" +
                " ROUND((CAST(COUNT(entrypoint) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS entryPerformance,\n" +
                " COUNT(adultweight) AS adultWeightNumerator,\n" +
                " COUNT(artStatus) AS adultWeightDenominator,\n" +
                " COUNT(artStatus) -  COUNT(adultweight) AS adultWeightVariance,\n" +
                " ROUND((CAST(COUNT(adultweight) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS adultWeightPerformance,\n" +
                " COUNT(peadweight) AS peadWeightNumerator,\n" +
                " COUNT(peadcURR) AS peadWeightDenominator,\n" +
                "  COUNT(peadcURR) - COUNT(peadweight) AS peadWeightVariance,\n" +
                " ROUND((CAST(COUNT(peadweight) AS DECIMAL) / COUNT(peadcURR)) * 100, 2) AS peadWeightPerformance,\n" +
                " COUNT(adultPre) AS pregNumerator,\n" +
                " COUNT(activeFemaleAdult) AS pregDenominator,\n" +
                "  COUNT(activeFemaleAdult) - COUNT(adultPre) AS pregVariance,\n" +
                " ROUND((CAST(COUNT(adultPre) AS DECIMAL) / COUNT(activeFemaleAdult)) * 100, 2) AS pregPerformance,\n" +
                " COUNT(ArtEqClinicD) AS artEqClinicNumerator,\n" +
                " COUNT(artStatus) AS artEqClinicDenominator,\n" +
                " COUNT(artStatus) - COUNT(ArtEqClinicD) AS artEqClinicVariance,\n" +
                " ROUND((CAST(COUNT(ArtEqClinicD) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS artEqClinicPerformance,\n" +
                " COUNT(ArtEqDrugPickupD) AS artEqLastPickupNumerator,\n" +
                " COUNT(artStatus) AS artEqLastPickupDenominator,\n" +
                " COUNT(artStatus) - COUNT(ArtEqDrugPickupD) AS artEqLastPickupVariance,\n" +
                " ROUND((CAST(COUNT(ArtEqDrugPickupD) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS artEqLastPickupPerformance,\n" +
                " COUNT(lGreaterConf) AS lGreaterConfNumerator,\n" +
                " COUNT(artStatus) AS lGreaterConfDenominator,\n" +
                " COUNT(artStatus) - COUNT(lGreaterConf) AS lGreaterConfVariance,\n" +
                " ROUND((CAST(COUNT(lGreaterConf) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS lGreaterConfPerformance,\n" +
                " COUNT(ArtGreaterTrans) AS artGreaterTransNumerator,\n" +
                " COUNT(artStatus) AS ArtGreaterTransDenominator,\n" +
                " COUNT(artStatus) - COUNT(ArtGreaterTrans) AS ArtGreaterTransVariance,\n" +
                " ROUND((CAST(COUNT(ArtGreaterTrans) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS ArtGreaterTransPerformance,\n" +
                " COUNT(lstPickGreaterDOb) AS lstPickGreaterDObNumerator,\n" +
                " COUNT(artStatus) AS lstPickGreaterDObDenominator,\n" +
                " COUNT(artStatus) - COUNT(lstPickGreaterDOb) AS lstPickGreaterDObVariance,\n" +
                " ROUND((CAST(COUNT(lstPickGreaterDOb) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS lstPickGreaterDObPerformance,\n" +
                " COUNT(DrugPickHigherThanTrans) AS lDrugPickHighNumerator,\n" +
                " COUNT(artStatus) AS lDrugPickHighDenominator,\n" +
                " COUNT(artStatus) - COUNT(DrugPickHigherThanTrans) AS lDrugPickHighVariance,\n" +
                " ROUND((CAST(COUNT(DrugPickHigherThanTrans) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS lDrugPickHighPerformance,\n" +
                " COUNT(DrugPickLessToday) AS lDrugPickHighTodayNumerator,\n" +
                " COUNT(artStatus) AS lDrugPickHighTodayDenominator,\n" +
                " COUNT(artStatus) - COUNT(DrugPickLessToday) AS lDrugPickHighTodayVariance,\n" +
                " ROUND((CAST(COUNT(DrugPickLessToday) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS lDrugPickHighTodayPerformance,\n" +
                " COUNT(clinicPickLessToday) AS clinicPickLessTodayNumerator,\n" +
                " COUNT(artStatus) AS clinicPickLessTodayDenominator,\n" +
                " COUNT(artStatus) - COUNT(clinicPickLessToday) AS clinicPickLessTodayVariance,\n" +
                " ROUND((CAST(COUNT(clinicPickLessToday) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS clinicPickLessTodayPerformance,\n" +
                " COUNT(artDateLessToday) AS artDateLessTodayNumerator,\n" +
                " COUNT(artStatus) AS artDateLessTodayDenominator,\n" +
                " COUNT(artStatus) - COUNT(artDateLessToday) AS artDateLessTodayVariance,\n" +
                " ROUND((CAST(COUNT(artDateLessToday) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS artDateLessTodayPerformance,\n" +
                " COUNT(vlSample) AS vlNumerator,\n" +
                " COUNT(artStatus) AS vlDenominator,\n" +
                " COUNT(artStatus) -  COUNT(vlSample) AS vlVariance,\n" +
                " ROUND((CAST(COUNT(vlSample) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS vlPerformance\n" +
                " FROM\n" +
                "  dataConsistence";


        public static final String PHARMACY_SUMMARY_QUERIES = "With pharmacySummary AS (\n" +
                "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
                ",p.date_of_birth AS dateOfBirth, CASE WHEN pharm.refill_period IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS refillMonth, pharm.visit_date AS visit_date, \n" +
                "\tCASE WHEN pharm.extra IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS regimen, \n" +
                "\tCASE WHEN st.status = 'Active' THEN 1 ELSE NULL END AS artStatus\n" +
                "  FROM patient_person p \n" +
                "  INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "  LEFT JOIN\n" +
                "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
                "LEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\n" +
                "  LEFT JOIN\n" +
                "  (SELECT DISTINCT ON (person_uuid)\n" +
                "    person_uuid, visit_date, refill_period, extra\n" +
                "FROM ( select person_uuid, refill_period, MAX(visit_date) AS visit_date, extra from hiv_art_pharmacy\n" +
                "where archived = 0\n" +
                "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi ORDER BY\n" +
                "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
                "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
                "  WHERE p.archived=0 AND p.facility_id= ?1\n" +
                "  GROUP BY e.id, ca.commenced, p.id, pc.display, st.status,p.hospital_number, p.date_of_birth, ca.visit_date, pharm.refill_period, p.facility_id, pharm.visit_date, pharm.extra\n" +
                "  ORDER BY p.id DESC )\n" +
                "SELECT\n" +
                "    COUNT(refillMonth) AS refillNumerator,\n" +
                "    COUNT(artStatus) AS refillDenominator,\n" +
                "COUNT(artStatus) - COUNT(refillMonth) AS refillVariance,\n" +
                "    ROUND((CAST(COUNT(refillMonth) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS refillPerformance,\n" +
                "    COUNT(regimen) AS regimenNumerator,\n" +
                "    COUNT(artStatus) AS regimenDenominator,\n" +
                "COUNT(artStatus) - COUNT(regimen) AS regimenVariance,\n" +
                "    ROUND((CAST(COUNT(regimen) AS DECIMAL) / COUNT(artStatus)) * 100, 2) AS regimenPerformance\n" +
                "FROM pharmacySummary;";


        public static final String LABORATORY_SUMMARY_QUERIES = "WITH vlSummary AS ( \n" +
                "SELECT e.unique_id AS patientId ,p.hospital_number AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
                "CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) AS age,\n" +
                "         p.date_of_birth AS dateOfBirth, CASE WHEN vl.dateSampleCollected IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS dateSampleCollected, vl.lastViralLoad, \n" +
                "CASE WHEN vl.dateOfLastViralLoad IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS dateOfLastViralLoad, pharm.visit_date,\n" +
                " (CASE WHEN pharm.visit_date >= NOW() - INTERVAL '1 YEAR' AND pharm.visit_date <= NOW() AND st.status = 'Active' THEN 1 ELSE null END) AS eligibleVl1year,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad >= NOW() - INTERVAL '1 YEAR' AND vl.dateOfLastViralLoad <= NOW() AND st.status = 'Active' THEN 1 ELSE null END) AS hadvl1year,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad IS NOT NULL AND vl.dateSampleCollected IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END) AS hadvlAndSampleDate,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad IS NOT NULL AND vl.pcrDate IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END) AS hadvlAndpcrDate,\n" +
                "(CASE WHEN vl.viralLoadType IS NOT NULL AND vl.dateSampleCollected IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END) AS hadVlIndicator,\n" +
                "(CASE WHEN vl.dateOfLastViralLoad > vl.dateSampleCollected AND st.status = 'Active' THEN 1 ELSE NULL END) AS vlDateGsDate,\n" +
                "(CASE WHEN cd4.cd4date >= NOW() - INTERVAL '1 YEAR' AND cd4.cd4date <= NOW() AND st.status = 'Active' THEN 1 ELSE null END) AS hadcd4vl1year,\n" +
                "st.status\n" +
                "FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "LEFT JOIN\n" +
                "(SELECT TRUE as commenced, hac.person_uuid FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "GROUP BY hac.person_uuid)ca ON p.uuid = ca.person_uuid\n" +
                "LEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
                "bac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
                "-- CASE WHEN lr.result_reported ~ E'^\\\\\\\\\\\\\\\\d+(\\\\\\\\\\\\\\\\.\\\\\\\\\\\\\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
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
                "vl.lastViralLoad, vl.dateOfLastViralLoad, pharm.visit_date, vl.pcrDate, vl.viralLoadType, cd4.cd4date, st.status\n" +
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
                "(CASE WHEN e.date_started >= NOW() - INTERVAL '1 YEAR' AND e.date_started <= NOW() AND st.status = 'Active' THEN 1 ELSE null END) AS hadVl1year,\n" +
                "(CASE WHEN ((pharm.visit_date  >= NOW() - INTERVAL '6 MONTH' AND pharm.visit_date <= NOW()) AND CAST(vl.clastViralLoad AS NUMERIC) > 1000) AND st.status = 'Active' THEN 1 ELSE null END) AS sixmVl,\n" +
                "(CASE WHEN ((pharm.visit_date  >= NOW() - INTERVAL '6 MONTH' AND pharm.visit_date <= NOW()) AND eac.eac_commenced IS NOT NULL AND CAST(vl.clastViralLoad AS NUMERIC) > 1000) AND st.status = 'Active'  THEN 1 ELSE null END) AS hadeac,\n" +
                "pharm.visit_date,\n" +
                "vl.dateSampleCollected,\n" +
                "eac1.status , eac1.last_viral_load, eac1.date_of_last_viral_load, eac1.visit_date AS eac_completion,\n" +
                "(CASE WHEN eac1.status is NOT NULL AND vl.dateSampleCollected IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END) AS compleacdate,\n" +
                "(CASE WHEN eac1.status is NOT NULL AND eac1.visit_date IS NOT NULL AND st.status = 'Active'  THEN 1 ELSE NULL END) AS cmpleac,\n" +
                "(CASE WHEN eac1.status is NOT NULL AND eac1.visit_date IS NOT NULL AND eac1.date_of_last_viral_load IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END) AS postEac,\n" +
                "st.status\n" +
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
                "LEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\t\n" +
                "LEFT JOIN (\n" +
                "SELECT DISTINCT ON(lo.patient_uuid) lo.patient_uuid as person_uuid, ll.lab_test_name as test,\n" +
                "bac_viral_load.display AS viralLoadType, ls.date_sample_collected as dateSampleCollected,\n" +
                "CASE WHEN lr.result_reported ~ E'^\\\\\\\\\\\\\\\\d+(\\\\\\\\\\\\\\\\.\\\\\\\\\\\\\\\\d+)?$' THEN CAST(lr.result_reported AS DECIMAL)\n" +
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
                "  eac1.status, eac1.last_viral_load, eac1.date_of_last_viral_load, eac1.visit_date, st.status\n" +
                "  ORDER BY p.id DESC )\n" +
                "  SELECT \n" +
                "  COUNT(hadeac) AS eacCommencedNumerator,\n" +
                "  COUNT(sixmVl) AS eacCommencedDenominator,\n" +
                "  COUNT(sixmVl) - COUNT(hadeac) AS eacCommencedVariance,\n" +
                "  CASE \n" +
                "        WHEN COUNT(sixmVl) > 0 THEN ROUND((CAST(COUNT(hadeac) AS DECIMAL) / COUNT(sixmVl)) * 100, 2)\n" +
                "        ELSE 0\n" +
                "    END AS eacCommencedPerformance,\n" +
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
                "    SELECT \n" +
                "        person_uuid,\n" +
                "        client_code,\n" +
                "        date_visit,\n" +
                "        target_group,\n" +
                "        hts_client_uuid,\n" +
                "        hasindex,\n" +
                "        testing_setting,\n" +
                "        gender,\n" +
                "        date_of_birth,\n" +
                "        age,\n" +
                "        hiv_test_result,\n" +
                "        poscount,\n" +
                "        adultPos,\n" +
                "        (CASE WHEN adultPos = 1 AND recency IS NOT NULL THEN 1 ELSE NULL END) AS adPosRec,\n" +
                "        rita,\n" +
                "        order_date,\n" +
                "        (CASE WHEN rita ILIKE '%Recent%' ESCAPE ' ' THEN 1 ELSE NULL END) AS recentInfection,\n" +
                "        (CASE WHEN (rita ILIKE '%Recent%' ESCAPE ' ' AND order_date IS NOT NULL) THEN 1 ELSE NULL END) AS recentwitVL,\n" +
                "        (CASE WHEN (rita ILIKE '%Recent%' ESCAPE ' ' AND result_reported IS NOT NULL AND order_date IS NOT NULL) THEN 1 ELSE NULL END) AS recentwitVlResl,\n" +
                "        (CASE WHEN resultdate > order_date THEN 1 ELSE NULL END) AS rsGreaterThan,\n" +
                "        resultdate,\n" +
                "        recency,\n" +
                "        recencydate,\n" +
                "        (CASE WHEN (recencydate >= date_visit) AND recencydate IS NOT NULL AND date_visit IS NOT NULL THEN 1 ELSE NULL END) AS dateconfirm,\n" +
                "        (CASE WHEN testing_setting IS NOT NULL AND hasindex IS NOT NULL THEN 1 ELSE NULL END) AS settings\n" +
                "    FROM \n" +
                "    (\n" +
                "        SELECT DISTINCT ON (person_uuid) \n" +
                "            hc.person_uuid, \n" +
                "            hc.client_code, \n" +
                "            hc.date_visit, \n" +
                "            hc.target_group, \n" +
                "            hie.hts_client_uuid, \n" +
                "            (CASE WHEN hc.index_client = 'true' THEN 1 ELSE null END) hasIndex,\n" +
                "            hc.testing_setting,\n" +
                "            CAST((hc.extra->>'gender') AS VARCHAR(100)) AS gender, \n" +
                "            CAST((hc.extra->>'date_of_birth') AS DATE) AS date_of_birth, \n" +
                "            CAST(EXTRACT(YEAR FROM AGE(NOW(), CAST(hc.extra->>'date_of_birth' AS DATE))) AS INTEGER) AS age,\n" +
                "            hc.hiv_test_result,\n" +
                "            (CASE WHEN hc.hiv_test_result = 'Positive' THEN 1 ELSE null END) AS posCount,\n" +
                "            (CASE WHEN CAST(EXTRACT(YEAR FROM AGE(NOW(), CAST(hc.extra->>'date_of_birth' AS DATE))) AS INTEGER) > 15 THEN 1 ELSE null END) AS adultPos,\n" +
                "            recency->>'rencencyId' AS recency,\n" +
                "            recency->>'rencencyInterpretation' AS rita,\n" +
                "            CAST(lo.order_date AS DATE),\n" +
                "            lr.result_reported, \n" +
                "            CAST(lr.date_result_reported AS DATE) AS resultdate, \n" +
                "            (CASE WHEN recency->>'optOutRTRITestDate' IS NOT NULL AND recency->>'optOutRTRITestDate' <> '' THEN CAST(recency->>'optOutRTRITestDate' AS DATE) ELSE NULL END) AS recencydate\n" +
                "        FROM \n" +
                "            hts_client hc\n" +
                "        LEFT JOIN \n" +
                "            hts_index_elicitation hie ON hc.uuid = hie.hts_client_uuid\n" +
                "        LEFT JOIN \n" +
                "            laboratory_order lo ON lo.patient_uuid = hc.person_uuid\n" +
                "        LEFT JOIN \n" +
                "            laboratory_result lr ON lr.patient_uuid = hc.person_uuid\n" +
                "        WHERE \n" +
                "            hc.facility_id = ?1\n" +
                "    ) pro\n" +
                ")\n" +
                "SELECT \n" +
                "    COUNT(adPosRec) AS totalPosNumerator,\n" +
                "    COUNT(adultpos) AS totalPosDenominator,\n" +
                "    COUNT(adultpos) - COUNT(adPosRec) AS totalPosVariance,\n" +
                "    COALESCE(\n" +
                "        ROUND((CAST(COUNT(adPosRec) AS DECIMAL) / NULLIF(COUNT(adultpos), 0)) * 100, 2),\n" +
                "        0\n" +
                "    ) AS totalPosPerformance,\n" +
                "    COUNT(recentwitVL) AS withVLNumerator,\n" +
                "    COUNT(recentInfection) AS withVLDenominator,\n" +
                "    COUNT(recentInfection) - COUNT(recentwitVL) AS withVLVariance, \n" +
                "    COALESCE(\n" +
                "        ROUND((CAST(COUNT(recentwitVL) AS DECIMAL) / NULLIF(COUNT(recentInfection), 0)) * 100, 2),\n" +
                "        0\n" +
                "    ) AS withVLPerformance,\n" +
                "    COUNT(recentwitVlResl) AS withVlResNumerator,\n" +
                "    COUNT(recentwitVL) AS withVlResDenominator,\n" +
                "    COUNT(recentwitVL) - COUNT(recentwitVlResl) AS withVlResVariance,\n" +
                "    COALESCE(\n" +
                "        ROUND((CAST(COUNT(recentwitVlResl) AS DECIMAL) / NULLIF(COUNT(recentwitVL), 0)) * 100, 2),\n" +
                "        0\n" +
                "    ) AS withVlResPerformance,\n" +
                "    COUNT(rsGreaterThan) AS rsGreaterNumerator,\n" +
                "    COUNT(resultdate) AS rsGreaterDenominator,\n" +
                "    COUNT(resultdate) - COUNT(rsGreaterThan) AS rsGreaterVariance,\n" +
                "    COALESCE(\n" +
                "        ROUND((CAST(COUNT(rsGreaterThan) AS DECIMAL) / NULLIF(COUNT(resultdate), 0)) * 100, 2),\n" +
                "        0\n" +
                "    ) AS rsGreaterPerformance,\n" +
                "    COUNT(dateconfirm) AS recencyNumerator,\n" +
                "    COUNT(recency) AS recencyDenominator,\n" +
                "    COUNT(recency) - COUNT(dateconfirm) AS recencyVariance,\n" +
                "    COALESCE(\n" +
                "        ROUND((CAST(COUNT(dateconfirm) AS DECIMAL) / NULLIF(COUNT(recency), 0)) * 100, 2),\n" +
                "        0\n" +
                "    ) AS recencyPerformance,\n" +
                "    COUNT(hts_client_uuid) AS elicitedNumerator,\n" +
                "    COUNT(hasindex) AS elicitedDenominator,\n" +
                "    COUNT(hasindex) - COUNT(hts_client_uuid) AS elicitedVariance,\n" +
                "    COALESCE(\n" +
                "        ROUND((CAST(COUNT(hts_client_uuid) AS DECIMAL) / NULLIF(COUNT(hasindex), 0)) * 100, 2),\n" +
                "        0\n" +
                "    ) AS elicitedPerformance,\n" +
                "    COUNT(settings) AS settingsNumerator,\n" +
                "    COUNT(hasindex) AS settingsDenominator,\n" +
                "    COUNT(hasindex) - COUNT(settings) AS settingsVariance,\n" +
                "    COALESCE(\n" +
                "        ROUND((CAST(COUNT(settings) AS DECIMAL) / NULLIF(COUNT(hasindex), 0)) * 100, 2),\n" +
                "        0\n" +
                "    ) AS settingsPerformance,\n" +
                "    COUNT(target_group) AS targNumerator,\n" +
                "    COUNT(person_uuid) AS targDenominator,\n" +
                "    COUNT(person_uuid) - COUNT(target_group) AS targVariance,\n" +
                "    COALESCE(\n" +
                "        ROUND((CAST(COUNT(target_group) AS DECIMAL) / NULLIF(COUNT(person_uuid), 0)) * 100, 2),\n" +
                "        0\n" +
                "    ) AS targPerformance\n" +
                "FROM \n" +
                "    htsSummary;";


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
                "  COALESCE(ROUND((CAST(COUNT(prep_offered) AS DECIMAL) / COUNT(person_uuid)) * 100, 2), 0) AS pOfferredPerformance,\n" +
                "  COUNT(prep_accepted) AS pAcceptedNumerator,\n" +
                "  COUNT(prep_offered) AS pAcceptedDenominator,\n" +
                "  COUNT(prep_offered) - COUNT(prep_accepted) AS pAcceptedVariance,\n" +
                "  COALESCE(ROUND((CAST(COUNT(prep_accepted) AS DECIMAL) / COUNT(prep_offered)) * 100, 2), 0) AS pAcceptedPerformance,\n" +
                "  COUNT(date_enrolled_prep) AS pEnrollNumerator,\n" +
                "  COUNT(offAndAccpt) AS pEnrollDenominator,\n" +
                "  COUNT(offAndAccpt) - COUNT(date_enrolled_prep) AS pEnrollVariance,\n" +
                "  COALESCE(ROUND((CAST(COUNT(date_enrolled_prep) AS DECIMAL) / COUNT(offAndAccpt)) * 100, 2), 0) AS pEnrollPerformance,\n" +
                "  COUNT(urinalysis) AS pEnrolledPrepUrinaNumerator,\n" +
                "  COUNT(date_enrolled_prep) AS pEnrolledPrepUrinaDenominator,\n" +
                "  COUNT(date_enrolled_prep) - COUNT(urinalysis) AS pEnrolledPrepUrinaVariance,\n" +
                "  COALESCE(ROUND((CAST(COUNT(urinalysis) AS DECIMAL) / COUNT(date_enrolled_prep)) * 100, 2), 0) AS pEnrolledPrepUrinaPerformance,\n" +
                "  COUNT(urinaGreaterthanenrollDate) AS pUrinaGreaterEnrollNumerator,\n" +
                "  COUNT(urinalysis) AS pUrinaGreaterEnrollDenominator,\n" +
                "  COUNT(urinalysis) - COUNT(urinaGreaterthanenrollDate) AS pUrinaGreaterEnrollVariance,\n" +
                "  COALESCE(ROUND((CAST(COUNT(urinaGreaterthanenrollDate) AS DECIMAL) / NULLIF(COUNT(urinalysis), 0)) * 100, 2),0) AS pUrinaGreaterEnrollPerformance,\n" +
                "  COUNT(urinaGreaterthanStatusDate) AS pUrinaGreaterStatusDateNumerator,\n" +
                "  COUNT(urinalysis) AS pUrinaGreaterStatusDateDenominator,\n" +
                "  COUNT(urinalysis) - COUNT(urinaGreaterthanStatusDate) AS pUrinaGreaterStatusDateVariance,\n" +
                "  COALESCE(ROUND((CAST(COUNT(urinaGreaterthanStatusDate) AS DECIMAL) / NULLIF(COUNT(urinalysis),0)) * 100, 2), 0) AS pUrinaGreaterStatusDatePerformance,\n" +
                "  COUNT(enrollDateLessThanCommenced) AS commencedNumerator,\n" +
                "  COUNT(encounter_date) AS commencedDenominator,\n" +
                "  COUNT(encounter_date) - COUNT(enrollDateLessThanCommenced) AS commencedVariance,\n" +
                "  COALESCE(ROUND((CAST(COUNT(enrollDateLessThanCommenced) AS DECIMAL) / NULLIF(COUNT(encounter_date),0)) * 100, 2), 0) AS commencedPerformance\n" +
                "FROM\n" +
                "prepSummary";
    }

    public static class ClinicalVariables {

        public static final String CLINICAL_VARIABLE_SUMMARY_QUERIES = "WITH PatientClinic AS (\n" +
                " SELECT e.unique_id AS patientId ,CASE WHEN p.hospital_number IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS hospitalNumber, INITCAP(p.sex) AS sex,p.date_of_birth AS dateOfBirth,\n" +
                "CASE WHEN pharm.refill_period IS NOT NULL AND st.status = 'Active'THEN 0 ELSE NULL END  AS refillMonth, CASE WHEN pharm.visit_date IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS drug_visit_date, \n" +
                "\tCASE WHEN pharm.regimen IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS regimen, CASE WHEN e.date_started IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END as start_date,\n" +
                "CASE WHEN e.date_confirmed_hiv IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS hiv_confirm_date, CASE WHEN e.target_group_id IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END as target_group, CASE WHEN e.entry_point_id IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS entryPoint,\n" +
                "CASE WHEN ca.visit_date IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END as commence_date,  CASE WHEN e.time_hiv_diagnosis IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END  as hivDiagnose, CASE WHEN CAST(EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS age,\n" +
                "preg.pregnancy_status as pregStatus, CASE WHEN tri.body_weight IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END as weight, CASE WHEN tri.visit_date IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS visit_date,\n" +
                "CASE WHEN preg.pregnancy_status IS NOT NULL AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 AND INITCAP(p.sex) = 'Female' AND st.status = 'Active' THEN 1 ELSE NULL END AS adultPre,\n" +
                "CASE WHEN CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12 AND INITCAP(p.sex) = 'Female'AND st.status = 'Active' THEN 1 ELSE NULL END AS adultAge, st.status\n" +
                "   FROM patient_person p\n" +
                "   INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "   LEFT JOIN\n" +
                "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "   GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
                "\tLEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\n" +
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
                "e.time_hiv_diagnosis,preg.pregnancy_status, tri.body_weight, tri.visit_date, pharm.regimen, st.status\n" +
                "   ORDER BY p.id DESC\n" +
                ")\n" +
                "      SELECT\n" +
                "    COUNT(refillMonth) AS refillMonthNumerator,\n" +
                "    COUNT(hospitalNumber) AS refillMonthDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(refillMonth) AS refillMonthVariance,\n" +
                "    ROUND((CAST(COUNT(refillMonth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS refillMonthPerformance,\n" +
                "COUNT(regimen) AS regimenNumerator,\n" +
                "    COUNT(hospitalNumber) AS regimenDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(regimen) AS regimenVariance,\n" +
                "    ROUND((CAST(COUNT(regimen) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS regimenPerformance,\n" +
                "COUNT(start_date) AS startDateNumerator,\n" +
                "    COUNT(hospitalNumber) AS startDateDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(start_date) AS startDateVariance,\n" +
                "    ROUND((CAST(COUNT(start_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS startDatePerformance,\n" +
                "COUNT(hiv_confirm_date) AS confirmDateNumerator,\n" +
                "    COUNT(hospitalNumber) AS confirmDateDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(hiv_confirm_date) AS confirmDateVariance,\n" +
                "    ROUND((CAST(COUNT(hiv_confirm_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS confirmDatePerformance,\n" +
                "COUNT(target_group) AS targNumerator,\n" +
                "    COUNT(hospitalNumber) AS targDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(target_group) AS targVariance, \n" +
                "    ROUND((CAST(COUNT(target_group) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS targPerformance,\n" +
                "COUNT(entryPoint) AS entryNumerator,\n" +
                "    COUNT(hospitalNumber) AS entryDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(entryPoint) AS entryVariance,\n" +
                "    ROUND((CAST(COUNT(entryPoint) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS entryPerformance,\n" +
                "COUNT(commence_date) AS commencedNumerator,\n" +
                "    COUNT(hospitalNumber) AS commencedDenominator,\n" +
                "   COUNT(hospitalNumber) - COUNT(commence_date) AS commencedVariance, \n" +
                "    ROUND((CAST(COUNT(commence_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS commencedPerformance,\n" +
                "COUNT(start_date) AS enrolledDateNumerator,\n" +
                "    COUNT(hospitalNumber) AS enrolledDateDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(start_date) AS enrolledDateVariance,\n" +
                "    ROUND((CAST(COUNT(start_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS enrolledDatePerformance,\n" +
                "COUNT(hivDiagnose) AS diagnoseNumerator,\n" +
                "    COUNT(hospitalNumber) AS diagnoseDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(hivDiagnose) AS diagnoseVariance,\n" +
                "    ROUND((CAST(COUNT(hivDiagnose) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS diagnosePerformance,\n" +
                "COUNT(adultPre) AS pregNumerator,\n" +
                "    COUNT(adultAge) AS pregDenominator,\n" +
                "COUNT(adultAge) - COUNT(adultPre) AS pregVariance,\n" +
                "    ROUND((CAST(COUNT(adultPre) AS DECIMAL) / COUNT(adultAge)) * 100, 2) AS pregPerformance,\n" +
                "COUNT(weight) AS weightNumerator,\n" +
                "    COUNT(hospitalNumber) AS weightDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(weight) AS weightVariance,\n" +
                "    ROUND((CAST(COUNT(weight) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS weightPerformance,\n" +
                "COUNT(visit_date) AS lastVisitNumerator,\n" +
                "    COUNT(hospitalNumber) AS lastVisitDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(visit_date) AS lastVisitVariance,\n" +
                "    ROUND((CAST(COUNT(visit_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lastVisitPerformance,\n" +
                "COUNT(age) AS ageNumerator,\n" +
                "    COUNT(hospitalNumber) AS ageDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(age) AS ageVariance,\n" +
                "    ROUND((CAST(COUNT(age) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS agePerformance,\n" +
                "COUNT(drug_visit_date) AS lastPickNumerator,\n" +
                "    COUNT(hospitalNumber) AS lastPickDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(drug_visit_date) AS lastPickVariance,\n" +
                "    ROUND((CAST(COUNT(drug_visit_date) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS lastPickPerformance\n" +
                "   FROM\n" +
                "   PatientClinic";
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
                "    COUNT(hospitalNumber) - COUNT(age) AS ageVariance,\n" +
                "    ROUND((CAST(COUNT(age) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS agePerformance,\n" +
                "    COUNT(sex) AS sexNumerator,\n" +
                "    COUNT(hospitalNumber) AS sexDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(sex) AS sexVariance,\n" +
                "    ROUND((CAST(COUNT(sex) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS sexPerformance,\n" +
                "    COUNT(dateOfBirth) AS dobNumerator,\n" +
                "    COUNT(hospitalNumber) AS dobDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(dateOfBirth) AS dobVariance,\n" +
                "    ROUND((CAST(COUNT(dateOfBirth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS dobPerformance,\n" +
                "    COUNT(marital_status) AS maritalNumerator,\n" +
                "    COUNT(hospitalNumber) AS maritalDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(marital_status) AS maritalVariance,\n" +
                "    ROUND((CAST(COUNT(marital_status) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS maritalPerformance,\n" +
                "    COUNT(education) AS eduNumerator,\n" +
                "    COUNT(hospitalNumber) AS eduDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(education) AS eduVariance,\n" +
                "    ROUND((CAST(COUNT(education) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS eduPerformance,\n" +
                "    COUNT(employment) AS employNumerator,\n" +
                "    COUNT(hospitalNumber) AS employDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(employment) AS employVariance,\n" +
                "    ROUND((CAST(COUNT(employment) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS employPerformance,\n" +
                "    COUNT(address) AS addressNumerator,\n" +
                "    COUNT(hospitalNumber) AS addressDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(address) AS addressVariance,\n" +
                "    ROUND((CAST(COUNT(address) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS addressPerformance,\n" +
                "    COUNT(patientId) AS pIdNumerator,\n" +
                "    COUNT(hospitalNumber) AS pIdDenominator,\n" +
                "    COUNT(hospitalNumber) - COUNT(patientId) AS pIdVariance,\n" +
                "    ROUND((CAST(COUNT(patientId) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS pIdPerformance\n" +
                "FROM\n" +
                "    PatientSummary;\n";

    }

    public static class BiometricQueries {

        public static final String BIOMETRIC_SUMMARY_QUERIES = "WITH PatientBiometrics AS (\n" +
                "SELECT  e.unique_id AS patientId , CASE WHEN p.hospital_number IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
                "p.date_of_birth AS dateOfBirth, CASE WHEN b.person_uuid IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS person_uuid1, CASE WHEN b.biometric_valid_captured IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS validcapture,\n" +
                "b.person_uuid AS personId, CASE WHEN bb.recapture IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS recapture, bb.person_uuid, CASE WHEN bb.biometric_valid_captured IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS validrecap\n" +
                "   FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "   LEFT JOIN\n" +
                "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "   GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
                "LEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\t\n" +
                "  LEFT JOIN (\n" +
                " SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
                "  WHERE archived != 1 GROUP BY person_uuid, recapture) b ON e.person_uuid = b.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
                " WHERE archived != 1 AND recapture != 0 GROUP BY person_uuid, recapture) bb ON e.person_uuid = bb.person_uuid\n" +
                "   LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
                "   WHERE p.archived=0 AND p.facility_id= ?1 \n" +
                "AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
                "   GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured,bb.biometric_valid_captured, b.person_uuid,\n" +
                "bb.recapture, bb.person_uuid, st.status\n" +
                "   ORDER BY p.id DESC\n" +
                ")\n" +
                "SELECT\n" +
                "    COUNT(person_uuid1) AS captureNumerator,\n" +
                "    COUNT(hospitalNumber) AS captureDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(person_uuid1) AS captureVariance,\n" +
                "    ROUND((CAST(COUNT(person_uuid1) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS capturePerformance,\n" +
                "COUNT(validcapture) AS validcapNumerator,\n" +
                "    COUNT(person_uuid1) AS validcapDenominator,\n" +
                "COUNT(person_uuid1) - COUNT(validcapture) validcapVariance,\n" +
                "    ROUND((CAST(COUNT(validcapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS validcapPerformance,\n" +
                "COUNT(recapture) AS recapNumerator,\n" +
                "    COUNT(person_uuid1) AS recapDenominator,\n" +
                "COUNT(person_uuid1) - COUNT(recapture) AS recapVariance,\n" +
                "    ROUND((CAST(COUNT(recapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS recapPerformance,\n" +
                "COUNT(validrecap) AS validRecapNumerator,\n" +
                "    COUNT(recapture) AS validRecapDenominator,\n" +
                "COUNT(recapture) - COUNT(validrecap) AS validRecapVariance,\n" +
                "    ROUND((CAST(COUNT(validrecap) AS DECIMAL) / COUNT(recapture)) * 100, 2) AS validRecapPerformance\n" +
                "FROM\n" +
                " PatientBiometrics";

        public static final String GET_PATIENTS_NOT_CAPTURE = "";
    }

    public static class ClientVerificationQueries {

        public static final String CLIENT_VERIFICATION = "WITH clientVerification AS (\n" +
                "SELECT  e.unique_id AS patientId ,CASE WHEN p.hospital_number IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS hospitalNumber, INITCAP(p.sex) AS sex,\n" +
                "p.date_of_birth AS dateOfBirth, CASE WHEN b.person_uuid IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS person_uuid1, b.enrollment_date, b.biometric_valid_captured AS validcapture,recap.person_uuid AS personUuid,recap.enrollment_date,\n" +
                "CASE WHEN (lastVisit.visit_date > recap.enrollment_date) AND b.enrollment_date IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS clinicNoRecapture,\n" +
                "recap.biometric_valid_captured AS recapture, lastVisit.visit_date AS lastClinicVisit, lastVisit.monthTillDate,\n" +
                "CASE WHEN lastVisit.monthTillDate >= 15 AND st.status = 'Active' THEN 1 ELSE NULL END AS lastClinicMonth,\n" +
                "CASE WHEN pickUp.monthApart >= 12 AND st.status = 'Active' THEN 1 ELSE NULL END AS pickUpOneYear,\n" +
                "CASE WHEN b.person_uuid IS NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS noBaseline,\n" +
                "CASE WHEN b.person_uuid IS NOT NULL AND recap.person_uuid IS NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS hasBaseLineNoRecapture,\n" +
                "CASE WHEN sameDemographics.uuid IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS sameDemographic, CASE WHEN sameClinical.person_uuid IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS dupClinical,\n" +
                "CASE WHEN incompleteEncounter.person_uuid IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS incomplete, \n" +
                "sampleCol.dateOfViralLoadSampleCollection, ca.visit_date,\n" +
                "CASE WHEN\n" +
                "CAST(DATE_PART('year', AGE(NOW(), ca.visit_date)) * 12 + DATE_PART('month', AGE(NOW(), ca.visit_date)) AS INTEGER ) >= 6 AND sampleCol.dateOfViralLoadSampleCollection IS NULL AND st.status = 'Active' THEN 1 ELSE NULL END AS vlPrior\n" +
                "   FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "   INNER JOIN\n" +
                "   (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "   GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
                "LEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\n" +
                "  LEFT JOIN (\n" +
                " SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, enrollment_date, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
                "  WHERE archived = 0 AND recapture = 0  GROUP BY person_uuid, recapture, enrollment_date) b ON  b.person_uuid = ca.person_uuid\n" +
                "LEFT JOIN (\n" +
                " SELECT DISTINCT ON (person_uuid) person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, enrollment_date, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured, recapture FROM biometric\n" +
                "  WHERE archived = 0 AND recapture != 0  GROUP BY person_uuid, recapture, enrollment_date) recap ON  recap.person_uuid = ca.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT\n" +
                "DISTINCT ON (p1.HOSPITAL_NUMBER)\n" +
                "    p1.HOSPITAL_NUMBER,\n" +
                "    INITCAP(p1.sex) AS sex,\n" +
                "    p1.date_of_birth,\n" +
                "p1.uuid,\n" +
                "ca1.person_uuid,\n" +
                "    ca1.visit_date,\n" +
                "    p2.HOSPITAL_NUMBER AS matching_hospital_number,\n" +
                "    INITCAP(p2.sex) AS matching_sex,\n" +
                "    p2.date_of_birth AS matching_date_of_birth,\n" +
                "    ca2.visit_date AS matching_visit_date\n" +
                "FROM\n" +
                "    PATIENT_PERSON p1\n" +
                "JOIN\n" +
                "    PATIENT_PERSON p2 ON p1.HOSPITAL_NUMBER = p2.HOSPITAL_NUMBER\n" +
                "    AND INITCAP(p1.sex) = INITCAP(p2.sex)\n" +
                "    AND p1.date_of_birth = p2.date_of_birth\n" +
                "    AND p1.uuid <> p2.uuid  -- Exclude the same row\n" +
                "INNER JOIN (\n" +
                "    SELECT TRUE AS commenced, hac1.person_uuid, hac1.visit_date\n" +
                "    FROM hiv_art_clinical hac1\n" +
                "    WHERE hac1.archived = 0 AND hac1.is_commencement IS TRUE\n" +
                "    GROUP BY hac1.person_uuid, hac1.visit_date\n" +
                ") ca1 ON ca1.person_uuid = p1.uuid\n" +
                "INNER JOIN (\n" +
                "    SELECT TRUE AS commenced, hac2.person_uuid, hac2.visit_date\n" +
                "    FROM hiv_art_clinical hac2\n" +
                "    WHERE hac2.archived = 0 AND hac2.is_commencement IS TRUE\n" +
                "    GROUP BY hac2.person_uuid, hac2.visit_date\n" +
                ") ca2 ON ca2.person_uuid = p2.uuid\n" +
                ") sameDemographics ON ca.person_uuid = sameDemographics.uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT * FROM (\n" +
                "SELECT \n" +
                "DISTINCT ON (person_uuid)\n" +
                "person_uuid, visit_date,next_appointment, tb_status,\n" +
                "CAST(DATE_PART('year', AGE(now(), visit_date)) * 12 + DATE_PART('month', AGE(now(), visit_date)) AS INTEGER ) AS monthTillDate,\n" +
                "ROW_NUMBER() OVER ( PARTITION BY person_uuid ORDER BY visit_date DESC)\n" +
                "FROM HIV_ART_CLINICAL \n" +
                "WHERE archived = 0\n" +
                ") visit where row_number = 1\n" +
                ") lastVisit ON ca.person_uuid = lastVisit.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT person_uuid, visit_date, previousPickUpDrugDate,\n" +
                "CAST(DATE_PART('year', AGE(visit_date, previousPickUpDrugDate)) * 12 + DATE_PART('month', AGE(visit_date, previousPickUpDrugDate)) AS INTEGER ) AS monthApart\n" +
                "FROM (\n" +
                "select person_uuid, visit_date,\n" +
                "ROW_NUMBER() OVER (PARTITION BY person_uuid ORDER BY visit_date DESC),\n" +
                "LEAD(visit_date) OVER (PARTITION BY person_uuid ORDER BY visit_date DESC) AS previousPickUpDrugDate\n" +
                "from hiv_art_pharmacy\n" +
                ") pharm where\n" +
                "row_number = 1\n" +
                ") pickUp ON ca.person_uuid = pickUP.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT\n" +
                "DISTINCT ON (person_uuid)\n" +
                "    person_uuid,\n" +
                "    visit_date,\n" +
                "    next_appointment\n" +
                "FROM\n" +
                "    HIV_ART_CLINICAL\n" +
                "WHERE\n" +
                "    archived = 0\n" +
                "    AND (person_uuid, visit_date, next_appointment) IN (\n" +
                "        SELECT\n" +
                "            person_uuid,\n" +
                "            visit_date,\n" +
                "            next_appointment\n" +
                "        FROM\n" +
                "            HIV_ART_CLINICAL\n" +
                "        WHERE\n" +
                "            archived = 0\n" +
                "        GROUP BY\n" +
                "            person_uuid,\n" +
                "            visit_date,\n" +
                "            next_appointment\n" +
                "        HAVING\n" +
                "            COUNT(*) > 1\n" +
                "    )\n" +
                ") sameClinical ON ca.person_uuid = sameClinical.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT DISTINCT ON (hap.person_uuid) hap.person_uuid,hac.pregnancy_status ,hac.next_appointment, hac.tb_status ,\n" +
                "hap.next_appointment , hap.extra IS ,hap.refill_period FROM hiv_art_clinical hac\n" +
                "LEFT JOIN HIV_ART_PHARMACY hap ON hap.person_uuid = hac.person_uuid\n" +
                "WHERE hap.archived = 0 AND hac.archived = 0 AND (hac.pregnancy_status IS NULL OR hac.next_appointment IS NULL OR hac.tb_status IS NULL OR\n" +
                "hap.next_appointment IS NULL OR hap.extra IS NULL OR hap.refill_period IS NULL)\n" +
                ") incompleteEncounter ON ca.person_uuid = incompleteEncounter.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT CAST(sample.date_sample_collected AS DATE ) as dateOfViralLoadSampleCollection, patient_uuid as person_uuid1  FROM (\n" +
                "     SELECT lt.viral_load_indication, sm.facility_id,sm.date_sample_collected, sm.patient_uuid, sm.archived, ROW_NUMBER () OVER (PARTITION BY sm.patient_uuid ORDER BY date_sample_collected DESC) as rnkk\n" +
                "     FROM public.laboratory_sample  sm\n" +
                "  INNER JOIN public.laboratory_test lt ON lt.id = sm.test_id\n" +
                "     WHERE lt.lab_test_id=16\n" +
                "       AND  lt.viral_load_indication !=719\n" +
                "       AND date_sample_collected IS NOT null\n" +
                " )as sample\n" +
                "         WHERE sample.rnkk = 1\n" +
                "           AND (sample.archived is null OR sample.archived = 0)\n" +
                ") sampleCol ON ca.person_uuid = sampleCol.person_uuid1\n" +
                "\n" +
                "WHERE p.archived=0 AND p.facility_id= ?1\n" +
                "--    AND CAST (EXTRACT(YEAR from AGE(NOW(), date_of_birth)) AS INTEGER) > 12\n" +
                "   GROUP BY e.id, p.id, p.hospital_number, p.date_of_birth, b.biometric_valid_captured, b.person_uuid, \n" +
                "recap.biometric_valid_captured, recap.person_uuid, sameDemographics.uuid, lastVisit.visit_date,\n" +
                "lastVisit.monthTillDate, pickUp.visit_date,pickUp.monthApart, pickUp.previousPickUpDrugDate, b.enrollment_date, recap.enrollment_date,\n" +
                "sameClinical.person_uuid, incompleteEncounter.person_uuid, sampleCol.dateOfViralLoadSampleCollection, ca.visit_date, st.status\n" +
                "   ORDER BY p.id DESC\n" +
                ")\n" +
                "SELECT\n" +
                "COUNT(noBaseline) AS noBaseLineNumerator,\n" +
                "COUNT(hospitalNumber) AS noBaseLineDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(noBaseline) AS noBaselineVariance,\n" +
                "ROUND((CAST(COUNT(noBaseline) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS noBaseLinePerformance,\n" +
                "COUNT(hasBaseLineNoRecapture) AS hasBaseLineNoRecaptureNumerator,\n" +
                "COUNT(person_uuid1) AS hasBaseLineNoRecaptureDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(hasBaseLineNoRecapture) AS hasBaseLineNoRecaptureVariance,\n" +
                "ROUND((CAST(COUNT(hasBaseLineNoRecapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS hasBaseLineNoRecapturePerformance,\n" +
                "COUNT(sameDemographic) AS sameDemographicsNumerator,\n" +
                "COUNT(hospitalNumber) AS sameDemographicsDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(sameDemographic) AS sameDemographicsVariance,\n" +
                "ROUND((CAST(COUNT(sameDemographic) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS sameDemographicsPerformance,\n" +
                "COUNT(lastClinicMonth) AS clinicMoreThanOneYearNumerator,\n" +
                "COUNT(hospitalNumber) AS clinicMoreThanOneYearDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(lastClinicMonth) AS clinicMoreThanOneYearVariance,\n" +
                "ROUND((CAST(COUNT(lastClinicMonth) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS clinicMoreThanOneYearPerformance,\n" +
                "COUNT(pickUpOneYear) AS pickUpOneYearNumerator,\n" +
                "COUNT(hospitalNumber) AS pickUpOneYearDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(pickUpOneYear) AS pickUpOneYearVariance,\n" +
                "ROUND((CAST(COUNT(pickUpOneYear) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS pickUpOneYearPerformance,\n" +
                "COUNT(clinicNoRecapture) AS clinicNoRecaptureNumerator,\n" +
                "COUNT(person_uuid1) AS clinicNoRecaptureDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(clinicNoRecapture) AS clinicNoRecaptureVariance,\n" +
                "ROUND((CAST(COUNT(clinicNoRecapture) AS DECIMAL) / COUNT(person_uuid1)) * 100, 2) AS clinicNoRecapturePerformance,\n" +
                "COUNT(dupClinical) AS sameClinicalNumerator,\n" +
                "COUNT(hospitalNumber) AS sameClinicalDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(dupClinical) AS sameClinicalVariance,\n" +
                "ROUND((CAST(COUNT(dupClinical) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS sameClinicalPerformance, \n" +
                "COUNT(incomplete) AS incompleteNumerator,\n" +
                "COUNT(hospitalNumber) AS incompleteDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(incomplete) AS incompleteVariance,\n" +
                "ROUND((CAST(COUNT(incomplete) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS incompletePerformance,\n" +
                "COUNT(vlPrior) AS labNumerator,\n" +
                "COUNT(hospitalNumber) AS labDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(vlPrior) AS labVariance,\n" +
                "ROUND((CAST(COUNT(vlPrior) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS labPerformance\n" +
                "FROM\n" +
                "    clientVerification";

    }

    public static class DataValidityQueries{

        public static final String DATA_VALIDITY_QUERY = "WITH validitySummary AS (\n" +
                "  SELECT e.unique_id AS patientId ,CASE WHEN p.hospital_number IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS hospitalNumber, INITCAP(p.sex) AS sex\n" +
                ",p.date_of_birth AS dateOfBirth, CASE WHEN b.biometric_valid_captured IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS validBio, \n" +
                "\tCASE WHEN pharm.refill_period IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS refill_period,\n" +
                "CASE WHEN vl.dateOfLastViralLoad IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS dateOfLastViralLoad, \n" +
                "CASE WHEN EXTRACT(YEAR FROM e.date_confirmed_hiv) BETWEEN 1985 AND EXTRACT(YEAR FROM NOW()) AND st.status = 'Active'\n" +
                "THEN EXTRACT(YEAR FROM e.date_confirmed_hiv) ELSE NULL END AS confirmed, \n" +
                "CASE WHEN e.date_confirmed_hiv IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS hivConfirm,\n" +
                "CASE WHEN EXTRACT(YEAR FROM e.date_started) BETWEEN 1985 AND EXTRACT(YEAR FROM NOW()) AND st.status = 'Active'\n" +
                "     THEN EXTRACT(YEAR FROM e.date_started) ELSE NULL END AS start_date, CASE WHEN e.date_started IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS date_started,\n" +
                "CASE WHEN CAST(EXTRACT(YEAR from AGE(NOW(), p.date_of_birth)) AS INTEGER) BETWEEN 0 AND 90 AND st.status = 'Active'\n" +
                "     THEN EXTRACT(YEAR FROM p.date_of_birth) ELSE NULL END AS ageInitiated,\n" +
                "CASE WHEN EXTRACT(YEAR FROM p.date_of_birth) > 1920 AND st.status = 'Active' THEN 1 ELSE NULL END AS normalDob\n" +
                "\n" +
                "  FROM patient_person p INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "  LEFT JOIN\n" +
                "  (SELECT TRUE as commenced, hac.person_uuid, hac.visit_date, hac.pregnancy_status  FROM hiv_art_clinical hac WHERE hac.archived=0 AND hac.is_commencement is true\n" +
                "  GROUP BY hac.person_uuid, hac.visit_date, hac.pregnancy_status)ca ON p.uuid = ca.person_uuid\n" +
                "LEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\n" +
                " LEFT JOIN (\n" +
                "SELECT person_uuid, COUNT(biometric_type) AS biometric_fingers_captured, COUNT(*) FILTER (WHERE ENCODE(CAST(template AS BYTEA), 'hex') LIKE '46%') AS biometric_valid_captured FROM biometric\n" +
                " WHERE archived != 1 GROUP BY person_uuid) b ON p.uuid = b.person_uuid\n" +
                "LEFT JOIN\n" +
                "  (SELECT DISTINCT ON (person_uuid)\n" +
                "    person_uuid, refill_period, extra\n" +
                "FROM ( select person_uuid, refill_period,  extra from hiv_art_pharmacy\n" +
                "  where archived = 0 AND refill_period  BETWEEN 14 AND 180\n" +
                "GROUP BY refill_period, person_uuid, extra ORDER BY person_uuid DESC ) fi ORDER BY\n" +
                "    person_uuid DESC ) pharm ON pharm.person_uuid = p.uuid\n" +
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
                "WHERE  lo.archived=0 AND\n" +
                "lr.date_result_reported IS NOT NULL \n" +
                "   AND\n" +
                "    EXTRACT(YEAR FROM lr.date_result_reported) BETWEEN 1985 AND EXTRACT(YEAR FROM NOW())\n" +
                ") vl ON e.person_uuid = vl.person_uuid\n" +
                "  LEFT JOIN base_application_codeset pc on pc.id = e.status_at_registration_id\n" +
                "  WHERE p.archived=0 AND p.facility_id= ?1 \n" +
                "  GROUP BY e.id, ca.commenced, p.id, pc.display, p.hospital_number, p.date_of_birth, ca.visit_date, ca.pregnancy_status, b.biometric_fingers_captured, b.biometric_valid_captured,\n" +
                "  pharm.refill_period, vl.dateOfLastViralLoad, e.date_confirmed_hiv, st.status\n" +
                "  ORDER BY p.id DESC   \n" +
                ") \n" +
                "SELECT\n" +
                "COUNT(validBio) AS bioNumerator,\n" +
                "COUNT(hospitalNumber) AS bioDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(validBio) AS bioVariance,\n" +
                "ROUND((CAST(COUNT(validBio) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS bioPerformance,\n" +
                "COUNT(refill_period) AS regimenNumerator,\n" +
                "COUNT(hospitalNumber) AS regimenDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(refill_period) AS regimenVariance,\n" +
                "ROUND((CAST(COUNT(refill_period) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS regimenPerformance,\n" +
                "COUNT(dateOfLastViralLoad) AS vlDateNumerator,\n" +
                "COUNT(hospitalNumber) AS vlDateDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(dateOfLastViralLoad) AS vlDateVariance,\n" +
                "ROUND((CAST(COUNT(dateOfLastViralLoad) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS vlDatePerformance,\n" +
                "COUNT(confirmed) AS hivDateNumerator,\n" +
                "COUNT(hivConfirm) AS hivDateDenominator,\n" +
                "COUNT(hivConfirm) - COUNT(confirmed) AS hivDateVariance,\n" +
                "ROUND((CAST(COUNT(confirmed) AS DECIMAL) / COUNT(hivConfirm)) * 100, 2) AS hivDatePerformance,\n" +
                "COUNT(start_date) AS startDateNumerator,\n" +
                "COUNT(date_started) AS startDateDenominator,\n" +
                "COUNT(date_started) - COUNT(start_date) AS startDateVariance,\n" +
                "ROUND((CAST(COUNT(start_date) AS DECIMAL) / COUNT(date_started)) * 100, 2) AS startDatePerformance,\n" +
                "COUNT(ageInitiated) AS ageInitiatedNumerator,\n" +
                "COUNT(hospitalNumber) AS ageInitiatedDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(ageInitiated) AS ageInitiatedVariance,\n" +
                "ROUND((CAST(COUNT(ageInitiated) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS ageInitiatedPerformance,\n" +
                "COUNT(normalDob) AS normalDobNumerator,\n" +
                "COUNT(hospitalNumber) AS normalDobDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(normalDob) AS normalDobVariance,\n" +
                "ROUND((CAST(COUNT(normalDob) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2) AS normalDobPerformance\n" +
                "FROM\n" +
                "    validitySummary";

    }

    public static class TBQueries{

        public static final String TB_SUMMARY_QUERY = "WITH tbSummary AS (\n" +
                "SELECT\n" +
                "    e.unique_id AS patientId,\n" +
                "    CASE WHEN p.hospital_number IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS hospitalNumber,\n" +
                "    INITCAP(p.sex) AS sex,\n" +
                "    CAST(EXTRACT(YEAR FROM AGE(NOW(), p.date_of_birth)) AS INTEGER) AS age,\n" +
                "    p.date_of_birth AS dateOfBirth,\n" +
                "    tpt.date_of_observation,\n" +
                "    tpt.data->'tptMonitoring'->> 'date' AS tptDate,\n" +
                "    CASE WHEN tpt.data->'tbIptScreening'->>'fever' IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END  AS doctb,\n" +
                "    CASE WHEN tpt.data->'tbIptScreening'->>'outcome' IS NOT NULL AND st.status = 'Active' THEN 0 ELSE NULL END AS tboutcome,\n" +
                "    tpt.data->'tbIptScreening'->>'tbTreatment' AS tbtreat,\n" +
                "    tpt.data->'tbIptScreening'->>'completionDate' AS sampleDate,\n" +
                "    tpt.data->'tbIptScreening'->>'treatementType' AS treatmenttype,\n" +
                "    tpt.data->'tbIptScreening'->>'eligibleForTPT' AS tptEligible,\n" +
                "    tpt.data->'tbIptScreening'->>'completionDate' AS tptcompletionDate,\n" +
                "tpt.data->'tbIptScreening'->>'treatmentOutcome' AS tbstatus,\n" +
                "(CASE WHEN \n" +
                "tpt.data->'tbIptScreening'->>'outcome' IS NOT NULL AND tpt.data->'tbIptScreening'->>'outcome' <> '' AND\n" +
                "tpt.data->'tbIptScreening'->>'fever' IS NOT NULL AND tpt.data->'tbIptScreening'->>'fever' <> '' AND st.status = 'Active'\n" +
                "THEN 1  ELSE NULL END) AS completeAnddoc,\n" +
                "(CASE WHEN tpt.data->'tbIptScreening'->>'outcome' ILIKE '%Presumptive TB case%' THEN 1 ELSE NULL END) AS presumptivetb,\n" +
                "(CASE WHEN  (tpt.data->'tbIptScreening'->>'outcome' ILIKE '%Presumptive TB case%' \n" +
                "AND (tpt.data->'tbIptScreening'->>'completionDate'  IS NOT NULL OR tpt.data->'tbIptScreening'->>'completionDate' <> '')) AND st.status = 'Active' THEN 1 ELSE NULL END) AS prsmptivecollection,\n" +
                "(CASE WHEN  (tpt.data->'tbIptScreening'->>'outcome' ILIKE '%Presumptive TB case%' \n" +
                "AND (tpt.data->'tbIptScreening'->>'completionDate'  IS NOT NULL OR tpt.data->'tbIptScreening'->>'completionDate' <> '') AND \n" +
                "tpt.data->'tbIptScreening'->>'treatementType' IS NOT NULL) AND st.status = 'Active' THEN 1 ELSE NULL END) AS prsmptivectionsamp,\n" +
                "(CASE WHEN tpt.data->'tbIptScreening'->>'tbTreatment' = 'Yes' THEN 1 ELSE NULL END) AS tbtreatyes,\n" +
                "(CASE WHEN tpt.data->'tbIptScreening'->>'tbTreatment' = 'Yes' AND (tpt.data->'tbIptScreening'->>'outcome' IS NOT NULL OR tpt.data->'tbIptScreening'->>'outcome' <>'') THEN 1 ELSE NULL END) AS tbtreatwithoutcome,\n" +
                "(CASE WHEN tpt.data->'tbIptScreening'->>'eligibleForTPT' = 'Yes' AND st.status = 'Active' THEN 1 ELSE NULL END) AS eligibeipt,\n" +
                "(CASE WHEN tpt.data->'tbIptScreening'->>'eligibleForTPT' = 'Yes' AND (tpt.data->'tptMonitoring'->> 'date' IS NOT NULL OR\n" +
                "tpt.data->'tptMonitoring'->> 'date' <> '') AND st.status = 'Active' THEN 1 ELSE NULL END) AS iptStartDate,\n" +
                "(CASE WHEN hap.lastV >= NOW() - INTERVAL '6 MONTH' AND hap.lastV <= NOW()  THEN 1 ELSE null END) AS hadVl6month,\n" +
                "ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate,\n" +
                "(CASE WHEN tpt.data->'tbIptScreening'->>'eligibleForTPT' = 'Yes' AND ipt.dateOfIptStart IS NOT NULL AND st.status = 'Active' THEN 1 ELSE NULL END) AS iptEliStart,\n" +
                "(CASE WHEN ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW() AND st.status = 'Active' THEN 1 ELSE null END) AS ipt6month,\n" +
                "(CASE WHEN (ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()) AND ipt.iptCompletionDate IS NOT NULL AND st.status = 'Active' THEN 1 ELSE null END) AS ipt6monthCompl,\n" +
                "ipt.iptCompletionStatus,\n" +
                "(CASE WHEN (ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()) AND ipt.iptCompletionStatus IS NOT NULL AND st.status = 'Active' THEN 1 ELSE null END) AS iptStatus,\n" +
                "(CASE WHEN (ipt.dateOfIptStart >= NOW() - INTERVAL '6 MONTH' AND ipt.dateOfIptStart <= NOW()) AND ipt.iptType IS NOT NULL AND st.status = 'Active' THEN 1 ELSE null END) AS iptTypeStatus\n" +
                "FROM\n" +
                "    patient_person p\n" +
                "INNER JOIN hiv_enrollment e ON p.uuid = e.person_uuid\n" +
                "LEFT JOIN \n" +
                "(\n" +
                "SELECT DISTINCT ON (person_uuid) PERSON_UUID, MAX(VISIT_DATE) AS lastV FROM HIV_ART_PHARMACY\n" +
                "GROUP BY PERSON_UUID, visit_date ORDER BY person_uuid, visit_date DESC\n" +
                ") hap ON p.uuid = hap.person_uuid\n" +
                "LEFT JOIN (\n" +
                "    SELECT DISTINCT ON (person_uuid)\n" +
                "        person_uuid,\n" +
                "        MAX(date_of_observation) AS date_of_observation,\n" +
                "       data\n" +
                "FROM\n" +
                "        hiv_observation\n" +
                "    WHERE\n" +
                "        type = 'Chronic Care'\n" +
                "    GROUP BY\n" +
                "        person_uuid,\n" +
                "        date_of_observation,\n" +
                "        type,\n" +
                "        data\n" +
                ") AS tpt ON e.person_uuid = tpt.person_uuid\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        TRUE AS commenced,\n" +
                "        hac.person_uuid\n" +
                "    FROM\n" +
                "        hiv_art_clinical hac\n" +
                "    WHERE\n" +
                "        hac.archived = 0\n" +
                "        AND hac.is_commencement IS TRUE\n" +
                "    GROUP BY\n" +
                "        hac.person_uuid\n" +
                ") ca ON p.uuid = ca.person_uuid\n" +
                "LEFT JOIN \n" +
                "    (\n" +
                "        SELECT \n" +
                "            personUuid, \n" +
                "            status \n" +
                "        FROM \n" +
                "            (\n" +
                "                SELECT DISTINCT ON (pharmacy.person_uuid) \n" +
                "                    pharmacy.person_uuid AS personUuid,\n" +
                "                    (\n" +
                "                        CASE\n" +
                "                            WHEN stat.hiv_status ILIKE '%DEATH%' OR stat.hiv_status ILIKE '%Died%' THEN 'Died'\n" +
                "                            WHEN (stat.status_date > pharmacy.maxdate AND (stat.hiv_status ILIKE '%stop%' OR stat.hiv_status ILIKE '%out%' OR stat.hiv_status ILIKE '%Invalid %')) THEN stat.hiv_status\n" +
                "                            ELSE pharmacy.status\n" +
                "                        END\n" +
                "                    ) AS status,\n" +
                "                    stat.cause_of_death, \n" +
                "                    stat.va_cause_of_death\n" +
                "                FROM \n" +
                "                    (\n" +
                "                        SELECT\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW() THEN 'IIT'\n" +
                "                                    ELSE 'Active'\n" +
                "                                END\n" +
                "                            ) status,\n" +
                "                            (\n" +
                "                                CASE\n" +
                "                                    WHEN hp.visit_date + hp.refill_period + INTERVAL '29 day' < NOW()  THEN hp.visit_date + hp.refill_period + INTERVAL '29 day'\n" +
                "                                    ELSE hp.visit_date\n" +
                "                                END\n" +
                "                            ) AS visit_date,\n" +
                "                            hp.person_uuid, \n" +
                "                            MAXDATE\n" +
                "                        FROM \n" +
                "                            hiv_art_pharmacy hp\n" +
                "                        INNER JOIN \n" +
                "                            (\n" +
                "                                SELECT \n" +
                "                                    hap.person_uuid, \n" +
                "                                    hap.visit_date AS MAXDATE, \n" +
                "                                    ROW_NUMBER() OVER (PARTITION BY hap.person_uuid ORDER BY hap.visit_date DESC) as rnkkk3\n" +
                "                                FROM \n" +
                "                                    public.hiv_art_pharmacy hap \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_art_pharmacy_regimens pr ON pr.art_pharmacy_id = hap.id \n" +
                "                                INNER JOIN \n" +
                "                                    hiv_enrollment h ON h.person_uuid = hap.person_uuid AND h.archived = 0 \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen r ON r.id = pr.regimens_id \n" +
                "                                INNER JOIN \n" +
                "                                    public.hiv_regimen_type rt ON rt.id = r.regimen_type_id \n" +
                "                                WHERE \n" +
                "                                    r.regimen_type_id IN (1,2,3,4,14) \n" +
                "                                    AND hap.archived = 0                \n" +
                "                            ) MAX ON MAX.MAXDATE = hp.visit_date AND MAX.person_uuid = hp.person_uuid AND MAX.rnkkk3 = 1\n" +
                "                        WHERE\n" +
                "                            hp.archived = 0\n" +
                "                    ) pharmacy\n" +
                "                LEFT JOIN \n" +
                "                    (\n" +
                "                        SELECT \n" +
                "                            hst.hiv_status,\n" +
                "                            hst.person_id,\n" +
                "                            hst.status_date,\n" +
                "                            hst.cause_of_death,\n" +
                "                            hst.va_cause_of_death\n" +
                "                        FROM \n" +
                "                            (\n" +
                "                                SELECT * FROM \n" +
                "                                    (\n" +
                "                                        SELECT \n" +
                "                                            DISTINCT (person_id) person_id, \n" +
                "                                            status_date, \n" +
                "                                            cause_of_death, \n" +
                "                                            va_cause_of_death,\n" +
                "                                            hiv_status, \n" +
                "                                            ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY status_date DESC)\n" +
                "                                        FROM \n" +
                "                                            hiv_status_tracker \n" +
                "                                        WHERE \n" +
                "                                            archived = 0 \n" +
                "                                    ) s\n" +
                "                                WHERE \n" +
                "                                    s.row_number = 1\n" +
                "                            ) hst\n" +
                "                        INNER JOIN \n" +
                "                            hiv_enrollment he ON he.person_uuid = hst.person_id\n" +
                "                    ) stat ON stat.person_id = pharmacy.person_uuid\n" +
                "            ) st\n" +
                "    ) st ON st.personUuid = e.person_uuid\n" +
                "LEFT JOIN (\n" +
                "SELECT\n" +
                "DISTINCT ON (hap.person_uuid) hap.person_uuid AS personUuid80,\n" +
                "ipt_type.regimen_name AS iptType,\n" +
                "hap.visit_date AS dateOfIptStart,\n" +
                "COALESCE(NULLIF(CAST(hap.ipt->>'completionStatus' AS text), ''), '') as iptCompletionStatus,\n" +
                "(\n" +
                "CASE\n" +
                "  WHEN MAX(CAST(complete.date_completed AS DATE)) > NOW() THEN NULL\n" +
                "  WHEN MAX(CAST(complete.date_completed AS DATE)) IS NULL\n" +
                "  AND CAST((hap.visit_date + 168) AS DATE) < NOW() THEN CAST((hap.visit_date + 168) AS DATE)\n" +
                "     ELSE MAX(CAST(complete.date_completed AS DATE))\n" +
                "     END\n" +
                "      ) AS iptCompletionDate\n" +
                "    FROM\n" +
                "   hiv_art_pharmacy hap\n" +
                "       INNER JOIN (\n" +
                "       SELECT\n" +
                "    DISTINCT person_uuid,\n" +
                "        MAX(visit_date) AS MAXDATE\n" +
                "       FROM\n" +
                "    hiv_art_pharmacy\n" +
                "       WHERE\n" +
                "    (ipt ->> 'type' ilike '%INITIATION%' or ipt ->> 'type' ilike 'START_REFILL')\n" +
                "  AND archived = 0\n" +
                "       GROUP BY\n" +
                "    person_uuid\n" +
                "       ORDER BY\n" +
                "    MAXDATE ASC\n" +
                "   ) AS max_ipt ON max_ipt.MAXDATE = hap.visit_date\n" +
                "       AND max_ipt.person_uuid = hap.person_uuid\n" +
                "       INNER JOIN (\n" +
                "       SELECT\n" +
                "    DISTINCT h.person_uuid,\n" +
                "        h.visit_date,\n" +
                "        CAST(pharmacy_object ->> 'regimenName' AS VARCHAR) AS regimen_name,\n" +
                "        CAST(pharmacy_object ->> 'duration' AS VARCHAR) AS duration,\n" +
                "        hrt.description\n" +
                "       FROM\n" +
                "    hiv_art_pharmacy h,\n" +
                "    jsonb_array_elements(h.extra -> 'regimens') WITH ORDINALITY p(pharmacy_object)\n" +
                "   RIGHT JOIN hiv_regimen hr ON hr.description = CAST(pharmacy_object ->> 'regimenName' AS VARCHAR)\n" +
                "   RIGHT JOIN hiv_regimen_type hrt ON hrt.id = hr.regimen_type_id\n" +
                "       WHERE\n" +
                "   hrt.id IN (15)\n" +
                "   ) AS ipt_type ON ipt_type.person_uuid = max_ipt.person_uuid\n" +
                "       AND ipt_type.visit_date = max_ipt.MAXDATE\n" +
                "       LEFT JOIN (\n" +
                "       SELECT\n" +
                "    hap.person_uuid,\n" +
                "    hap.visit_date,\n" +
                "   TO_DATE(NULLIF(NULLIF(TRIM(hap.ipt->>'dateCompleted'), ''), 'null'), 'YYYY-MM-DD') AS date_completed\n" +
                "       FROM\n" +
                "    hiv_art_pharmacy hap\n" +
                "   INNER JOIN (\n" +
                "   SELECT\n" +
                "       DISTINCT person_uuid,\n" +
                "    MAX(visit_date) AS MAXDATE\n" +
                "   FROM\n" +
                "       hiv_art_pharmacy\n" +
                "   WHERE\n" +
                "    ipt ->> 'dateCompleted' IS NOT NULL\n" +
                "   GROUP BY\n" +
                "       person_uuid\n" +
                "   ORDER BY\n" +
                "       MAXDATE ASC\n" +
                "    ) AS complete_ipt ON CAST(complete_ipt.MAXDATE AS DATE) = hap.visit_date\n" +
                "   AND complete_ipt.person_uuid = hap.person_uuid\n" +
                "   ) complete ON complete.person_uuid = hap.person_uuid\n" +
                "    WHERE\n" +
                "       hap.archived = 0\n" +
                "       AND hap.visit_date < CAST (NOW() AS DATE)\n" +
                "    GROUP BY\n" +
                "   hap.person_uuid,\n" +
                "   ipt_type.regimen_name,\n" +
                "   hap.ipt,\n" +
                "   hap.visit_date\n" +
                ") ipt ON e.person_uuid = ipt.personuuid80\n" +
                "LEFT JOIN base_application_codeset pc ON pc.id = e.status_at_registration_id\n" +
                "WHERE\n" +
                "    p.archived = 0\n" +
                "    AND p.facility_id = ?1\n" +
                "GROUP BY\n" +
                "    e.id,\n" +
                "    ca.commenced,\n" +
                "    p.id,\n" +
                "    pc.display,\n" +
                "    p.hospital_number,\n" +
                "    p.date_of_birth,\n" +
                "    tpt.data,\n" +
                "    tpt.date_of_observation,\n" +
                "hap.lastv, ipt.iptType, ipt.dateOfIptStart, ipt.iptCompletionDate, ipt.iptCompletionStatus, st.status\n" +
                "ORDER BY\n" +
                "    p.id DESC\n" +
                ")\n" +
                "SELECT\n" +
                " COUNT(doctb) AS tbScreenNumerator,\n" +
                "COUNT(hospitalNumber) AS tbScreenDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(doctb) AS tbScreenVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(doctb) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2),0) AS tbScreenPerformance,\n" +
                "COUNT(completeAnddoc) AS docAndCompletedNumerator,\n" +
                "COUNT(doctb) AS docAndCompletedDenominator,\n" +
                "COUNT(doctb) - COUNT(completeAnddoc) AS docAndCompletedVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(completeAnddoc) AS DECIMAL) / COUNT(doctb)) * 100, 2),0) AS docAndCompletedPerformance,\n" +
                "COUNT(tboutcome) AS tbstatusNumerator,\n" +
                "COUNT(hospitalNumber) AS tbstatusDenominator,\n" +
                "COUNT(hospitalNumber) - COUNT(tboutcome) AS tbStatusVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(tboutcome) AS DECIMAL) / COUNT(hospitalNumber)) * 100, 2),0) AS tbstatusPerformance,\n" +
                "COUNT(prsmptivecollection) AS preSampleNumerator,\n" +
                "COUNT(tboutcome) AS preSampleDenominator,\n" +
                "COUNT(tboutcome) - COUNT(prsmptivecollection) AS preSampleVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(prsmptivecollection) AS DECIMAL) / COUNT(tboutcome)) * 100, 2),0) AS preSamplePerformance,\n" +
                "COUNT(prsmptivectionsamp) AS preSampleTypeNumerator,\n" +
                "COUNT(prsmptivecollection) AS preSampleTypeDenominator,\n" +
                "COUNT(prsmptivecollection) - COUNT(prsmptivectionsamp) AS preSampleTypeVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(prsmptivectionsamp) AS DECIMAL) / COUNT(prsmptivecollection)) * 100, 2),0) AS preSampleTypePerformance,\n" +
                "COUNT(iptStartDate) AS tptstartNumerator,\n" +
                "COUNT(eligibeipt) AS tptstartDenominator,\n" +
                "COUNT(eligibeipt) - COUNT(iptStartDate) AS tptStartVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(iptStartDate) AS DECIMAL) / COUNT(eligibeipt)) * 100, 2), 0) AS tptstartPerformance,\n" +
                "COUNT(iptEliStart) AS iptEliStartNumerator,\n" +
                "COUNT(eligibeipt) AS iptEliStartDenominator,\n" +
                "COUNT(eligibeipt) - COUNT(iptEliStart) AS iptEliStartVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(iptEliStart) AS DECIMAL) / COUNT(eligibeipt)) * 100, 2),0) AS iptEliStartPerformance,\n" +
                "COUNT(ipt6monthCompl) AS ipt6monthComplNumerator,\n" +
                "COUNT(ipt6month) AS ipt6monthComplDenominator,\n" +
                "COUNT(ipt6month) - COUNT(ipt6monthCompl) AS ipt6monthComplVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(ipt6monthCompl) AS DECIMAL) / NULLIF(COUNT(ipt6month),0)) * 100, 2), 0) AS ipt6monthComplPerformance,\n" +
                "COUNT(iptStatus) AS iptComplStatususNumerator,\n" +
                "COUNT(ipt6month) AS iptComplStatususDenominator,\n" +
                "COUNT(ipt6month) - COUNT(iptStatus) AS iptComplStatususVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(iptStatus) AS DECIMAL) / NULLIF(COUNT(ipt6month),0)) * 100, 2), 0) AS iptComplStatususPerformance,\n" +
                "COUNT(iptTypeStatus) AS iptTypeStatusNumerator,\n" +
                "COUNT(ipt6month) AS iptTypeStatusDenominator,\n" +
                "COUNT(ipt6month) - COUNT(iptTypeStatus) AS iptTypeStatusVariance,\n" +
                "COALESCE(ROUND((CAST(COUNT(iptTypeStatus) AS DECIMAL) / NULLIF(COUNT(ipt6month),0)) * 100, 2), 0) AS iptTypeStatusPerformance\n" +
                "FROM\n" +
                "tbSummary";

    }

}
