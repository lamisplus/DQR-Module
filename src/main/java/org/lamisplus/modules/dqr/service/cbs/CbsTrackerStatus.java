package org.lamisplus.modules.dqr.service.cbs;

import org.lamisplus.modules.dqr.domain.cbs.CbsStatus;

/**
 * hiv_status_tracker.hiv_status is free-ish text. The same status turns up as "ART Transfer Out",
 * "ART_TRANSFER_OUT", "Transferred out" and even "ART_TRANSFER_OUT;ART_TRANSFER_OUT;ART_TRANSFER_OUT".
 * Everything is turned into one code here so the rest of the code only deals with these values.
 */
public final class CbsTrackerStatus {
    private CbsTrackerStatus() {
    }

    public static final String ART_START = "ART_START";
    public static final String TRANSFER_IN = "TRANSFER_IN";
    public static final String TRANSFER_OUT = "TRANSFER_OUT";
    public static final String DIED = "DIED";
    public static final String LOST_TO_FOLLOWUP = "LOST_TO_FOLLOWUP";
    public static final String STOPPED_TREATMENT = "STOPPED_TREATMENT";
    public static final String RETURN_TO_CARE = "RETURN_TO_CARE";
    public static final String OTHER = "OTHER";

    public static String normalise(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return OTHER;
        }
        // "X;X;X" duplicates come from a save bug in older versions; the first part is enough
        String s = raw.split(";")[0].trim().toUpperCase().replaceAll("[^A-Z]+", "_");
        if (s.contains("DIED") || s.contains("DEATH") || s.contains("DEAD")) {
            return DIED;
        }
        if (s.contains("TRANSFER_OUT") || s.contains("TRANSFERRED_OUT")) {
            return TRANSFER_OUT;
        }
        if (s.contains("TRANSFER_IN") && !s.contains("NOT_ACTIVE")) {
            return TRANSFER_IN;
        }
        if (s.contains("LOST_TO_FOLLOW") || s.equals("IIT")) {
            return LOST_TO_FOLLOWUP;
        }
        if (s.contains("STOPPED")) {
            return STOPPED_TREATMENT;
        }
        if (s.contains("RETURN_TO_CARE") || s.contains("RESTART")) {
            return RETURN_TO_CARE;
        }
        if (s.equals("ART_START")) {
            return ART_START;
        }
        return OTHER;
    }

    /**
     * Treatment status implied by an explicit tracking outcome. ART_START is left out on
     * purpose: it is written once at commencement and never updated, so comparing it with
     * the refill-based status would flag almost every interrupted client.
     */
    public static String impliedTreatmentStatus(String normalised) {
        switch (normalised) {
            case DIED:
                return CbsStatus.DECEASED;
            case TRANSFER_OUT:
                return CbsStatus.TRANSFERRED_OUT;
            case LOST_TO_FOLLOWUP:
            case STOPPED_TREATMENT:
                return CbsStatus.INTERRUPTED;
            case RETURN_TO_CARE:
                return CbsStatus.ACTIVE;
            default:
                return null;
        }
    }
}
