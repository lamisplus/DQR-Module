package org.lamisplus.modules.dqr.service.cbs;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.base.controller.apierror.AccessDeniedException;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsPermissionsDto;
import org.lamisplus.modules.dqr.service.CurrentUserOrganizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Permission and facility checks for every CBS request. Done in code rather than with
 * @PreAuthorize so the check is guaranteed to run inside the module's own context.
 * Roles with all_permission (Super Admin, RDE, ...) pass every check, as elsewhere in LAMISPlus.
 */
@Component
@RequiredArgsConstructor
public class CbsAccess {

    // These names are declared in module.yml so they show up on the role screen.
    public static final String VIEW = "cbs_view";
    public static final String VIEW_PATIENT = "cbs_view_patient";
    public static final String MANAGE_ISSUES = "cbs_manage_issues";
    public static final String APPROVE_EXCEPTION = "cbs_approve_exception";
    public static final String EXPORT = "cbs_export";
    private static final String ALL = "all_permission";

    private final CurrentUserOrganizationService organizationService;

    public Long facilityId() {
        Long facilityId = organizationService.getCurrentUserOrganization();
        if (facilityId == null) {
            throw new AccessDeniedException(CbsAccess.class, "facility", "none selected");
        }
        return facilityId;
    }

    public String userName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "unknown" : auth.getName();
    }

    /** Dashboard and reports: any CBS permission is enough. */
    public void requireView() {
        require(VIEW, VIEW_PATIENT, MANAGE_ISSUES);
    }

    /** Anything that shows patient-level records. */
    public void requirePatientView() {
        require(VIEW_PATIENT, MANAGE_ISSUES);
    }

    public void requireManage() {
        require(MANAGE_ISSUES);
    }

    public void requireApproveException() {
        require(APPROVE_EXCEPTION);
    }

    public void requireExport() {
        require(EXPORT);
    }

    public boolean canViewPatients() {
        return hasAny(VIEW_PATIENT, MANAGE_ISSUES);
    }

    public CbsPermissionsDto permissions() {
        return new CbsPermissionsDto(
                hasAny(VIEW, VIEW_PATIENT, MANAGE_ISSUES),
                canViewPatients(),
                hasAny(MANAGE_ISSUES),
                hasAny(APPROVE_EXCEPTION),
                hasAny(EXPORT));
    }

    // Core's RestExceptionHandler turns AccessDeniedException into a 403 for every module
    private void require(String... permissions) {
        if (!hasAny(permissions)) {
            throw new AccessDeniedException(CbsAccess.class, "permission", String.join(" or ", permissions));
        }
    }

    private boolean hasAny(String... permissions) {
        Set<String> mine = authorities();
        return mine.contains(ALL) || Arrays.stream(permissions).anyMatch(mine::contains);
    }

    private Set<String> authorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Collections.emptySet();
        }
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }
}
