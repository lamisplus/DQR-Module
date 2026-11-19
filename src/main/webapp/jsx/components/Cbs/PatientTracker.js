import React, { useCallback, useEffect, useState } from "react";
import moment from "moment";
import { Input, Label } from "reactstrap";
import { toast } from "react-toastify";
import cbsApi, { errorMessage } from "./cbsApi";
import {
  BackLink,
  FINDING_TYPE_LABEL,
  IssueStatusBadge,
  SeverityBadge,
  STATE,
  StateIcon,
  StateLabel,
  formatDate,
  formatDateTime,
  stateForStatus,
} from "./common";

const LEGEND = [
  ["DOCUMENTED", "Documented", "Confirmed by source records. Actual event date shown."],
  ["MISSING_EXPECTED", "Missing Expected Event", "Expected by now but no qualifying record found. Review required."],
  ["NOT_YET_DUE", "Not Yet Due", "Expected later; the allowed time has not passed."],
  ["NO_EVENT", "No Event Documented", "Relevant records assessed; no event identified."],
  ["UNABLE_TO_DETERMINE", "Unable to Determine", "Records unavailable or conflicting."],
  ["NOT_APPLICABLE", "Not Applicable", "Not expected for this patient."],
];

const DETAIL_LABEL = {
  htsTestDate: "HTS test date",
  statusAtRegistration: "Status at registration",
  recordedStatus: "Recorded status",
  causeOfDeath: "Cause of death",
  lastRefillDate: "Last refill",
  drugsRanOutOn: "Drugs ran out on",
  daysWithoutDrugs: "Days without drugs",
  rule: "How it was identified",
  result: "Result",
  note: "Note",
};

const StatusBox = ({ label, result }) => {
  if (!result) return null;
  const shown = result.status === "NOT_APPLICABLE" ? "Not Applicable" : result.status;
  return (
    <div className="cbs-status-box">
      <StateIcon state={stateForStatus(result.status)} size={30} />
      <div>
        <div className="box-label">{label}</div>
        <div className="box-value">{shown}</div>
        <div className="box-evidence">
          {result.evidence}
          {result.effectiveDate ? ` (effective ${formatDate(result.effectiveDate)})` : ""}
        </div>
      </div>
    </div>
  );
};

const EventRow = ({ event, supporting }) => {
  const [open, setOpen] = useState(false);
  const details = event.details || {};
  return (
    <li className={supporting ? "supporting" : ""}>
      <StateIcon state="DOCUMENTED" size={supporting ? 16 : 24} />
      <div style={{ flex: 1 }}>
        <div className="event-title">
          {event.label} {event.needsReview && <StateLabel state="MISSING_EXPECTED" label="Needs review" />}
        </div>
        <div className="event-meta">
          {formatDate(event.eventDate)} | Source: {event.sourceModule} | Verification: {event.verificationMethod}
        </div>
        {open && (
          <table className="cbs-kv" style={{ marginTop: 6 }}>
            <tbody>
              {Object.keys(details).map((k) => (
                <tr key={k}>
                  <td>{DETAIL_LABEL[k] || k}</td>
                  <td>{String(details[k])}</td>
                </tr>
              ))}
              <tr>
                <td>Source record</td>
                <td>{event.sourceTable} / {event.sourceRecordId}</td>
              </tr>
              <tr>
                <td>Date entered</td>
                <td>{event.recordedDate ? formatDateTime(event.recordedDate) : "Not documented"}</td>
              </tr>
            </tbody>
          </table>
        )}
      </div>
      <div className="event-right">
        {!supporting && <StateLabel state="DOCUMENTED" label="Documented" />}
        <div>
          <button type="button" className="cbs-link" onClick={() => setOpen(!open)}>
            {open ? "Hide details" : "View details"}
          </button>
        </div>
      </div>
    </li>
  );
};

