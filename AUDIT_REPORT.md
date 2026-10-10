# EventHub Full Integration Audit Report

## Result

The Phase 7 package was inspected across backend source, frontend source, database migrations, REST mappings, Docker configuration, CI configuration, and environment configuration.

## Defects found and fixed

| Area | Defect | Fix |
|---|---|---|
| Backend / Certificates | `CertificateService` contained physical newlines inside Java string literals, causing compilation failure | Replaced with escaped `\\n` strings |
| Authentication API | Specification called for `GET /api/auth/me`, but endpoint was missing | Added endpoint and service method |
| Authentication concurrency | Concurrent failed logins could race on the same user record | Added pessimistic user-row lock during login |
| Refresh-token rotation | Concurrent refresh calls could reuse the same token before revocation was committed | Added pessimistic refresh-token row lock |
| JWT security | Temporary account lockout did not invalidate already-issued access tokens | JWT validation now checks `lockedUntil` |
| Public event discovery | DRAFT events could be returned from the public `/api/events` endpoint | Public search excludes DRAFT events |
| Draft event details | Anonymous users could fetch draft event details | Draft details require owning organizer/admin |
| Attendance | A valid ticket could be checked in outside the event lifecycle | Check-in now requires `ONGOING` event status |
| Registration cancellation | Active registrations could be cancelled after the event started; old ticket remained usable as a stale artifact | Cancellation restricted to pre-event states and ticket is revoked/deleted |
| Waitlist | Cancellation after registration closed could promote waitlisted users incorrectly | Automatic promotion occurs only while registration is open |
| Admin catalog UI | Frontend sent/displayed a Venue `capacity` field that does not exist in backend/schema | Removed capacity and aligned UI with VenueRequest/Venue entity |
| Docker | Backend health check used `wget` in a JRE runtime image that does not guarantee wget | Installed curl and changed health check to curl |
| Email | SMTP failure could roll back a successful core business transaction | Email delivery is now best-effort and logged |
| Secrets | Local DB password had a hardcoded default in application configuration | DB password is now required from environment configuration |

## Structural checks

- Java package/path mismatches: none
- Duplicate Java fully-qualified classes: none
- Frontend local imports missing: none
- Explicit entity columns missing from Flyway SQL: none
- Entity tables missing from Flyway SQL: none
- `pom.xml`: valid XML
- `package.json`: valid JSON
- Docker Compose YAML: valid
- GitHub Actions YAML: valid
- Java lexical/bracket scan: passed

## Runtime limitation

The audit environment did not provide Maven, Docker, or a functioning npm dependency installation. Therefore a live Spring Boot/MySQL/Docker/browser run could not be performed here. The GitHub Actions workflow remains the authoritative automated runtime/build check when the repository is pushed.

## Remaining intentional scope

The following are not represented as implemented merely to make the project appear complete:

- Password-reset email workflow
- Account activation workflow
- Admin user-management UI
- Event-staff assignment model
- Advanced popularity aggregation
- AWS deployment
- Kubernetes
- Jenkins
- Production image publishing
