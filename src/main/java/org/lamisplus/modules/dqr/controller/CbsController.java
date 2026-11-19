package org.lamisplus.modules.dqr.controller;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.dto.*;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsSourceRows;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.lamisplus.modules.dqr.service.cbs.CbsAccess;
import org.lamisplus.modules.dqr.service.cbs.CbsContextService;
import org.lamisplus.modules.dqr.service.cbs.CbsExportService;
import org.lamisplus.modules.dqr.service.cbs.CbsIssueService;
import org.lamisplus.modules.dqr.service.cbs.CbsQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * HIV Case-Based Surveillance monitoring API.
 * The services work out the user's facility, and errors are turned into responses by
 * Core's RestExceptionHandler, so each method only checks permission and calls a service.
 * Permissions are checked with CbsAccess instead of @PreAuthorize because method security
 * is not applied to modules loaded at runtime.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dqr/cbs")
public class CbsController {

    private final CbsAccess access;
    private final CbsContextService contextService;
    private final CbsQueryService queryService;
    private final CbsIssueService issueService;
    private final CbsExportService exportService;

    @GetMapping("/permissions")
    public ResponseEntity<CbsPermissionsDto> getPermissions() {
        return ResponseEntity.ok(access.permissions());
    }

    // Dashboard and refresh
    @GetMapping("/overview")
    public ResponseEntity<CbsOverviewDto> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        access.requireView();
        return ResponseEntity.ok(queryService.overview(from, to));
    }

    @PostMapping("/refresh")
    public ResponseEntity<CbsRefreshResponse> refresh() {
        access.requireView();
        return ResponseEntity.ok(contextService.startRefresh());
    }

    @GetMapping("/refresh/status")
    public ResponseEntity<CbsContextDto> getRefreshStatus() {
        access.requireView();
        return ResponseEntity.ok(contextService.current());
    }

    // Patients
    @GetMapping("/patients")
    public ResponseEntity<CbsPage<CbsViews.PatientRow>> getPatients(CbsPatientFilter filter) {
        access.requirePatientView();
        return ResponseEntity.ok(queryService.patients(filter));
    }

    @GetMapping("/patients/{personUuid}")
    public ResponseEntity<CbsPatientDetailDto> getPatient(@PathVariable String personUuid,
                                                          @RequestParam(required = false)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        access.requirePatientView();
        return ResponseEntity.ok(queryService.patient(personUuid, asOf));
    }

    // Data quality monitoring
    @GetMapping("/findings")
    public ResponseEntity<CbsFindingsPage> getFindings(CbsFindingFilter filter) {
        access.requirePatientView();
        return ResponseEntity.ok(queryService.findings(filter));
    }

    @GetMapping("/rules")
    public ResponseEntity<CbsRulesDto> getRules() {
        access.requireView();
        return ResponseEntity.ok(queryService.rules());
    }

    // Issue tracker
    @GetMapping("/issues")
    public ResponseEntity<CbsIssuesPage> getIssues(CbsIssueFilter filter) {
        access.requirePatientView();
        return ResponseEntity.ok(queryService.issues(filter));
    }

    @GetMapping("/issues/{id}")
    public ResponseEntity<CbsIssueDetailDto> getIssue(@PathVariable long id) {
        access.requirePatientView();
        return ResponseEntity.ok(queryService.issue(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<CbsSourceRows.User>> getAssignableUsers() {
        access.requireManage();
        return ResponseEntity.ok(queryService.assignableUsers());
    }

    @PostMapping("/issues/{id}/assign")
    public ResponseEntity<CbsIssueDetailDto> assign(@PathVariable long id, @Valid @RequestBody CbsAssignRequest request) {
        access.requireManage();
        return ResponseEntity.ok(issueService.assign(id, request));
    }

    @PostMapping("/issues/{id}/acknowledge")
    public ResponseEntity<CbsIssueDetailDto> acknowledge(@PathVariable long id) {
        access.requireManage();
        return ResponseEntity.ok(issueService.acknowledge(id));
    }

    @PostMapping("/issues/{id}/start-review")
    public ResponseEntity<CbsIssueDetailDto> startReview(@PathVariable long id) {
        access.requireManage();
        return ResponseEntity.ok(issueService.startReview(id));
    }

    @PostMapping("/issues/{id}/investigation")
    public ResponseEntity<CbsIssueDetailDto> addInvestigationFinding(@PathVariable long id, @Valid @RequestBody CbsNoteRequest request) {
        access.requireManage();
        return ResponseEntity.ok(issueService.addInvestigationFinding(id, request));
    }

    @PostMapping("/issues/{id}/corrective-action")
    public ResponseEntity<CbsIssueDetailDto> addCorrectiveAction(@PathVariable long id,
                                                                 @Valid @RequestBody CbsCorrectiveActionRequest request) {
        access.requireManage();
        return ResponseEntity.ok(issueService.addCorrectiveAction(id, request));
    }

    @PostMapping("/issues/{id}/revalidate")
    public ResponseEntity<CbsIssueDetailDto> revalidate(@PathVariable long id) {
        access.requireManage();
        return ResponseEntity.ok(issueService.revalidate(id));
    }

    @PostMapping("/issues/{id}/exception-request")
    public ResponseEntity<CbsIssueDetailDto> requestException(@PathVariable long id, @Valid @RequestBody CbsExceptionRequest request) {
        access.requireManage();
        return ResponseEntity.ok(issueService.requestException(id, request));
    }

    @PostMapping("/issues/{id}/exception-decision")
    public ResponseEntity<CbsIssueDetailDto> decideException(@PathVariable long id,
                                                             @Valid @RequestBody CbsExceptionDecisionRequest request) {
        access.requireApproveException();
        return ResponseEntity.ok(issueService.decideException(id, request));
    }

    @PostMapping("/issues/{id}/escalate")
    public ResponseEntity<CbsIssueDetailDto> escalate(@PathVariable long id, @Valid @RequestBody CbsNoteRequest request) {
        access.requireManage();
        return ResponseEntity.ok(issueService.escalate(id, request));
    }

    // Reports summary
    @GetMapping("/reports/summary")
    public ResponseEntity<CbsReportSummaryDto> getReportSummary(CbsReportFilter filter) {
        access.requireView();
        return ResponseEntity.ok(queryService.reportSummary(filter));
    }

    // Reports export
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> export(@RequestParam String report, CbsReportFilter filter) {
        access.requireExport();
        return exportService.export(report, filter);
    }
}
