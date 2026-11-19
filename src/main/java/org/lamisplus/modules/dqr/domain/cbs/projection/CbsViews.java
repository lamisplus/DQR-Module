package org.lamisplus.modules.dqr.domain.cbs.projection;

import java.util.Date;

/** Row shapes sent to the CBS screens. Property names are what the React code reads. */
public final class CbsViews {
    private CbsViews() {
    }

    public interface NameCount {
        String getName();
        Long getCount();
    }

    public interface OverviewCounts {
        Long getClientsAssessed();
        Long getRecordsWithFlags();
        Long getEventGaps();
        Long getStatusDiscrepancies();
    }

    public interface PeriodCounts {
        Long getRecordsWithFlags();
        Long getEventGaps();
        Long getStatusDiscrepancies();
        Long getHighPriorityUnresolved();
    }

    public interface PatientRow {
        String getPersonUuid();
        String getPatientId();
        String getHospitalNumber();
        String getSex();
        String getTreatmentStatus();
        Date getTreatmentStatusDate();
        String getVitalStatus();
        String getLatestEventType();
        Date getLatestEventDate();
        Integer getOpenFlags();
        Integer getOpenGaps();
        Boolean getStatusDiscrepancy();
        Date getAsOfDate();
        String getFacilityName();
    }

    public interface IdentityRow {
        String getPersonUuid();
        String getPatientId();
        String getHospitalNumber();
        String getSex();
        Date getDateOfBirth();
    }

    public interface FindingRow {
        Long getFindingId();
        Long getIssueId();
        String getPersonUuid();
        String getPatientId();
        String getHospitalNumber();
        String getRuleCode();
        String getCategory();
        String getFindingType();
        String getSeverity();
        String getEventType();
        String getDescription();
        String getEvidence();
        Boolean getActive();
        Date getEventDate();
        Date getDateIdentified();
        Date getLastDetected();
        Date getClearedAt();
        String getIssueStatus();
        String getAssignedTo();
    }

    public interface FindingCounts {
        Long getAll();
        Long getValidation();
        Long getGaps();
        Long getStatus();
    }

    public interface CategoryRow {
        String getName();
        String getSeverity();
        Long getUnresolved();
        Long getPatients();
    }

    public interface IssueSummary {
        Long getOpen();
        Long getUnderReview();
        Long getOverdue();
        Long getResolved();
        Long getApprovedExceptions();
        Long getUnassigned();
        Long getAssigned();
        Long getEscalated();
    }

    public interface IssueRow {
        Long getId();
        String getPersonUuid();
        String getPatientId();
        String getHospitalNumber();
        String getRuleCode();
        String getCategory();
        String getFindingType();
        String getSeverity();
        String getDescription();
        String getStatus();
        String getAssignedTo();
        Date getDueDate();
        Boolean getEscalated();
        Date getCreatedAt();
        Date getResolvedAt();
        Date getEventDate();
        Boolean getOverdue();
    }

    public interface IssueDetail extends IssueRow {
        Date getUpdatedAt();
        Long getFindingId();
        Integer getRuleVersion();
        String getEventType();
        String getEvidence();
        Boolean getActive();
        Date getDateIdentified();
        Date getLastDetected();
        Date getClearedAt();
    }

    public interface HistoryRow {
        Long getId();
        String getAction();
        String getFromStatus();
        String getToStatus();
        String getNote();
        Date getActionDate();
        String getDetails();
        String getPerformedBy();
        Date getPerformedAt();
    }
}
