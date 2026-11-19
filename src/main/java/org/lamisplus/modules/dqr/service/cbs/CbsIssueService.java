package org.lamisplus.modules.dqr.service.cbs;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.base.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.dqr.domain.cbs.CbsFinding;
import org.lamisplus.modules.dqr.domain.cbs.PatientAssessment;
import org.lamisplus.modules.dqr.domain.cbs.PatientSource;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsAssignRequest;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsCorrectiveActionRequest;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsExceptionDecisionRequest;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsExceptionRequest;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsIssueDetailDto;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsNoteRequest;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsIssue;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsIssueHistory;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsStoredFinding;
import org.lamisplus.modules.dqr.repository.cbs.CbsClinicalRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsFindingRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsIssueHistoryRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsIssueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.lamisplus.modules.dqr.domain.cbs.CbsStatus.*;

/**
 * The Data Quality Issue Tracker workflow:
 * Open -> Acknowledged -> Under Review -> Corrected / Exception Requested -> Resolved / Approved Exception.
 *
 * Nothing here edits clinical records. Corrections happen in the original module, and
 * "revalidate" re-reads the clinical tables to confirm the problem is really gone.
 * Actions not allowed in the current state throw IllegalArgumentException, which Core's
 * RestExceptionHandler returns as a 400 with the message shown to the user.
 */
@Service
@RequiredArgsConstructor
public class CbsIssueService {

    private static final Set<String> CLOSED = new HashSet<>(Arrays.asList(ISSUE_RESOLVED, ISSUE_APPROVED_EXCEPTION));
    private static final String INVESTIGATION_FINDING = "INVESTIGATION_FINDING";

    private final CbsAccess access;
    private final CbsIssueRepository issueRepository;
    private final CbsIssueHistoryRepository historyRepository;
    private final CbsFindingRepository findingRepository;
    private final CbsClinicalRepository clinicalRepository;
    private final CbsQueryService queryService;
    private final CbsSourceLoader sourceLoader;
    private final CbsEngine engine;
    private final CbsSettingsService settings;
    private final CbsStore store;

    public CbsIssueDetailDto assign(long issueId, CbsAssignRequest request) {
        CbsIssue issue = openIssue(issueId);
        String assignee = request.getAssignedTo().trim();
        boolean allowed = clinicalRepository.assignableUsers(issue.getFacilityId()).stream()
                .anyMatch(u -> assignee.equals(u.getUserName()));
        if (!allowed) {
            throw new IllegalArgumentException("This user does not have access to the facility and can't be assigned");
        }
        String previous = issue.getAssignedTo();
        issue.setAssignedTo(assignee);
        save(issue);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("previousAssignee", previous);
        details.put("newAssignee", assignee);
        history(issue, previous == null ? "ASSIGNED" : "REASSIGNED", issue.getStatus(), "Assigned to " + assignee, null, details);
        return queryService.issue(issueId);
    }

    public CbsIssueDetailDto acknowledge(long issueId) {
        return move(issueId, Arrays.asList(ISSUE_OPEN), ISSUE_ACKNOWLEDGED, "ACKNOWLEDGED");
    }

    public CbsIssueDetailDto startReview(long issueId) {
        return move(issueId, Arrays.asList(ISSUE_OPEN, ISSUE_ACKNOWLEDGED), ISSUE_UNDER_REVIEW, "REVIEW_STARTED");
    }

    public CbsIssueDetailDto addInvestigationFinding(long issueId, CbsNoteRequest request) {
        CbsIssue issue = openIssue(issueId);
        history(issue, INVESTIGATION_FINDING, issue.getStatus(), request.getNote().trim(), LocalDate.now(), null);
        return queryService.issue(issueId);
    }

    /** Records what was corrected (in the source module) and marks the issue ready for revalidation. */
    public CbsIssueDetailDto addCorrectiveAction(long issueId, CbsCorrectiveActionRequest request) {
        CbsIssue issue = openIssue(issueId);
        requireInvestigationFinding(issue);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("sourceReference", request.getSourceReference());
        String from = issue.getStatus();
        issue.setStatus(ISSUE_CORRECTED);
        save(issue);
        history(issue, "CORRECTIVE_ACTION", from, request.getNote().trim(), request.getActionDate(), details);
        return queryService.issue(issueId);
    }

    /**
     * Re-reads the patient's clinical records and runs the rules again. Resolves the issue only
     * when the finding is no longer produced; otherwise it stays open with the reason recorded.
     */
    public CbsIssueDetailDto revalidate(long issueId) {
        CbsIssue issue = openIssue(issueId);
        CbsStoredFinding finding = findingRepository.findById(issue.getFindingId())
                .orElseThrow(() -> new EntityNotFoundException(CbsStoredFinding.class, "id", String.valueOf(issue.getFindingId())));
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("ruleCode", finding.getRuleCode());
        details.put("ruleVersion", finding.getRuleVersion());
        details.put("assessmentDate", LocalDate.now().toString());
        String from = issue.getStatus();

        PatientSource source = sourceLoader.loadPatient(issue.getFacilityId(), issue.getPersonUuid());
        if (source == null) {
            details.put("outcome", "UNABLE_TO_ASSESS");
            history(issue, "REVALIDATED", from, "Unable to assess: the patient's records could not be read for this facility", null, details);
            return queryService.issue(issueId);
        }

        PatientAssessment a = engine.assess(source, LocalDate.now(), LocalDate.now(), settings.current());
        CbsFinding still = a.getFindings().stream()
                .filter(f -> f.getDedupKey().equals(finding.getDedupKey())).findFirst().orElse(null);
        if (still != null) {
            details.put("outcome", "FAILED");
            details.put("evidence", readableDates(still.getEvidence()));
            if (ISSUE_CORRECTED.equals(from)) {
                issue.setStatus(ISSUE_UNDER_REVIEW);
                save(issue);
            }
            history(issue, "REVALIDATED", from, "The problem is still present: " + still.getDescription(), null, details);
        } else {
            details.put("outcome", "PASSED");
            finding.setActive(false);
            finding.setClearedAt(LocalDateTime.now());
            findingRepository.save(finding);
            issue.setStatus(ISSUE_RESOLVED);
            issue.setResolvedAt(LocalDateTime.now());
            save(issue);
            history(issue, "REVALIDATED", from, "Revalidation passed: the condition is no longer present in the source records", null, details);
        }
        store.updateSummaryCounts(issue.getFacilityId(), issue.getPersonUuid());
        return queryService.issue(issueId);
    }

