package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** Used for investigation findings and escalations, which only need a note. */
@Data
public class CbsNoteRequest {
    @NotBlank(message = "Enter a note")
    private String note;
}
