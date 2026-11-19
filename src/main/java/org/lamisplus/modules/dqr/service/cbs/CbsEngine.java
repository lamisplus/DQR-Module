package org.lamisplus.modules.dqr.service.cbs;

import org.lamisplus.modules.dqr.domain.cbs.CbsEvent;
import org.lamisplus.modules.dqr.domain.cbs.CbsEventType;
import org.lamisplus.modules.dqr.domain.cbs.CbsFinding;
import org.lamisplus.modules.dqr.domain.cbs.CbsRule;
import org.lamisplus.modules.dqr.domain.cbs.CbsSettings;
import org.lamisplus.modules.dqr.domain.cbs.PatientAssessment;
import org.lamisplus.modules.dqr.domain.cbs.PatientAssessment.EventSummary;
import org.lamisplus.modules.dqr.domain.cbs.PatientAssessment.StatusResult;
import org.lamisplus.modules.dqr.domain.cbs.PatientSource;
import org.lamisplus.modules.dqr.domain.cbs.projection.CbsSourceRows;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lamisplus.modules.dqr.domain.cbs.CbsStatus.*;

/**
 * Turns one patient's clinical records into CBS events, an as-of treatment and vital status,
 * the event summary and the data quality findings.
 *
 * Events follow the Event_Catalogue / Derivation_Logic sheets and rules follow the
 * DQ_Rules_Catalogue sheet of the BA mapping workbook (v1.1, 23 Oct 2026), with the BA team's
 * answers of 17 Nov 2026:
 *  - interruption date = last pickup + refill period + iit_days (28);
 *  - CBS-DQ-05 follow-up is due one day after the interruption date;
 *  - CBS-DQ-14 and CBS-DQ-26 are flagged immediately (no waiting period);
 *  - enrolled clients with no enrolment date are flagged (they fall under CBS-DQ-14).
 *
 * Ground rules from the requirements document still hold:
 *  - only records dated on or before the as-of date count;
 *  - a missing record is never treated as proof that something did not happen;
 *  - transfer out and death are not interruptions;
 *  - repeated interruptions and returns all stay on the timeline.
 */
@Component
public class CbsEngine {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy");
    // viral_load_indication "Confirmation (3-6 months after intense adherence counselling)"
    private static final int INDICATION_POST_EAC = 302;
    private static final String COMMENCEMENT_TABLE = "hiv_enrollment_commencement";