    public CbsIssueDetailDto requestException(long issueId, CbsExceptionRequest request) {
        CbsIssue issue = openIssue(issueId);
        requireInvestigationFinding(issue);
        String from = issue.getStatus();
        issue.setStatus(ISSUE_EXCEPTION_REQUESTED);
        save(issue);
        history(issue, "EXCEPTION_REQUESTED", from, request.getJustification().trim(), null, null);
        return queryService.issue(issueId);
    }

    public CbsIssueDetailDto decideException(long issueId, CbsExceptionDecisionRequest request) {
        CbsIssue issue = openIssue(issueId);
        if (!ISSUE_EXCEPTION_REQUESTED.equals(issue.getStatus())) {
            throw new IllegalArgumentException("There is no exception request waiting for a decision");
        }
        boolean approve = request.getApprove();
        String from = issue.getStatus();
        issue.setStatus(approve ? ISSUE_APPROVED_EXCEPTION : ISSUE_UNDER_REVIEW);
        if (approve) {
            issue.setResolvedAt(LocalDateTime.now());
        }
        save(issue);
        history(issue, approve ? "EXCEPTION_APPROVED" : "EXCEPTION_REJECTED", from, request.getNote().trim(), null, null);
        store.updateSummaryCounts(issue.getFacilityId(), issue.getPersonUuid());
        return queryService.issue(issueId);
    }

    public CbsIssueDetailDto escalate(long issueId, CbsNoteRequest request) {
        CbsIssue issue = openIssue(issueId);
        issue.setEscalated(true);
        save(issue);
        history(issue, "ESCALATED", issue.getStatus(), request.getNote().trim(), null, null);
        return queryService.issue(issueId);
    }

    // ---------------------------------------------------------------- helpers

    private CbsIssueDetailDto move(long issueId, List<String> allowedFrom, String to, String action) {
        CbsIssue issue = openIssue(issueId);
        String from = issue.getStatus();
        if (!allowedFrom.contains(from)) {
            throw new IllegalArgumentException("This action is not allowed while the issue is " + from.replace('_', ' ').toLowerCase());
        }
        issue.setStatus(to);
        save(issue);
        history(issue, action, from, null, null, null);
        return queryService.issue(issueId);
    }

    /** Loads the issue only if it belongs to the user's facility, and refuses closed ones. */
    private CbsIssue openIssue(long issueId) {
        CbsIssue issue = issueRepository.findByIdAndFacilityId(issueId, access.facilityId())
                .orElseThrow(() -> new EntityNotFoundException(CbsIssue.class, "id", String.valueOf(issueId)));
        if (CLOSED.contains(issue.getStatus())) {
            throw new IllegalArgumentException("This issue is closed. It reopens automatically if the problem is found again.");
        }
        return issue;
    }

    // The spec asks for an investigation finding before a correction or exception can be submitted
    private void requireInvestigationFinding(CbsIssue issue) {
        if (!historyRepository.existsByIssueIdAndAction(issue.getId(), INVESTIGATION_FINDING)) {
            throw new IllegalArgumentException("Record an investigation finding first");
        }
    }

    private void save(CbsIssue issue) {
        issue.setUpdatedAt(LocalDateTime.now());
        issueRepository.save(issue);
    }

    // fromStatus is the status before the action; toStatus is read from the issue after it
    private void history(CbsIssue issue, String action, String fromStatus, String note, LocalDate actionDate,
                         Map<String, Object> details) {
        CbsIssueHistory h = new CbsIssueHistory();
        h.setIssueId(issue.getId());
        h.setAction(action);
        h.setFromStatus(fromStatus);
        h.setToStatus(issue.getStatus());
        h.setNote(note);
        h.setActionDate(actionDate);
        h.setDetails(details);
        h.setPerformedBy(access.userName());
        h.setPerformedAt(LocalDateTime.now());
        historyRepository.save(h);
    }

    // The jsonb column type would store LocalDate as [2011, 3, 2]; keep dates readable as 2011-03-02
    private static Map<String, Object> readableDates(Map<String, Object> evidence) {
        Map<String, Object> out = new LinkedHashMap<>();
        evidence.forEach((k, v) -> {
            if (v instanceof LocalDate) {
                out.put(k, v.toString());
            } else if (v instanceof List) {
                out.put(k, ((List<?>) v).stream().map(i -> i instanceof LocalDate ? i.toString() : i).collect(Collectors.toList()));
            } else {
                out.put(k, v);
            }
        });
        return out;
    }
}
