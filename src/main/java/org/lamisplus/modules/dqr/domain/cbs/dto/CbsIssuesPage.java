package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CbsIssuesPage extends CbsPage<CbsViews.IssueRow> {
    private CbsViews.IssueSummary summary;

    public CbsIssuesPage(Page<CbsViews.IssueRow> page, CbsViews.IssueSummary summary) {
        super(page);
        this.summary = summary;
    }
}
