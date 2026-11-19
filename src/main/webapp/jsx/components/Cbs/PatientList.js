import React, { forwardRef, useEffect, useRef, useState } from "react";
import MaterialTable from "material-table";
import { Input, Label } from "reactstrap";
import { Button } from "semantic-ui-react";
import ChevronLeft from "@material-ui/icons/ChevronLeft";
import ChevronRight from "@material-ui/icons/ChevronRight";
import FirstPage from "@material-ui/icons/FirstPage";
import LastPage from "@material-ui/icons/LastPage";
import ArrowUpward from "@material-ui/icons/ArrowUpward";
import cbsApi, { errorMessage } from "./cbsApi";
import {
  ACCENT,
  CATEGORIES,
  EVENT_LABEL,
  SEVERITIES,
  TREATMENT_STATUSES,
  formatDate,
  stateForStatus,
  StateLabel,
  tableOptions,
  DateRangeFilter,
} from "./common";

export const tableIcons = {
  FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
  LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
  NextPage: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  PreviousPage: forwardRef((props, ref) => <ChevronLeft {...props} ref={ref} />),
  SortArrow: forwardRef((props, ref) => <ArrowUpward {...props} ref={ref} />),
};

const CARD_LABEL = { flags: "Records with DQR flags", gaps: "Patients with event gaps", discrepancies: "Patients with status discrepancies" };

const PatientList = ({ preset, onOpenPatient }) => {
  const tableRef = useRef();
  const [filters, setFilters] = useState({
    search: "",
    status: preset.status || "",
    category: preset.category || "",
    severity: preset.severity || "",
    card: preset.card || "",
    // only patients with an unresolved finding whose event date is in this range
    from: preset.from || "",
    to: preset.to || "",
  });
  const [error, setError] = useState(null);
  const firstRender = useRef(true);

  // Re-run the server query whenever a filter changes (search waits for the user to stop typing)
  useEffect(() => {
    if (firstRender.current) {
      firstRender.current = false;
      return undefined;
    }
    const timer = setTimeout(() => tableRef.current && tableRef.current.onQueryChange(), 400);
    return () => clearTimeout(timer);
  }, [filters]);

  const set = (key) => (e) => setFilters({ ...filters, [key]: e.target.value });
  const clear = () => setFilters({ search: "", status: "", category: "", severity: "", card: "", from: "", to: "" });
  const hasFilters = Object.values(filters).some((v) => v);

  const load = (query) =>
    cbsApi
      .patients({
        ...filters,
        page: query.page,
        size: query.pageSize,
        sort: query.orderBy ? query.orderBy.field : "patientId",
        direction: query.orderDirection || "asc",
      })
      .then((data) => {
        setError(null);
        return { data: data.content, page: data.page, totalCount: data.totalElements };
      })
      .catch((e) => {
        setError(errorMessage(e, "Could not load the patient list."));
        return { data: [], page: 0, totalCount: 0 };
      });

  return (
    <>
      <h4 style={{ color: "#014d88" }}>Patient Surveillance List</h4>
      <p className="text-muted">Search and select a patient to review longitudinal events and current status.</p>

      <div className="cbs-filters">
        <div>
          <Label for="cbs-search">Patient ID / Hospital number</Label>
          <Input id="cbs-search" value={filters.search} onChange={set("search")} placeholder="Search..." />
        </div>
        <div>
          <Label for="cbs-status">Current status</Label>
          <Input id="cbs-status" type="select" value={filters.status} onChange={set("status")}>
            <option value="">All</option>
            {TREATMENT_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </Input>
        </div>
        <div>
          <Label for="cbs-category">DQ flag category</Label>
          <Input id="cbs-category" type="select" value={filters.category} onChange={set("category")}>
            <option value="">All</option>
            {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
          </Input>
        </div>
        <div>
          <Label for="cbs-severity">Severity</Label>
          <Input id="cbs-severity" type="select" value={filters.severity} onChange={set("severity")}>
            <option value="">All</option>
            {SEVERITIES.map((s) => <option key={s} value={s}>{s}</option>)}
          </Input>
        </div>
        <div>
          <Label for="cbs-card">Show</Label>
          <Input id="cbs-card" type="select" value={filters.card} onChange={set("card")}>
            <option value="">All patients</option>
            {Object.keys(CARD_LABEL).map((k) => <option key={k} value={k}>{CARD_LABEL[k]}</option>)}
          </Input>
        </div>
        <DateRangeFilter idPrefix="cbs" from={filters.from} to={filters.to}
                         onChange={(from, to) => setFilters((f) => ({ ...f, from, to }))} />
        <div>
          <Button disabled={!hasFilters} onClick={clear} style={{ backgroundColor: ACCENT, color: "#fff" }}>
            Clear filters
          </Button>
        </div>
      </div>

      {error && <div className="cbs-warning error">{error}</div>}

      <MaterialTable
        tableRef={tableRef}
        icons={tableIcons}
        title="Patient surveillance records"
        data={load}
        onRowClick={(e, row) => onOpenPatient(row.personUuid)}
        localization={{ body: { emptyDataSourceMessage: "No matching records" } }}
        columns={[
          { title: "Patient ID", field: "patientId", render: (r) => <button type="button" className="cbs-link">{r.patientId || "-"}</button> },
          { title: "Hospital No.", field: "hospitalNumber", sorting: false },
          { title: "Facility", field: "facilityName", sorting: false },
          {
            title: "Current status",
            field: "treatmentStatus",
            render: (r) => <StateLabel state={stateForStatus(r.treatmentStatus)} label={r.treatmentStatus || "-"} />,
          },
          {
            title: "Latest event",
            field: "latestEventDate",
            render: (r) => (r.latestEventDate ? `${EVENT_LABEL[r.latestEventType] || r.latestEventType}, ${formatDate(r.latestEventDate)}` : "-"),
          },
          { title: "Open DQ flags", field: "openFlags" },
          { title: "Event gaps", field: "openGaps", sorting: false },
          { title: "Last refresh", field: "asOfDate", sorting: false, render: (r) => formatDate(r.asOfDate) },
        ]}
        options={{ ...tableOptions, sorting: true }}
      />
      <div className="cbs-context" style={{ marginTop: 10 }}>
        Select a patient to open the Patient Surveillance Event Tracker.
      </div>
    </>
  );
};

export default PatientList;
