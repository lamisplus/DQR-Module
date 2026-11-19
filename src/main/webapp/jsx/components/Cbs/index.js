import React, { useCallback, useEffect, useRef, useState } from "react";
import { Card, CardContent } from "@material-ui/core";
import { Button } from "semantic-ui-react";
import { toast } from "react-toastify";
import { FaSyncAlt } from "react-icons/fa";
import cbsApi, { errorMessage } from "./cbsApi";
import { ACCENT, formatDate, formatDateTime } from "./common";
import Overview from "./Overview";
import PatientList from "./PatientList";
import PatientTracker from "./PatientTracker";
import Monitoring from "./Monitoring";
import IssueTracker from "./IssueTracker";
import IssueDetail from "./IssueDetail";
import Reports from "./Reports";

const NAV = [
  { key: "overview", label: "Overview Dashboard" },
  { key: "patients", label: "Patient Surveillance List" },
  { key: "monitoring", label: "Surveillance Data Quality Monitoring" },
  { key: "issues", label: "Data Quality Issue Tracker" },
  { key: "reports", label: "Reports & Exports" },
];

/**
 * HIV Case-Based Surveillance Monitoring. One component switches between the screens so
 * the existing DQR tab keeps working exactly as before and nothing needs a new route.
 */
const Cbs = () => {
  const [view, setView] = useState("overview");
  const [overview, setOverview] = useState(null);
  const [permissions, setPermissions] = useState({});
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  // filters handed from a dashboard card to the list it opens
  const [preset, setPreset] = useState({});
  const [patient, setPatient] = useState(null); // { personUuid, from }
  const [issue, setIssue] = useState(null); // { id, from }
  const pollRef = useRef(null);

  const loadOverview = useCallback(() => {
    return cbsApi
      .overview()
      .then((data) => {
        setOverview(data);
        setLoadError(null);
        return data;
      })
      .catch((error) => {
        setLoadError(errorMessage(error, "Could not load surveillance data."));
        return null;
      })
      .finally(() => setLoading(false));
  }, []);

  const stopPolling = () => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  };

  // While a refresh runs on the server, check every few seconds and reload once it finishes
  const startPolling = useCallback(() => {
    stopPolling();
    pollRef.current = setInterval(() => {
      cbsApi
        .refreshStatus()
        .then((status) => {
          if (!status.processing) {
            stopPolling();
            loadOverview().then((data) => {
              if (data && data.latestRunFailed) toast.error("The data refresh failed. The previous results are still shown.");
              else toast.success("Surveillance data refreshed.");
            });
          }
        })
        .catch(() => stopPolling());
    }, 4000);
  }, [loadOverview]);

  useEffect(() => {
    cbsApi.permissions().then(setPermissions).catch(() => setPermissions({}));
    loadOverview().then((data) => {
      if (data && data.processing) startPolling();
    });
    return stopPolling;
  }, [loadOverview, startPolling]);

  const refresh = () => {
    cbsApi
      .refresh()
      .then((data) => {
        setOverview((old) => ({ ...(old || {}), ...data, processing: true }));
        toast.info(data.started ? "Refreshing surveillance data. This can take a minute." : "A refresh is already running.");
        startPolling();
      })
      .catch((error) => toast.error(errorMessage(error)));
  };

  const go = (key, filters = {}) => {
    setPreset(filters);
    setView(key);
    window.scrollTo(0, 0);
  };

  const openPatient = (personUuid, from) => {
    setPatient({ personUuid, from: from || view });
    setView("patient");
    window.scrollTo(0, 0);
  };

  const openIssue = (id, from) => {
    setIssue({ id, from: from || view });
    setView("issue");
    window.scrollTo(0, 0);
  };

  const processing = overview && overview.processing;
  const lastRefresh = overview && overview.lastSuccessfulRefresh;
  const activeNav = view === "patient" ? "patients" : view === "issue" ? "issues" : view;

  if (loading) return <div className="cbs-empty">Loading surveillance data...</div>;
  if (loadError && !overview) return <div className="cbs-warning error">{loadError}</div>;

  return (
    <Card className="cbs">
      <CardContent>
        <h2>HIV Case-Based Surveillance Monitoring</h2>

        <div className="cbs-context">
          <span>
            Facility: <strong>{overview.facilityName || "-"}</strong>
          </span>
          <span>
            As-of date: <strong>{formatDate(overview.asOfDate)}</strong>
          </span>
          <span>
            Last successful refresh: <strong>{lastRefresh ? formatDateTime(lastRefresh.finishedAt) : "Never"}</strong>
          </span>
          <Button
            className="cbs-refresh-btn"
            style={{ backgroundColor: ACCENT, color: "#fff" }}
            onClick={refresh}
            disabled={processing}
            loading={processing}
          >
            <FaSyncAlt /> {processing ? "Refreshing..." : "Refresh data"}
          </Button>
        </div>

        {processing && (
          <div className="cbs-warning">Data refresh in progress. Figures update automatically when it finishes.</div>
        )}
        {!processing && !lastRefresh && (
          <div className="cbs-warning">
            Surveillance data has not been processed for this facility yet. Click <strong>Refresh data</strong> to run it now;
            it also runs automatically every night.
          </div>
        )}
        {overview.stale && !processing && (
          <div className="cbs-warning">
            Stale data: the last successful refresh was on {formatDateTime(lastRefresh.finishedAt)}. Figures may be out of date.
          </div>
        )}
        {overview.latestRunFailed && !processing && (
          <div className="cbs-warning error">
            The latest refresh failed{overview.latestRun && overview.latestRun.errorMessage ? `: ${overview.latestRun.errorMessage}` : ""}.
            The results shown are from the last successful refresh.
          </div>
        )}

        <div className="cbs-subnav" role="tablist">
          {NAV.map((n) => (
            <button
              key={n.key}
              type="button"
              role="tab"
              aria-selected={activeNav === n.key}
              className={activeNav === n.key ? "active" : ""}
              onClick={() => go(n.key)}
            >
              {n.label}
            </button>
          ))}
        </div>

        {view === "overview" && <Overview overview={overview} onNavigate={go} />}
        {view === "patients" && (
          <PatientList preset={preset} permissions={permissions} onOpenPatient={(id) => openPatient(id, "patients")} />
        )}
        {view === "patient" && patient && (
          <PatientTracker
            personUuid={patient.personUuid}
            onBack={() => setView(patient.from === "patient" ? "patients" : patient.from)}
            onOpenIssue={(id) => openIssue(id, "patient")}
          />
        )}
        {view === "monitoring" && (
          <Monitoring
            preset={preset}
            onOpenPatient={(id) => openPatient(id, "monitoring")}
            onOpenIssue={(id) => openIssue(id, "monitoring")}
          />
        )}
        {view === "issues" && (
          <IssueTracker preset={preset} onOpenIssue={(id) => openIssue(id, "issues")} />
        )}
        {view === "issue" && issue && (
          <IssueDetail
            issueId={issue.id}
            onBack={() => setView(issue.from === "patient" ? "patient" : issue.from)}
            onOpenPatient={(id) => openPatient(id, "issue")}
            permissions={permissions}
            onChanged={loadOverview}
          />
        )}
        {view === "reports" && <Reports permissions={permissions} onNavigate={go} />}
      </CardContent>
    </Card>
  );
};

export default Cbs;
