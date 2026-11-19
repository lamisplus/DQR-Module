package org.lamisplus.modules.dqr.service.cbs;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.base.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.dqr.domain.cbs.CbsEvent;
import org.lamisplus.modules.dqr.domain.cbs.CbsRule;
import org.lamisplus.modules.dqr.domain.cbs.PatientAssessment;
import org.lamisplus.modules.dqr.domain.cbs.PatientSource;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsFindingFilter;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsFindingsPage;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsIssueDetailDto;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsIssueFilter;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsIssuesPage;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsOverviewDto;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsPage;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsPatientDetailDto;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsPatientFilter;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsReportFilter;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsReportSummaryDto;
import org.lamisplus.modules.dqr.domain.cbs.dto.CbsRulesDto;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsIssue;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsPatientSummary;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsSourceRows;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsViews;
import org.lamisplus.modules.dqr.repository.cbs.CbsClinicalRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsFindingRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsIssueHistoryRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsIssueRepository;
import org.lamisplus.modules.dqr.repository.cbs.CbsPatientSummaryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.lamisplus.modules.dqr.domain.cbs.CbsStatus.ISSUE_APPROVED_EXCEPTION;
import static org.lamisplus.modules.dqr.domain.cbs.CbsStatus.ISSUE_RESOLVED;

/** Read side of the CBS screens. Every query is limited to the current user's facility. */
@Service
@RequiredArgsConstructor
public class CbsQueryService {

    // Only these columns can be sorted on; anything else falls back to the default.
    // No table alias here: Spring Data adds it to native queries itself.
    private static final Map<String, String> PATIENT_SORTS = new HashMap<>();
    private static final Map<String, String> ISSUE_SORTS = new HashMap<>();

    static {
        PATIENT_SORTS.put("patientId", "patient_id");
        PATIENT_SORTS.put("latestEventDate", "latest_event_date");
        PATIENT_SORTS.put("treatmentStatus", "treatment_status");
        PATIENT_SORTS.put("openFlags", "open_flags");
        ISSUE_SORTS.put("id", "id");
        ISSUE_SORTS.put("dueDate", "due_date");
        ISSUE_SORTS.put("status", "status");
        ISSUE_SORTS.put("createdAt", "created_at");
        ISSUE_SORTS.put("severity", "(CASE f.severity WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END)");
    }

    private static final Map<String, String> INDICATOR_DEFINITIONS = new LinkedHashMap<>();

    static {
        INDICATOR_DEFINITIONS.put("clientsAssessed", "Distinct patients (HIV enrolment or HTS positive) whose records were checked in the last successful refresh. Not a count of clinical assessments.");
        INDICATOR_DEFINITIONS.put("patientsWithOpenFlags", "Distinct patients with at least one data quality finding that is not resolved or an approved exception. Each patient counted once.");
        INDICATOR_DEFINITIONS.put("missingExpectedEvents", "Unresolved findings where an expected event (linkage, ART initiation, follow-up, EAC) is not documented after its due date. Counted per finding.");
        INDICATOR_DEFINITIONS.put("statusDiscrepancies", "Distinct patients whose client tracking status differs from the status worked out from treatment records (CBS-DQ-04).");
        INDICATOR_DEFINITIONS.put("resolvedIssues", "Issues closed because revalidation showed the problem is gone. Approved exceptions are counted separately.");
        INDICATOR_DEFINITIONS.put("approvedExceptions", "Issues closed by an approved exception instead of a correction.");
        INDICATOR_DEFINITIONS.put("overdueIssues", "Unresolved issues past their due date. Due dates come from severity: High 7, Medium 14, Low 30 days by default.");
    }

    private final CbsAccess access;
    private final CbsClinicalRepository clinicalRepository;
    private final CbsPatientSummaryRepository summaryRepository;
    private final CbsFindingRepository findingRepository;
    private final CbsIssueRepository issueRepository;
    private final CbsIssueHistoryRepository historyRepository;
    private final CbsSourceLoader sourceLoader;
    private final CbsEngine engine;
    private final CbsSettingsService settings;
    private final CbsAuditService audit;
    private final CbsContextService contextService;

    //context and dashboard
    public CbsOverviewDto overview() {
        return overview(null, null);
    }

