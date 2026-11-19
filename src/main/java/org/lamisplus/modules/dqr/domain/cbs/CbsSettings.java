package org.lamisplus.modules.dqr.domain.cbs;

import lombok.Data;

import java.util.Map;

/** Thresholds read from cbs_setting. Defaults match the values seeded by the installer. */
@Data
public class CbsSettings {
    // EV06: interrupted once refill period + iit_days have passed since the last pickup (BA, 17 Nov 2026)
    private int iitDays = 28;
    private int defaultRefillDays = 30;
    private int linkageDays = 30;
    // CBS-DQ-05: tracking is due one day after the interruption date (BA, 17 Nov 2026)
    private int followUpDays = 1;
    private int eacDays = 30;
    private double vlUnsuppressedCopies = 1000;
    private double vlUndetectableCopies = 50;
    private int vlEligibilityMonths = 6;
    // CBS-DQ-21: the confirmation VL indication says "3-6 months after" EAC, so it is overdue after 6 months
    private int postEacVlDays = 180;
    // EV09 / CBS-DQ-26: a CD4 sample on or before ART start + this many days is the baseline.
    // 0 because the BA asked for a CD4 at ART initiation and an immediate flag without one.
    private int cd4BaselineDays = 0;
    // CBS-DQ-10: an event dated more than this many days after the record was entered is invalid
    private int entryDateToleranceDays = 1;
    private int aliveEvidenceDays = 365;
    private int crossModuleToleranceDays = 7;
    private int dueDaysHigh = 7;
    private int dueDaysMedium = 14;
    private int dueDaysLow = 30;
    private int staleHours = 24;

    public static CbsSettings from(Map<String, String> values) {
        CbsSettings s = new CbsSettings();
        s.iitDays = intValue(values, "iit_days", s.iitDays);
        s.defaultRefillDays = intValue(values, "default_refill_days", s.defaultRefillDays);
        s.linkageDays = intValue(values, "linkage_days", s.linkageDays);
        s.followUpDays = intValue(values, "follow_up_days", s.followUpDays);
        s.eacDays = intValue(values, "eac_days", s.eacDays);
        s.vlUnsuppressedCopies = intValue(values, "vl_unsuppressed_copies", (int) s.vlUnsuppressedCopies);
        s.vlUndetectableCopies = intValue(values, "vl_undetectable_copies", (int) s.vlUndetectableCopies);
        s.vlEligibilityMonths = intValue(values, "vl_eligibility_months", s.vlEligibilityMonths);
        s.postEacVlDays = intValue(values, "post_eac_vl_days", s.postEacVlDays);
        s.cd4BaselineDays = intValue(values, "cd4_baseline_days", s.cd4BaselineDays);
        s.entryDateToleranceDays = intValue(values, "entry_date_tolerance_days", s.entryDateToleranceDays);
        s.aliveEvidenceDays = intValue(values, "alive_evidence_days", s.aliveEvidenceDays);
        s.crossModuleToleranceDays = intValue(values, "cross_module_tolerance_days", s.crossModuleToleranceDays);
        s.dueDaysHigh = intValue(values, "due_days_high", s.dueDaysHigh);
        s.dueDaysMedium = intValue(values, "due_days_medium", s.dueDaysMedium);
        s.dueDaysLow = intValue(values, "due_days_low", s.dueDaysLow);
        s.staleHours = intValue(values, "stale_hours", s.staleHours);
        return s;
    }

    public int dueDaysFor(String severity) {
        if ("High".equals(severity)) {
            return dueDaysHigh;
        }
        if ("Medium".equals(severity)) {
            return dueDaysMedium;
        }
        return dueDaysLow;
    }

    private static int intValue(Map<String, String> values, String key, int fallback) {
        try {
            String v = values.get(key);
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
