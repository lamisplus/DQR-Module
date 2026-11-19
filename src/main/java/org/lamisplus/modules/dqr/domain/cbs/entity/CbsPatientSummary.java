package org.lamisplus.modules.dqr.domain.cbs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/** Per-patient result of the last refresh, kept so the dashboard and list load quickly. */
@Data
@Entity
@Table(name = "cbs_patient_summary")
@IdClass(CbsPatientSummary.Key.class)
public class CbsPatientSummary {
    @Id
    private Long facilityId;
    @Id
    private String personUuid;
    private String patientId;
    private String hospitalNumber;
    private String sex;
    private LocalDate dateOfBirth;
    private String treatmentStatus;
    private LocalDate treatmentStatusDate;
    private String vitalStatus;
    private String latestEventType;
    private LocalDate latestEventDate;
    private Integer openFlags;
    private Integer openGaps;
    private Boolean statusDiscrepancy;
    private LocalDate asOfDate;
    private Long runId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private Long facilityId;
        private String personUuid;
    }
}