    /**
     * With an "Event date from / to" range the flag, gap, discrepancy and High severity numbers
     * only count findings whose event (the record the problem is about) is dated in that range,
     * and clients assessed counts the patients with any record dated in it. The treatment status
     * breakdown is "as of the last refresh", so it doesn't change with the range.
     */
    public CbsOverviewDto overview(LocalDate from, LocalDate to) {
        requireValidRange(from, to);
        Long facilityId = access.facilityId();
        CbsOverviewDto out = new CbsOverviewDto();
        contextService.fill(out, facilityId);
        if (out.getLastSuccessfulRefresh() == null) {
            // no cards until data exists, so the screen shows "Not available" instead of zeros
            return out;
        }
        CbsViews.OverviewCounts counts = summaryRepository.overviewCounts(facilityId);
        CbsOverviewDto.Cards cards = new CbsOverviewDto.Cards();
        cards.setClientsAssessed(counts.getClientsAssessed());
        cards.setRecordsWithFlags(counts.getRecordsWithFlags());
        cards.setEventGaps(counts.getEventGaps());
        cards.setStatusDiscrepancies(counts.getStatusDiscrepancies());
        cards.setHighPriorityUnresolved(findingRepository.countUnresolvedHighSeverity(facilityId));
        if (from != null || to != null) {
            CbsViews.PeriodCounts period = findingRepository.unresolvedCountsForEventDates(facilityId, date(from), date(to));
            cards.setRecordsWithFlags(period.getRecordsWithFlags());
            cards.setEventGaps(period.getEventGaps());
            cards.setStatusDiscrepancies(period.getStatusDiscrepancies());
            cards.setHighPriorityUnresolved(period.getHighPriorityUnresolved());
            cards.setClientsAssessed(summaryRepository.clientsWithActivityBetween(facilityId, date(from), date(to)));
        }
        out.setCards(cards);
        out.setTreatmentStatusBreakdown(summaryRepository.treatmentStatusBreakdown(facilityId));
        return out;
    }

    //patients
    public CbsPage<CbsViews.PatientRow> patients(CbsPatientFilter filter) {
        return patients(filter, filter.getSize());
    }

    // Exports pass a large size; screens are limited to 200 rows per page by the filter
    CbsPage<CbsViews.PatientRow> patients(CbsPatientFilter filter, int size) {
        requireValidRange(filter.getFrom(), filter.getTo());
        String column = PATIENT_SORTS.getOrDefault(filter.getSort(), "patient_id");
        Sort order = JpaSort.unsafe(direction(filter.getDirection(), Sort.Direction.ASC), column).and(JpaSort.unsafe("person_uuid"));
        return new CbsPage<>(summaryRepository.search(access.facilityId(), prefix(filter.getSearch()), blank(filter.getStatus()),
                blank(filter.getCard()), blank(filter.getCategory()), blank(filter.getSeverity()),
                date(filter.getFrom()), date(filter.getTo()),
                PageRequest.of(filter.getPage(), size, order)));
    }

    /** Worked out live from the clinical records for the chosen as-of date; nothing is saved. */
    public CbsPatientDetailDto patient(String personUuid, LocalDate asOf) {
        Long facilityId = access.facilityId();
        PatientSource source = sourceLoader.loadPatient(facilityId, personUuid);
        if (source == null) {
            // same answer whether the patient doesn't exist or belongs to another facility
            throw new EntityNotFoundException(CbsPatientSummary.class, "personUuid", personUuid);
        }
        LocalDate today = LocalDate.now();
        LocalDate date = asOf == null || asOf.isAfter(today) ? today : asOf;
        PatientAssessment a = engine.assess(source, date, today, settings.current());

        CbsPatientDetailDto out = new CbsPatientDetailDto();
        out.setContext(contextService.current());
        out.setPatient(header(source, date));
        out.setTreatmentStatus(a.getTreatmentStatus());
        out.setVitalStatus(a.getVitalStatus());
        out.setViralLoadStatus(a.getViralLoadStatus());
        out.setEacStatus(a.getEacStatus());
        out.setDocumentedTreatmentStatus(a.getDocumentedTreatmentStatus());
        List<CbsPatientDetailDto.Event> timeline = new ArrayList<>();
        List<CbsPatientDetailDto.Event> supporting = new ArrayList<>();
        a.getEvents().forEach(e -> (e.getType().isPrimary() ? timeline : supporting).add(event(e)));
        out.setTimeline(timeline);
        out.setSupporting(supporting);
        out.setEventSummary(a.getEventSummary());
        List<CbsPatientDetailDto.Indicator> indicators = new ArrayList<>();
        a.getIndicators().forEach((label, value) -> indicators.add(new CbsPatientDetailDto.Indicator(label, String.valueOf(value))));
        out.setIndicators(indicators);
        out.setFindingsAsOf(a.getFindings().size());
        out.setIssues(findingRepository.unresolvedForPatient(facilityId, personUuid));
        out.setIdentityConflicts(identityConflicts(facilityId, source));

        Map<String, Object> details = new HashMap<>();
        details.put("asOfDate", date.toString());
        audit.record(facilityId, "VIEW_PATIENT", personUuid, details, access.userName());
        return out;
    }

