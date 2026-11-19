package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CbsIssueFilter extends CbsPageFilter {
    // an issue status, or the card groups OPEN_GROUP, REVIEW_GROUP, UNRESOLVED
    private String status;
    // a user name, or UNASSIGNED
    private String assignedTo;
    private String severity;
    private String category;
    // overdue or escalated
    private String flag;
    private String search;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;
}
