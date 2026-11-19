package org.lamisplus.modules.dqr.domain.cbs.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * A data quality finding as saved in cbs_finding. Named "stored" to keep it apart from the
 * engine's CbsFinding, which is what a rule produces before it is saved.
 */
@Data
@Entity
@Table(name = "cbs_finding")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CbsStoredFinding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long facilityId;
    private String personUuid;
    private String ruleCode;
    private Integer ruleVersion;
    private String category;
    private String findingType;
    private String severity;
    private String eventType;
    private String description;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> evidence;
    private String dedupKey;
    private Boolean active;
    private LocalDateTime firstDetectedAt;
    private LocalDateTime lastDetectedAt;
    private LocalDateTime clearedAt;
}
