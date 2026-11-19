package org.lamisplus.modules.dqr.domain.cbs.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/** Audit trail of an issue. Entries are added, never edited or deleted (spec US-24 AC6). */
@Data
@Entity
@Table(name = "cbs_issue_history")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CbsIssueHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long issueId;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String note;
    private LocalDate actionDate;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;
    private String performedBy;
    private LocalDateTime performedAt;
}