    //data quality monitoring
    public CbsFindingsPage findings(CbsFindingFilter filter) {
        requireValidRange(filter.getFrom(), filter.getTo());
        Long facilityId = access.facilityId();
        return new CbsFindingsPage(findingRepository.search(facilityId, blank(filter.getView()), blank(filter.getIssueStatus()),
                blank(filter.getCategory()), blank(filter.getSeverity()), blank(filter.getRuleCode()), prefix(filter.getSearch()),
                date(filter.getFrom()), date(filter.getTo()), PageRequest.of(filter.getPage(), filter.getSize())),
                findingCounts(facilityId));
    }

    public CbsRulesDto rules() {
        Map<String, Long> counts = new HashMap<>();
        findingRepository.unresolvedByRule(access.facilityId()).forEach(r -> counts.put(r.getName(), r.getCount()));
        List<CbsRulesDto.Rule> rules = new ArrayList<>();
        for (CbsRule r : CbsRule.values()) {
            CbsRulesDto.Rule rule = new CbsRulesDto.Rule();
            rule.setCode(r.getCode());
            rule.setName(r.getName());
            rule.setVersion(r.getVersion());
            rule.setCategory(r.getCategory());
            rule.setFindingType(r.getFindingType());
            rule.setSeverity(r.getSeverity());
            rule.setDescription(r.getDescription());
            rule.setOpenFindings(counts.getOrDefault(r.getCode(), 0L));
            rules.add(rule);
        }
        return new CbsRulesDto(rules, settings.all());
    }

    //issues
    public CbsIssuesPage issues(CbsIssueFilter filter) {
        return issues(filter, filter.getSize());
    }

    CbsIssuesPage issues(CbsIssueFilter filter, int size) {
        requireValidRange(filter.getFrom(), filter.getTo());
        Long facilityId = access.facilityId();
        String column = ISSUE_SORTS.getOrDefault(filter.getSort(), "id");
        Sort order = JpaSort.unsafe(direction(filter.getDirection(), Sort.Direction.DESC), column).and(JpaSort.unsafe("id"));
        String search = filter.getSearch();
        // "DQ-1001" and "1001" both find issue 1001
        String issueNumber = search == null ? null : search.trim().replaceAll("(?i)^DQ-", "");
        return new CbsIssuesPage(issueRepository.search(facilityId, blank(filter.getStatus()), blank(filter.getAssignedTo()),
                blank(filter.getSeverity()), blank(filter.getCategory()), blank(filter.getFlag()), prefix(search),
                blank(issueNumber), date(filter.getFrom()), date(filter.getTo()),
                PageRequest.of(filter.getPage(), size, order)), issueRepository.summary(facilityId));
    }

    public CbsIssueDetailDto issue(long issueId) {
        CbsViews.IssueDetail detail = issueRepository.detail(access.facilityId(), issueId)
                .orElseThrow(() -> new EntityNotFoundException(CbsIssue.class, "id", String.valueOf(issueId)));
        CbsRule rule = CbsRule.fromCode(detail.getRuleCode());
        return new CbsIssueDetailDto(detail, rule == null ? null : rule.getName(), historyRepository.historyFor(issueId));
    }

    public List<CbsSourceRows.User> assignableUsers() {
        return clinicalRepository.assignableUsers(access.facilityId());
    }

