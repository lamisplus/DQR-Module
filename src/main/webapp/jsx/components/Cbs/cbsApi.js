import axios from "axios";
import { token, url as baseUrl } from "../../../api";

const http = axios.create({
  baseURL: `${baseUrl}dqr/cbs`,
  headers: { Authorization: `Bearer ${token}` },
});

// Turns Core's error response ({ apierror: { message, subErrors } }) into one readable sentence
// for the toast messages. Validation errors say "Validation error" with the real reason in subErrors.
export const errorMessage = (error, fallback = "Something went wrong. Please try again.") => {
  const response = error && error.response;
  if (response && response.status === 403) return "You do not have permission for this action.";
  const body = response && response.data;
  const data = body && body.apierror ? body.apierror : body;
  if (data && data.subErrors && data.subErrors.length > 0 && data.subErrors[0].message) return data.subErrors[0].message;
  if (data && data.message) return data.message;
  return fallback;
};

const clean = (params) => {
  const out = {};
  Object.keys(params || {}).forEach((key) => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== "") out[key] = params[key];
  });
  return out;
};

const cbsApi = {
  permissions: () => http.get("/permissions").then((r) => r.data),
  overview: (params) => http.get("/overview", { params: clean(params) }).then((r) => r.data),
  refresh: () => http.post("/refresh").then((r) => r.data),
  refreshStatus: () => http.get("/refresh/status").then((r) => r.data),
  patients: (params) => http.get("/patients", { params: clean(params) }).then((r) => r.data),
  patient: (personUuid, asOf) => http.get(`/patients/${personUuid}`, { params: clean({ asOf }) }).then((r) => r.data),
  findings: (params) => http.get("/findings", { params: clean(params) }).then((r) => r.data),
  rules: () => http.get("/rules").then((r) => r.data),
  issues: (params) => http.get("/issues", { params: clean(params) }).then((r) => r.data),
  issue: (id) => http.get(`/issues/${id}`).then((r) => r.data),
  users: () => http.get("/users").then((r) => r.data),
  issueAction: (id, action, body = {}) => http.post(`/issues/${id}/${action}`, body).then((r) => r.data),
  reportSummary: (params) => http.get("/reports/summary", { params: clean(params) }).then((r) => r.data),
  exportReport: (params) =>
    http.get("/reports/export", { params: clean(params), responseType: "blob" }).then((r) => r.data),
};

export default cbsApi;