const PatientTracker = ({ personUuid, onBack, onOpenIssue }) => {
  const [asOf, setAsOf] = useState(moment().format("YYYY-MM-DD"));
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showSupporting, setShowSupporting] = useState(true);

  const load = useCallback(() => {
    setLoading(true);
    cbsApi
      .patient(personUuid, asOf)
      .then((d) => {
        setData(d);
        setError(null);
      })
      .catch((e) => {
        const message = errorMessage(e, "Could not load the patient.");
        setError(message);
        toast.error(message);
      })
      .finally(() => setLoading(false));
  }, [personUuid, asOf]);

  useEffect(load, [load]);

  if (loading && !data) return <div className="cbs-empty">Loading patient surveillance record...</div>;
  if (error && !data) {
    return (
      <>
        <BackLink onClick={onBack} label="Back to results" />
        <div className="cbs-warning error">{error}</div>
      </>
    );
  }

  const p = data.patient;
  const ctx = data.context || {};
  const lastRefresh = ctx.lastSuccessfulRefresh;
  const events = showSupporting
    ? [...data.timeline.map((e) => ({ ...e, primary: true })), ...data.supporting.map((e) => ({ ...e, primary: false }))]
        .sort((a, b) => (a.eventDate || "").localeCompare(b.eventDate || ""))
    : data.timeline.map((e) => ({ ...e, primary: true }));

  return (
    <>
      <BackLink onClick={onBack} label="Back to results" />
      <h4 style={{ color: "#014d88" }}>Patient Surveillance Event Tracker</h4>

      <div className="cbs-patient-header">
        <div>
          <div className="item-label">Patient ID</div>
          <div className="item-value">{p.patientId || "-"}</div>
          <div className="item-label">Hospital No: {p.hospitalNumber || "-"}</div>
        </div>
        <div>
          <div className="item-label">Sex</div>
          <div className="item-value">{p.sex || "-"}</div>
          <div className="item-label">Age: {p.age !== null && p.age !== undefined ? `${p.age} years` : "-"}</div>
        </div>
        <div>
          <div className="item-label">Facility</div>
          <div className="item-value">{ctx.facilityName || "-"}</div>
        </div>
        <div>
          <Label for="cbs-asof" className="item-label" style={{ color: "#666" }}>As-of date</Label>
          <Input
            id="cbs-asof"
            type="date"
            value={asOf}
            max={moment().format("YYYY-MM-DD")}
            onChange={(e) => e.target.value && setAsOf(e.target.value)}
          />
        </div>
        <div>
          <div className="item-label">Last source refresh</div>
          <div className="item-value">{lastRefresh ? formatDateTime(lastRefresh.finishedAt) : "Not available"}</div>
          <div className="item-label">Status below is worked out live from the clinical records</div>
        </div>
      </div>

      {data.identityConflicts && data.identityConflicts.length > 0 && (
        <div className="cbs-warning error">
          <strong>Potential Patient Identity Conflict:</strong> Multiple patient records have been identified with duplicated or
          inconsistent identifiers. The system cannot confirm whether these records belong to the same patient. Please review the
          affected records before proceeding.
          <ul style={{ marginBottom: 0 }}>
            {data.identityConflicts.map((c) => (
              <li key={c.personUuid}>
                Patient ID {c.patientId || "-"}, hospital no. {c.hospitalNumber || "-"}, {c.sex || "-"}, born {formatDate(c.dateOfBirth)}
              </li>
            ))}
          </ul>
        </div>
      )}

      <div className="cbs-status-boxes">
        <StatusBox label="Treatment status" result={data.treatmentStatus} />
        <StatusBox label="Vital status" result={data.vitalStatus} />
        <StatusBox label="Viral load status" result={data.viralLoadStatus} />
        <StatusBox label="EAC status" result={data.eacStatus} />
      </div>
      {data.documentedTreatmentStatus && data.documentedTreatmentStatus !== data.treatmentStatus.status && (
        <div className="cbs-warning">
          Client tracking records the status as <strong>{data.documentedTreatmentStatus}</strong>, but the treatment records show{" "}
          <strong>{data.treatmentStatus.status}</strong>. Both are shown so the difference can be reviewed.
        </div>
      )}

      <div className="cbs-tracker-grid">
        <div>
          <div className="card">
            <div className="card-header cbs-section-header">
              <h5 className="card-title">Surveillance Event History</h5>
              <label style={{ color: "#fff", fontSize: 13, margin: 0 }}>
                <input type="checkbox" checked={showSupporting} onChange={() => setShowSupporting(!showSupporting)} /> Show viral load and EAC
              </label>
            </div>
            <div className="card-body">
              {events.length === 0 ? (
                <div className="cbs-empty">No Surveillance Events Found</div>
              ) : (
                <ul className="cbs-timeline">
                  {events.map((e, i) => (
                    <EventRow key={`${e.type}-${e.sourceRecordId}-${i}`} event={e} supporting={!e.primary} />
                  ))}
                </ul>
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-header cbs-section-header">
              <h5 className="card-title">Surveillance Event Summary</h5>
            </div>
            <div className="card-body">
              <table className="cbs-kv">
                <tbody>
                  {data.eventSummary.map((s) => (
                    <tr key={s.label}>
                      <td>
                        <StateIcon state={s.outcome} size={16} /> {s.label}
                      </td>
                      <td>
                        <StateLabel state={s.outcome} label={s.outcomeLabel} />
                        <div className="text-muted" style={{ fontSize: 12 }}>
                          {s.explanation}
                          {s.ruleCode ? ` (${s.ruleCode})` : ""}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div>
          {data.issues && data.issues.length > 0 ? (
            <div className="cbs-side-card issue">
              <h6>Data quality issues requiring review ({data.issues.length})</h6>
              <div className="body">
                {data.issues.map((f) => (
                  <div key={f.findingId} style={{ borderBottom: "1px solid #f3d9c4", padding: "8px 0" }}>
                    <div>
                      <SeverityBadge severity={f.severity} /> <strong>{f.category}</strong>
                    </div>
                    <div style={{ margin: "4px 0" }}>{f.description}</div>
                    <div className="text-muted">
                      {f.ruleCode} | {FINDING_TYPE_LABEL[f.findingType] || f.findingType} | Identified {formatDate(f.dateIdentified)}
                    </div>
                    <div style={{ marginTop: 4 }}>
                      <IssueStatusBadge status={f.issueStatus} />{" "}
                      {f.issueId && (
                        <button type="button" className="cbs-link" onClick={() => onOpenIssue(f.issueId)}>
                          View in Data Quality Issue Tracker
                        </button>
                      )}
                    </div>
                  </div>
                ))}
                <div className="text-muted" style={{ marginTop: 6 }}>
                  Review the source records; do not assume an event did not happen only because it is not documented.
                </div>
              </div>
            </div>
          ) : (
            <div className="cbs-side-card">
              <h6>Data quality issues</h6>
              <div className="body">No unresolved data quality issues for this patient.</div>
            </div>
          )}

          <div className="cbs-side-card">
            <h6>Key surveillance indicators (as at {formatDate(p.asOfDate)})</h6>
            <div className="body">
              <table className="cbs-kv">
                <tbody>
                  {data.indicators.map((row) => (
                    <tr key={row.label}>
                      <td>{row.label}</td>
                      <td><strong>{row.value === "NOT_APPLICABLE" ? "Not Applicable" : row.value}</strong></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="cbs-side-card">
            <h6>Event status legend</h6>
            <div className="body">
              {LEGEND.map(([state, label, text]) => (
                <div key={state} style={{ display: "flex", gap: 8, marginBottom: 6 }}>
                  <StateIcon state={state} size={18} />
                  <div>
                    <strong style={{ color: STATE[state].color }}>{label}</strong>
                    <div className="text-muted" style={{ fontSize: 12 }}>{text}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default PatientTracker;
