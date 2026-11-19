import React, { useCallback, useEffect, useState } from "react";
import { Button } from "semantic-ui-react";
import { toast } from "react-toastify";
import { FaFileDownload } from "react-icons/fa";
import cbsApi, { errorMessage } from "./cbsApi";
import { ACCENT, DateRangeFilter, ISSUE_STATUS_LABEL, PRIMARY, SeverityBadge, SummaryCard, formatDateTime } from "./common";

const INDICATORS = [
  ["clientsAssessed", "Clients Assessed", PRIMARY],
  ["patientsWithOpenFlags", "Patients with Open DQ Flags", "#ed6c02"],
  ["missingExpectedEvents", "Missing Expected Surveillance Events", "#ed6c02"],
  ["statusDiscrepancies", "Status Discrepancies", "#c62828"],
  ["resolvedIssues", "Resolved DQ Issues", "#2e7d32"],
  ["approvedExceptions", "Approved Exceptions", "#6c757d"],
  ["overdueIssues", "Overdue Corrective-Action Issues", "#c62828"],
];

const REPORTS = [
  ["summary", "CBS Data Quality Summary", "Indicators by issue category and severity"],
  ["outstanding", "Outstanding Issues Report", "Open, overdue and escalated issues with assignment status"],
  ["corrective", "Corrective Action & Resolution Report", "All issues with their outcome and resolution date"],
  ["patients", "Patient Surveillance List", "Every assessed patient with status, open flags and gaps"],
];

const Reports = ({ permissions, onNavigate }) => {
  const [period, setPeriod] = useState({ from: "", to: "" });
  const [data, setData] = useState(null);
  const [downloading, setDownloading] = useState(null);

  const load = useCallback(() => {
    cbsApi
      .reportSummary(period)
      .then(setData)
      .catch((e) => toast.error(errorMessage(e, "Could not load the report.")));
  }, [period]);

  useEffect(load, [load]);

  const download = (report) => {
    setDownloading(report);
    cbsApi
      .exportReport({ report, ...period })
      .then((blob) => {
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = `cbs-${report}-${new Date().toISOString().slice(0, 10)}.csv`;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(link.href);
      })
      .catch((e) => toast.error(errorMessage(e, "The export failed. No file was created.")))
      .finally(() => setDownloading(null));
  };

  if (!data) return <div className="cbs-empty">Loading report...</div>;

  const perms = permissions || {};
  const ind = data.indicators || {};
  const hasData = !!data.lastSuccessfulRefresh;

  return (
    <>
      <h4 style={{ color: PRIMARY }}>Reports & Exports</h4>
      <p className="text-muted">Surveillance monitoring reports for your facility and permitted data exports.</p>

      <div className="cbs-filters">
        <DateRangeFilter idPrefix="r" from={period.from} to={period.to} onChange={(from, to) => setPeriod({ from, to })}
                         fromLabel="Reporting period from" toLabel="Reporting period to" />
        <div className="text-muted" style={{ fontSize: 13 }}>
          The period applies to resolved issues, exceptions and exports. Other figures are as of the last refresh
          ({data.lastSuccessfulRefresh ? formatDateTime(data.lastSuccessfulRefresh.finishedAt) : "not available"}).
        </div>
      </div>

      {!hasData && <div className="cbs-warning">No data has been processed yet, so figures are not available.</div>}

      <div className="cbs-cards">
        {INDICATORS.map(([key, label, accent]) => (
          <SummaryCard
            key={key}
            value={hasData ? Number(ind[key]).toLocaleString() : "Not available"}
            label={label}
            help={data.definitions && data.definitions[key]}
            accent={accent}
          />
        ))}
      </div>

      <div className="card">
        <div className="card-header cbs-section-header">
          <h5 className="card-title">Unresolved findings by category and severity</h5>
        </div>
        <div className="card-body">
          {data.byCategory.length === 0 ? (
            <div className="cbs-empty">No matching records found.</div>
          ) : (
            <table className="table table-bordered" style={{ fontSize: 13 }}>
              <thead>
                <tr><th>Category</th><th>Severity</th><th>Unresolved findings</th><th>Patients</th></tr>
              </thead>
              <tbody>
                {data.byCategory.map((r) => (
                  <tr key={`${r.name}-${r.severity}`} style={{ cursor: "pointer" }}
                      onClick={() => onNavigate("monitoring", { category: r.name, severity: r.severity })}>
                    <td>{r.name}</td>
                    <td><SeverityBadge severity={r.severity} /></td>
                    <td>{Number(r.unresolved).toLocaleString()}</td>
                    <td>{Number(r.patients).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          <div className="text-muted" style={{ fontSize: 12 }}>Click a row to see the patients behind it.</div>
        </div>
      </div>

      <div className="card">
        <div className="card-header cbs-section-header">
          <h5 className="card-title">Issues by status</h5>
        </div>
        <div className="card-body">
          <table className="table table-bordered" style={{ fontSize: 13, maxWidth: 500 }}>
            <tbody>
              {data.issueStatus.map((r) => (
                <tr key={r.name}><td>{ISSUE_STATUS_LABEL[r.name] || r.name}</td><td>{Number(r.count).toLocaleString()}</td></tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <div className="card-header cbs-section-header">
          <h5 className="card-title">Available reports</h5>
        </div>
        <div className="card-body">
          {!perms.export && <div className="cbs-warning">You can view reports but do not have permission to export them.</div>}
          <table className="table" style={{ fontSize: 14 }}>
            <tbody>
              {REPORTS.map(([key, title, text]) => (
                <tr key={key}>
                  <td><strong style={{ color: PRIMARY }}>{title}</strong><div className="text-muted">{text}</div></td>
                  <td style={{ textAlign: "right", verticalAlign: "middle" }}>
                    {perms.export && (
                      <Button style={{ backgroundColor: ACCENT, color: "#fff" }} loading={downloading === key}
                              disabled={!!downloading || !hasData} onClick={() => download(key)}>
                        <FaFileDownload /> Export CSV
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="text-muted" style={{ fontSize: 12 }}>
            Every export is recorded with your name, the report and the time. Patient IDs are included only if you are allowed to view patient details.
          </div>
        </div>
      </div>
    </>
  );
};

export default Reports;
