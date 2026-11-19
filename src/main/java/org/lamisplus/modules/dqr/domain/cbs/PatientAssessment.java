package org.lamisplus.modules.dqr.domain.cbs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What the engine works out for one patient as of one date. */
@Data
public class PatientAssessment {
    private String personUuid;
    private LocalDate asOfDate;

    private final List<CbsEvent> events = new ArrayList<>();
    private final List<CbsFinding> findings = new ArrayList<>();
    private final List<EventSummary> eventSummary = new ArrayList<>();
    private final Map<String, Object> indicators = new LinkedHashMap<>();

    private StatusResult treatmentStatus;
    private StatusResult vitalStatus;
    private StatusResult viralLoadStatus;
    private StatusResult eacStatus;
    // latest explicit outcome written in client tracking, used by CBS-DQ-04
    private String documentedTreatmentStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusResult {
        private String status;
        private LocalDate effectiveDate;
        private String evidence;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSummary {
        private CbsEventType category;
        private String label;
        private String outcome;
        private String outcomeLabel;
        private String explanation;
        private int occurrences;
        private String ruleCode;
    }
}
