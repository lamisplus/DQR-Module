package org.lamisplus.modules.dqr.domain.cbs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.lamisplus.modules.dqr.domain.cbs.CbsStatus.MISSING_EXPECTED_EVENT;
import static org.lamisplus.modules.dqr.domain.cbs.CbsStatus.POSSIBLE_DISCREPANCY;
import static org.lamisplus.modules.dqr.domain.cbs.CbsStatus.UNABLE_TO_ASSESS;
import static org.lamisplus.modules.dqr.domain.cbs.CbsStatus.VALIDATION_ERROR;

/**
 * Validation rules catalogue. Codes, names, outcomes and messages follow the DQ_Rules_Catalogue
 * sheet of the BA mapping workbook (AFGHS-P-LAMISPlus_CBS_Event_Mapping, v1.1, 23 Oct 2026).
 *
 * Version 2 is the workbook catalogue. Version 1 was the earlier 15-rule list, which used some
 * of the same codes for different checks; the version is part of each finding's dedup key so
 * old findings are cleared instead of being carried over to a rule that now means something else.
 * Raise the version of a rule when its logic changes.
 */
@Getter
@RequiredArgsConstructor
public enum CbsRule {
    DQ01("CBS-DQ-01", 2, "ART initiation before diagnosis", "Date and chronology", VALIDATION_ERROR, "High",
            "ART initiation date is earlier than the recorded HIV diagnosis date."),
    DQ02("CBS-DQ-02", 2, "ART initiation without linkage documentation", "Expected-event gaps", MISSING_EXPECTED_EVENT, "High",
            "ART has started but no linkage (enrolment) record was found."),
    DQ03("CBS-DQ-03", 2, "Service after verified death", "Status consistency", POSSIBLE_DISCREPANCY, "High",
            "A pharmacy, clinic or lab service was recorded after the patient's verified date of death."),
    DQ04("CBS-DQ-04", 2, "Documented vs derived treatment status", "Status consistency", POSSIBLE_DISCREPANCY, "Medium",
            "The documented ART status differs from the status calculated from treatment records."),
    DQ05("CBS-DQ-05", 2, "Interruption without follow-up", "Transfer and interruption", MISSING_EXPECTED_EVENT, "Medium",
            "Qualifying treatment interruption with no recorded follow-up within the approved period."),
    DQ06("CBS-DQ-06", 2, "Mortality record incomplete", "Completeness", VALIDATION_ERROR, "Medium",
            "Death record is missing the date of death or the cause of death."),
    DQ07("CBS-DQ-07", 2, "Potential duplicate event", "Event and duplicate consistency", POSSIBLE_DISCREPANCY, "Low",
            "Two or more records appear to describe the same event."),
    DQ08("CBS-DQ-08", 2, "Diagnosis evidence absent", "Completeness", MISSING_EXPECTED_EVENT, "High",
            "Enrolled in care but no positive HTS result or verified historical diagnosis date."),
    DQ09("CBS-DQ-09", 2, "Cross-module disagreement", "Cross-module consistency", POSSIBLE_DISCREPANCY, "Medium",
            "Records in different modules disagree."),
    DQ10("CBS-DQ-10", 2, "Future or invalid event date", "Date and chronology", VALIDATION_ERROR, "High",
            "The event date is after the as-of date or after the date it was entered."),
    DQ11("CBS-DQ-11", 2, "Event before date of birth", "Date and chronology", VALIDATION_ERROR, "High",
            "An event date is earlier than the patient's date of birth."),
    DQ12("CBS-DQ-12", 2, "Enrolment before diagnosis", "Date and chronology", VALIDATION_ERROR, "High",
            "Enrolment date is earlier than the HIV diagnosis date."),
    DQ13("CBS-DQ-13", 2, "Diagnosed, not linked within window", "Expected-event gaps", MISSING_EXPECTED_EVENT, "High",
            "Diagnosed HIV-positive but no enrolment record was found within the approved period."),
    DQ14("CBS-DQ-14", 2, "Enrolled, ART not initiated", "Expected-event gaps", MISSING_EXPECTED_EVENT, "Medium",
            "Enrolled in HIV care but no ART commencement was found."),
    DQ15("CBS-DQ-15", 2, "Multiple ART commencement records", "Event and duplicate consistency", POSSIBLE_DISCREPANCY, "Medium",
            "More than one ART commencement record exists."),
    DQ16("CBS-DQ-16", 2, "ART initiation without regimen", "Completeness", VALIDATION_ERROR, "Medium",
            "ART initiation record has no regimen or regimen line."),
    DQ17("CBS-DQ-17", 2, "ART initiation without ARV dispensing", "Cross-form consistency", POSSIBLE_DISCREPANCY, "Medium",
            "ART was commenced but no ARV dispensing was found."),
    DQ18("CBS-DQ-18", 2, "Service after transfer out", "Transfer and interruption", POSSIBLE_DISCREPANCY, "Medium",
            "Services were recorded at this facility after the patient transferred out."),
    DQ19("CBS-DQ-19", 2, "Viral load test due, no result", "Expected-event gaps", MISSING_EXPECTED_EVENT, "High",
            "Eligible for a viral load test and no sample was found in the eligibility period."),
    DQ20("CBS-DQ-20", 2, "Unsuppressed VL without EAC", "Expected-event gaps", MISSING_EXPECTED_EVENT, "High",
            "Viral load is not suppressed and no EAC session is recorded."),
    DQ21("CBS-DQ-21", 2, "Post-EAC VL overdue", "Expected-event gaps", MISSING_EXPECTED_EVENT, "Medium",
            "EAC is complete but no repeat (confirmation) viral load was documented."),
    DQ22("CBS-DQ-22", 2, "Result date before sample date", "Date and chronology", VALIDATION_ERROR, "Medium",
            "A lab result date is earlier than the sample collection date."),
    DQ23("CBS-DQ-23", 2, "Duplicate patient identifier", "Source and identity", POSSIBLE_DISCREPANCY, "High",
            "The same hospital number or ART unique ID is used by more than one patient in the facility."),
    DQ24("CBS-DQ-24", 2, "Event without patient association", "Source and identity", UNABLE_TO_ASSESS, "Medium",
            "A source record could not be linked to a patient and is excluded from timelines."),
    DQ25("CBS-DQ-25", 2, "Transfer-in without original ART start", "Completeness", MISSING_EXPECTED_EVENT, "Medium",
            "Client transferred in but the original ART start date is not documented."),
    DQ26("CBS-DQ-26", 2, "No baseline CD4 at ART initiation", "Expected-event gaps", MISSING_EXPECTED_EVENT, "Medium",
            "No baseline CD4 result was found for this ART initiation."),
    DQ27("CBS-DQ-27", 2, "Regimen switch without documented VL failure", "Cross-module consistency", POSSIBLE_DISCREPANCY, "Medium",
            "Regimen line changed without a documented unsuppressed viral load."),
    // Never raised as a finding: every refresh reads only non-archived records, so a finding whose
    // source gets archived is not found again and is cleared (superseded) automatically.
    DQ28("CBS-DQ-28", 2, "Archived source still referenced", "Source and identity", VALIDATION_ERROR, "Low",
            "The source record for an event was archived; its findings are superseded on the next refresh.");

    private final String code;
    private final int version;
    private final String name;
    private final String category;
    private final String findingType;
    private final String severity;
    private final String description;

    public static CbsRule fromCode(String code) {
        for (CbsRule rule : values()) {
            if (rule.code.equals(code)) {
                return rule;
            }
        }
        return null;
    }
}
