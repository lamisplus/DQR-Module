import React, { useEffect, useState } from "react";
import moment from "moment";
import { FormFeedback, Input, Label } from "reactstrap";
import {
  FaCheckCircle,
  FaExclamationTriangle,
  FaTimesCircle,
  FaMinusCircle,
  FaQuestionCircle,
  FaClock,
} from "react-icons/fa";
import "./cbs.css";

// LAMISPlus colours
export const PRIMARY = "#014d88";
export const ACCENT = "rgb(153,46,98)";

export const STATE = {
  DOCUMENTED: { color: "#2e7d32", Icon: FaCheckCircle },
  MISSING_EXPECTED: { color: "#ed6c02", Icon: FaExclamationTriangle },
  NOT_YET_DUE: { color: "#0277bd", Icon: FaClock },
  NO_EVENT: { color: "#6c757d", Icon: FaMinusCircle },
  NOT_APPLICABLE: { color: "#6c757d", Icon: FaMinusCircle },
  UNABLE_TO_DETERMINE: { color: "#1565c0", Icon: FaQuestionCircle },
  INVALID: { color: "#c62828", Icon: FaTimesCircle },
};

// Status boxes on the patient tracker: which state each status value maps to
const STATUS_STATE = {
  "Active on ART": "DOCUMENTED",
  Alive: "DOCUMENTED",
  Suppressed: "DOCUMENTED",
  "EAC Documented": "DOCUMENTED",
  "Treatment Interrupted": "MISSING_EXPECTED",
  "EAC Due": "NOT_YET_DUE",
  "EAC Missing": "MISSING_EXPECTED",
  "Not Suppressed": "INVALID",
  Deceased: "NO_EVENT",
  "Transferred Out": "NO_EVENT",
  "Not Yet Initiated on ART": "NO_EVENT",
  NOT_APPLICABLE: "NOT_APPLICABLE",
  "No Result": "NO_EVENT",
};

export const stateForStatus = (status) => STATUS_STATE[status] || "UNABLE_TO_DETERMINE";

export const StateIcon = ({ state, size = 22 }) => {
  const s = STATE[state] || STATE.UNABLE_TO_DETERMINE;
  return <s.Icon size={size} color={s.color} aria-hidden="true" />;
};

export const StateLabel = ({ state, label }) => {
  const s = STATE[state] || STATE.UNABLE_TO_DETERMINE;
  return (
    <span className="cbs-pill" style={{ color: s.color, borderColor: s.color }}>
      <s.Icon size={12} aria-hidden="true" /> {label}
    </span>
  );
};

const SEVERITY = { High: "#c62828", Medium: "#ed6c02", Low: "#0277bd" };
export const SeverityBadge = ({ severity }) => (
  <span className="cbs-badge" style={{ backgroundColor: SEVERITY[severity] || "#6c757d" }}>
    {severity}
  </span>
);

export const ISSUE_STATUS_LABEL = {
  OPEN: "Open",
  ACKNOWLEDGED: "Acknowledged",
  UNDER_REVIEW: "Under Review",
  CORRECTED: "Corrected - awaiting revalidation",
  EXCEPTION_REQUESTED: "Exception Requested",
  APPROVED_EXCEPTION: "Approved Exception",
  RESOLVED: "Resolved",
};
const ISSUE_STATUS_COLOR = {
  OPEN: "#c62828",
  ACKNOWLEDGED: "#ed6c02",
  UNDER_REVIEW: "#ed6c02",
  CORRECTED: "#0277bd",
  EXCEPTION_REQUESTED: "#6a1b9a",
  APPROVED_EXCEPTION: "#6c757d",
  RESOLVED: "#2e7d32",
};
export const IssueStatusBadge = ({ status }) =>
  status ? (
    <span className="cbs-badge" style={{ backgroundColor: ISSUE_STATUS_COLOR[status] || "#6c757d" }}>
      {ISSUE_STATUS_LABEL[status] || status}
    </span>
  ) : (
    <span className="text-muted">Not linked</span>
  );

export const FINDING_TYPE_LABEL = {
  VALIDATION_ERROR: "Validation Error",
  POSSIBLE_DISCREPANCY: "Possible Discrepancy",
  MISSING_EXPECTED_EVENT: "Missing Expected Event",
  UNABLE_TO_ASSESS: "Unable to Assess",
};

export const EVENT_LABEL = {
  HIV_DIAGNOSIS: "HIV Diagnosis",
  LINKAGE_TO_CARE: "Linkage to Care",
  ART_INITIATION: "ART Initiation",
  TRANSFER_IN: "Transfer In",
  TRANSFER_OUT: "Transfer Out",
  TREATMENT_INTERRUPTION: "Treatment Interruption",
  RETURN_TO_CARE: "Return to Care",
  DEATH: "Death",
  VIRAL_LOAD: "Viral Load Test",
  EAC_SESSION: "EAC Session",
  BASELINE_CD4: "Baseline CD4 / AHD",
  POST_EAC_VIRAL_LOAD: "Post-EAC Viral Load",
  REGIMEN_SWITCH: "Regimen Line Switch",
};

