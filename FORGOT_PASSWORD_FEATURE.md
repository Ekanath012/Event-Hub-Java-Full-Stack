# EventHub — Forgot Password Feature

## Added flow

- The login page now links to `/forgot-password`.
- `POST /api/auth/forgot-password` accepts an email address and always returns the same public response to avoid disclosing whether an account exists.
- When email delivery is enabled and the account exists, the backend generates a cryptographically random one-time token, stores only its SHA-256 hash, and emails a reset link.
- Reset tokens expire after 15 minutes by default and older unused tokens are invalidated when a new request is made.
- `/reset-password?token=...` lets the user set a strong new password.
- `POST /api/auth/reset-password` validates the token and expiry, hashes the new password with the existing password encoder, marks the token as used, clears failed-login counters/temporary lockout, and invalidates existing refresh-token sessions.
- The existing login and registration flows have not been intentionally changed.

## Local email testing

The application never sends a reset link when `MAIL_ENABLED=false`. This is intentional: a reset token is not exposed through the API or written to logs.

For local Docker development:

1. Copy `.env.example` to `.env` if you do not already have a local `.env`.
2. Ensure `.env` contains `MAIL_ENABLED=true`, `MAIL_HOST=localhost`, and `MAIL_PORT=1025` for a backend launched directly on Windows.
3. Start Mailpit: `docker compose up -d mailpit`.
4. Start the backend with `scripts\\run-backend.bat` and run the frontend as usual.
5. Request a reset link from the login page, then open `http://localhost:8025` to see the captured email.
6. For a full Docker Compose run, the backend is configured to use the internal `mailpit` hostname. Open the same Mailpit web UI at `http://localhost:8025`.

For a real deployment, configure `MAIL_HOST`, `MAIL_PORT`, credentials, TLS/auth settings, `MAIL_ENABLED=true`, and `FRONTEND_BASE_URL` for the actual SMTP provider and public frontend URL. Never log or expose reset tokens or commit a real `.env` file.

## Database

Flyway migration `V8__create_password_reset_tokens.sql` adds the reset-token table. The previous V7 role-normalization migration is included to maintain the project's previously used migration sequence.

## Verification status

- YAML syntax check for `docker-compose.yml`: passed.
- JSON parsing for frontend package manifests: passed.
- Static checks for reset routes, login link, and migration files: passed.
- Frontend dependency installation/build: not completed; `npm ci` did not finish in this environment.
- Maven compilation/tests and live MySQL/email/browser flow: not run because Maven/Docker runtime tools are not available in this environment.

Additional static checks after packaging preparation:

- JavaScript/JSX syntax parsing: 30 source files parsed with no syntax diagnostics.
- Docker Compose YAML, frontend package JSON/lockfile, route references, and migration-file presence: passed.
- ZIP integrity: to be checked on the final archive.