    public PatientAssessment assess(PatientSource s, LocalDate asOf, LocalDate today, CbsSettings cfg) {
        PatientAssessment a = new PatientAssessment();
        a.setPersonUuid(s.getPersonUuid());
        a.setAsOfDate(asOf);

        List<PatientSource.Refill> refills = s.getRefills().stream()
                .filter(r -> r.getVisitDate() != null && !r.getVisitDate().isAfter(asOf))
                .sorted(Comparator.comparing(PatientSource.Refill::getVisitDate))
                .collect(Collectors.toList());

        CbsEvent diagnosis = diagnosisEvent(s, asOf);
        CbsEvent linkage = linkageEvent(s, asOf);
        CbsEvent artStart = artInitiationEvent(s, asOf);
        List<CbsEvent> transfersIn = transferInEvents(s, asOf);
        List<CbsEvent> transfersOut = trackerEvents(s, asOf, CbsTrackerStatus.TRANSFER_OUT, CbsEventType.TRANSFER_OUT);
        List<CbsEvent> deaths = deathEvents(s, asOf);
        LocalDate deathDate = deaths.isEmpty() ? null : deaths.get(0).getEventDate();
        LocalDate lastTransferOut = transfersOut.isEmpty() ? null : transfersOut.get(transfersOut.size() - 1).getEventDate();

        List<CbsEvent> interruptions = new ArrayList<>();
        List<CbsEvent> returns = new ArrayList<>();
        List<LocalDate> gapExplainers = new ArrayList<>();
        transfersOut.forEach(t -> gapExplainers.add(t.getEventDate()));
        if (deathDate != null) {
            gapExplainers.add(deathDate);
        }
        LocalDate coverageEnd = interruptionsFromRefills(refills, asOf, deathDate, lastTransferOut, gapExplainers, cfg,
                interruptions, returns);
        documentedInterruptions(s, asOf, refills, interruptions, returns);

        List<CbsEvent> viralLoads = viralLoadEvents(s, asOf);
        CbsEvent baselineCd4 = baselineCd4Event(s, asOf, artStart, cfg);
        PatientSource.DatedRecord completedEac = latest(s.getCompletedEacs(), asOf);
        CbsEvent postEacVl = postEacViralLoad(s, asOf, completedEac, viralLoads);
        List<CbsEvent> switches = regimenSwitches(refills);

        addIfPresent(a.getEvents(), diagnosis);
        addIfPresent(a.getEvents(), linkage);
        addIfPresent(a.getEvents(), artStart);
        a.getEvents().addAll(transfersIn);
        a.getEvents().addAll(transfersOut);
        a.getEvents().addAll(interruptions);
        a.getEvents().addAll(returns);
        a.getEvents().addAll(deaths);
        addIfPresent(a.getEvents(), baselineCd4);
        a.getEvents().addAll(viralLoads);
        a.getEvents().addAll(eacEvents(s, asOf));
        a.getEvents().addAll(switches);
        a.getEvents().sort(Comparator.comparing(CbsEvent::getEventDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(e -> e.getType().ordinal()));

        a.setVitalStatus(vitalStatus(s, asOf, deathDate, cfg));
        a.setTreatmentStatus(treatmentStatus(s, asOf, artStart, refills, coverageEnd, deathDate, lastTransferOut, cfg));
        a.setDocumentedTreatmentStatus(documentedStatus(s, asOf));
        viralLoadAndEac(s, asOf, artStart, cfg, a);

        Context c = new Context();
        c.s = s;
        c.asOf = asOf;
        c.today = today;
        c.cfg = cfg;
        c.a = a;
        c.diagnosis = diagnosis;
        c.linkage = linkage;
        c.artStart = artStart;
        c.transfersIn = transfersIn;
        c.transfersOut = transfersOut;
        c.deaths = deaths;
        c.deathDate = deathDate;
        c.interruptions = interruptions;
        c.returns = returns;
        c.refills = refills;
        c.baselineCd4 = baselineCd4;
        c.completedEac = completedEac;
        c.postEacVl = postEacVl;
        c.switches = switches;
        runRules(c);
        eventSummary(c);
        markEventsNeedingReview(a);
        indicators(a, refills, baselineCd4);
        return a;
    }

    /**
     * CBS-DQ-24. Records that point at no patient can't be put on any timeline, so they are
     * reported on their own. The finding is keyed on the person uuid the record carries (or
     * "unlinked" when it has none) so it still shows in the facility lists.
     */
    public PatientAssessment unlinkedRecords(List<CbsSourceRows.OrphanRecord> rows) {
        PatientAssessment a = new PatientAssessment();
        a.setPersonUuid(null);
        for (CbsSourceRows.OrphanRecord r : rows) {
            String person = r.getPersonUuid() == null || r.getPersonUuid().trim().isEmpty() ? "unlinked" : r.getPersonUuid();
            LocalDate date = r.getRecordDate() == null ? null : toLocalDate(r.getRecordDate());
            a.getFindings().add(CbsFinding.of(CbsRule.DQ24, person, null, r.getSourceTable() + "|" + r.getUuid(),
                    "A record in " + r.getSourceTable() + (date == null ? "" : " dated " + fmt(date))
                            + " could not be linked to a patient and is excluded from timelines until reviewed.")
                    .on(date).evidence("sourceTable", r.getSourceTable()).evidence("recordId", r.getUuid())
                    .evidence("patientKey", r.getPersonUuid()).evidence("recordDate", date));
        }
        return a;
    }

    /** Everything the rules and the summary need, so the methods don't take fifteen parameters. */
    private static class Context {
        PatientSource s;
        LocalDate asOf;
        LocalDate today;
        CbsSettings cfg;
        PatientAssessment a;
        CbsEvent diagnosis;
        CbsEvent linkage;
        CbsEvent artStart;
        List<CbsEvent> transfersIn;
        List<CbsEvent> transfersOut;
        List<CbsEvent> deaths;
        LocalDate deathDate;
        List<CbsEvent> interruptions;
        List<CbsEvent> returns;
        List<PatientSource.Refill> refills;
        CbsEvent baselineCd4;
        PatientSource.DatedRecord completedEac;
        CbsEvent postEacVl;
        List<CbsEvent> switches;

        boolean transferIn() {
            return s.getEnrollment() != null && isTransferIn(s.getEnrollment().getStatusAtRegistration());
        }
    }

    // ---------------------------------------------------------------- events

    /**
     * EV01: the earliest confirmed diagnosis. That is either the HTS final positive result
     * (hts_encounter) or the historical "date confirmed HIV positive" on the enrolment form.
     * Later positives are retests and are ignored.
     */
    private CbsEvent diagnosisEvent(PatientSource s, LocalDate asOf) {
        PatientSource.DatedRecord hts = earliest(s.getHtsPositives(), asOf);
        LocalDate enrolDate = s.getEnrollment() == null ? null : s.getEnrollment().getDateConfirmedHiv();
        if (enrolDate != null && enrolDate.isAfter(asOf)) {
            enrolDate = null;
        }
        CbsEvent e;
        if (hts != null && (enrolDate == null || !hts.getDate().isAfter(enrolDate))) {
            e = CbsEvent.of(CbsEventType.HIV_DIAGNOSIS, hts.getDate(), "HTS", "hts_encounter", hts.getUuid(), "clinical");
            e.setRecordedDate(hts.getCreatedDate());
        } else if (enrolDate != null) {
            e = CbsEvent.of(CbsEventType.HIV_DIAGNOSIS, enrolDate, "HIV Enrollment", COMMENCEMENT_TABLE,
                    s.getEnrollment().getUuid(), "historical");
            e.setRecordedDate(s.getEnrollment().getCreatedDate());
        } else {
            return null;
        }
        // The spec asks to keep the diagnosis date and the test date apart, so both are kept
        return e.detail("htsTestDate", hts == null ? null : hts.getDate())
                .detail("htsRecordId", hts == null ? null : hts.getUuid())
                .detail("enrolmentDiagnosisDate", enrolDate);
    }

    /**
     * EV02: completed enrolment (with an ART unique ID) of a client diagnosed here. Transfer-in
     * enrolments are not linkage; it happened at the other facility.
     */
    private CbsEvent linkageEvent(PatientSource s, LocalDate asOf) {
        PatientSource.Enrollment en = s.getEnrollment();
        if (en == null || isTransferIn(en.getStatusAtRegistration()) || en.getDateOfRegistration() == null
                || en.getDateOfRegistration().isAfter(asOf) || isBlank(en.getUniqueId())) {
            return null;
        }
        CbsEvent e = CbsEvent.of(CbsEventType.LINKAGE_TO_CARE, en.getDateOfRegistration(), "HIV Enrollment",
                COMMENCEMENT_TABLE, en.getUuid(), "clinical");
        e.setRecordedDate(en.getCreatedDate());
        return e.detail("statusAtRegistration", en.getStatusAtRegistration()).detail("artUniqueId", en.getUniqueId());
    }

    /**
     * EV03: earliest ART commencement. A refill on its own is not an initiation. For a
     * transfer-in with no commencement here, the original ART start on the enrolment form is used.
     */
    private CbsEvent artInitiationEvent(PatientSource s, LocalDate asOf) {
        PatientSource.Commencement c = earliestCommencement(s, asOf);
        if (c != null) {
            CbsEvent e = CbsEvent.of(CbsEventType.ART_INITIATION, c.getDate(), "ART Commencement", COMMENCEMENT_TABLE,
                    c.getUuid(), "clinical");
            e.setRecordedDate(c.getCreatedDate());
            if (s.getEnrollment() != null && isTransferIn(s.getEnrollment().getStatusAtRegistration())) {
                e.detail("note", "Client transferred in; initiation may have happened at another facility");
            }
            return e;
        }
        PatientSource.Enrollment en = s.getEnrollment();
        if (en != null && isTransferIn(en.getStatusAtRegistration()) && en.getDateStarted() != null
                && !en.getDateStarted().isAfter(asOf)) {
            CbsEvent e = CbsEvent.of(CbsEventType.ART_INITIATION, en.getDateStarted(), "HIV Enrollment", COMMENCEMENT_TABLE,
                    en.getUuid(), "historical");
            e.setRecordedDate(en.getCreatedDate());
            return e.detail("note", "Original ART start documented at transfer in");
        }
        return null;
    }

    /** EV04: transfer-in status at enrolment, the transfer-in form, and tracker transfer-in statuses. */
    private List<CbsEvent> transferInEvents(PatientSource s, LocalDate asOf) {
        Map<LocalDate, CbsEvent> byDate = new TreeMap<>();
        PatientSource.Enrollment en = s.getEnrollment();
        if (en != null && isTransferIn(en.getStatusAtRegistration()) && en.getDateOfRegistration() != null
                && !en.getDateOfRegistration().isAfter(asOf)) {
            byDate.put(en.getDateOfRegistration(), CbsEvent.of(CbsEventType.TRANSFER_IN, en.getDateOfRegistration(),
                    "HIV Enrollment", COMMENCEMENT_TABLE, en.getUuid(), "clinical")
                    .detail("statusAtRegistration", en.getStatusAtRegistration()));
        }
        for (PatientSource.DatedRecord f : s.getTransferInForms()) {
            if (f.getDate() != null && !f.getDate().isAfter(asOf)) {
                byDate.putIfAbsent(f.getDate(), CbsEvent.of(CbsEventType.TRANSFER_IN, f.getDate(), "HIV Transfer",
                        "hiv_patient_transfer_in", f.getUuid(), "clinical"));
            }
        }
        for (CbsEvent e : trackerEvents(s, asOf, CbsTrackerStatus.TRANSFER_IN, CbsEventType.TRANSFER_IN)) {
            byDate.putIfAbsent(e.getEventDate(), e);
        }
        return new ArrayList<>(byDate.values());
    }

    private List<CbsEvent> trackerEvents(PatientSource s, LocalDate asOf, String status, CbsEventType type) {
        Map<LocalDate, CbsEvent> byDate = new TreeMap<>();
        for (PatientSource.TrackerStatus t : s.getTrackerStatuses()) {
            if (status.equals(t.getStatus()) && t.getStatusDate() != null && !t.getStatusDate().isAfter(asOf)) {
                byDate.putIfAbsent(t.getStatusDate(), CbsEvent.of(type, t.getStatusDate(), "Client Tracking",
                        "hiv_status_tracker", t.getUuid(), "tracking").detail("recordedStatus", t.getRawStatus()));
            }
        }
        return new ArrayList<>(byDate.values());
    }

    /** EV08: death from the status tracker and the patient tracker form; same-day records are merged. */
    private List<CbsEvent> deathEvents(PatientSource s, LocalDate asOf) {
        Map<LocalDate, CbsEvent> byDate = new TreeMap<>();
        for (PatientSource.TrackerStatus t : s.getTrackerStatuses()) {
            if (CbsTrackerStatus.DIED.equals(t.getStatus()) && t.getStatusDate() != null && !t.getStatusDate().isAfter(asOf)) {
                CbsEvent e = byDate.computeIfAbsent(t.getStatusDate(), d -> CbsEvent.of(CbsEventType.DEATH, d,
                        "Client Tracking", "hiv_status_tracker", t.getUuid(), "tracking"));
                e.detail("causeOfDeath", t.getCauseOfDeath());
            }
        }
        for (PatientSource.PatientTracking t : s.getTrackings()) {
            if (t.getDateOfDeath() != null && !t.getDateOfDeath().isAfter(asOf)) {
                CbsEvent e = byDate.computeIfAbsent(t.getDateOfDeath(), d -> CbsEvent.of(CbsEventType.DEATH, d,
                        "Patient Tracker", "hiv_patient_tracker", t.getUuid(), "tracking"));
                if (!e.getDetails().containsKey("causeOfDeath")) {
                    e.detail("causeOfDeath", t.getCauseOfDeath());
                }
            }
        }
        // patient_person.deceased is deliberately not used: it is true for most patients
        // in migrated databases and never carries a date.
        return new ArrayList<>(byDate.values());
    }

    /**
     * EV06/EV07 rule-based. Walks the refills in date order. A client is interrupted once
     * iit_days have passed since the day their last supply ran out (pickup + refill period);
     * the next refill after that is a return to care. A gap that contains a transfer out or a
     * death is not an interruption; refills after those are picked up by CBS-DQ-18 and CBS-DQ-03.
     * Returns the day the supply runs out after the last refill (null when there are none).
     */
    private LocalDate interruptionsFromRefills(List<PatientSource.Refill> refills, LocalDate asOf, LocalDate deathDate,
                                               LocalDate lastTransferOut, List<LocalDate> gapExplainers, CbsSettings cfg,
                                               List<CbsEvent> interruptions, List<CbsEvent> returns) {
        LocalDate coverageEnd = null;
        PatientSource.Refill last = null;
        for (PatientSource.Refill r : refills) {
            if (coverageEnd != null) {
                LocalDate iitDate = coverageEnd.plusDays(cfg.getIitDays());
                LocalDate gapStart = last.getVisitDate();
                boolean explained = gapExplainers.stream()
                        .anyMatch(d -> !d.isBefore(gapStart) && d.isBefore(r.getVisitDate()));
                if (r.getVisitDate().isAfter(iitDate) && !explained) {
                    interruptions.add(interruption(iitDate, last, coverageEnd));
                    returns.add(CbsEvent.of(CbsEventType.RETURN_TO_CARE, r.getVisitDate(), "Pharmacy", "hiv_art_pharmacy",
                            r.getUuid(), "derived").detail("rule", "First refill after an interruption")
                            .detail("daysWithoutDrugs", ChronoUnit.DAYS.between(coverageEnd, r.getVisitDate())));
                }
            }
            LocalDate end = r.getVisitDate().plusDays(refillDays(r, cfg));
            coverageEnd = coverageEnd == null || end.isAfter(coverageEnd) ? end : coverageEnd;
            last = r;
        }
        if (coverageEnd != null) {
            LocalDate iitDate = coverageEnd.plusDays(cfg.getIitDays());
            boolean endedByDeath = deathDate != null && !deathDate.isAfter(iitDate);
            boolean endedByTransfer = lastTransferOut != null && !lastTransferOut.isBefore(last.getVisitDate())
                    && !lastTransferOut.isAfter(iitDate);
            if (iitDate.isBefore(asOf) && !endedByDeath && !endedByTransfer) {
                interruptions.add(interruption(iitDate, last, coverageEnd));
            }
        }
        return coverageEnd;
    }

    private CbsEvent interruption(LocalDate iitDate, PatientSource.Refill lastRefill, LocalDate coverageEnd) {
        return CbsEvent.of(CbsEventType.TREATMENT_INTERRUPTION, iitDate, "Pharmacy", "hiv_art_pharmacy",
                lastRefill.getUuid(), "derived")
                .detail("rule", "No ARVs for the configured number of days after the last supply ran out")
                .detail("lastRefillDate", lastRefill.getVisitDate())
                .detail("drugsRanOutOn", coverageEnd);
    }

    /**
     * EV06/EV07 documented. A "stopped treatment" status is an interruption on its status date.
     * When it falls in the same gap between refills as a rule-based interruption the two are one
     * episode, so the rule-based event just notes it. The first refill (or documented restart)
     * after a documented interruption is its return to care.
     *
     * Lost-to-follow-up statuses are not used here: in migrated databases they are written in
     * bulk by the ETL and carry no real tracking date.
     */
    private void documentedInterruptions(PatientSource s, LocalDate asOf, List<PatientSource.Refill> refills,
                                         List<CbsEvent> interruptions, List<CbsEvent> returns) {
        List<CbsEvent> stops = trackerEvents(s, asOf, CbsTrackerStatus.STOPPED_TREATMENT, CbsEventType.TREATMENT_INTERRUPTION);
        List<CbsEvent> restarts = trackerEvents(s, asOf, CbsTrackerStatus.RETURN_TO_CARE, CbsEventType.RETURN_TO_CARE);
        List<CbsEvent> added = new ArrayList<>();
        for (CbsEvent stop : stops) {
            LocalDate d = stop.getEventDate();
            LocalDate gapStart = refills.stream().map(PatientSource.Refill::getVisitDate).filter(v -> !v.isAfter(d))
                    .max(Comparator.naturalOrder()).orElse(null);
            LocalDate gapEnd = refills.stream().map(PatientSource.Refill::getVisitDate).filter(v -> v.isAfter(d))
                    .min(Comparator.naturalOrder()).orElse(null);
            CbsEvent sameGap = interruptions.stream().filter(i -> sameGap(i, gapStart, gapEnd, d)).findFirst().orElse(null);
            if (sameGap != null) {
                sameGap.detail("documentedStopDate", d).detail("documentedStopRecord", stop.getSourceRecordId());
                continue;
            }
            if (added.stream().anyMatch(i -> sameGap(i, gapStart, gapEnd, d))) {
                continue;
            }
            stop.detail("rule", "Stopped treatment documented in client tracking").detail("lastRefillDate", gapStart);
            added.add(stop);
            if (gapEnd != null) {
                PatientSource.Refill back = refills.stream().filter(r -> r.getVisitDate().equals(gapEnd)).findFirst().orElse(null);
                returns.add(CbsEvent.of(CbsEventType.RETURN_TO_CARE, gapEnd, "Pharmacy", "hiv_art_pharmacy",
                        back == null ? null : back.getUuid(), "derived").detail("rule", "First refill after a documented stop"));
            }
        }
        interruptions.addAll(added);
        interruptions.sort(Comparator.comparing(CbsEvent::getEventDate));
        // A documented restart counts when nothing in pharmacy shows the return yet
        for (int i = 0; i < interruptions.size(); i++) {
            LocalDate from = interruptions.get(i).getEventDate();
            LocalDate to = i + 1 < interruptions.size() ? interruptions.get(i + 1).getEventDate() : asOf.plusDays(1);
            boolean hasReturn = returns.stream().anyMatch(r -> r.getEventDate().isAfter(from) && r.getEventDate().isBefore(to));
            if (!hasReturn) {
                restarts.stream().filter(r -> r.getEventDate().isAfter(from) && r.getEventDate().isBefore(to)).findFirst()
                        .ifPresent(r -> returns.add(r.detail("rule", "Restart documented in client tracking")));
            }
        }
        returns.sort(Comparator.comparing(CbsEvent::getEventDate));
    }

    private static boolean sameGap(CbsEvent interruption, LocalDate gapStart, LocalDate gapEnd, LocalDate d) {
        Object last = interruption.getDetails().get("lastRefillDate");
        if (last == null || gapStart == null) {
            // no refill before the stop: same episode only if it is the same documented date
            return last == null && gapStart == null;
        }
        return last.equals(gapStart) && (gapEnd == null || d.isBefore(gapEnd));
    }

    /** EV10 (and EV13 for confirmation tests after EAC), dated by sample collection. */
    private List<CbsEvent> viralLoadEvents(PatientSource s, LocalDate asOf) {
        List<CbsEvent> out = new ArrayList<>();
        for (PatientSource.LabTest vl : s.getViralLoads()) {
            LocalDate d = vl.getEventDate();
            if (d != null && !d.isAfter(asOf)) {
                out.add(CbsEvent.of(CbsEventType.VIRAL_LOAD, d, "Laboratory", "laboratory_sample", vl.getUuid(), "laboratory")
                        .detail("sampleDate", vl.getSampleDate()).detail("resultDate", vl.getResultDate())
                        .detail("result", vl.getRawResult() == null ? null : vl.getRawResult().trim())
                        .detail("indication", vl.getIndication()));
            }
        }
        return out;
    }

    /**
     * EV09: the first CD4 sample on or before ART start + cd4_baseline_days, or the CD4 written
     * on the commencement form. AHD when CD4 is under 200 or WHO stage is III or IV.
     */
    private CbsEvent baselineCd4Event(PatientSource s, LocalDate asOf, CbsEvent artStart, CbsSettings cfg) {
        if (artStart == null) {
            return null;
        }
        LocalDate limit = artStart.getEventDate().plusDays(cfg.getCd4BaselineDays());
        PatientSource.LabTest lab = s.getCd4Tests().stream()
                .filter(t -> t.getEventDate() != null && !t.getEventDate().isAfter(limit) && !t.getEventDate().isAfter(asOf))
                .min(Comparator.comparing(PatientSource.LabTest::getEventDate)).orElse(null);
        PatientSource.Commencement c = earliestCommencement(s, asOf);
        String stage = c == null ? null : c.getClinicalStage();
        CbsEvent e;
        Double count;
        if (lab != null) {
            e = CbsEvent.of(CbsEventType.BASELINE_CD4, lab.getEventDate(), "Laboratory", "laboratory_sample", lab.getUuid(), "laboratory")
                    .detail("result", lab.getRawResult());
            count = lab.getCopies();
        } else if (c != null && c.getCd4AtStart() != null) {
            e = CbsEvent.of(CbsEventType.BASELINE_CD4, c.getDate(), "ART Commencement", COMMENCEMENT_TABLE, c.getUuid(), "clinical")
                    .detail("result", c.getCd4AtStart());
            count = c.getCd4AtStart().endsWith("%") ? null : CbsSourceLoader.parseCopies(c.getCd4AtStart());
        } else {
            return null;
        }
        boolean lowCd4 = count != null && count < 200;
        boolean stage34 = stage != null && (stage.endsWith("_III") || stage.endsWith("_IV"));
        return e.detail("whoStage", stage).detail("advancedHivDisease", lowCd4 || stage34);
    }

    /** EV13: the first confirmation viral load collected after the latest completed EAC. */
    private CbsEvent postEacViralLoad(PatientSource s, LocalDate asOf, PatientSource.DatedRecord completedEac,
                                      List<CbsEvent> viralLoads) {
        if (completedEac == null) {
            return null;
        }
        PatientSource.LabTest vl = s.getViralLoads().stream()
                .filter(t -> t.getIndication() != null && t.getIndication() == INDICATION_POST_EAC)
                .filter(t -> t.getEventDate() != null && !t.getEventDate().isBefore(completedEac.getDate()) && !t.getEventDate().isAfter(asOf))
                .min(Comparator.comparing(PatientSource.LabTest::getEventDate)).orElse(null);
        if (vl == null) {
            return null;
        }
        // Shown as a post-EAC viral load instead of a plain one
        CbsEvent e = viralLoads.stream().filter(v -> Objects.equals(v.getSourceRecordId(), vl.getUuid())).findFirst().orElse(null);
        if (e != null) {
            e.setType(CbsEventType.POST_EAC_VIRAL_LOAD);
            e.detail("eacCompletedOn", completedEac.getDate());
        }
        return e;
    }

    /** EV14: a refill whose ART regimen line is higher than the previous one (1st to 2nd, 2nd to 3rd). */
    private List<CbsEvent> regimenSwitches(List<PatientSource.Refill> refills) {
        List<CbsEvent> out = new ArrayList<>();
        Integer previous = null;
        for (PatientSource.Refill r : refills) {
            if (r.getRegimenLine() == null) {
                continue;
            }
            if (previous != null && r.getRegimenLine() > previous) {
                out.add(CbsEvent.of(CbsEventType.REGIMEN_SWITCH, r.getVisitDate(), "Pharmacy", "hiv_art_pharmacy", r.getUuid(), "derived")
                        .detail("fromLine", lineLabel(previous)).detail("toLine", lineLabel(r.getRegimenLine())));
            }
            previous = r.getRegimenLine();
        }
        return out;
    }

    private List<CbsEvent> eacEvents(PatientSource s, LocalDate asOf) {
        List<CbsEvent> out = new ArrayList<>();
        for (PatientSource.DatedRecord eac : s.getEacSessions()) {
            if (eac.getDate() != null && !eac.getDate().isAfter(asOf)) {
                out.add(CbsEvent.of(CbsEventType.EAC_SESSION, eac.getDate(), "EAC", "hiv_eac_session", eac.getUuid(), "clinical"));
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- statuses

    private StatusResult vitalStatus(PatientSource s, LocalDate asOf, LocalDate deathDate, CbsSettings cfg) {
        if (deathDate != null) {
            return new StatusResult(DECEASED, deathDate, "Confirmed death recorded in client tracking");
        }
        LocalDate lastContact = lastContact(s, asOf);
        if (lastContact != null && ChronoUnit.DAYS.between(lastContact, asOf) <= cfg.getAliveEvidenceDays()) {
            return new StatusResult(ALIVE, lastContact, "Last clinical contact on " + fmt(lastContact));
        }
        return new StatusResult(UNABLE, lastContact, lastContact == null
                ? "No clinical contact and no death record"
                : "No clinical contact in the last " + cfg.getAliveEvidenceDays() + " days and no death record");
    }

    private StatusResult treatmentStatus(PatientSource s, LocalDate asOf, CbsEvent artStart,
                                         List<PatientSource.Refill> refills, LocalDate coverageEnd,
                                         LocalDate deathDate, LocalDate lastTransferOut, CbsSettings cfg) {
        if (deathDate != null) {
            return new StatusResult(DECEASED, deathDate, "Confirmed death; no longer counted as receiving treatment");
        }
        PatientSource.Refill lastRefill = refills.isEmpty() ? null : refills.get(refills.size() - 1);
        if (lastTransferOut != null && (lastRefill == null || !lastRefill.getVisitDate().isAfter(lastTransferOut))
                && !transferredBackIn(s, lastTransferOut, asOf)) {
            return new StatusResult(TRANSFERRED_OUT, lastTransferOut, "Transfer out recorded in client tracking");
        }
        if (artStart == null) {
            if (lastRefill != null) {
                return new StatusResult(UNABLE, lastRefill.getVisitDate(), "ARV refills recorded but no ART commencement record");
            }
            return s.getEnrollment() != null
                    ? new StatusResult(NOT_INITIATED, null, "Enrolled in HIV care with no ART commencement record")
                    : new StatusResult(NOT_INITIATED, null, "HIV positive, not enrolled in HIV care");
        }
        if (lastRefill == null) {
            return new StatusResult(UNABLE, artStart.getEventDate(), "ART started but no pharmacy refill recorded");
        }
        LocalDate iitDate = coverageEnd.plusDays(cfg.getIitDays());
        if (!asOf.isAfter(iitDate)) {
            return new StatusResult(ACTIVE, lastRefill.getVisitDate(), "Last refill " + fmt(lastRefill.getVisitDate())
                    + " for " + refillDays(lastRefill, cfg) + " days");
        }
        return new StatusResult(INTERRUPTED, iitDate, "Drugs ran out on " + fmt(coverageEnd) + "; no refill within "
                + cfg.getIitDays() + " days");
    }

    private String documentedStatus(PatientSource s, LocalDate asOf) {
        return s.getTrackerStatuses().stream()
                .filter(t -> t.getStatusDate() != null && !t.getStatusDate().isAfter(asOf))
                .filter(t -> CbsTrackerStatus.impliedTreatmentStatus(t.getStatus()) != null)
                .max(Comparator.comparing(PatientSource.TrackerStatus::getStatusDate))
                .map(t -> CbsTrackerStatus.impliedTreatmentStatus(t.getStatus()))
                .orElse(null);
    }

    /**
     * EV11 and EV12. Only results from samples taken at least vl_eligibility_months after ART
     * start count for suppression. Under vl_undetectable_copies is "Undetectable", which is
     * also suppressed.
     */
    private void viralLoadAndEac(PatientSource s, LocalDate asOf, CbsEvent artStart, CbsSettings cfg, PatientAssessment a) {
        LocalDate eligibleFrom = artStart == null ? null : artStart.getEventDate().plusMonths(cfg.getVlEligibilityMonths());
        List<PatientSource.LabTest> results = s.getViralLoads().stream()
                .filter(v -> v.getResultDate() != null && !v.getResultDate().isAfter(asOf))
                .collect(Collectors.toList());
        PatientSource.LabTest latest = results.stream()
                .filter(v -> eligibleFrom == null || (v.getEventDate() != null && !v.getEventDate().isBefore(eligibleFrom)))
                .max(Comparator.comparing(PatientSource.LabTest::getResultDate)).orElse(null);
        if (latest == null) {
            a.setViralLoadStatus(results.isEmpty()
                    ? new StatusResult("No Result", null, "No viral load result recorded")
                    : new StatusResult("No Eligible Result", null, "Viral load results are all from the first "
                    + cfg.getVlEligibilityMonths() + " months on ART"));
            a.setEacStatus(new StatusResult(NOT_APPLICABLE, null, "No eligible viral load result"));
            return;
        }
        if (latest.getCopies() == null) {
            a.setViralLoadStatus(new StatusResult(UNABLE, latest.getResultDate(), "Result \"" + latest.getRawResult() + "\" can't be read"));
            a.setEacStatus(new StatusResult(UNABLE, null, "Latest viral load result can't be read"));
            return;
        }
        boolean suppressed = latest.getCopies() < cfg.getVlUnsuppressedCopies();
        String category = latest.getCopies() < cfg.getVlUndetectableCopies() ? "Undetectable" : suppressed ? "Suppressed" : "Not Suppressed";
        a.setViralLoadStatus(new StatusResult(category, latest.getResultDate(),
                trimNumber(latest.getCopies()) + " copies/mL on " + fmt(latest.getResultDate())));
        if (suppressed) {
            a.setEacStatus(new StatusResult(NOT_APPLICABLE, null, "Viral load suppressed"));
            return;
        }
        LocalDate eac = s.getEacSessions().stream().map(PatientSource.DatedRecord::getDate)
                .filter(Objects::nonNull).filter(d -> !d.isBefore(latest.getResultDate()) && !d.isAfter(asOf))
                .min(Comparator.naturalOrder()).orElse(null);
        if (eac != null) {
            a.setEacStatus(new StatusResult("EAC Documented", eac, "First EAC session on " + fmt(eac)));
        } else if (ChronoUnit.DAYS.between(latest.getResultDate(), asOf) > cfg.getEacDays()) {
            a.setEacStatus(new StatusResult("EAC Missing", latest.getResultDate().plusDays(cfg.getEacDays()),
                    "No EAC session within " + cfg.getEacDays() + " days of the unsuppressed result"));
        } else {
            a.setEacStatus(new StatusResult("EAC Due", latest.getResultDate().plusDays(cfg.getEacDays()),
                    "EAC due by " + fmt(latest.getResultDate().plusDays(cfg.getEacDays()))));
        }
    }

    // ---------------------------------------------------------------- rules

    private void runRules(Context c) {
        PatientSource s = c.s;
        String p = s.getPersonUuid();
        List<CbsFinding> f = c.a.getFindings();
        LocalDate asOf = c.asOf;
        CbsSettings cfg = c.cfg;
        boolean alive = c.deaths.isEmpty();
        boolean transferredOut = TRANSFERRED_OUT.equals(c.a.getTreatmentStatus().getStatus());
        PatientSource.Commencement commencement = earliestCommencement(s, asOf);

        // CBS-DQ-01: same-day diagnosis and ART start is normal (test and start), only "before" is flagged
        if (c.diagnosis != null && c.artStart != null && c.artStart.getEventDate().isBefore(c.diagnosis.getEventDate())) {
            f.add(CbsFinding.of(CbsRule.DQ01, p, CbsEventType.ART_INITIATION, String.valueOf(c.artStart.getEventDate()),
                    "ART initiation date (" + fmt(c.artStart.getEventDate()) + ") is earlier than the recorded HIV diagnosis date ("
                            + fmt(c.diagnosis.getEventDate()) + "). Review the affected records to verify the correct dates.")
                    .on(c.artStart.getEventDate()).evidence("artInitiationDate", c.artStart.getEventDate()).evidence("artSource", ref(c.artStart))
                    .evidence("diagnosisDate", c.diagnosis.getEventDate()).evidence("diagnosisSource", ref(c.diagnosis)));
        }

        // CBS-DQ-02: transfer-ins are excluded, their linkage happened elsewhere
        if (c.artStart != null && !c.transferIn()
                && (c.linkage == null || c.linkage.getEventDate().isAfter(c.artStart.getEventDate()))) {
            String why = c.linkage == null ? missingLinkageReason(s)
                    : "the enrolment date (" + fmt(c.linkage.getEventDate()) + ") is after the ART start";
            f.add(CbsFinding.of(CbsRule.DQ02, p, CbsEventType.LINKAGE_TO_CARE, String.valueOf(c.artStart.getEventDate()),
                    "ART started on " + fmt(c.artStart.getEventDate()) + " but no linkage (enrolment) record was found: " + why
                            + ". This is a documentation gap, not proof that linkage did not occur.")
                    .on(c.artStart.getEventDate()).evidence("artInitiationDate", c.artStart.getEventDate()).evidence("artSource", ref(c.artStart))
                    .evidence("enrolmentDate", c.linkage == null ? null : c.linkage.getEventDate()));
        }

        // CBS-DQ-03: compares service dates, so pre-death services entered after the death are not flagged
        if (c.deathDate != null) {
            Service after = firstServiceAfter(s, c.deathDate, asOf);
            if (after != null) {
                f.add(CbsFinding.of(CbsRule.DQ03, p, CbsEventType.DEATH, String.valueOf(c.deathDate),
                        "A " + after.label + " on " + fmt(after.date) + " was recorded after the patient's verified date of death ("
                                + fmt(c.deathDate) + "). Check for retrospective entry or an incorrect death record.")
                        .on(after.date).evidence("deathDate", c.deathDate).evidence("deathSource", ref(c.deaths.get(0)))
                        .evidence("serviceDate", after.date).evidence("service", after.label).evidence("serviceRecordId", after.uuid));
            }
        }

        // CBS-DQ-04: only when both statuses are actually known
        String documented = c.a.getDocumentedTreatmentStatus();
        String derived = c.a.getTreatmentStatus().getStatus();
        if (documented != null && !UNABLE.equals(derived) && !documented.equals(derived)) {
            f.add(CbsFinding.of(CbsRule.DQ04, p, null, documented + ">" + derived,
                    "The documented ART status (\"" + documented + "\") differs from the status calculated from treatment records (\""
                            + derived + "\").")
                    .on(statusDate(c)).evidence("documentedStatus", documented).evidence("derivedStatus", derived)
                    .evidence("derivedEvidence", c.a.getTreatmentStatus().getEvidence()));
        }

        // CBS-DQ-05: follow-up is due follow_up_days (1) after the interruption date. Only the
        // latest interruption with no return can still be open.
        if (!c.interruptions.isEmpty()) {
            CbsEvent lastIit = c.interruptions.get(c.interruptions.size() - 1);
            boolean returned = c.returns.stream().anyMatch(r -> r.getEventDate().isAfter(lastIit.getEventDate()));
            if (!returned && ChronoUnit.DAYS.between(lastIit.getEventDate(), asOf) > cfg.getFollowUpDays()
                    && !hasTrackingSince(s, lastIit.getEventDate(), asOf)) {
                f.add(CbsFinding.of(CbsRule.DQ05, p, CbsEventType.TREATMENT_INTERRUPTION, String.valueOf(lastIit.getEventDate()),
                        "This patient has a qualifying treatment interruption from " + fmt(lastIit.getEventDate())
                                + " with no recorded follow-up within the approved period (" + cfg.getFollowUpDays() + " day(s)).")
                        .on(lastIit.getEventDate()).evidence("interruptionDate", lastIit.getEventDate())
                        .evidence("lastRefillDate", lastIit.getDetails().get("lastRefillDate"))
                        .evidence("expectedBy", lastIit.getEventDate().plusDays(cfg.getFollowUpDays())));
            }
        }

        // CBS-DQ-06: date and cause of death are both required
        for (CbsEvent d : c.deaths) {
            if (!d.getDetails().containsKey("causeOfDeath")) {
                f.add(CbsFinding.of(CbsRule.DQ06, p, CbsEventType.DEATH, String.valueOf(d.getEventDate()),
                        "Required mortality details are incomplete: death on " + fmt(d.getEventDate()) + " has no cause of death.")
                        .on(d.getEventDate()).evidence("deathDate", d.getEventDate()).evidence("missing", "cause of death").evidence("source", ref(d)));
            }
        }
        for (PatientSource.TrackerStatus t : s.getTrackerStatuses()) {
            if (CbsTrackerStatus.DIED.equals(t.getStatus()) && t.getStatusDate() == null) {
                f.add(CbsFinding.of(CbsRule.DQ06, p, CbsEventType.DEATH, "nodate|" + t.getUuid(),
                        "Required mortality details are incomplete: a death status (\"" + t.getRawStatus() + "\") has no date of death"
                                + (t.getCauseOfDeath() == null ? " and no cause of death." : "."))
                        .on(null).evidence("missing", t.getCauseOfDeath() == null ? "date and cause of death" : "date of death")
                        .evidence("recordId", t.getUuid()));
            }
        }

        // CBS-DQ-07
        duplicates(c, f);

        // CBS-DQ-08
        if (s.getEnrollment() != null && c.diagnosis == null) {
            f.add(CbsFinding.of(CbsRule.DQ08, p, CbsEventType.HIV_DIAGNOSIS, "",
                    "The patient is enrolled in care but no qualifying HIV diagnosis evidence was found "
                            + "(no HTS positive result and no date of confirmed HIV diagnosis on the enrolment).")
                    .on(enrolmentDate(s)).evidence("enrollmentId", s.getEnrollment().getUuid()));
        }

        // CBS-DQ-09
        crossModule(c, f);

        // CBS-DQ-10 and CBS-DQ-11
        dateChecks(c, f);

        // CBS-DQ-12
        if (c.linkage != null && c.diagnosis != null && c.linkage.getEventDate().isBefore(c.diagnosis.getEventDate())) {
            f.add(CbsFinding.of(CbsRule.DQ12, p, CbsEventType.LINKAGE_TO_CARE, String.valueOf(c.linkage.getEventDate()),
                    "Enrolment date (" + fmt(c.linkage.getEventDate()) + ") is earlier than the HIV diagnosis date ("
                            + fmt(c.diagnosis.getEventDate()) + ").")
                    .on(c.linkage.getEventDate()).evidence("enrolmentDate", c.linkage.getEventDate()).evidence("diagnosisDate", c.diagnosis.getEventDate())
                    .evidence("diagnosisSource", ref(c.diagnosis)));
        }

        // CBS-DQ-13: locally diagnosed (HTS positive here), alive, not transferred out
        PatientSource.DatedRecord hts = earliest(s.getHtsPositives(), asOf);
        if (hts != null && c.linkage == null && !c.transferIn() && alive && c.transfersOut.isEmpty()
                && ChronoUnit.DAYS.between(hts.getDate(), asOf) > cfg.getLinkageDays()) {
            f.add(CbsFinding.of(CbsRule.DQ13, p, CbsEventType.LINKAGE_TO_CARE, String.valueOf(hts.getDate()),
                    "This patient was diagnosed HIV-positive on " + fmt(hts.getDate()) + " but no enrolment record was found within "
                            + cfg.getLinkageDays() + " days" + (s.getEnrollment() != null ? " (" + missingLinkageReason(s) + ")" : "") + ".")
                    .on(hts.getDate()).evidence("htsTestDate", hts.getDate()).evidence("htsRecordId", hts.getUuid())
                    .evidence("expectedBy", hts.getDate().plusDays(cfg.getLinkageDays())));
        }

        // CBS-DQ-14: flagged straight away (BA, 17 Nov 2026). This also catches the clients
        // whose enrolment has no "date enrolled in HIV care".
        if (s.getEnrollment() != null && c.artStart == null && alive && c.transfersOut.isEmpty()) {
            LocalDate enrolled = s.getEnrollment().getDateOfRegistration();
            f.add(CbsFinding.of(CbsRule.DQ14, p, CbsEventType.ART_INITIATION, "",
                    "This patient is enrolled" + (enrolled == null ? " (no enrolment date recorded)" : " since " + fmt(enrolled))
                            + " but no ART commencement was found.")
                    .on(enrolmentDate(s)).evidence("enrollmentId", s.getEnrollment().getUuid())
                    .evidence("enrollmentDate", enrolled)
                    .evidence("enrollmentDateMissing", enrolled == null ? Boolean.TRUE : null));
        }

        // CBS-DQ-15
        List<PatientSource.Commencement> commencements = s.getCommencements().stream()
                .filter(x -> x.getDate() != null && !x.getDate().isAfter(asOf)).collect(Collectors.toList());
        if (commencements.size() > 1) {
            f.add(CbsFinding.of(CbsRule.DQ15, p, CbsEventType.ART_INITIATION, "commencement",
                    "More than one ART commencement record exists (" + commencements.size() + "). Confirm which is the first "
                            + "initiation and whether others are restarts or duplicates.")
                    .on(commencements.get(0).getDate()).evidence("records", commencements.stream().map(x -> fmt(x.getDate()) + " (" + x.getUuid() + ")")
                            .collect(Collectors.toList())));
        }

        // CBS-DQ-16
        if (commencement != null && (commencement.getRegimenId() == null || commencement.getRegimenLineId() == null)) {
            String missing = commencement.getRegimenId() == null && commencement.getRegimenLineId() == null ? "regimen and regimen line"
                    : commencement.getRegimenId() == null ? "regimen" : "regimen line";
            f.add(CbsFinding.of(CbsRule.DQ16, p, CbsEventType.ART_INITIATION, commencement.getUuid(),
                    "ART initiation record of " + fmt(commencement.getDate()) + " has no " + missing + ".")
                    .on(commencement.getDate()).evidence("commencementId", commencement.getUuid()).evidence("missing", missing));
        }

        // CBS-DQ-17: unless the client transferred out before the first pickup
        if (commencement != null && c.artStart != null
                && c.refills.stream().noneMatch(r -> !r.getVisitDate().isBefore(c.artStart.getEventDate()))
                && c.transfersOut.isEmpty()) {
            f.add(CbsFinding.of(CbsRule.DQ17, p, CbsEventType.ART_INITIATION, String.valueOf(c.artStart.getEventDate()),
                    "ART was commenced on " + fmt(c.artStart.getEventDate()) + " but no ARV dispensing was found.")
                    .on(c.artStart.getEventDate()).evidence("artInitiationDate", c.artStart.getEventDate()).evidence("artSource", ref(c.artStart)));
        }

        // CBS-DQ-18: any service at this facility after transfer out, unless a transfer back in is documented
        for (CbsEvent out : c.transfersOut) {
            Service after = firstServiceAfter(s, out.getEventDate(), asOf);
            if (after != null && !transferredBackIn(s, out.getEventDate(), after.date)) {
                f.add(CbsFinding.of(CbsRule.DQ18, p, CbsEventType.TRANSFER_OUT, String.valueOf(out.getEventDate()),
                        "A " + after.label + " on " + fmt(after.date) + " was recorded at this facility after the patient transferred out on "
                                + fmt(out.getEventDate()) + ". Check if the patient returned (transfer back in) or the transfer out is wrong.")
                        .on(after.date).evidence("transferOutDate", out.getEventDate()).evidence("transferOutSource", ref(out))
                        .evidence("serviceDate", after.date).evidence("service", after.label).evidence("serviceRecordId", after.uuid));
            }
        }

        // CBS-DQ-19: active, on ART for vl_eligibility_months, no VL sample in the last vl_eligibility_months
        if (ACTIVE.equals(derived) && c.artStart != null
                && !c.artStart.getEventDate().isAfter(asOf.minusMonths(cfg.getVlEligibilityMonths()))) {
            LocalDate windowStart = asOf.minusMonths(cfg.getVlEligibilityMonths());
            boolean recent = s.getViralLoads().stream().map(PatientSource.LabTest::getEventDate).filter(Objects::nonNull)
                    .anyMatch(d -> d.isAfter(windowStart) && !d.isAfter(asOf));
            if (!recent) {
                LocalDate lastVl = s.getViralLoads().stream().map(PatientSource.LabTest::getEventDate).filter(Objects::nonNull)
                        .filter(d -> !d.isAfter(asOf)).max(Comparator.naturalOrder()).orElse(null);
                // the day the test became due: N months after the last sample, or after ART start
                LocalDate vlDueDate = (lastVl == null ? c.artStart.getEventDate() : lastVl).plusMonths(cfg.getVlEligibilityMonths());
                f.add(CbsFinding.of(CbsRule.DQ19, p, CbsEventType.VIRAL_LOAD, String.valueOf(windowStart),
                        "This patient is eligible for a viral load test and none was found in the last " + cfg.getVlEligibilityMonths()
                                + " months" + (lastVl == null ? " (no viral load ever recorded)." : " (last sample " + fmt(lastVl) + ")."))
                        .on(vlDueDate).evidence("artInitiationDate", c.artStart.getEventDate()).evidence("lastViralLoadSample", lastVl)
                        .evidence("windowStart", windowStart));
            }
        }

        // CBS-DQ-20
        if ("EAC Missing".equals(c.a.getEacStatus().getStatus()) && alive && !transferredOut) {
            LocalDate vlDate = c.a.getViralLoadStatus().getEffectiveDate();
            f.add(CbsFinding.of(CbsRule.DQ20, p, CbsEventType.EAC_SESSION, String.valueOf(vlDate),
                    "VL result on " + fmt(vlDate) + " is not suppressed (" + c.a.getViralLoadStatus().getEvidence()
                            + ") and no EAC session is recorded.")
                    .on(vlDate).evidence("viralLoadDate", vlDate).evidence("viralLoad", c.a.getViralLoadStatus().getEvidence())
                    .evidence("expectedBy", vlDate.plusDays(cfg.getEacDays())));
        }

        // CBS-DQ-21
        if (c.completedEac != null && c.postEacVl == null && alive && !transferredOut
                && ChronoUnit.DAYS.between(c.completedEac.getDate(), asOf) > cfg.getPostEacVlDays()) {
            f.add(CbsFinding.of(CbsRule.DQ21, p, CbsEventType.POST_EAC_VIRAL_LOAD, String.valueOf(c.completedEac.getDate()),
                    "EAC was completed on " + fmt(c.completedEac.getDate()) + " but no repeat viral load (indication \"Confirmation\") "
                            + "was documented within " + cfg.getPostEacVlDays() + " days.")
                    .on(c.completedEac.getDate()).evidence("eacCompletedOn", c.completedEac.getDate()).evidence("eacRecordId", c.completedEac.getUuid())
                    .evidence("expectedBy", c.completedEac.getDate().plusDays(cfg.getPostEacVlDays())));
        }

        // CBS-DQ-22
        resultBeforeSample(p, f, s.getViralLoads(), "viral load", CbsEventType.VIRAL_LOAD);
        resultBeforeSample(p, f, s.getCd4Tests(), "CD4", CbsEventType.BASELINE_CD4);

        // CBS-DQ-23: merging is never automatic, this only points the records out
        for (PatientSource.DuplicateId d : s.getDuplicateIds()) {
            f.add(CbsFinding.of(CbsRule.DQ23, p, null, d.getIdType() + "|" + d.getIdValue(),
                    "Potential Patient Identity Conflict: " + d.getIdType() + " \"" + d.getIdValue() + "\" is also used by "
                            + d.getOtherPatients() + " other patient record(s) in this facility. The system cannot confirm whether "
                            + "these records belong to the same patient. Please review the affected records before proceeding.")
                    .on(null).evidence("identifierType", d.getIdType()).evidence("identifier", d.getIdValue())
                    .evidence("otherPatients", d.getOtherPatients()));
        }

        // CBS-DQ-25
        if (c.transferIn() && s.getEnrollment().getDateStarted() == null && commencement == null) {
            f.add(CbsFinding.of(CbsRule.DQ25, p, CbsEventType.TRANSFER_IN, "",
                    "This client transferred in but the original ART start date is not documented.")
                    .on(enrolmentDate(s)).evidence("enrollmentId", s.getEnrollment().getUuid())
                    .evidence("statusAtRegistration", s.getEnrollment().getStatusAtRegistration()));
        }

        // CBS-DQ-26: every ART initiation needs a baseline CD4, flagged straight away (BA, 17 Nov 2026).
        // Transfer-ins are excluded.
        if (c.artStart != null && c.baselineCd4 == null && !c.transferIn()) {
            f.add(CbsFinding.of(CbsRule.DQ26, p, CbsEventType.BASELINE_CD4, String.valueOf(c.artStart.getEventDate()),
                    "No baseline CD4 result was found for the ART initiation on " + fmt(c.artStart.getEventDate())
                            + " (no CD4 sample by ART start and no CD4 on the commencement form).")
                    .on(c.artStart.getEventDate()).evidence("artInitiationDate", c.artStart.getEventDate()).evidence("artSource", ref(c.artStart)));
        }

        // CBS-DQ-27: a switch should follow an unsuppressed viral load
        for (CbsEvent sw : c.switches) {
            boolean failure = s.getViralLoads().stream().anyMatch(v -> v.getCopies() != null
                    && v.getCopies() >= cfg.getVlUnsuppressedCopies() && v.getEventDate() != null
                    && !v.getEventDate().isAfter(sw.getEventDate()));
            if (!failure) {
                f.add(CbsFinding.of(CbsRule.DQ27, p, CbsEventType.REGIMEN_SWITCH, String.valueOf(sw.getEventDate()),
                        "Regimen line changed from " + sw.getDetails().get("fromLine") + " to " + sw.getDetails().get("toLine")
                                + " on " + fmt(sw.getEventDate()) + " without a documented unsuppressed viral load.")
                        .on(sw.getEventDate()).evidence("switchDate", sw.getEventDate()).evidence("fromLine", sw.getDetails().get("fromLine"))
                        .evidence("toLine", sw.getDetails().get("toLine")).evidence("refillRecordId", sw.getSourceRecordId()));
            }
        }

        // CBS-DQ-28 raises no finding: records are re-read on every refresh and archived ones are
        // skipped, so findings that came from them clear (are superseded) on their own.
    }

    /** CBS-DQ-07: same person, same type, same date, same module. Nothing is ever removed automatically. */
    private void duplicates(Context c, List<CbsFinding> f) {
        PatientSource s = c.s;
        LocalDate asOf = c.asOf;
        sameDay(c, f, "pharmacy refill", s.getRefills(), PatientSource.Refill::getVisitDate, PatientSource.Refill::getUuid);
        sameDay(c, f, "clinic visit", s.getClinicVisits(), PatientSource.DatedRecord::getDate, PatientSource.DatedRecord::getUuid);
        sameDay(c, f, "EAC session", s.getEacSessions(), PatientSource.DatedRecord::getDate, PatientSource.DatedRecord::getUuid);
        sameDay(c, f, "HTS positive result", s.getHtsPositives(), PatientSource.DatedRecord::getDate, PatientSource.DatedRecord::getUuid);
        sameDay(c, f, "transfer-in form", s.getTransferInForms(), PatientSource.DatedRecord::getDate, PatientSource.DatedRecord::getUuid);
        // Only statuses that are CBS events. "ART Start" is left out: ART initiation comes from the
        // commencement form, and migration wrote that status twice for most clients.
        Set<String> eventStatuses = new HashSet<>(java.util.Arrays.asList(CbsTrackerStatus.TRANSFER_OUT, CbsTrackerStatus.TRANSFER_IN,
                CbsTrackerStatus.DIED, CbsTrackerStatus.STOPPED_TREATMENT, CbsTrackerStatus.RETURN_TO_CARE));
        Map<String, List<PatientSource.TrackerStatus>> statuses = s.getTrackerStatuses().stream()
                .filter(t -> t.getStatusDate() != null && !t.getStatusDate().isAfter(asOf) && eventStatuses.contains(t.getStatus()))
                .collect(Collectors.groupingBy(t -> t.getStatus() + "|" + t.getStatusDate(), TreeMap::new, Collectors.toList()));
        statuses.forEach((key, list) -> {
            if (list.size() > 1) {
                addDuplicate(c, f, "client status \"" + list.get(0).getRawStatus() + "\"", list.get(0).getStatusDate(),
                        list.stream().map(PatientSource.TrackerStatus::getUuid).collect(Collectors.toList()));
            }
        });
        // Two samples on the same day are fine when they carry different sample numbers
        labDuplicates(c, f, "viral load sample", s.getViralLoads());
        labDuplicates(c, f, "CD4 sample", s.getCd4Tests());
    }

    private <T> void sameDay(Context c, List<CbsFinding> f, String what, List<T> records,
                             Function<T, LocalDate> date, Function<T, String> id) {
        Map<LocalDate, List<T>> byDay = records.stream().filter(r -> date.apply(r) != null && !date.apply(r).isAfter(c.asOf))
                .collect(Collectors.groupingBy(date, TreeMap::new, Collectors.toList()));
        byDay.forEach((day, list) -> {
            if (list.size() > 1) {
                addDuplicate(c, f, what, day, list.stream().map(id).collect(Collectors.toList()));
            }
        });
    }

    private void labDuplicates(Context c, List<CbsFinding> f, String what, List<PatientSource.LabTest> tests) {
        Map<LocalDate, List<PatientSource.LabTest>> byDay = tests.stream()
                .filter(t -> t.getEventDate() != null && !t.getEventDate().isAfter(c.asOf))
                .collect(Collectors.groupingBy(PatientSource.LabTest::getEventDate, TreeMap::new, Collectors.toList()));
        byDay.forEach((day, list) -> {
            Set<String> numbers = list.stream().map(PatientSource.LabTest::getSampleNumber).filter(Objects::nonNull).collect(Collectors.toSet());
            if (list.size() > 1 && numbers.size() < list.size()) {
                addDuplicate(c, f, what, day, list.stream().map(PatientSource.LabTest::getUuid).collect(Collectors.toList()));
            }
        });
    }

    private void addDuplicate(Context c, List<CbsFinding> f, String what, LocalDate day, List<String> ids) {
        f.add(CbsFinding.of(CbsRule.DQ07, c.s.getPersonUuid(), null, what + "|" + day,
                "Two or more records appear to describe the same event: " + ids.size() + " " + what + " records on " + fmt(day)
                        + ". Verify before any change; nothing is deleted automatically.")
                .on(day).evidence("eventDate", day).evidence("recordType", what).evidence("records", ids));
    }

    /**
     * CBS-DQ-09. The workbook's two examples (HTS negative but on ART; death recorded while
     * tracking still shows the client active) plus two disagreements found in real data:
     * diagnosis dates that differ between enrolment and HTS, and death recorded on different dates.
     */
    private void crossModule(Context c, List<CbsFinding> f) {
        PatientSource s = c.s;
        String p = s.getPersonUuid();
        PatientSource.DatedRecord negative = latest(s.getHtsNegatives(), c.asOf);
        if (negative != null && s.getHtsPositives().isEmpty() && c.artStart != null) {
            f.add(CbsFinding.of(CbsRule.DQ09, p, CbsEventType.HIV_DIAGNOSIS, "hts-negative-on-art",
                    "Records in different modules disagree: the HTS final result on " + fmt(negative.getDate())
                            + " is Negative but the patient is on ART (started " + fmt(c.artStart.getEventDate()) + ").")
                    .on(negative.getDate()).evidence("htsNegativeDate", negative.getDate()).evidence("htsRecordId", negative.getUuid())
                    .evidence("artInitiationDate", c.artStart.getEventDate()).evidence("artSource", ref(c.artStart)));
        }
        if (c.deathDate != null) {
            s.getTrackerStatuses().stream()
                    .filter(t -> t.getStatusDate() != null && t.getStatusDate().isAfter(c.deathDate) && !t.getStatusDate().isAfter(c.asOf))
                    .filter(t -> CbsTrackerStatus.RETURN_TO_CARE.equals(t.getStatus()) || CbsTrackerStatus.ART_START.equals(t.getStatus())
                            || CbsTrackerStatus.TRANSFER_IN.equals(t.getStatus()))
                    .min(Comparator.comparing(PatientSource.TrackerStatus::getStatusDate))
                    .ifPresent(t -> f.add(CbsFinding.of(CbsRule.DQ09, p, CbsEventType.DEATH, "died-then-active",
                            "Records in different modules disagree: death is recorded on " + fmt(c.deathDate)
                                    + " but client tracking shows \"" + t.getRawStatus() + "\" on " + fmt(t.getStatusDate()) + ".")
                            .on(t.getStatusDate()).evidence("deathDate", c.deathDate).evidence("deathSource", ref(c.deaths.get(0)))
                            .evidence("laterStatus", t.getRawStatus()).evidence("laterStatusDate", t.getStatusDate())));
        }
        if (c.deaths.size() > 1) {
            f.add(CbsFinding.of(CbsRule.DQ09, p, CbsEventType.DEATH, "death-dates",
                    "Records in different modules disagree: death is recorded with " + c.deaths.size() + " different dates ("
                            + c.deaths.stream().map(d -> fmt(d.getEventDate()) + " in " + d.getSourceTable()).collect(Collectors.joining(", ")) + ").")
                    .on(c.deathDate).evidence("deathDates", c.deaths.stream().map(CbsEvent::getEventDate).collect(Collectors.toList())));
        }
        if (c.diagnosis != null) {
            LocalDate htsDate = (LocalDate) c.diagnosis.getDetails().get("htsTestDate");
            LocalDate enrolDate = (LocalDate) c.diagnosis.getDetails().get("enrolmentDiagnosisDate");
            if (htsDate != null && enrolDate != null
                    && Math.abs(ChronoUnit.DAYS.between(htsDate, enrolDate)) > c.cfg.getCrossModuleToleranceDays()) {
                f.add(CbsFinding.of(CbsRule.DQ09, p, CbsEventType.HIV_DIAGNOSIS, "diagnosis",
                        "Records in different modules disagree: the diagnosis date on the enrolment (" + fmt(enrolDate)
                                + ") differs from the HTS positive test date (" + fmt(htsDate) + ").")
                        .on(c.diagnosis.getEventDate()).evidence("enrollmentDiagnosisDate", enrolDate).evidence("htsTestDate", htsDate));
            }
        }
    }

    /**
     * CBS-DQ-10: a date after the as-of date, or more than entry_date_tolerance_days after the day
     * the record was entered (a future date at the time of entry). The as-of date of a refresh is
     * today; the single-patient view also compares with today so that picking an earlier as-of
     * date doesn't turn every later record into a "future" one.
     * CBS-DQ-11: dates before the date of birth, unless the date of birth is estimated.
     */
    private void dateChecks(Context c, List<CbsFinding> f) {
        PatientSource s = c.s;
        List<Dated> all = new ArrayList<>();
        PatientSource.Enrollment en = s.getEnrollment();
        if (en != null) {
            all.add(new Dated("enrolment diagnosis date", en.getDateConfirmedHiv(), en.getCreatedDate(), en.getUuid(), CbsEventType.HIV_DIAGNOSIS));
            all.add(new Dated("enrolment date", en.getDateOfRegistration(), en.getCreatedDate(), en.getUuid(), CbsEventType.LINKAGE_TO_CARE));
        }
        s.getCommencements().forEach(x -> all.add(new Dated("ART commencement date", x.getDate(), x.getCreatedDate(), x.getUuid(), CbsEventType.ART_INITIATION)));
        s.getHtsPositives().forEach(x -> all.add(new Dated("HTS test date", x.getDate(), x.getCreatedDate(), x.getUuid(), CbsEventType.HIV_DIAGNOSIS)));
        s.getRefills().forEach(x -> all.add(new Dated("pharmacy refill date", x.getVisitDate(), x.getCreatedDate(), x.getUuid(), null)));
        s.getClinicVisits().forEach(x -> all.add(new Dated("clinic visit date", x.getDate(), x.getCreatedDate(), x.getUuid(), null)));
        s.getEacSessions().forEach(x -> all.add(new Dated("EAC session date", x.getDate(), x.getCreatedDate(), x.getUuid(), CbsEventType.EAC_SESSION)));
        s.getTransferInForms().forEach(x -> all.add(new Dated("transfer-in date", x.getDate(), x.getCreatedDate(), x.getUuid(), CbsEventType.TRANSFER_IN)));
        s.getTrackerStatuses().forEach(x -> all.add(new Dated("client status date", x.getStatusDate(), null, x.getUuid(), null)));
        for (PatientSource.LabTest t : s.getViralLoads()) {
            all.add(new Dated("viral load sample date", t.getSampleDate(), null, t.getUuid(), CbsEventType.VIRAL_LOAD));
            all.add(new Dated("viral load result date", t.getResultDate(), null, t.getUuid(), CbsEventType.VIRAL_LOAD));
        }
        for (PatientSource.LabTest t : s.getCd4Tests()) {
            all.add(new Dated("CD4 sample date", t.getSampleDate(), null, t.getUuid(), CbsEventType.BASELINE_CD4));
        }

        String p = s.getPersonUuid();
        for (Dated d : all) {
            if (d.date == null) {
                continue;
            }
            if (d.date.isAfter(c.today)) {
                f.add(CbsFinding.of(CbsRule.DQ10, p, d.type, d.what + "|" + d.date + "|" + d.uuid,
                        "The " + d.what + " (" + fmt(d.date) + ") is in the future.")
                        .on(d.date).evidence("recordedDate", d.date).evidence("field", d.what).evidence("recordId", d.uuid));
            } else if (d.entered != null && d.date.isAfter(d.entered.toLocalDate().plusDays(c.cfg.getEntryDateToleranceDays()))) {
                f.add(CbsFinding.of(CbsRule.DQ10, p, d.type, d.what + "|" + d.date + "|" + d.uuid,
                        "The " + d.what + " (" + fmt(d.date) + ") is later than the date the record was entered ("
                                + fmt(d.entered.toLocalDate()) + "), so it was a future date when it was entered.")
                        .on(d.date).evidence("recordedDate", d.date).evidence("enteredOn", d.entered.toLocalDate())
                        .evidence("field", d.what).evidence("recordId", d.uuid));
            }
        }

        LocalDate dob = s.getDateOfBirth();
        if (dob != null && !s.isDateOfBirthEstimated()) {
            List<Dated> before = all.stream().filter(d -> d.date != null && d.date.isBefore(dob))
                    .sorted(Comparator.comparing(d -> d.date)).collect(Collectors.toList());
            if (!before.isEmpty()) {
                f.add(CbsFinding.of(CbsRule.DQ11, p, before.get(0).type, "dob",
                        "An event date is earlier than the patient's date of birth (" + fmt(dob) + "): " + before.size()
                                + " date(s), the earliest being the " + before.get(0).what + " of " + fmt(before.get(0).date) + ".")
                        .on(before.get(0).date).evidence("dateOfBirth", dob)
                        .evidence("dates", before.stream().limit(10).map(d -> d.what + " " + d.date).collect(Collectors.toList())));
            }
        }
    }

    private static class Dated {
        final String what;
        final LocalDate date;
        final LocalDateTime entered;
        final String uuid;
        final CbsEventType type;

        Dated(String what, LocalDate date, LocalDateTime entered, String uuid, CbsEventType type) {
            this.what = what;
            this.date = date;
            this.entered = entered;
            this.uuid = uuid;
            this.type = type;
        }
    }

    private void resultBeforeSample(String p, List<CbsFinding> f, List<PatientSource.LabTest> tests, String what, CbsEventType type) {
        for (PatientSource.LabTest t : tests) {
            if (t.getSampleDate() != null && t.getResultDate() != null && t.getResultDate().isBefore(t.getSampleDate())) {
                f.add(CbsFinding.of(CbsRule.DQ22, p, type, t.getUuid(),
                        "The " + what + " result date (" + fmt(t.getResultDate()) + ") is earlier than the sample collection date ("
                                + fmt(t.getSampleDate()) + ").")
                        .on(t.getResultDate()).evidence("sampleDate", t.getSampleDate()).evidence("resultDate", t.getResultDate()).evidence("recordId", t.getUuid()));
            }
        }
    }

    // Event dates for findings that aren't about one dated record. CBS-DQ-04: the date the
    // calculated status took effect. Enrolment rules: the enrolment date, or the day the
    // enrolment was entered when it has no date (the 904 clients flagged under CBS-DQ-14).
    private static LocalDate statusDate(Context c) {
        if (c.a.getTreatmentStatus().getEffectiveDate() != null) {
            return c.a.getTreatmentStatus().getEffectiveDate();
        }
        // "Not yet initiated" has no date, so use the documented status the comparison was made with
        return c.s.getTrackerStatuses().stream()
                .filter(t -> t.getStatusDate() != null && !t.getStatusDate().isAfter(c.asOf))
                .filter(t -> CbsTrackerStatus.impliedTreatmentStatus(t.getStatus()) != null)
                .map(PatientSource.TrackerStatus::getStatusDate)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    private static LocalDate enrolmentDate(PatientSource s) {
        PatientSource.Enrollment en = s.getEnrollment();
        if (en == null) {
            return null;
        }
        if (en.getDateOfRegistration() != null) {
            return en.getDateOfRegistration();
        }
        return en.getCreatedDate() == null ? null : en.getCreatedDate().toLocalDate();
    }

    private static String missingLinkageReason(PatientSource s) {
        PatientSource.Enrollment en = s.getEnrollment();
        if (en == null) {
            return "there is no HIV enrolment";
        }
        if (en.getDateOfRegistration() == null) {
            return "the enrolment has no date enrolled in HIV care";
        }
        if (isBlank(en.getUniqueId())) {
            return "the enrolment has no ART unique ID";
        }
        return "the enrolment is dated after the as-of date";
    }

    // ---------------------------------------------------------------- event summary

    private void eventSummary(Context c) {
        List<EventSummary> out = c.a.getEventSummary();
        Map<CbsRule, CbsFinding> byRule = new HashMap<>();
        c.a.getFindings().forEach(f -> byRule.putIfAbsent(f.getRule(), f));

        if (c.diagnosis != null) {
            out.add(summary(CbsEventType.HIV_DIAGNOSIS, DOCUMENTED, "Documented", "Diagnosed " + fmt(c.diagnosis.getEventDate()), 1, null));
        } else if (byRule.containsKey(CbsRule.DQ08)) {
            out.add(summary(CbsEventType.HIV_DIAGNOSIS, MISSING_EXPECTED, "Missing Expected Event", "No diagnosis date or HTS positive result", 0, CbsRule.DQ08));
        } else {
            out.add(summary(CbsEventType.HIV_DIAGNOSIS, UNABLE_TO_DETERMINE, "Unable to Determine", "No diagnosis evidence in the assessed records", 0, null));
        }

        if (c.linkage != null) {
            out.add(summary(CbsEventType.LINKAGE_TO_CARE, DOCUMENTED, "Documented", "Enrolled " + fmt(c.linkage.getEventDate()), 1,
                    byRule.containsKey(CbsRule.DQ12) ? CbsRule.DQ12 : null));
        } else if (c.transferIn()) {
            out.add(summary(CbsEventType.LINKAGE_TO_CARE, NOT_APPLICABLE, "Not Applicable", "Transferred in; linkage happened at another facility", 0, null));
        } else if (byRule.containsKey(CbsRule.DQ13) || byRule.containsKey(CbsRule.DQ02)) {
            CbsRule rule = byRule.containsKey(CbsRule.DQ13) ? CbsRule.DQ13 : CbsRule.DQ02;
            out.add(summary(CbsEventType.LINKAGE_TO_CARE, MISSING_EXPECTED, "Missing Expected Event", "No qualifying enrolment: " + missingLinkageReason(c.s), 0, rule));
        } else if (!c.s.getHtsPositives().isEmpty()) {
            out.add(summary(CbsEventType.LINKAGE_TO_CARE, NOT_YET_DUE, "Not Yet Due", "Linkage expected within " + c.cfg.getLinkageDays() + " days of diagnosis", 0, null));
        } else {
            out.add(summary(CbsEventType.LINKAGE_TO_CARE, UNABLE_TO_DETERMINE, "Unable to Determine", "No qualifying enrolment record", 0, null));
        }

        if (c.artStart != null) {
            out.add(summary(CbsEventType.ART_INITIATION, DOCUMENTED, "Documented", "ART started " + fmt(c.artStart.getEventDate()), 1,
                    byRule.containsKey(CbsRule.DQ15) ? CbsRule.DQ15 : null));
        } else if (byRule.containsKey(CbsRule.DQ14)) {
            out.add(summary(CbsEventType.ART_INITIATION, MISSING_EXPECTED, "Missing Expected Event", "No ART commencement record", 0, CbsRule.DQ14));
        } else if (byRule.containsKey(CbsRule.DQ25)) {
            out.add(summary(CbsEventType.ART_INITIATION, MISSING_EXPECTED, "Missing Expected Event", "Transferred in without the original ART start date", 0, CbsRule.DQ25));
        } else if (c.s.getEnrollment() != null) {
            out.add(summary(CbsEventType.ART_INITIATION, UNABLE_TO_DETERMINE, "Unable to Determine", "Died or transferred out before ART commencement", 0, null));
        } else {
            out.add(summary(CbsEventType.ART_INITIATION, NOT_APPLICABLE, "Not Applicable", "Not enrolled in HIV care", 0, null));
        }

        int transfers = c.transfersIn.size() + c.transfersOut.size();
        out.add(transfers > 0
                ? summary(CbsEventType.TRANSFER_OUT, DOCUMENTED, "Documented", c.transfersIn.size() + " in, " + c.transfersOut.size() + " out", transfers,
                byRule.containsKey(CbsRule.DQ18) ? CbsRule.DQ18 : null)
                : summary(CbsEventType.TRANSFER_OUT, NO_EVENT, "No Transfer Documented", "Records assessed; no transfer found", 0, null));

        if (!c.interruptions.isEmpty()) {
            out.add(summary(CbsEventType.TREATMENT_INTERRUPTION, DOCUMENTED, "Documented", c.interruptions.size() + " interruption(s)", c.interruptions.size(), null));
        } else if (!c.refills.isEmpty()) {
            out.add(summary(CbsEventType.TREATMENT_INTERRUPTION, NO_EVENT, "No Interruption Identified", "Refills assessed; no interruption found", 0, null));
        } else if (c.artStart != null) {
            out.add(summary(CbsEventType.TREATMENT_INTERRUPTION, UNABLE_TO_DETERMINE, "Unable to Determine", "No pharmacy refills to assess", 0, null));
        } else {
            out.add(summary(CbsEventType.TREATMENT_INTERRUPTION, NOT_APPLICABLE, "Not Applicable", "Not on ART", 0, null));
        }

        // Return to care is only expected after an interruption
        if (!c.returns.isEmpty()) {
            out.add(summary(CbsEventType.RETURN_TO_CARE, DOCUMENTED, "Documented", c.returns.size() + " return(s)", c.returns.size(), null));
        } else if (byRule.containsKey(CbsRule.DQ05)) {
            out.add(summary(CbsEventType.RETURN_TO_CARE, MISSING_EXPECTED, "Missing Expected Event", "No follow-up after interruption", 0, CbsRule.DQ05));
        } else if (!c.interruptions.isEmpty()) {
            out.add(summary(CbsEventType.RETURN_TO_CARE, NO_EVENT, "No Event Documented", "Interrupted; no return yet", 0, null));
        } else {
            out.add(summary(CbsEventType.RETURN_TO_CARE, NOT_APPLICABLE, "Not Applicable", "No interruption to return from", 0, null));
        }

        out.add(!c.deaths.isEmpty()
                ? summary(CbsEventType.DEATH, DOCUMENTED, "Documented", "Died " + fmt(c.deaths.get(0).getEventDate()), c.deaths.size(),
                byRule.containsKey(CbsRule.DQ06) ? CbsRule.DQ06 : null)
                : summary(CbsEventType.DEATH, NO_EVENT, "No Mortality Record", "Records assessed; no death recorded", 0, null));
    }

    private EventSummary summary(CbsEventType type, String outcome, String outcomeLabel, String explanation, int count, CbsRule rule) {
        String label = type == CbsEventType.TRANSFER_OUT ? "Transfer History" : type == CbsEventType.DEATH ? "Mortality" : type.getLabel();
        return new EventSummary(type, label, outcome, outcomeLabel, explanation, count, rule == null ? null : rule.getCode());
    }

    private void markEventsNeedingReview(PatientAssessment a) {
        for (CbsFinding f : a.getFindings()) {
            if (f.getEventType() == null) {
                continue;
            }
            a.getEvents().stream().filter(e -> e.getType() == f.getEventType()).forEach(e -> e.setNeedsReview(true));
        }
    }

    private void indicators(PatientAssessment a, List<PatientSource.Refill> refills, CbsEvent baselineCd4) {
        a.getIndicators().put("Treatment status", a.getTreatmentStatus().getStatus());
        a.getIndicators().put("Vital status", a.getVitalStatus().getStatus());
        a.getIndicators().put("Latest viral load", a.getViralLoadStatus().getEvidence());
        a.getIndicators().put("Viral load status", a.getViralLoadStatus().getStatus());
        a.getIndicators().put("EAC status", a.getEacStatus().getStatus());
        a.getIndicators().put("Baseline CD4", baselineCd4 == null ? "None" : baselineCd4.getDetails().get("result") + " on " + fmt(baselineCd4.getEventDate()));
        if (baselineCd4 != null) {
            a.getIndicators().put("Advanced HIV disease", Boolean.TRUE.equals(baselineCd4.getDetails().get("advancedHivDisease")) ? "Yes" : "No");
        }
        a.getIndicators().put("Pharmacy refills assessed", refills.size());
        a.getIndicators().put("Last refill", refills.isEmpty() ? "None" : fmt(refills.get(refills.size() - 1).getVisitDate()));
        a.getIndicators().put("Open data quality findings", a.getFindings().size());
    }

    // ---------------------------------------------------------------- helpers

    private static class Service {
        final LocalDate date;
        final String label;
        final String uuid;

        Service(LocalDate date, String label, String uuid) {
            this.date = date;
            this.label = label;
            this.uuid = uuid;
        }
    }

    /** First pharmacy dispensing, clinic visit or lab sample dated after a date (and on or before as-of). */
    private Service firstServiceAfter(PatientSource s, LocalDate after, LocalDate asOf) {
        List<Service> all = new ArrayList<>();
        s.getRefills().forEach(r -> all.add(new Service(r.getVisitDate(), "pharmacy dispensing", r.getUuid())));
        s.getClinicVisits().forEach(v -> all.add(new Service(v.getDate(), "clinic visit", v.getUuid())));
        s.getViralLoads().forEach(t -> all.add(new Service(t.getEventDate(), "viral load test", t.getUuid())));
        s.getCd4Tests().forEach(t -> all.add(new Service(t.getEventDate(), "CD4 test", t.getUuid())));
        return all.stream().filter(x -> x.date != null && x.date.isAfter(after) && !x.date.isAfter(asOf))
                .min(Comparator.comparing(x -> x.date)).orElse(null);
    }

    private boolean transferredBackIn(PatientSource s, LocalDate after, LocalDate upTo) {
        boolean tracker = s.getTrackerStatuses().stream().anyMatch(t -> t.getStatusDate() != null
                && (CbsTrackerStatus.TRANSFER_IN.equals(t.getStatus()) || CbsTrackerStatus.RETURN_TO_CARE.equals(t.getStatus()))
                && t.getStatusDate().isAfter(after) && !t.getStatusDate().isAfter(upTo));
        boolean form = s.getTransferInForms().stream().anyMatch(t -> t.getDate() != null
                && t.getDate().isAfter(after) && !t.getDate().isAfter(upTo));
        return tracker || form;
    }

    private boolean hasTrackingSince(PatientSource s, LocalDate from, LocalDate upTo) {
        boolean tracker = s.getTrackerStatuses().stream().anyMatch(t -> t.getStatusDate() != null
                && !t.getStatusDate().isBefore(from) && !t.getStatusDate().isAfter(upTo));
        boolean tracking = s.getTrackings().stream().anyMatch(t -> t.getTrackingDate() != null
                && !t.getTrackingDate().isBefore(from) && !t.getTrackingDate().isAfter(upTo));
        return tracker || tracking;
    }

    private LocalDate lastContact(PatientSource s, LocalDate asOf) {
        List<LocalDate> dates = new ArrayList<>();
        s.getRefills().forEach(r -> dates.add(r.getVisitDate()));
        s.getClinicVisits().forEach(v -> dates.add(v.getDate()));
        s.getCommencements().forEach(c -> dates.add(c.getDate()));
        s.getViralLoads().forEach(v -> dates.add(v.getEventDate()));
        s.getCd4Tests().forEach(v -> dates.add(v.getEventDate()));
        s.getEacSessions().forEach(e -> dates.add(e.getDate()));
        return dates.stream().filter(Objects::nonNull).filter(d -> !d.isAfter(asOf)).max(Comparator.naturalOrder()).orElse(null);
    }

    private PatientSource.DatedRecord earliest(Collection<PatientSource.DatedRecord> records, LocalDate asOf) {
        return records.stream().filter(r -> r.getDate() != null && !r.getDate().isAfter(asOf))
                .min(Comparator.comparing(PatientSource.DatedRecord::getDate)).orElse(null);
    }

    private PatientSource.DatedRecord latest(Collection<PatientSource.DatedRecord> records, LocalDate asOf) {
        return records.stream().filter(r -> r.getDate() != null && !r.getDate().isAfter(asOf))
                .max(Comparator.comparing(PatientSource.DatedRecord::getDate)).orElse(null);
    }

    private PatientSource.Commencement earliestCommencement(PatientSource s, LocalDate asOf) {
        return s.getCommencements().stream().filter(c -> c.getDate() != null && !c.getDate().isAfter(asOf))
                .min(Comparator.comparing(PatientSource.Commencement::getDate)).orElse(null);
    }

    private int refillDays(PatientSource.Refill r, CbsSettings cfg) {
        return r.getRefillPeriod() == null || r.getRefillPeriod() <= 0 ? cfg.getDefaultRefillDays() : r.getRefillPeriod();
    }

    private static boolean isTransferIn(String statusAtRegistration) {
        return statusAtRegistration != null && statusAtRegistration.toLowerCase().contains("transfer in");
    }

    private static boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private static String lineLabel(int line) {
        return line == 1 ? "1st line" : line == 2 ? "2nd line" : "3rd line";
    }

    private static void addIfPresent(List<CbsEvent> list, CbsEvent e) {
        if (e != null) {
            list.add(e);
        }
    }

    private static String ref(CbsEvent e) {
        return e.getSourceModule() + " / " + e.getSourceTable() + " / " + e.getSourceRecordId();
    }

    private static String trimNumber(Double d) {
        return d == Math.floor(d) ? String.valueOf(d.longValue()) : String.valueOf(d);
    }

    private static LocalDate toLocalDate(java.util.Date d) {
        if (d instanceof java.sql.Date) {
            return ((java.sql.Date) d).toLocalDate();
        }
        return Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static String fmt(LocalDate d) {
        return d == null ? "unknown date" : d.format(DISPLAY);
    }
}
