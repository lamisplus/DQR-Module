package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsRefreshRun;

import java.time.LocalDate;

/** Facility and data freshness shown at the top of every CBS screen. */
@Data
public class CbsContextDto {
    private Long facilityId;
    private String facilityName;
    private CbsRefreshRun lastSuccessfulRefresh;
    private CbsRefreshRun latestRun;
    private boolean processing;
    // as-of date of the last successful refresh; null until data has been processed once
    private LocalDate asOfDate;
    private boolean stale;
    private boolean latestRunFailed;

    /** Copies the context into a subclass, so screen responses can carry it at the top level. */
    public void copyFrom(CbsContextDto other) {
        this.facilityId = other.facilityId;
        this.facilityName = other.facilityName;
        this.lastSuccessfulRefresh = other.lastSuccessfulRefresh;
        this.latestRun = other.latestRun;
        this.processing = other.processing;
        this.asOfDate = other.asOfDate;
        this.stale = other.stale;
        this.latestRunFailed = other.latestRunFailed;
    }
}
