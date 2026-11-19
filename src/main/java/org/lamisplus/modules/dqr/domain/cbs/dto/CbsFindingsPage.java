package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CbsFindingsPage extends CbsPage<CbsViews.FindingRow> {
    private Counts counts;

    public CbsFindingsPage(Page<CbsViews.FindingRow> page, Counts counts) {
        super(page);
        this.counts = counts;
    }

    /** Unresolved findings, for the four view cards and the breakdowns. */
    @Data
    public static class Counts {
        private long all;
        private long validation;
        private long gaps;
        private long status;
        private List<CbsViews.NameCount> byCategory;
        private List<CbsViews.NameCount> bySeverity;
        private List<CbsViews.NameCount> byRule;
    }
}
