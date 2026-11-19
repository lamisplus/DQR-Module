package org.lamisplus.modules.dqr.repository.cbs;

/** SQL fragments shared by the CBS repositories so "unresolved" means the same thing everywhere. */
final class CbsSqlParts {
    private CbsSqlParts() {
    }

    // A finding counts as unresolved until its issue is resolved or accepted as an exception
    static final String UNRESOLVED = " COALESCE(i.status, '') NOT IN ('RESOLVED', 'APPROVED_EXCEPTION') ";
    static final String FINDING_JOINS = " FROM cbs_finding f LEFT JOIN cbs_issue i ON i.finding_id = f.id " +
            "LEFT JOIN cbs_patient_summary s ON s.facility_id = f.facility_id AND s.person_uuid = f.person_uuid ";
    static final String ISSUE_JOINS = " FROM cbs_issue i JOIN cbs_finding f ON f.id = i.finding_id " +
            "LEFT JOIN cbs_patient_summary s ON s.facility_id = i.facility_id AND s.person_uuid = i.person_uuid ";
    // CBS-DQ-04 (documented vs derived status) is shown on its own as "Status Discrepancies", so it is left out of validation errors
    static final String VIEW_FILTER = " AND (CAST(:view AS VARCHAR) IS NULL OR CAST(:view AS VARCHAR) = 'all' " +
            "OR (CAST(:view AS VARCHAR) = 'validation' AND f.finding_type IN ('VALIDATION_ERROR', 'POSSIBLE_DISCREPANCY') AND f.rule_code <> 'CBS-DQ-04') " +
            "OR (CAST(:view AS VARCHAR) = 'gaps' AND f.finding_type = 'MISSING_EXPECTED_EVENT') " +
            "OR (CAST(:view AS VARCHAR) = 'status' AND f.rule_code = 'CBS-DQ-04')) ";
}
