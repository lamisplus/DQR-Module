package org.lamisplus.modules.dqr.domain.cbs.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/** Who viewed patient-level CBS details or exported a report. Rows are only ever added. */
@Data
@Entity
@Table(name = "cbs_audit_log")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CbsAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long facilityId;
    private String action;
    private String reference;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;
    private String performedBy;
    private LocalDateTime performedAt;
}
