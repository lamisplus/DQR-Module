package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CbsOverviewDto extends CbsContextDto {
    // null until data has been processed, so the screen shows "Not available" instead of zeros
    private Cards cards;
    private List<CbsViews.NameCount> treatmentStatusBreakdown;

    @Data
    public static class Cards {
        private long clientsAssessed;
        private long recordsWithFlags;
        private long eventGaps;
        private long statusDiscrepancies;
        private long highPriorityUnresolved;
    }
}
