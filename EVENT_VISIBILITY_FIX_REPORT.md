# EventHub Event Visibility Repair Report

## Changes included in this archive

1. Added read-only transaction boundaries around EventService read methods (`search`, `get`, `getForViewer`, and `mine`). This keeps entity-to-response DTO conversion inside an active persistence context and helps prevent lazy-loaded relationship failures.
2. Added server-side logging for unexpected exceptions in `GlobalExceptionHandler` while continuing to return a generic client-facing error.
3. Updated the admin dashboard so summary and recent-events API failures display a useful error and Retry control instead of silently remaining on “Loading…”.
4. Preserved the existing event visibility policy: draft events are not made public just to address display issues. Admin/organizer access remains governed by the current repository and authorization rules.

## Validation performed on this archive

- ZIP archive integrity checked after packaging.
- Project source, `pom.xml`, `package.json`, and lockfile are included.
- This environment did not complete a Maven build, frontend dependency installation/build, live database integration, or browser end-to-end run. The project must not be described as fully runtime-tested until those checks are run in an environment with the required dependencies and database.

## Environment setup

Use the root `.env.example` as the template for a local `.env`. Set a private `JWT_SECRET` meeting the project's validation requirements and correct database settings. Do not commit real credentials. The provided `scripts/run-backend.bat` expects the root `.env` file to exist and loads it before starting Maven.

## Key next verification

Start the backend and database, sign in as an administrator, create and publish an event, confirm the event is returned by `GET /api/events`, then verify it appears on the admin/organizer list and the student Events page according to the event's status and access rules. Follow with registration, notification, refresh, and route regression tests.
