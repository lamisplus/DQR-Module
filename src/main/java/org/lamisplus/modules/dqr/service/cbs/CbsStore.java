package org.lamisplus.modules.dqr.service.cbs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.dqr.domain.cbs.CbsEvent;
import org.lamisplus.modules.dqr.domain.cbs.CbsFinding;
import org.lamisplus.modules.dqr.domain.cbs.CbsSettings;
import org.lamisplus.modules.dqr.domain.cbs.CbsStatus;
import org.lamisplus.modules.dqr.domain.cbs.PatientAssessment;
import org.lamisplus.modules.dqr.domain.cbs.PatientSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bulk writes made by a facility refresh. These stay on JDBC batching on purpose: a refresh
 * touches thousands of findings and summary rows at once, which takes seconds this way and
 * minutes when saving JPA entities one by one. Everything else uses the JPA repositories.
 * Clinical tables are never written from here.
 */
@Slf4j
@Repository
public class CbsStore {

    public static final String SYSTEM_USER = "system";
    private static final int BATCH = 1000;
    private static final String CLOSED = "('" + CbsStatus.ISSUE_RESOLVED + "', '" + CbsStatus.ISSUE_APPROVED_EXCEPTION + "')";

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public CbsStore(@Qualifier("cbsJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Saves one facility's refresh. The caller wraps this in a transaction.
     * Events are not stored (timelines are always rebuilt live); only their count is reported.
     * Returns {active findings, new findings, cleared findings, events derived}.
     */
    public int[] saveFacility(long runId, Long facilityId, LocalDate asOf, CbsSettings cfg,
                              Collection<PatientSource> sources, Map<String, PatientAssessment> assessments) {
        int events = assessments.values().stream().mapToInt(a -> a.getEvents().size()).sum();
        int[] findingCounts = syncFindings(facilityId, assessments.values(), cfg);
        replaceSummaries(runId, facilityId, asOf, sources, assessments);
        updateSummaryCounts(facilityId, null);
        return new int[]{findingCounts[0], findingCounts[1], findingCounts[2], events};
    }

    private void replaceSummaries(long runId, Long facilityId, LocalDate asOf, Collection<PatientSource> sources,
                                  Map<String, PatientAssessment> assessments) {
        jdbc.update("DELETE FROM cbs_patient_summary WHERE facility_id = :facilityId", new MapSqlParameterSource("facilityId", facilityId));
        List<SqlParameterSource> rows = new ArrayList<>();
        for (PatientSource s : sources) {
            PatientAssessment a = assessments.get(s.getPersonUuid());
            CbsEvent latest = null;
            for (CbsEvent e : a.getEvents()) {
                if (e.getType().isPrimary() && e.getEventDate() != null
                        && (latest == null || !e.getEventDate().isBefore(latest.getEventDate()))) {
                    latest = e;
                }
            }
            rows.add(new MapSqlParameterSource("facilityId", facilityId).addValue("person", s.getPersonUuid())
                    .addValue("patientId", s.getPatientId()).addValue("hospitalNumber", s.getHospitalNumber())
                    .addValue("sex", s.getSex()).addValue("dob", s.getDateOfBirth())
                    .addValue("treatment", a.getTreatmentStatus().getStatus())
                    .addValue("treatmentDate", a.getTreatmentStatus().getEffectiveDate())
                    .addValue("vital", a.getVitalStatus().getStatus())
                    .addValue("latestType", latest == null ? null : latest.getType().name())
                    .addValue("latestDate", latest == null ? null : latest.getEventDate())
                    .addValue("asOf", asOf).addValue("runId", runId));
        }
        batch("INSERT INTO cbs_patient_summary (facility_id, person_uuid, patient_id, hospital_number, sex, date_of_birth, " +
                "treatment_status, treatment_status_date, vital_status, latest_event_type, latest_event_date, as_of_date, run_id) " +
                "VALUES (:facilityId, :person, :patientId, :hospitalNumber, :sex, :dob, :treatment, :treatmentDate, :vital, " +
                ":latestType, :latestDate, :asOf, :runId)", rows);
    }

    /**
     * Open flags leave out findings whose issue is resolved or an approved exception, which is
     * why this also runs after issue changes. Pass a person to update just that patient.
     */
    public void updateSummaryCounts(Long facilityId, String personUuid) {
        String personFilter = personUuid == null ? "" : " AND s.person_uuid = :person";
        jdbc.update("UPDATE cbs_patient_summary s SET " +
                        "open_flags = COALESCE(c.flags, 0), open_gaps = COALESCE(c.gaps, 0), status_discrepancy = COALESCE(c.status, FALSE) " +
                        "FROM cbs_patient_summary s2 LEFT JOIN (" +
                        "  SELECT f.person_uuid, COUNT(*) AS flags, " +
                        "         COUNT(*) FILTER (WHERE f.finding_type = '" + CbsStatus.MISSING_EXPECTED_EVENT + "') AS gaps, " +
                        "         BOOL_OR(f.rule_code = 'CBS-DQ-04') AS status " +
                        "  FROM cbs_finding f LEFT JOIN cbs_issue i ON i.finding_id = f.id " +
                        "  WHERE f.facility_id = :facilityId AND f.active AND COALESCE(i.status, '') NOT IN " + CLOSED +
                        "  GROUP BY f.person_uuid) c ON c.person_uuid = s2.person_uuid " +
                        "WHERE s.facility_id = :facilityId AND s2.facility_id = s.facility_id AND s2.person_uuid = s.person_uuid" + personFilter,
                new MapSqlParameterSource("facilityId", facilityId).addValue("person", personUuid));
    }

    /**
     * Brings cbs_finding in line with what the engine found:
     * new problems are inserted and get an issue, problems seen again are touched,
     * problems no longer found are cleared and their issue resolved by the system.
     * Returns {active, new, cleared}.
     */
    private int[] syncFindings(Long facilityId, Collection<PatientAssessment> assessments, CbsSettings cfg) {
        Map<String, Object[]> existing = new HashMap<>(); // dedup_key -> {id, active}
        jdbc.query("SELECT id, dedup_key, active FROM cbs_finding WHERE facility_id = :facilityId",
                new MapSqlParameterSource("facilityId", facilityId),
                rs -> {
                    existing.put(rs.getString("dedup_key"), new Object[]{rs.getLong("id"), rs.getBoolean("active")});
                });

        Map<String, CbsFinding> current = new LinkedHashMap<>();
        for (PatientAssessment a : assessments) {
            for (CbsFinding f : a.getFindings()) {
                current.putIfAbsent(f.getDedupKey(), f);
            }
        }

        Timestamp now = now();
        List<SqlParameterSource> inserts = new ArrayList<>();
        List<SqlParameterSource> updates = new ArrayList<>();
        List<Long> reactivated = new ArrayList<>();
        for (CbsFinding f : current.values()) {
            MapSqlParameterSource row = findingParams(facilityId, f, now);
            Object[] old = existing.get(f.getDedupKey());
            if (old == null) {
                inserts.add(row);
            } else {
                updates.add(row.addValue("id", old[0]));
                if (!(Boolean) old[1]) {
                    reactivated.add((Long) old[0]);
                }
            }
        }
        List<Long> cleared = new ArrayList<>();
        existing.forEach((key, old) -> {
            if ((Boolean) old[1] && !current.containsKey(key)) {
                cleared.add((Long) old[0]);
            }
        });

        batch("INSERT INTO cbs_finding (facility_id, person_uuid, rule_code, rule_version, category, finding_type, severity, " +
                "event_type, description, evidence, dedup_key, event_date, active, first_detected_at, last_detected_at) VALUES (:facilityId, " +
                ":person, :ruleCode, :ruleVersion, :category, :findingType, :severity, :eventType, :description, " +
                "CAST(:evidence AS jsonb), :dedupKey, :eventDate, TRUE, :now, :now)", inserts);
        batch("UPDATE cbs_finding SET rule_version = :ruleVersion, category = :category, finding_type = :findingType, " +
                "severity = :severity, event_type = :eventType, description = :description, evidence = CAST(:evidence AS jsonb), " +
                "event_date = :eventDate, " +
                "active = TRUE, last_detected_at = :now, cleared_at = NULL WHERE id = :id", updates);
        clearFindings(cleared, now);

        if (!inserts.isEmpty()) {
            createIssuesForNewFindings(facilityId, inserts, cfg, now);
        }
        // A resolved issue whose problem came back is reopened, keeping its history.
        // Approved exceptions stay as they are.
        for (List<Long> chunk : chunks(reactivated)) {
            MapSqlParameterSource p = new MapSqlParameterSource("ids", chunk).addValue("now", now).addValue("user", SYSTEM_USER);
            jdbc.update("INSERT INTO cbs_issue_history (issue_id, action, from_status, to_status, note, performed_by, performed_at) " +
                    "SELECT id, 'REOPENED', status, 'OPEN', 'The problem was found again during data refresh', :user, :now " +
                    "FROM cbs_issue WHERE finding_id IN (:ids) AND status = 'RESOLVED'", p);
            jdbc.update("UPDATE cbs_issue SET status = 'OPEN', resolved_at = NULL, updated_at = :now " +
                    "WHERE finding_id IN (:ids) AND status = 'RESOLVED'", p);
        }
        return new int[]{current.size(), inserts.size(), cleared.size()};
    }

    private void createIssuesForNewFindings(Long facilityId, List<SqlParameterSource> inserts, CbsSettings cfg, Timestamp now) {
        Set<String> newKeys = new HashSet<>();
        inserts.forEach(r -> newKeys.add((String) ((MapSqlParameterSource) r).getValue("dedupKey")));
        List<SqlParameterSource> issues = new ArrayList<>();
        jdbc.query("SELECT id, dedup_key, person_uuid, severity FROM cbs_finding WHERE facility_id = :facilityId AND first_detected_at = :now",
                new MapSqlParameterSource("facilityId", facilityId).addValue("now", now), rs -> {
                    if (newKeys.contains(rs.getString("dedup_key"))) {
                        issues.add(new MapSqlParameterSource("findingId", rs.getLong("id")).addValue("facilityId", facilityId)
                                .addValue("person", rs.getString("person_uuid")).addValue("status", CbsStatus.ISSUE_OPEN)
                                .addValue("due", LocalDate.now().plusDays(cfg.dueDaysFor(rs.getString("severity"))))
                                .addValue("now", now));
                    }
                });
        batch("INSERT INTO cbs_issue (finding_id, facility_id, person_uuid, status, due_date, escalated, created_at, updated_at) " +
                "VALUES (:findingId, :facilityId, :person, :status, :due, FALSE, :now, :now)", issues);
        jdbc.update("INSERT INTO cbs_issue_history (issue_id, action, to_status, note, performed_by, performed_at) " +
                        "SELECT i.id, 'CREATED', i.status, 'Detected by data refresh', :user, :now FROM cbs_issue i " +
                        "WHERE i.facility_id = :facilityId AND i.created_at = :now",
                new MapSqlParameterSource("facilityId", facilityId).addValue("now", now).addValue("user", SYSTEM_USER));
    }

    /** Clears findings no longer produced by the rules and resolves their open issues as the system. */
    private void clearFindings(List<Long> findingIds, Timestamp now) {
        for (List<Long> chunk : chunks(findingIds)) {
            MapSqlParameterSource p = new MapSqlParameterSource("ids", chunk).addValue("now", now).addValue("user", SYSTEM_USER);
            jdbc.update("UPDATE cbs_finding SET active = FALSE, cleared_at = :now WHERE id IN (:ids)", p);
            jdbc.update("INSERT INTO cbs_issue_history (issue_id, action, from_status, to_status, note, performed_by, performed_at) " +
                    "SELECT id, 'AUTO_RESOLVED', status, 'RESOLVED', 'Revalidated: the condition is no longer present in the source records', " +
                    ":user, :now FROM cbs_issue WHERE finding_id IN (:ids) AND status NOT IN " + CLOSED, p);
            jdbc.update("UPDATE cbs_issue SET status = 'RESOLVED', resolved_at = :now, updated_at = :now " +
                    "WHERE finding_id IN (:ids) AND status NOT IN " + CLOSED, p);
        }
    }

    private MapSqlParameterSource findingParams(Long facilityId, CbsFinding f, Timestamp now) {
        return new MapSqlParameterSource("facilityId", facilityId).addValue("person", f.getPersonUuid())
                .addValue("ruleCode", f.getRule().getCode()).addValue("ruleVersion", f.getRule().getVersion())
                .addValue("category", f.getRule().getCategory()).addValue("findingType", f.getRule().getFindingType())
                .addValue("severity", f.getRule().getSeverity())
                .addValue("eventType", f.getEventType() == null ? null : f.getEventType().name())
                .addValue("description", f.getDescription()).addValue("evidence", toJson(f.getEvidence()))
                .addValue("dedupKey", f.getDedupKey()).addValue("eventDate", f.getEventDate()).addValue("now", now);
    }

    private void batch(String sql, List<SqlParameterSource> rows) {
        for (int i = 0; i < rows.size(); i += BATCH) {
            List<SqlParameterSource> part = rows.subList(i, Math.min(rows.size(), i + BATCH));
            jdbc.batchUpdate(sql, part.toArray(new SqlParameterSource[0]));
        }
    }

    private static List<List<Long>> chunks(List<Long> ids) {
        List<List<Long>> out = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += BATCH) {
            out.add(ids.subList(i, Math.min(ids.size(), i + BATCH)));
        }
        return out;
    }

    private String toJson(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Could not write CBS json: {}", e.getMessage());
            return "{}";
        }
    }

    private static Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }
}
