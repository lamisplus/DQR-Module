package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CbsRefreshResponse extends CbsContextDto {
    // false when a refresh for the facility was already running
    private boolean started;
}
