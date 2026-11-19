package org.lamisplus.modules.dqr.domain.cbs.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Data
public class CbsCorrectiveActionRequest {
    @NotBlank(message = "Describe the corrective action")
    private String note;

    @NotNull(message = "Enter the date the action was taken")
    @PastOrPresent(message = "The action date can't be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actionDate;

    // Optional pointer to the record that was corrected in the source module
    private String sourceReference;
}
