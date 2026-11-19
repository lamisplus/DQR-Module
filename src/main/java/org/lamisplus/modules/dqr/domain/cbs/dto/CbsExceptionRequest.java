package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CbsExceptionRequest {
    @NotBlank(message = "Give the reason this should be accepted as an exception")
    private String justification;
}
