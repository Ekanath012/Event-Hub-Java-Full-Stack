# EventHub — Full Integration Audit / Phase 7.1

This package is the corrected Phase 7 build after a whole-project integration audit.

## Verified statically

- Backend Java package/path consistency: passed
- Duplicate Java fully-qualified classes: none
- Explicit entity columns vs Flyway migrations: passed
- Entity tables vs Flyway migrations: passed
- Frontend local imports: passed
- `pom.xml`: valid XML
- `package.json`: valid JSON
- Docker Compose YAML: valid
- GitHub Actions YAML: valid
- Java lexical/bracket scan: passed

## Important fixes made

1. Fixed a Java compilation-breaking multiline string in `CertificateService` PDF generation.
2. Added the missing `GET /api/auth/me` endpoint.
3. Added row locking for login user records to make failed-login/account-lock updates concurrency-safe.
4. Added row locking for refresh-token rotation to prevent concurrent refresh-token reuse.
5. JWT validation now respects temporary account lockouts.
6. Public event discovery no longer exposes DRAFT events.
7. Draft event details are only available to the owning organizer/admin.
8. QR check-in is restricted to events in `ONGOING` state.
9. Registration cancellation is restricted to the pre-event registration lifecycle and revokes the ticket.
10. Waitlist promotion only occurs while registration is open.
11. Removed the frontend's nonexistent Venue `capacity` field and aligned the catalog UI with the actual Venue entity/API.
12. Docker backend health checks now use `curl`, which is installed in the runtime image.
13. Email delivery failures are treated as non-critical side effects and no longer roll back core event operations.

## Runtime limitation

This audit environment does not have Maven, Docker, or a working npm dependency installation available, so a live Spring Boot/MySQL/Docker/browser run could not be honestly claimed here. GitHub Actions is configured to perform the runtime builds/tests when the repository is pushed.

## Intentional scope

Jenkins, Kubernetes, AWS deployment, password-reset email workflow, admin user-management UI, event-staff assignment, and advanced popularity aggregation remain separate feature work. They are not silently represented as implemented by this package.
