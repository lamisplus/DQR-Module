package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CbsPatientFilter extends CbsPageFilter {
    private String search;
    private String status;
    private String category;
    private String severity;
    // flags, gaps or discrepancies: the dashboard card the list was opened from
    private String card;
    // "Event date from / to": only patients with an unresolved finding whose event date is in this range
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;
}
