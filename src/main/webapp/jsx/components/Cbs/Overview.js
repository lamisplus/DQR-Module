import React, { useEffect, useState } from "react";
import { FaChevronRight } from "react-icons/fa";
import cbsApi, { errorMessage } from "./cbsApi";
import { DateRangeFilter, SummaryCard, cardValue, PRIMARY } from "./common";

const COMPONENTS = [
  { key: "patients", title: "Patient Surveillance List", text: "Search patients and review longitudinal events, timelines and current status" },
  { key: "monitoring", title: "Surveillance Data Quality Monitoring", text: "Review validation errors, discrepancies and missing expected events" },
  { key: "issues", title: "Data Quality Issue Tracker", text: "Assign, investigate, revalidate and resolve identified issues" },
  { key: "reports", title: "Reports & Exports", text: "View surveillance monitoring reports and generate authorized exports" },
];

/** Landing page: four summary cards, each opening the list behind its number, and the four components. */
const Overview = ({ overview, onNavigate }) => {
  // "Event date from / to": the flag, gap and discrepancy cards then only count findings whose
  // event (the record the problem is about) is dated in the range. The range is handed on to
  // whichever list a card opens.
  const [range, setRange] = useState({ from: "", to: "" });
  const [periodCards, setPeriodCards] = useState(null);
  const [periodError, setPeriodError] = useState(null);
  const hasRange = Boolean(range.from || range.to);

  useEffect(() => {
    if (!hasRange || !overview.cards) {
      setPeriodCards(null);
      setPeriodError(null);
      return undefined;
    }
    let cancelled = false;
    setPeriodCards(null);
    cbsApi
      .overview(range)
      .then((d) => {
        if (!cancelled) {
          setPeriodCards(d.cards || {});
          setPeriodError(null);
        }
      })
      .catch((e) => !cancelled && setPeriodError(errorMessage(e, "Could not load the counts for this period.")));
    return () => {
      cancelled = true;
    };
    // overview changes after a refresh, so the period counts are reloaded too
  }, [range, hasRange, overview]);

  const cards = hasRange ? periodCards || {} : overview.cards || {};
  const processing = overview.processing;
  const loadingPeriod = hasRange && !periodCards && !periodError;
  const value = (v) => (loadingPeriod ? "..." : cardValue(v, processing));
  const go = (screen, preset = {}) => onNavigate(screen, hasRange ? { ...preset, from: range.from, to: range.to } : preset);

  return (
    <>
      <div className="cbs-filters">
        <DateRangeFilter idPrefix="o" from={range.from} to={range.to} onChange={(from, to) => setRange({ from, to })} />
        {hasRange && (
          <div>
            <button type="button" className="btn btn-link p-0" onClick={() => setRange({ from: "", to: "" })}>
              Clear dates
            </button>
          </div>
        )}
      </div>
      {hasRange && (
        <p className="text-muted" style={{ marginTop: -6 }}>
          Flags, event gaps, status discrepancies and High severity issues count only problems whose event (the refill,
          ART start, death, lab test...) is dated in this period. Clients assessed counts the clients with any record
          dated in this period.
        </p>
      )}
      {periodError && <div className="cbs-warning error">{periodError}</div>}

      <div className="cbs-cards">
        <SummaryCard
          value={value(cards.clientsAssessed)}
          label="Clients Assessed"
          help={hasRange
            ? "Patients in the CBS list with any record dated in this period (enrolment, ART start, pickup, visit, lab, EAC, tracking)"
            : "Patients whose records were checked in the last refresh (not clinical assessments)"}
          onClick={overview.cards ? () => onNavigate("patients") : undefined}
        />
        <SummaryCard
          value={value(cards.recordsWithFlags)}
          label="Records with DQR Flags"
          help="Patients with at least one unresolved data quality flag"
          accent="#ed6c02"
          onClick={overview.cards ? () => go("patients", { card: "flags" }) : undefined}
        />
        <SummaryCard
          value={value(cards.eventGaps)}
          label="Event Gaps Detected"
          help="Unresolved missing expected events"
          accent="#ed6c02"
          onClick={overview.cards ? () => go("monitoring", { view: "gaps" }) : undefined}
        />
        <SummaryCard
          value={value(cards.statusDiscrepancies)}
          label="Status Discrepancies"
          help="Patients whose recorded and derived treatment status differ"
          accent="#c62828"
          onClick={overview.cards ? () => go("monitoring", { view: "status" }) : undefined}
        />
      </div>

      {overview.cards && !loadingPeriod && cards.highPriorityUnresolved > 0 && (
        <div className="cbs-warning error" role="button" style={{ cursor: "pointer" }}
             onClick={() => go("issues", { severity: "High", status: "UNRESOLVED" })}>
          <strong>{Number(cards.highPriorityUnresolved).toLocaleString()}</strong> unresolved High severity issues{hasRange ? " dated in this period" : ""} need attention.
          Click to view them in the Issue Tracker.
        </div>
      )}

      <h4 style={{ color: PRIMARY, marginTop: 10 }}>Surveillance monitoring components</h4>
      <div className="cbs-components">
        {COMPONENTS.map((c, i) => (
          <div
            key={c.key}
            className="cbs-component"
            role="button"
            tabIndex={0}
            onClick={() => onNavigate(c.key)}
            onKeyPress={(e) => e.key === "Enter" && onNavigate(c.key)}
          >
            <div className="cbs-component-number">{String(i + 1).padStart(2, "0")}</div>
            <div>
              <div className="cbs-component-title">{c.title}</div>
              <div className="cbs-component-text">{c.text}</div>
            </div>
            <FaChevronRight className="cbs-component-arrow" />
          </div>
        ))}
      </div>

      {overview.treatmentStatusBreakdown && overview.treatmentStatusBreakdown.length > 0 && (
        <>
          <h4 style={{ color: PRIMARY, marginTop: 18 }}>Treatment status (as of last refresh)</h4>
          <div className="cbs-cards">
            {overview.treatmentStatusBreakdown.map((row) => (
              <SummaryCard
                key={row.name || "none"}
                value={Number(row.count).toLocaleString()}
                label={row.name || "Not determined"}
                onClick={() => onNavigate("patients", { status: row.name })}
              />
            ))}
          </div>
        </>
      )}
    </>
  );
};

export default Overview;
