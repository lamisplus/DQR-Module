export const  token = (new URLSearchParams(window.location.search)).get("jwt")
export const url = '/api/v1/'
// export const url = "http://localhost:8989/api/v1/";
// export const token =
//   "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJndWVzdEBsYW1pc3BsdXMub3JnIiwiYXV0aCI6IlN1cGVyIEFkbWluIiwibmFtZSI6Ikd1ZXN0IEd1ZXN0IiwiZXhwIjoxNzA1NzM5OTUyfQ.3F3Z7TC73AhraDmIw4bv2pzzk0Q54F_jkqRzpCQxOUkHBQ4Z3BrFfxJejZvmjakoilIz8G6RZ8x6gHet5IMa7w"