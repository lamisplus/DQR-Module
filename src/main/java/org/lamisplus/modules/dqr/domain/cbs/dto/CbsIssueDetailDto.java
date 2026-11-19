package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;

import java.util.List;

/** One issue with its finding, evidence and full history. Every workflow action returns this. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbsIssueDetailDto {
    private CbsViews.IssueDetail issue;
    private String ruleName;
    private List<CbsViews.HistoryRow> history;
}
