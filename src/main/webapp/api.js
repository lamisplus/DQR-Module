export const  token = (new URLSearchParams(window.location.search)).get("jwt")
export const url = '/api/v1/'
// export const url = "http://localhost:8383/api/v1/";
// export const token =
//   "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJndWVzdEBsYW1pc3BsdXMub3JnIiwiYXV0aCI6IlN1cGVyIEFkbWluIiwibmFtZSI6Ikd1ZXN0IEd1ZXN0IiwiZXhwIjoxNzEzOTgzMDY0fQ.1lVOdt5-m6QGVuT7Dur1G_T4vGIDQDoy1-FHOctGM0Yvhs2DGPNPCiTeSv4BrjBlTURNQ-luJ4ZODui2MmbzdA"