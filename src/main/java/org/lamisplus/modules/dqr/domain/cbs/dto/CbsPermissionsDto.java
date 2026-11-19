package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** What the current user may do, so the screens can hide what isn't allowed. The server still checks every call. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbsPermissionsDto {
    private boolean view;
    private boolean viewPatient;
    private boolean manageIssues;
    private boolean approveException;
    private boolean export;
}
