export const  token = (new URLSearchParams(window.location.search)).get("jwt")
export const url = '/api/v1/'
// export const url = "http://localhost:8383/api/v1/";
// export const token =
//   "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJndWVzdEBsYW1pc3BsdXMub3JnIiwiYXV0aCI6IlN1cGVyIEFkbWluIiwibmFtZSI6Ikd1ZXN0IEd1ZXN0IiwiZXhwIjoxNzE1ODg0MzI0fQ.0A_DyuRzFtizpE6yYLs9LTe_7gKGTHOhr_hreMW48gbcamEUufxCUx8n5P9W-pv3_s2WybJyLPZ6AQ5Ubdoqog"