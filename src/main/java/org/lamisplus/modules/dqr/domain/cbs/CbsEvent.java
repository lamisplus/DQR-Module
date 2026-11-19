package org.lamisplus.modules.dqr.domain.cbs;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class CbsEvent {
    private CbsEventType type;
    private LocalDate eventDate;
    private LocalDateTime recordedDate;
    private String sourceModule;
    private String sourceTable;
    private String sourceRecordId;
    // clinical, laboratory, tracking, historical or derived (see "verification_method" in the spec)
    private String verificationMethod;
    private final Map<String, Object> details = new LinkedHashMap<>();
    // set by the engine when a finding points at this event, so the timeline can show a review marker
    private boolean needsReview;

    public static CbsEvent of(CbsEventType type, LocalDate date, String module, String table,
                              String recordId, String verification) {
        CbsEvent event = new CbsEvent();
        event.type = type;
        event.eventDate = date;
        event.sourceModule = module;
        event.sourceTable = table;
        event.sourceRecordId = recordId;
        event.verificationMethod = verification;
        return event;
    }

    public CbsEvent detail(String key, Object value) {
        if (value != null) {
            details.put(key, value);
        }
        return this;
    }
}