export const TREATMENT_STATUSES = [
  "Active on ART",
  "Treatment Interrupted",
  "Transferred Out",
  "Deceased",
  "Not Yet Initiated on ART",
  "Unable to Determine",
];

export const CATEGORIES = [
  "Missing Linkage",
  "Missing ART Initiation Information",
  "Invalid Event Date/Sequence",
  "Duplicate Event",
  "Transfer Discrepancy",
  "Treatment Status Conflict",
  "Mortality Discrepancy",
  "Missing Required Information",
  "Missing Follow-up",
  "Missing EAC",
  "Cross-module Inconsistency",
];

export const SEVERITIES = ["High", "Medium", "Low"];

export const formatDate = (value) => (value ? moment(value).format("DD MMM YYYY") : "-");
export const formatDateTime = (value) => (value ? moment(value).format("DD MMM YYYY, HH:mm") : "-");

// Same header style as MaterialTable everywhere else in DQR
export const tableOptions = {
  headerStyle: { backgroundColor: PRIMARY, color: "#fff" },
  search: false,
  pageSizeOptions: [10, 20, 50],
  pageSize: 10,
  debounceInterval: 400,
  draggable: false,
};

export const SummaryCard = ({ value, label, help, accent = PRIMARY, onClick, active }) => (
  <div
    className={`cbs-summary-card${onClick ? " clickable" : ""}${active ? " active" : ""}`}
    style={{ borderTopColor: accent }}
    onClick={onClick}
    role={onClick ? "button" : undefined}
    tabIndex={onClick ? 0 : undefined}
    onKeyPress={(e) => onClick && e.key === "Enter" && onClick()}
    title={help}
  >
    <div className="cbs-summary-value">{value}</div>
    <div className="cbs-summary-label">{label}</div>
    {help && <div className="cbs-summary-help">{help}</div>}
  </div>
);

// Card value while data is missing, as the spec asks: never show a misleading 0
export const cardValue = (value, processing) => {
  if (value === null || value === undefined) return processing ? "Processing" : "Not available";
  return Number(value).toLocaleString();
};

export const SectionHeader = ({ title, children }) => (
  <div className="card-header cbs-section-header">
    <h5 className="card-title">{title}</h5>
    {children}
  </div>
);

export const BackLink = ({ onClick, label }) => (
  <button type="button" className="cbs-back" onClick={onClick}>
    {"<<"} {label}
  </button>
);

// The message names whatever the two boxes are called on that screen
export const dateRangeError = (fromLabel, toLabel) => `"${toLabel}" can't be earlier than "${fromLabel}".`;

// Dates come from <input type="date"> as YYYY-MM-DD, so comparing the strings compares the dates
export const isReversedRange = (from, to) => Boolean(from && to && to < from);

/**
 * Date range boxes used on every CBS screen ("Event date from / to", and "Reporting period" on Reports). The picker won't offer a "to" date
 * before "from" (or a "from" after "to"), and a date typed in by hand that breaks that rule is
 * marked in red with a message and NOT passed on, so the screen keeps showing the last valid range.
 * Renders two grid cells, so it drops straight into a .cbs-filters row.
 */
export const DateRangeFilter = ({
  idPrefix, from, to, onChange, fromLabel = "Event date from", toLabel = "Event date to",
}) => {
  const [draft, setDraft] = useState({ from: from || "", to: to || "" });

  // a screen clearing its filters (or a preset from the Overview) resets the boxes
  useEffect(() => {
    setDraft({ from: from || "", to: to || "" });
  }, [from, to]);

  const reversed = isReversedRange(draft.from, draft.to);

  const update = (key) => (e) => {
    const next = { ...draft, [key]: e.target.value };
    setDraft(next);
    if (!isReversedRange(next.from, next.to)) onChange(next.from, next.to);
  };

  return (
    <>
      <div>
        <Label for={`${idPrefix}-from`}>{fromLabel}</Label>
        <Input id={`${idPrefix}-from`} type="date" value={draft.from} max={draft.to || undefined}
               invalid={reversed} onChange={update("from")} />
      </div>
      <div>
        <Label for={`${idPrefix}-to`}>{toLabel}</Label>
        <Input id={`${idPrefix}-to`} type="date" value={draft.to} min={draft.from || undefined}
               invalid={reversed} onChange={update("to")} />
        <FormFeedback>{dateRangeError(fromLabel, toLabel)}</FormFeedback>
      </div>
    </>
  );
};
