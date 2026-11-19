package org.lamisplus.modules.dqr.service.cbs;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsContextDto;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsIssueFilter;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsPatientFilter;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsReportFilter;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsReportSummaryDto;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV exports for the Reports & Exports screen. Each file starts with the report title,
 * facility, period, as-of date and last refresh, as the spec requires, and every attempt
 * (successful or not) goes into cbs_audit_log.
 */
@Service
@RequiredArgsConstructor
public class CbsExportService {

    private static final List<String> REPORTS = Arrays.asList("summary", "outstanding", "corrective", "patients");
    private static final int MAX_ROWS = 100000;
    // Marks a column to leave out, e.g. patient IDs for users without patient-detail permission
    private static final Object SKIP = new Object();

    private final CbsAccess access;
    private final CbsQueryService queryService;
    private final CbsContextService contextService;
    private final CbsAuditService audit;

    /** Builds the CSV download for one report. */
    public ResponseEntity<byte[]> export(String report, CbsReportFilter filter) {
        CbsQueryService.requireValidRange(filter.getFrom(), filter.getTo());
        Long facilityId = access.facilityId();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("report", report);
        details.put("from", filter.getFrom() == null ? null : filter.getFrom().toString());
        details.put("to", filter.getTo() == null ? null : filter.getTo().toString());
        details.put("format", "CSV");
        if (!REPORTS.contains(report)) {
            details.put("outcome", "REJECTED");
            audit.record(facilityId, "EXPORT", report, details, access.userName());
            throw new IllegalArgumentException("Unknown report type: " + report);
        }
        try {
            String csv = build(report, filter, access.canViewPatients());
            details.put("outcome", "SUCCESS");
            details.put("bytes", csv.length());
            audit.record(facilityId, "EXPORT", report, details, access.userName());
            String fileName = "cbs-" + report + "-" + LocalDate.now() + ".csv";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .body(csv.getBytes(StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            details.put("outcome", "FAILED");
            details.put("error", e.getMessage());
            audit.record(facilityId, "EXPORT", report, details, access.userName());
            throw e;
        }
    }

    private String build(String report, CbsReportFilter filter, boolean ids) {
        CbsContextDto ctx = contextService.current();
        StringBuilder csv = new StringBuilder();
        line(csv, "Report", title(report));
        line(csv, "Facility", ctx.getFacilityName());
        line(csv, "Reporting period", (filter.getFrom() == null ? "start" : filter.getFrom()) + " to "
                + (filter.getTo() == null ? "today" : filter.getTo()));
        line(csv, "As-of date", ctx.getAsOfDate());
        line(csv, "Last successful refresh", ctx.getLastSuccessfulRefresh() == null
                ? "Not available" : ctx.getLastSuccessfulRefresh().getFinishedAt());
        line(csv, "Exported", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        if (ctx.isStale() || ctx.isLatestRunFailed()) {
            line(csv, "Notice", "Data may be incomplete or out of date: the last refresh is old or the latest refresh failed");
        }
        csv.append("\n");

        switch (report) {
            case "summary":
                summary(csv, filter);
                break;
            case "outstanding":
                issues(csv, "UNRESOLVED", filter, ids);
                break;
            case "corrective":
                issues(csv, null, filter, ids);
                break;
            default:
                patients(csv, ids);
        }
        return csv.toString();
    }

    private void summary(StringBuilder csv, CbsReportFilter filter) {
        CbsReportSummaryDto s = queryService.reportSummary(filter);
        CbsReportSummaryDto.Indicators ind = s.getIndicators();
        Map<String, String> defs = CbsQueryService.indicatorDefinitions();
        line(csv, "Indicator", "Value", "Definition");
        line(csv, "clientsAssessed", ind.getClientsAssessed(), defs.get("clientsAssessed"));
        line(csv, "patientsWithOpenFlags", ind.getPatientsWithOpenFlags(), defs.get("patientsWithOpenFlags"));
        line(csv, "missingExpectedEvents", ind.getMissingExpectedEvents(), defs.get("missingExpectedEvents"));
        line(csv, "statusDiscrepancies", ind.getStatusDiscrepancies(), defs.get("statusDiscrepancies"));
        line(csv, "resolvedIssues", ind.getResolvedIssues(), defs.get("resolvedIssues"));
        line(csv, "approvedExceptions", ind.getApprovedExceptions(), defs.get("approvedExceptions"));
        line(csv, "overdueIssues", ind.getOverdueIssues(), defs.get("overdueIssues"));
        csv.append("\n");
        line(csv, "Issue category", "Severity", "Unresolved findings", "Patients");
        for (CbsViews.CategoryRow r : s.getByCategory()) {
            line(csv, r.getName(), r.getSeverity(), r.getUnresolved(), r.getPatients());
        }
    }

    private void issues(StringBuilder csv, String status, CbsReportFilter period, boolean ids) {
        CbsIssueFilter filter = new CbsIssueFilter();
        filter.setStatus(status);
        filter.setFrom(period.getFrom());
        filter.setTo(period.getTo());
        filter.setSort("id");
        filter.setDirection("asc");
        line(csv, "Issue ID", onlyIf(ids, "Patient ID"), "Rule", "Category", "Severity", "Description", "Status",
                "Assigned to", "Due date", "Overdue", "Escalated", "Date identified", "Resolved at");
        for (CbsViews.IssueRow r : queryService.issues(filter, MAX_ROWS).getContent()) {
            line(csv, "DQ-" + r.getId(), onlyIf(ids, r.getPatientId()), r.getRuleCode(), r.getCategory(),
                    r.getSeverity(), r.getDescription(), r.getStatus(), r.getAssignedTo(), r.getDueDate(),
                    r.getOverdue(), r.getEscalated(), r.getCreatedAt(), r.getResolvedAt());
        }
    }

    private void patients(StringBuilder csv, boolean ids) {
        CbsPatientFilter filter = new CbsPatientFilter();
        filter.setSort("patientId");
        filter.setDirection("asc");
        line(csv, onlyIf(ids, "Patient ID"), "Sex", "Treatment status", "Status date", "Vital status", "Latest event",
                "Latest event date", "Open DQ flags", "Event gaps", "Status discrepancy");
        for (CbsViews.PatientRow r : queryService.patients(filter, MAX_ROWS).getContent()) {
            line(csv, onlyIf(ids, r.getPatientId()), r.getSex(), r.getTreatmentStatus(), r.getTreatmentStatusDate(),
                    r.getVitalStatus(), r.getLatestEventType(), r.getLatestEventDate(), r.getOpenFlags(),
                    r.getOpenGaps(), r.getStatusDiscrepancy());
        }
    }

    private static String title(String report) {
        switch (report) {
            case "summary":
                return "CBS Data Quality Summary";
            case "outstanding":
                return "Outstanding Issues Report";
            case "corrective":
                return "Corrective Action and Resolution Report";
            default:
                return "Patient Surveillance List";
        }
    }

    private static void line(StringBuilder csv, Object... values) {
        boolean first = true;
        for (Object value : values) {
            if (value == SKIP) {
                continue;
            }
            if (!first) {
                csv.append(',');
            }
            csv.append(escape(value));
            first = false;
        }
        csv.append('\n');
    }

    private static Object onlyIf(boolean include, Object value) {
        return include ? value : SKIP;
    }

    private static String escape(Object v) {
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v);
        // Leading = + - @ would make Excel treat the cell as a formula
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
