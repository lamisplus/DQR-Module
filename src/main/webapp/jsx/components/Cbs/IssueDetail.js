import React, { useCallback, useEffect, useState } from "react";
import moment from "moment";
import { Input, Label } from "reactstrap";
import { Button } from "semantic-ui-react";
import { toast } from "react-toastify";
import cbsApi, { errorMessage } from "./cbsApi";
import {
  ACCENT,
  BackLink,
  EVENT_LABEL,
  FINDING_TYPE_LABEL,
  ISSUE_STATUS_LABEL,
  IssueStatusBadge,
  PRIMARY,
  SeverityBadge,
  formatDate,
  formatDateTime,
} from "./common";

const CLOSED = ["RESOLVED", "APPROVED_EXCEPTION"];

const ACTION_LABEL = {
  CREATED: "Issue created",
  ASSIGNED: "Assigned",
  REASSIGNED: "Reassigned",
  ACKNOWLEDGED: "Acknowledged",
  REVIEW_STARTED: "Investigation started",
  INVESTIGATION_FINDING: "Investigation finding",
  CORRECTIVE_ACTION: "Corrective action recorded",
  REVALIDATED: "Revalidated",
  AUTO_RESOLVED: "Resolved by data refresh",
  REOPENED: "Reopened",
  EXCEPTION_REQUESTED: "Exception requested",
  EXCEPTION_APPROVED: "Exception approved",
  EXCEPTION_REJECTED: "Exception rejected",
  ESCALATED: "Escalated",
};

const parse = (text) => {
  try {
    return text ? JSON.parse(text) : null;
  } catch (e) {
    return null;
  }
};
const prettyKey = (k) => k.replace(/([A-Z])/g, " $1").replace(/^./, (c) => c.toUpperCase());

const ActionButton = ({ children, ...props }) => (
  <Button style={{ backgroundColor: ACCENT, color: "#fff", marginTop: 6 }} {...props}>
    {children}
  </Button>
);

