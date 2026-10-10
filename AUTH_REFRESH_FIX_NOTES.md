# EventHub authentication refresh fix notes

## Changes made

- Frontend startup and Axios recovery now use one shared `refreshSession()` operation, preventing duplicate refresh calls within the same tab (including React StrictMode development effects).
- Where supported, the browser Web Locks API serializes refresh calls across tabs so a waiting tab uses the newly rotated HttpOnly cookie.
- The Axios interceptor retries eligible protected requests on HTTP 401, but does not recursively refresh login, registration, refresh, or logout calls. `/api/auth/me` can now participate in normal refresh recovery.
- Spring Security returns HTTP 401 for unauthenticated protected requests, allowing the frontend's refresh interceptor to run after access-token expiry. `/api/auth/me` is explicitly protected before the broad `/api/auth/**` public matcher.
- Refresh rejection logging now records a safe reason code without logging token values or hashes.
- Refresh rejection transactions no longer roll back the intended revoked-token/audit changes when `BadCredentialsException` is thrown.
- Login no longer revokes every other active device session. It cleans up expired tokens and revoked records older than one day instead.

## Verification status

- Passed: `node --check frontend/src/services/api.js`.
- Passed: frontend `package.json` JSON parsing and backend `pom.xml` XML parsing.
- Not verified: full Vite production build and Maven tests. Package/dependency retrieval was unavailable in this environment (npm registry DNS failures and Maven Central 403), so no claim of a successful compile or end-to-end run is made.
- No database migration was added; existing schema is reused.

## Manual verification after replacing the project

1. The archive intentionally excludes the real `.env` file to avoid distributing database credentials and JWT secrets. Copy `.env.example` to `.env` and fill in your own local database password and a random JWT secret of at least 32 characters.
2. Stop any old EventHub backend process before starting the updated one.
3. Delete the stale `eventhub_refresh` cookie for `localhost` and sign in again.
4. Confirm login returns 200 and sets an HttpOnly `eventhub_refresh` cookie.
5. Reload the page. Concurrent startup effects should share one refresh request; expect a successful refresh when the cookie is valid.
6. Leave the app open until the access token expires, then visit a protected route. The protected API should return 401, refresh should run, and the original request should retry.
7. Confirm `GET /api/events` remains public and works while logged out and for STUDENT, ORGANIZER, and ADMIN accounts. Confirm organizer/admin protected actions remain protected.
8. Confirm logout revokes the refresh token and clears the cookie.
