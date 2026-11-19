package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class CbsReportSummaryDto extends CbsContextDto {
    private Indicators indicators;
    // indicator name -> plain-language definition (spec US-25 AC3)
    private Map<String, String> definitions;
    private List<CbsViews.CategoryRow> byCategory;
    private List<CbsViews.NameCount> issueStatus;
    private LocalDate from;
    private LocalDate to;

    @Data
    public static class Indicators {
        private long clientsAssessed;
        private long patientsWithOpenFlags;
        private long missingExpectedEvents;
        private long statusDiscrepancies;
        private long resolvedIssues;
        private long approvedExceptions;
        private long overdueIssues;
    }
}
