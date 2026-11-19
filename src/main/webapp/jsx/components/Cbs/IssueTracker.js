import React, { useEffect, useRef, useState } from "react";
import MaterialTable from "material-table";
import { Input, Label } from "reactstrap";
import cbsApi, { errorMessage } from "./cbsApi";
import { tableIcons } from "./PatientList";
import {
  CATEGORIES,
  ISSUE_STATUS_LABEL,
  IssueStatusBadge,
  SEVERITIES,
  SeverityBadge,
  SummaryCard,
  formatDate,
  tableOptions,
  DateRangeFilter,
} from "./common";

// Each card sets the filters that reproduce its number
const CARDS = [
  { key: "open", label: "Open", filters: { status: "OPEN_GROUP", flag: "" }, accent: "#c62828" },
  { key: "underReview", label: "Under Review", filters: { status: "REVIEW_GROUP", flag: "" }, accent: "#ed6c02" },
  { key: "overdue", label: "Overdue", filters: { status: "", flag: "overdue" }, accent: "#c62828" },
  { key: "resolved", label: "Resolved", filters: { status: "RESOLVED", flag: "" }, accent: "#2e7d32" },
];

const IssueTracker = ({ preset, onOpenIssue }) => {
  const tableRef = useRef();
  const firstRender = useRef(true);
  const [summary, setSummary] = useState(null);
  const [users, setUsers] = useState([]);
  const [error, setError] = useState(null);
  const [filters, setFilters] = useState({
    status: preset.status || "UNRESOLVED",
    assignedTo: "",
    severity: preset.severity || "",
    category: "",
    flag: "",
    search: "",
    // event date of the problem behind the issue (the refill, ART start, death...)
    from: preset.from || "",
    to: preset.to || "",
  });

  useEffect(() => {
    cbsApi.users().then(setUsers).catch(() => setUsers([]));
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

  const load = (query) =>
    cbsApi
      .issues({
        ...filters,
        page: query.page,
        size: query.pageSize,
        sort: query.orderBy ? query.orderBy.field : "id",
        direction: query.orderDirection || "desc",
      })
      .then((d) => {
        setSummary(d.summary);
        setError(null);
        return { data: d.content, page: d.page, totalCount: d.totalElements };
      })
      .catch((e) => {
        setError(errorMessage(e, "Could not load issues."));
        return { data: [], page: 0, totalCount: 0 };
      });

  return (
    <>
      <h4 style={{ color: "#014d88" }}>Data Quality Issue Tracker</h4>
      <p className="text-muted">Assign, investigate, document corrective actions, revalidate and resolve issues.</p>

      <div className="cbs-cards">
        {CARDS.map((c) => (
          <SummaryCard
            key={c.key}
            value={summary ? Number(summary[c.key]).toLocaleString() : "..."}
            label={c.label}
            accent={c.accent}
            active={filters.status === c.filters.status && filters.flag === c.filters.flag}
            onClick={() => setFilters({ ...filters, ...c.filters })}
          />
        ))}
      </div>
      {summary && (
        <div className="cbs-context">
          <span>Unassigned: <strong>{Number(summary.unassigned).toLocaleString()}</strong></span>
          <span>Assigned: <strong>{Number(summary.assigned).toLocaleString()}</strong></span>
          <span>Escalated: <strong>{Number(summary.escalated).toLocaleString()}</strong></span>
          <span>Approved exceptions: <strong>{Number(summary.approvedExceptions).toLocaleString()}</strong></span>
        </div>
      )}

      <div className="cbs-filters">
        <div>
          <Label for="i-search">Issue ID / Patient ID</Label>
          <Input id="i-search" value={filters.search} onChange={set("search")} placeholder="e.g. DQ-1001" />
        </div>
        <div>
          <Label for="i-assigned">Assigned to</Label>
          <Input id="i-assigned" type="select" value={filters.assignedTo} onChange={set("assignedTo")}>
            <option value="">All</option>
            <option value="UNASSIGNED">Unassigned</option>
            {users.map((u) => <option key={u.userName} value={u.userName}>{u.fullName || u.userName}</option>)}
          </Input>
        </div>
        <div>
          <Label for="i-status">Issue status</Label>
          <Input id="i-status" type="select" value={filters.status} onChange={set("status")}>
            <option value="UNRESOLVED">All unresolved</option>
            <option value="">All</option>
            {Object.keys(ISSUE_STATUS_LABEL).map((s) => <option key={s} value={s}>{ISSUE_STATUS_LABEL[s]}</option>)}
          </Input>
        </div>
        <div>
          <Label for="i-flag">Escalation</Label>
          <Input id="i-flag" type="select" value={filters.flag} onChange={set("flag")}>
            <option value="">Any</option>
            <option value="overdue">Overdue only</option>
            <option value="escalated">Escalated only</option>
          </Input>
        </div>
        <div>
          <Label for="i-severity">Priority</Label>
          <Input id="i-severity" type="select" value={filters.severity} onChange={set("severity")}>
            <option value="">All</option>
            {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
          </Input>
        </div>
        <div>
          <Label for="i-category">Issue category</Label>
          <Input id="i-category" type="select" value={filters.category} onChange={set("category")}>
            <option value="">All</option>
            {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
          </Input>
        </div>
        <DateRangeFilter idPrefix="i" from={filters.from} to={filters.to}
                         onChange={(from, to) => setFilters((f) => ({ ...f, from, to }))} />
      </div>

      {error && <div className="cbs-warning error">{error}</div>}

      <MaterialTable
        tableRef={tableRef}
        icons={tableIcons}
        title="Issues"
        data={load}
        onRowClick={(e, row) => onOpenIssue(row.id)}
        localization={{ body: { emptyDataSourceMessage: "No matching records" } }}
        columns={[
          { title: "Issue ID", field: "id", render: (r) => <button type="button" className="cbs-link">DQ-{r.id}</button> },
          { title: "Patient ID", field: "patientId", sorting: false },
          { title: "Issue", field: "description", sorting: false, cellStyle: { maxWidth: 380 } },
          { title: "Assigned to", field: "assignedTo", sorting: false, render: (r) => r.assignedTo || <span className="text-muted">Unassigned</span> },
          { title: "Event date", field: "eventDate", sorting: false, render: (r) => formatDate(r.eventDate) },
          { title: "Priority", field: "severity", render: (r) => <SeverityBadge severity={r.severity} /> },
          {
            title: "Due",
            field: "dueDate",
            render: (r) => (
              <span style={{ color: r.overdue ? "#c62828" : undefined, fontWeight: r.overdue ? "bold" : undefined }}>
                {formatDate(r.dueDate)} {r.overdue ? "(Overdue)" : ""}
              </span>
            ),
          },
          {
            title: "Status",
            field: "status",
            render: (r) => (
              <>
                <IssueStatusBadge status={r.status} /> {r.escalated && <span className="cbs-badge" style={{ backgroundColor: "#6a1b9a" }}>Escalated</span>}
              </>
            ),
          },
        ]}
        options={{ ...tableOptions, sorting: true }}
      />
    </>
  );
};

export default IssueTracker;
