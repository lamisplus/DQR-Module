package org.lamisplus.modules.dqr.domain.cbs.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A finding that needs follow-up in the Data Quality Issue Tracker. One issue per finding. */
@Data
@Entity
@Table(name = "cbs_issue")
public class CbsIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long findingId;
    private Long facilityId;
    private String personUuid;
    private String status;
    private String assignedTo;
    private LocalDate dueDate;
    private Boolean escalated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
