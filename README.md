# EventHub — Enterprise Event Management Platform

EventHub is a Java full-stack event management platform built as a production-style portfolio project.

## Stack

- Java 21
- Spring Boot
- Spring Security + JWT
- Spring Data JPA / Hibernate
- MySQL 8
- Flyway
- React + Vite
- Axios
- Docker / Docker Compose
- GitHub Actions
- JUnit 5 / Mockito

## Local Docker run

1. Copy `.env.example` to `.env`.
2. Replace the example passwords and JWT secret with local values.
3. Run:

```bash
docker compose up --build
```

4. Open `http://localhost:5173`.
5. Backend health: `http://localhost:8080/actuator/health`.
6. Swagger: `http://localhost:8080/swagger-ui/index.html`.

Stop:

```bash
docker compose down
```

Remove the local database volume too:

```bash
docker compose down -v
```

## Password reset (local development)

The login page includes a **Forgot password?** link. Reset requests use a random, one-time token stored only as a SHA-256 hash, expire after 15 minutes by default, and invalidate existing refresh-token sessions when the password changes. The API returns a generic response regardless of whether the email exists.

With Docker Compose, Mailpit is available to catch reset emails without sending real email. Start the project with `docker compose up --build`, then open `http://localhost:8025` to view the reset email. When running the backend directly through `scripts\run-backend.bat`, start the local Mailpit service with `docker compose up -d mailpit`, set `MAIL_ENABLED=true` and `MAIL_HOST=localhost` in `.env`, and keep `MAIL_PORT=1025`. For production, configure a real SMTP provider and set `FRONTEND_BASE_URL` to the public frontend origin.

## Integration and security notes

- Public event discovery hides draft events.
- Draft event details are restricted to the owning organizer/admin.
- Registration and waitlist operations use database locking and unique constraints.
- Refresh-token rotation uses row locking to prevent concurrent token reuse.
- QR check-in is accepted only for ongoing events.
- Email delivery is optional and a mail-provider failure does not roll back the core business operation.
- No production secrets are committed.

## CI/CD

GitHub Actions runs on pushes and pull requests. It tests the backend, builds the frontend, validates Docker Compose, builds the container images, and runs a Trivy filesystem security scan.

This repository does not automatically deploy to AWS and does not publish images to a registry. That is intentional for the current portfolio scope.

## Runtime verification

The repository is configured for GitHub Actions to run the backend tests, frontend build, Docker image builds, and security scanning. The preparation environment used for this package did not provide Maven/Docker runtime access, so those checks were not falsely marked as locally executed.
