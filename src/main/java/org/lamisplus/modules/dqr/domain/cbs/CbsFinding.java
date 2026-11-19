package org.lamisplus.modules.dqr.domain.cbs;

import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class CbsFinding {
    private CbsRule rule;
    private String personUuid;
    private CbsEventType eventType;
    private String description;
    private final Map<String, Object> evidence = new LinkedHashMap<>();
    // Same patient + rule + this key = same finding across refreshes. Keeps one open
    // finding per problem instead of a new one every night.
    private String dedupKey;
    // The date of the record the problem is about (the refill, the ART start, the death...).
    // The screens' "Event date from / to" filter uses it. Null when the problem has no single
    // date (e.g. a duplicate patient ID); those only show when no date range is set.
    private LocalDate eventDate;

    public static CbsFinding of(CbsRule rule, String personUuid, CbsEventType eventType, String keyPart,
                                String description) {
        CbsFinding finding = new CbsFinding();
        finding.rule = rule;
        finding.personUuid = personUuid;
        finding.eventType = eventType;
        finding.description = description;
        // The rule version is in the key so a finding from an older catalogue never carries over
        finding.dedupKey = rule.getCode() + "@v" + rule.getVersion() + "|" + personUuid + "|" + (keyPart == null ? "" : keyPart);
        return finding;
    }

    public CbsFinding on(LocalDate date) {
        this.eventDate = date;
        return this;
    }

    public CbsFinding evidence(String key, Object value) {
        if (value != null) {
            evidence.put(key, value);
        }
        return this;
    }
}
