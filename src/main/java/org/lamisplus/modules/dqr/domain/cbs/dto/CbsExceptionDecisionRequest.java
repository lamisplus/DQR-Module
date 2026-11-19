package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CbsExceptionDecisionRequest {
    // A real boolean: a missing or misspelled value is rejected instead of counting as "reject"
    @NotNull(message = "Say whether the exception is approved")
    private Boolean approve;

    @NotBlank(message = "Give the reason for the decision")
    private String note;
}