    //reports
    public CbsReportSummaryDto reportSummary(CbsReportFilter filter) {
        requireValidRange(filter.getFrom(), filter.getTo());
        Long facilityId = access.facilityId();
        String from = date(filter.getFrom());
        String to = date(filter.getTo());
        CbsReportSummaryDto out = new CbsReportSummaryDto();
        contextService.fill(out, facilityId);
        CbsReportSummaryDto.Indicators ind = new CbsReportSummaryDto.Indicators();
        ind.setClientsAssessed(summaryRepository.countByFacilityId(facilityId));
        ind.setPatientsWithOpenFlags(summaryRepository.countByFacilityIdAndOpenFlagsGreaterThan(facilityId, 0));
        ind.setMissingExpectedEvents(findingRepository.countUnresolvedGaps(facilityId));
        ind.setStatusDiscrepancies(summaryRepository.countByFacilityIdAndStatusDiscrepancyTrue(facilityId));
        ind.setResolvedIssues(issueRepository.countClosedBetween(facilityId, ISSUE_RESOLVED, from, to));
        ind.setApprovedExceptions(issueRepository.countClosedBetween(facilityId, ISSUE_APPROVED_EXCEPTION, from, to));
        ind.setOverdueIssues(issueRepository.summary(facilityId).getOverdue());
        out.setIndicators(ind);
        out.setDefinitions(INDICATOR_DEFINITIONS);
        out.setByCategory(findingRepository.unresolvedByCategoryAndSeverity(facilityId));
        out.setIssueStatus(issueRepository.countByStatus(facilityId));
        out.setFrom(filter.getFrom());
        out.setTo(filter.getTo());
        return out;
    }

    public static Map<String, String> indicatorDefinitions() {
        return Collections.unmodifiableMap(INDICATOR_DEFINITIONS);
    }

    //helpers
    private CbsFindingsPage.Counts findingCounts(Long facilityId) {
        CbsViews.FindingCounts c = findingRepository.unresolvedCounts(facilityId);
        CbsFindingsPage.Counts out = new CbsFindingsPage.Counts();
        out.setAll(c.getAll());
        out.setValidation(c.getValidation());
        out.setGaps(c.getGaps());
        out.setStatus(c.getStatus());
        out.setByCategory(findingRepository.unresolvedByCategory(facilityId));
        out.setBySeverity(findingRepository.unresolvedBySeverity(facilityId));
        out.setByRule(findingRepository.unresolvedByRule(facilityId));
        return out;
    }

    private static CbsPatientDetailDto.Header header(PatientSource source, LocalDate asOf) {
        CbsPatientDetailDto.Header h = new CbsPatientDetailDto.Header();
        h.setPersonUuid(source.getPersonUuid());
        h.setPatientId(source.getPatientId());
        h.setHospitalNumber(source.getHospitalNumber());
        h.setSex(source.getSex());
        h.setDateOfBirth(source.getDateOfBirth());
        h.setAge(source.getDateOfBirth() == null ? null : Period.between(source.getDateOfBirth(), asOf).getYears());
        h.setAsOfDate(asOf);
        return h;
    }

    private static CbsPatientDetailDto.Event event(CbsEvent e) {
        CbsPatientDetailDto.Event row = new CbsPatientDetailDto.Event();
        row.setType(e.getType().name());
        row.setLabel(e.getType().getLabel());
        row.setEventDate(e.getEventDate());
        row.setRecordedDate(e.getRecordedDate());
        row.setSourceModule(e.getSourceModule());
        row.setSourceTable(e.getSourceTable());
        row.setSourceRecordId(e.getSourceRecordId());
        row.setVerificationMethod(e.getVerificationMethod());
        row.setDetails(e.getDetails());
        row.setNeedsReview(e.isNeedsReview());
        return row;
    }

    /** Other patients at the facility sharing this hospital number or unique ID. Shown, never merged. */
    private List<CbsViews.IdentityRow> identityConflicts(Long facilityId, PatientSource s) {
        if (blank(s.getHospitalNumber()) == null && blank(s.getPatientId()) == null) {
            return Collections.emptyList();
        }
        return summaryRepository.identityConflicts(facilityId, s.getPersonUuid(), s.getHospitalNumber(), s.getPatientId());
    }

    /**
     * The screens already stop a "to" date earlier than the "from" date; this catches the same
     * mistake from anything else that calls the API. Core turns it into a 400 with this message.
     */
    static void requireValidRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("The end date (" + to + ") can't be earlier than the start date (" + from + ").");
        }
    }

    private static Sort.Direction direction(String value, Sort.Direction fallback) {
        if ("asc".equalsIgnoreCase(value)) {
            return Sort.Direction.ASC;
        }
        return "desc".equalsIgnoreCase(value) ? Sort.Direction.DESC : fallback;
    }

    // Search matches the start of an ID, so typing part of a hospital number still finds it
    private static String prefix(String search) {
        return blank(search) == null ? null : search.trim() + "%";
    }

    private static String blank(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    // Dates go to the native queries as text; a null LocalDate would be sent with no SQL type
    private static String date(LocalDate d) {
        return d == null ? null : d.toString();
    }
}
