package org.lamisplus.modules.dqr.domain.cbs;

/**
 * Display values used across the CBS screens. Kept as plain strings because they are
 * stored in cbs_patient_summary and sent to the UI as they are.
 */
public final class CbsStatus {
    private CbsStatus() {
    }

    public static final String ACTIVE = "Active on ART";
    public static final String INTERRUPTED = "Treatment Interrupted";
    public static final String TRANSFERRED_OUT = "Transferred Out";
    public static final String NOT_INITIATED = "Not Yet Initiated on ART";
    public static final String DECEASED = "Deceased";
    public static final String ALIVE = "Alive";
    public static final String UNABLE = "Unable to Determine";

    // Outcomes for the Surveillance Event Summary panel
    public static final String DOCUMENTED = "DOCUMENTED";
    public static final String MISSING_EXPECTED = "MISSING_EXPECTED";
    public static final String NO_EVENT = "NO_EVENT";
    public static final String NOT_APPLICABLE = "NOT_APPLICABLE";
    public static final String NOT_YET_DUE = "NOT_YET_DUE";
    public static final String UNABLE_TO_DETERMINE = "UNABLE_TO_DETERMINE";

    // Finding classifications from the validation outcome table in the spec
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String POSSIBLE_DISCREPANCY = "POSSIBLE_DISCREPANCY";
    public static final String MISSING_EXPECTED_EVENT = "MISSING_EXPECTED_EVENT";
    // CBS-DQ-24: a source record with no patient link can't be assessed
    public static final String UNABLE_TO_ASSESS = "UNABLE_TO_ASSESS";

    // Issue workflow
    public static final String ISSUE_OPEN = "OPEN";
    public static final String ISSUE_ACKNOWLEDGED = "ACKNOWLEDGED";
    public static final String ISSUE_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String ISSUE_CORRECTED = "CORRECTED";
    public static final String ISSUE_EXCEPTION_REQUESTED = "EXCEPTION_REQUESTED";
    public static final String ISSUE_APPROVED_EXCEPTION = "APPROVED_EXCEPTION";
    public static final String ISSUE_RESOLVED = "RESOLVED";
}
