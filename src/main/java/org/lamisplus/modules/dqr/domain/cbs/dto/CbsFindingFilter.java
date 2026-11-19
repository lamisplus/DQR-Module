package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CbsFindingFilter extends CbsPageFilter {
    // all, validation, gaps or status
    private String view = "all";
    private String category;
    private String severity;
    // UNRESOLVED (default), ALL, or one issue status
    private String issueStatus;
    private String ruleCode;
    private String search;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;
}
