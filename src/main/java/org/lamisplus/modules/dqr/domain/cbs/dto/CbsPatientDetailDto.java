package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.PatientAssessment;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Everything on the Patient Surveillance Event Tracker, worked out for one as-of date. */
@Data
public class CbsPatientDetailDto {
    private CbsContextDto context;
    private Header patient;
    private PatientAssessment.StatusResult treatmentStatus;
    private PatientAssessment.StatusResult vitalStatus;
    private PatientAssessment.StatusResult viralLoadStatus;
    private PatientAssessment.StatusResult eacStatus;
    private String documentedTreatmentStatus;
    private List<Event> timeline;
    private List<Event> supporting;
    private List<PatientAssessment.EventSummary> eventSummary;
    private List<Indicator> indicators;
    private int findingsAsOf;
    private List<CbsViews.FindingRow> issues;
    private List<CbsViews.IdentityRow> identityConflicts;

    @Data
    public static class Header {
        private String personUuid;
        private String patientId;
        private String hospitalNumber;
        private String sex;
        private LocalDate dateOfBirth;
        private Integer age;
        private LocalDate asOfDate;
    }

    @Data
    public static class Event {
        private String type;
        private String label;
        private LocalDate eventDate;
        private LocalDateTime recordedDate;
        private String sourceModule;
        private String sourceTable;
        private String sourceRecordId;
        private String verificationMethod;
        // Extra facts that differ per event type (cause of death, HTS test date, ...)
        private Map<String, Object> details;
        private boolean needsReview;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Indicator {
        private String label;
        private String value;
    }
}
