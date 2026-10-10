# EventHub Backend

Java 21 / Spring Boot 3 / Spring Security / JWT / JPA / MySQL / Flyway.

## Phase 2 APIs
- `GET /api/categories`
- `POST /api/categories` — ADMIN/SUPER_ADMIN
- `PUT /api/categories/{id}` — ADMIN/SUPER_ADMIN
- `DELETE /api/categories/{id}` — ADMIN/SUPER_ADMIN
- `GET /api/venues`
- `POST /api/venues` — ADMIN/SUPER_ADMIN
- `PUT /api/venues/{id}` — ADMIN/SUPER_ADMIN
- `DELETE /api/venues/{id}` — ADMIN/SUPER_ADMIN
- `GET /api/events`
- `GET /api/events/{id}`
- `GET /api/events/mine` — ORGANIZER/ADMIN/SUPER_ADMIN
- `POST /api/events` — ORGANIZER/ADMIN/SUPER_ADMIN
- `PUT /api/events/{id}` — owner organizer or ADMIN/SUPER_ADMIN
- `DELETE /api/events/{id}` — owner organizer or ADMIN/SUPER_ADMIN, draft only
- `PATCH /api/events/{id}/status` — owner organizer or ADMIN/SUPER_ADMIN

## Event discovery parameters
`q`, `categoryId`, `venueId`, `status`, `fromDate`, `toDate`, `page`, `size`, `sortBy`, `direction`.

Supported sorting in Phase 2: `date`, `title`, `createdAt`. Popularity is intentionally deferred until registration data exists.
