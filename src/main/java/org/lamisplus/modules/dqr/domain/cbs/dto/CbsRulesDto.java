package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsSetting;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbsRulesDto {
    private List<Rule> rules;
    private List<CbsSetting> settings;

    @Data
    public static class Rule {
        private String code;
        private String name;
        private int version;
        private String category;
        private String findingType;
        private String severity;
        private String description;
        private long openFindings;
    }
}
