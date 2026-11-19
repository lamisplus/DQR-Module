package org.lamisplus.modules.dqr.domain.cbs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Everything read from the clinical tables for one patient. The engine works only on this
 * object, so it never touches the database and the same logic serves both the facility
 * refresh and the single-patient view with a chosen as-of date.
 */
@Data
public class PatientSource {
    private String personUuid;
    private Long facilityId;
    private String hospitalNumber;
    private String sex;
    private LocalDate dateOfBirth;
    private boolean dateOfBirthEstimated;

    private Enrollment enrollment;
    private final List<Commencement> commencements = new ArrayList<>();
    private final List<DatedRecord> htsPositives = new ArrayList<>();
    private final List<DatedRecord> htsNegatives = new ArrayList<>();
    private final List<Refill> refills = new ArrayList<>();
    private final List<DatedRecord> clinicVisits = new ArrayList<>();
    private final List<TrackerStatus> trackerStatuses = new ArrayList<>();
    private final List<PatientTracking> trackings = new ArrayList<>();
    private final List<DatedRecord> transferInForms = new ArrayList<>();
    private final List<LabTest> viralLoads = new ArrayList<>();
    private final List<LabTest> cd4Tests = new ArrayList<>();
    private final List<DatedRecord> eacSessions = new ArrayList<>();
    // completed EAC episodes; the date is the last session of the episode
    private final List<DatedRecord> completedEacs = new ArrayList<>();
    // "hospital number 123 is also used by 2 other patients" style notes, for CBS-DQ-23
    private final List<DuplicateId> duplicateIds = new ArrayList<>();

    public String getPatientId() {
        if (enrollment != null && enrollment.getUniqueId() != null && !enrollment.getUniqueId().trim().isEmpty()) {
            return enrollment.getUniqueId();
        }
        return hospitalNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Enrollment {
        private String uuid;
        private String uniqueId;
        private LocalDate dateConfirmedHiv;
        private LocalDate dateOfRegistration;
        private LocalDate dateStarted;
        private String statusAtRegistration;
        private LocalDateTime createdDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatedRecord {
        private String uuid;
        private LocalDate date;
        private LocalDateTime createdDate;
    }

    /** An ART commencement (is_commencement row of hiv_enrollment_commencement). */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Commencement {
        private String uuid;
        private LocalDate date;
        private LocalDateTime createdDate;
        private Long regimenId;
        private Long regimenLineId;
        // CD4 written on the commencement form itself (count, percentage or LFA result)
        private String cd4AtStart;
        private String clinicalStage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Refill {
        private String uuid;
        private LocalDate visitDate;
        private Integer refillPeriod;
        // 1, 2 or 3 for the highest ART regimen line dispensed; null when no ART line was dispensed
        private Integer regimenLine;
        private LocalDateTime createdDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackerStatus {
        private String uuid;
        private String rawStatus;
        private String status;          // normalised, see CbsTrackerStatus
        private LocalDate statusDate;
        private String causeOfDeath;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientTracking {
        private String uuid;
        private LocalDate trackingDate;
        private LocalDate dateOfDeath;
        private String causeOfDeath;
        private LocalDate dateReturnedToCare;
    }

    /** One lab test order with its earliest sample and its result (either can be missing). */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabTest {
        private String uuid;            // the result uuid when there is a result, else the test uuid
        private LocalDate sampleDate;
        private String sampleNumber;
        private Integer indication;     // viral_load_indication codeset id
        private LocalDate resultDate;
        private String rawResult;
        private Double copies;          // null when the result can't be read as a number

        /** EV10: dated by sample collection, falling back to the result date. */
        public LocalDate getEventDate() {
            return sampleDate != null ? sampleDate : resultDate;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateId {
        private String idType;          // "Hospital number" or "ART unique ID"
        private String idValue;
        private int otherPatients;
    }
}