const IssueDetail = ({ issueId, permissions, onBack, onOpenPatient, onChanged }) => {
  const [data, setData] = useState(null);
  const [users, setUsers] = useState([]);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ assignee: "", finding: "", action: "", actionDate: moment().format("YYYY-MM-DD"), sourceReference: "", justification: "", decisionNote: "", escalateNote: "" });

  const load = useCallback(() => {
    cbsApi
      .issue(issueId)
      .then(setData)
      .catch((e) => toast.error(errorMessage(e, "Could not load the issue.")));
  }, [issueId]);

  useEffect(load, [load]);
  useEffect(() => {
    cbsApi.users().then(setUsers).catch(() => setUsers([]));
  }, []);

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  // Every action returns the updated issue, so the page refreshes from the server's answer
  const run = (action, body, message, reset = {}) => {
    setSaving(true);
    cbsApi
      .issueAction(issueId, action, body)
      .then((d) => {
        setData(d);
        setForm((f) => ({ ...f, ...reset }));
        toast.success(message);
        if (onChanged) onChanged();
      })
      .catch((e) => toast.error(errorMessage(e)))
      .finally(() => setSaving(false));
  };

  if (!data) return <div className="cbs-empty">Loading issue...</div>;

  const issue = data.issue;
  const perms = permissions || {};
  const closed = CLOSED.includes(issue.status);
  const canManage = perms.manageIssues && !closed;
  const evidence = parse(issue.evidence) || {};
  const hasFinding = data.history.some((h) => h.action === "INVESTIGATION_FINDING");

  return (
    <>
      <BackLink onClick={onBack} label="Back" />
      <h4 style={{ color: PRIMARY }}>
        Issue DQ-{issue.id} <IssueStatusBadge status={issue.status} />{" "}
        {issue.escalated && <span className="cbs-badge" style={{ backgroundColor: "#6a1b9a" }}>Escalated</span>}{" "}
        {issue.overdue && <span className="cbs-badge" style={{ backgroundColor: "#c62828" }}>Overdue</span>}
      </h4>

      <div className="cbs-tracker-grid">
        <div>
          <div className="card">
            <div className="card-header cbs-section-header">
              <h5 className="card-title">Finding and evidence</h5>
            </div>
            <div className="card-body">
              <p style={{ fontSize: 15 }}>{issue.description}</p>
              <table className="cbs-kv">
                <tbody>
                  <tr><td>Patient ID</td><td>
                    <button type="button" className="cbs-link" onClick={() => onOpenPatient(issue.personUuid)}>{issue.patientId}</button>
                    {issue.hospitalNumber ? ` (hospital no. ${issue.hospitalNumber})` : ""}
                  </td></tr>
                  <tr><td>Validation rule</td><td>{issue.ruleCode} {data.ruleName ? `- ${data.ruleName}` : ""} (version {issue.ruleVersion})</td></tr>
                  <tr><td>Classification</td><td>{FINDING_TYPE_LABEL[issue.findingType] || issue.findingType}</td></tr>
                  <tr><td>Category</td><td>{issue.category}</td></tr>
                  <tr><td>Affected event</td><td>{issue.eventType ? EVENT_LABEL[issue.eventType] || issue.eventType : "Patient record"}</td></tr>
                  <tr><td>Severity</td><td><SeverityBadge severity={issue.severity} /></td></tr>
                  <tr><td>Date identified</td><td>{formatDateTime(issue.dateIdentified)}</td></tr>
                  <tr><td>Last seen by a refresh</td><td>{formatDateTime(issue.lastDetected)}</td></tr>
                  <tr><td>Assigned to</td><td>{issue.assignedTo || "Unassigned"}</td></tr>
                  <tr><td>Due date</td><td>{formatDate(issue.dueDate)}</td></tr>
                  {Object.keys(evidence).map((k) => (
                    <tr key={k}>
                      <td>{prettyKey(k)}</td>
                      <td>{Array.isArray(evidence[k]) ? evidence[k].join(", ") : String(evidence[k])}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="cbs-warning" style={{ marginTop: 12 }}>
                Corrections are made in the module that owns the record (for example the HIV or HTS module), never here.
                After correcting it, record the action below and click <strong>Revalidate</strong>.
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header cbs-section-header">
              <h5 className="card-title">History</h5>
            </div>
            <div className="card-body">
              <ul className="cbs-history">
                {data.history.map((h) => {
                  const details = parse(h.details);
                  return (
                    <li key={h.id}>
                      <strong>{ACTION_LABEL[h.action] || h.action}</strong>
                      {h.toStatus && h.fromStatus !== h.toStatus ? ` (${ISSUE_STATUS_LABEL[h.fromStatus] || "new"} to ${ISSUE_STATUS_LABEL[h.toStatus] || h.toStatus})` : ""}
                      {h.note && <div>{h.note}</div>}
                      {h.actionDate && <div className="who">Action date: {formatDate(h.actionDate)}</div>}
                      {details && details.sourceReference && <div className="who">Source record: {details.sourceReference}</div>}
                      {details && details.outcome && <div className="who">Revalidation outcome: {details.outcome}</div>}
                      <div className="who">{h.performedBy} on {formatDateTime(h.performedAt)}</div>
                    </li>
                  );
                })}
              </ul>
            </div>
          </div>
        </div>

        <div>
          {closed && (
            <div className="cbs-side-card">
              <h6>Issue closed</h6>
              <div className="body">
                This issue is {ISSUE_STATUS_LABEL[issue.status].toLowerCase()}. It reopens automatically if a later data refresh finds the same problem again.
              </div>
            </div>
          )}
          {!perms.manageIssues && !closed && (
            <div className="cbs-side-card">
              <h6>Read only</h6>
              <div className="body">You can view this issue but do not have permission to manage issues.</div>
            </div>
          )}

          {canManage && (
            <>
              <div className="cbs-side-card">
                <h6>Assignment</h6>
                <div className="body">
                  <Label for="d-assignee">Assign to</Label>
                  <Input id="d-assignee" type="select" value={form.assignee} onChange={set("assignee")}>
                    <option value="">Select a user</option>
                    {users.map((u) => <option key={u.userName} value={u.userName}>{u.fullName || u.userName}</option>)}
                  </Input>
                  <ActionButton disabled={!form.assignee || saving}
                                onClick={() => run("assign", { assignedTo: form.assignee }, "Issue assigned.", { assignee: "" })}>
                    Assign
                  </ActionButton>
                </div>
              </div>

              <div className="cbs-side-card">
                <h6>Review</h6>
                <div className="body">
                  {issue.status === "OPEN" && (
                    <ActionButton disabled={saving} onClick={() => run("acknowledge", {}, "Issue acknowledged.")}>Acknowledge</ActionButton>
                  )}{" "}
                  {["OPEN", "ACKNOWLEDGED"].includes(issue.status) && (
                    <ActionButton disabled={saving} onClick={() => run("start-review", {}, "Investigation started.")}>Start investigation</ActionButton>
                  )}
                  <div style={{ marginTop: 10 }}>
                    <Label for="d-finding">Investigation finding</Label>
                    <Input id="d-finding" type="textarea" rows={3} value={form.finding} onChange={set("finding")}
                           placeholder="What did you find when you checked the records?" />
                    <ActionButton disabled={!form.finding.trim() || saving}
                                  onClick={() => run("investigation", { note: form.finding }, "Finding recorded.", { finding: "" })}>
                      Save finding
                    </ActionButton>
                  </div>
                </div>
              </div>

              <div className="cbs-side-card">
                <h6>Corrective action</h6>
                <div className="body">
                  {!hasFinding && <div className="text-muted" style={{ marginBottom: 6 }}>Record an investigation finding first.</div>}
                  <Label for="d-action">What was corrected</Label>
                  <Input id="d-action" type="textarea" rows={3} value={form.action} onChange={set("action")} disabled={!hasFinding} />
                  <Label for="d-date" style={{ marginTop: 6 }}>Action date</Label>
                  <Input id="d-date" type="date" value={form.actionDate} max={moment().format("YYYY-MM-DD")} onChange={set("actionDate")} disabled={!hasFinding} />
                  <Label for="d-ref" style={{ marginTop: 6 }}>Source record corrected (optional)</Label>
                  <Input id="d-ref" value={form.sourceReference} onChange={set("sourceReference")} placeholder="e.g. HIV enrolment of patient 7991" disabled={!hasFinding} />
                  <ActionButton disabled={!hasFinding || !form.action.trim() || !form.actionDate || saving}
                                onClick={() => run("corrective-action", { note: form.action, actionDate: form.actionDate, sourceReference: form.sourceReference },
                                  "Corrective action recorded. Revalidate to confirm.", { action: "", sourceReference: "" })}>
                    Save corrective action
                  </ActionButton>
                  <div style={{ marginTop: 10 }}>
                    <ActionButton disabled={saving} onClick={() => run("revalidate", {}, "Revalidation finished. See the history for the outcome.")}>
                      Revalidate now
                    </ActionButton>
                  </div>
                </div>
              </div>

              <div className="cbs-side-card">
                <h6>Exception</h6>
                <div className="body">
                  {issue.status === "EXCEPTION_REQUESTED" ? (
                    perms.approveException ? (
                      <>
                        <Label for="d-decision">Decision note</Label>
                        <Input id="d-decision" type="textarea" rows={2} value={form.decisionNote} onChange={set("decisionNote")} />
                        <ActionButton disabled={!form.decisionNote.trim() || saving}
                                      onClick={() => run("exception-decision", { approve: true, note: form.decisionNote }, "Exception approved.", { decisionNote: "" })}>
                          Approve exception
                        </ActionButton>{" "}
                        <ActionButton disabled={!form.decisionNote.trim() || saving}
                                      onClick={() => run("exception-decision", { approve: false, note: form.decisionNote }, "Exception rejected.", { decisionNote: "" })}>
                          Reject
                        </ActionButton>
                      </>
                    ) : (
                      <div className="text-muted">Waiting for a user with exception-approval permission.</div>
                    )
                  ) : (
                    <>
                      {!hasFinding && <div className="text-muted" style={{ marginBottom: 6 }}>Record an investigation finding first.</div>}
                      <Label for="d-just">Why this should be accepted without correction</Label>
                      <Input id="d-just" type="textarea" rows={2} value={form.justification} onChange={set("justification")} disabled={!hasFinding} />
                      <ActionButton disabled={!hasFinding || !form.justification.trim() || saving}
                                    onClick={() => run("exception-request", { justification: form.justification }, "Exception requested.", { justification: "" })}>
                        Request exception
                      </ActionButton>
                    </>
                  )}
                </div>
              </div>

              {!issue.escalated && (
                <div className="cbs-side-card">
                  <h6>Escalate</h6>
                  <div className="body">
                    <Input type="textarea" rows={2} value={form.escalateNote} onChange={set("escalateNote")} placeholder="Reason for escalation" />
                    <ActionButton disabled={!form.escalateNote.trim() || saving}
                                  onClick={() => run("escalate", { note: form.escalateNote }, "Issue escalated.", { escalateNote: "" })}>
                      Escalate
                    </ActionButton>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </>
  );
};

export default IssueDetail;
