package org.lamisplus.modules.dqr.domain.cbs.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One refresh of a facility's CBS data. The latest COMPLETED run is the "last successful refresh". */
@Data
@Entity
@Table(name = "cbs_refresh_run")
public class CbsRefreshRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long facilityId;
    private LocalDate asOfDate;
    private String status;
    private String triggeredBy;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer patientsProcessed;
    private Integer eventsCreated;
    private Integer findingsActive;
    private Integer findingsNew;
    private Integer findingsCleared;
    private String errorMessage;
}
