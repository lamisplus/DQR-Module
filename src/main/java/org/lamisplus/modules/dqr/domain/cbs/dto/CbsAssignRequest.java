package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CbsAssignRequest {
    @NotBlank(message = "Choose the user to assign the issue to")
    private String assignedTo;
}
