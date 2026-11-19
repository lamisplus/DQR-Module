import React, { useEffect, useRef, useState } from "react";
import MaterialTable from "material-table";
import { Input, Label } from "reactstrap";
import { Button } from "semantic-ui-react";
import cbsApi, { errorMessage } from "./cbsApi";
import { tableIcons } from "./PatientList";
import {
  ACCENT,
  CATEGORIES,
  EVENT_LABEL,
  FINDING_TYPE_LABEL,
  ISSUE_STATUS_LABEL,
  IssueStatusBadge,
  SEVERITIES,
  SeverityBadge,
  SummaryCard,
  formatDate,
  tableOptions,
  DateRangeFilter,
} from "./common";

const VIEWS = [
  { key: "all", label: "All Issues", count: "all" },
  { key: "validation", label: "Validation Errors", count: "validation" },
  { key: "gaps", label: "Event Gaps", count: "gaps" },
  { key: "status", label: "Status Discrepancies", count: "status" },
];

const prettyKey = (k) => k.replace(/([A-Z])/g, " $1").replace(/^./, (c) => c.toUpperCase());

// Shows the stored evidence as label/value rows so the reviewer can compare values side by side
const Evidence = ({ row, onOpenPatient, onOpenIssue }) => {
  let evidence = {};
  try {
    evidence = row.evidence ? JSON.parse(row.evidence) : {};
  } catch (e) {
    evidence = {};
  }
  return (
    <div style={{ padding: "10px 24px", background: "#f9fbfd" }}>
      <div style={{ marginBottom: 6 }}>
        <strong>{row.ruleCode}</strong> {row.ruleName ? `- ${row.ruleName}` : ""} | {FINDING_TYPE_LABEL[row.findingType] || row.findingType}
        {row.eventType ? ` | Affected event: ${EVENT_LABEL[row.eventType] || row.eventType}` : ""}
      </div>
      <div style={{ marginBottom: 6 }}>{row.description}</div>
      <table className="cbs-kv" style={{ maxWidth: 700 }}>
        <tbody>
          {Object.keys(evidence).map((k) => (
            <tr key={k}>
              <td>{prettyKey(k)}</td>
              <td>{Array.isArray(evidence[k]) ? evidence[k].join(", ") : String(evidence[k])}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div style={{ marginTop: 8, display: "flex", gap: 16 }}>
        <button type="button" className="cbs-link" onClick={() => onOpenPatient(row.personUuid)}>Open patient timeline</button>
        {row.issueId ? (
          <button type="button" className="cbs-link" onClick={() => onOpenIssue(row.issueId)}>Open in Issue Tracker (DQ-{row.issueId})</button>
        ) : (
          <span className="text-muted">No issue is linked to this finding.</span>
        )}
      </div>
    </div>
  );
};

const Monitoring = ({ preset, onOpenPatient, onOpenIssue }) => {
  const tableRef = useRef();
  const firstRender = useRef(true);
  const [counts, setCounts] = useState(null);
  const [rules, setRules] = useState([]);
  const [showRules, setShowRules] = useState(false);
  const [error, setError] = useState(null);
  const [filters, setFilters] = useState({
    view: preset.view || "all",
    category: preset.category || "",
    severity: preset.severity || "",
    issueStatus: "UNRESOLVED",
    ruleCode: "",
    search: "",
    from: preset.from || "",
    to: preset.to || "",
  });

  useEffect(() => {
    cbsApi.rules().then((d) => setRules(d.rules)).catch(() => setRules([]));
  }, []);

  useEffect(() => {
    if (firstRender.current) {
      firstRender.current = false;
      return undefined;
    }
    const timer = setTimeout(() => tableRef.current && tableRef.current.onQueryChange(), 400);
    return () => clearTimeout(timer);
  }, [filters]);

  const set = (key) => (e) => setFilters({ ...filters, [key]: e.target.value });
  const ruleName = (code) => (rules.find((r) => r.code === code) || {}).name;

  const load = (query) =>
    cbsApi
      .findings({ ...filters, page: query.page, size: query.pageSize })
      .then((d) => {
        setCounts(d.counts);
        setError(null);
        return { data: d.content.map((r) => ({ ...r, ruleName: ruleName(r.ruleCode) })), page: d.page, totalCount: d.totalElements };
      })
      .catch((e) => {
        setError(errorMessage(e, "Could not load data quality findings."));
        return { data: [], page: 0, totalCount: 0 };
      });

  return (
    <>
      <h4 style={{ color: "#014d88" }}>Surveillance Data Quality Monitoring</h4>
      <p className="text-muted">Consolidated review of validation errors, status discrepancies and expected-event gaps.</p>

      <div className="cbs-cards">
        {VIEWS.map((v) => (
          <SummaryCard
            key={v.key}
            value={counts ? Number(counts[v.count]).toLocaleString() : "..."}
            label={v.label}
            active={filters.view === v.key}
            onClick={() => setFilters({ ...filters, view: v.key })}
          />
        ))}
      </div>

      <div className="cbs-filters">
        <div>
          <Label for="m-search">Patient ID</Label>
          <Input id="m-search" value={filters.search} onChange={set("search")} placeholder="Search..." />
        </div>
        <div>
          <Label for="m-category">Issue category</Label>
          <Input id="m-category" type="select" value={filters.category} onChange={set("category")}>
            <option value="">All</option>
            {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
          </Input>
        </div>
        <div>
          <Label for="m-severity">Severity</Label>
          <Input id="m-severity" type="select" value={filters.severity} onChange={set("severity")}>
            <option value="">All</option>
            {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
          </Input>
        </div>
        <div>
          <Label for="m-rule">Validation rule</Label>
          <Input id="m-rule" type="select" value={filters.ruleCode} onChange={set("ruleCode")}>
            <option value="">All</option>
            {rules.map((r) => <option key={r.code} value={r.code}>{r.code} {r.name}</option>)}
          </Input>
        </div>
        <div>
          <Label for="m-status">Resolution</Label>
          <Input id="m-status" type="select" value={filters.issueStatus} onChange={set("issueStatus")}>
            <option value="UNRESOLVED">Unresolved</option>
            <option value="ALL">All</option>
            {Object.keys(ISSUE_STATUS_LABEL).map((s) => <option key={s} value={s}>{ISSUE_STATUS_LABEL[s]}</option>)}
          </Input>
        </div>
        <DateRangeFilter idPrefix="m" from={filters.from} to={filters.to}
                         onChange={(from, to) => setFilters((f) => ({ ...f, from, to }))} />
      </div>

      {error && <div className="cbs-warning error">{error}</div>}

      <MaterialTable
        tableRef={tableRef}
        icons={tableIcons}
        title={VIEWS.find((v) => v.key === filters.view).label}
        data={load}
        localization={{ body: { emptyDataSourceMessage: "No matching records" } }}
        detailPanel={(row) => <Evidence row={row} onOpenPatient={onOpenPatient} onOpenIssue={onOpenIssue} />}
        onRowClick={(e, row, togglePanel) => togglePanel()}
        columns={[
          { title: "Patient ID", field: "patientId" },
          { title: "Issue type", field: "findingType", render: (r) => FINDING_TYPE_LABEL[r.findingType] || r.findingType },
          { title: "Category", field: "category" },
          { title: "Identified issue", field: "description", cellStyle: { maxWidth: 420 } },
          { title: "Severity", field: "severity", render: (r) => <SeverityBadge severity={r.severity} /> },
          { title: "Event date", field: "eventDate", render: (r) => formatDate(r.eventDate) },
          { title: "Identified", field: "dateIdentified", render: (r) => formatDate(r.dateIdentified) },
          { title: "Status", field: "issueStatus", render: (r) => <IssueStatusBadge status={r.issueStatus} /> },
        ]}
        options={{ ...tableOptions, sorting: false }}
      />

      <div style={{ marginTop: 16 }}>
        <Button onClick={() => setShowRules(!showRules)} style={{ backgroundColor: ACCENT, color: "#fff" }}>
          {showRules ? "Hide" : "Show"} validation rules catalogue
        </Button>
      </div>
      {showRules && (
        <table className="table table-bordered" style={{ marginTop: 10, fontSize: 13 }}>
          <thead style={{ backgroundColor: "#014d88", color: "#fff" }}>
            <tr>
              <th>Rule</th>
              <th>Name</th>
              <th>Checks for</th>
              <th>Outcome</th>
              <th>Severity</th>
              <th>Version</th>
              <th>Open</th>
            </tr>
          </thead>
          <tbody>
            {rules.map((r) => (
              <tr key={r.code}>
                <td>{r.code}</td>
                <td>{r.name}</td>
                <td>{r.description}</td>
                <td>{FINDING_TYPE_LABEL[r.findingType]}</td>
                <td><SeverityBadge severity={r.severity} /></td>
                <td>{r.version}</td>
                <td>{Number(r.openFindings).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
};

export default Monitoring;
