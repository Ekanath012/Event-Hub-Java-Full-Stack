# EventHub login fix report

## Scope

This patch is limited to the shared login flow and refresh-token cleanup invoked during login. It does not change frontend styling, event features, user roles, password policy, registration logic, or authorization rules.

## Confirmed issue from supplied backend trace

The backend returned HTTP 500 from `POST /api/auth/login` in `AuthService.login` when calling `RefreshTokenRepository.deleteByUser_IdAndExpiresAtBefore(...)`. The exception reported that a Spring AOP proxy returned `null` for a repository method declared with primitive return type `long`. The second derived delete method used the same return contract and had the same risk.

## Code changes

- Replaced the two derived-delete methods returning primitive `long` with explicit JPQL bulk-delete methods annotated with `@Modifying` and returning `int` update counts.
- Updated `AuthService.login` to call the new methods. The cleanup semantics are preserved: expired tokens are removed for that user, and revoked tokens older than 24 hours are removed for that user. Unexpired, non-revoked sessions are not deleted by these housekeeping queries.
- Added `AuthServiceLoginTest` unit tests for the service login path for both `STUDENT` and `ADMIN` roles when cleanup finds zero matching token rows.

## Verification status

- Passed: original ZIP archive integrity check before modification.
- Passed: static inspection confirmed both old repository method declarations and both login call sites were updated.
- Not run: Maven tests/build, because Maven is not installed in the execution environment.
- Not run: live MySQL / Swagger / browser login tests, because the user's database and running application are not available here.

## Retest on Windows

1. Stop the existing backend with `Ctrl+C`.
2. Extract this ZIP to a new folder. Copy your existing private `.env` into the extracted `EventHub` root; the real `.env` is intentionally not included in the archive.
3. From the project root, run `scripts\run-backend.bat`.
4. Verify `POST http://localhost:8080/api/auth/login` returns success for a valid student and admin account, and an authentication error (not HTTP 500) for invalid credentials.
5. Run `cd backend && mvn test` and verify the new `AuthServiceLoginTest` plus the existing tests pass.
