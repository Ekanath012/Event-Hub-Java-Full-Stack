-- Normalize legacy role values before Hibernate converts them to the Role enum.
-- This prevents malformed or blank student-role data from breaking login/JWT creation.
-- Unknown values are downgraded to STUDENT; this migration never grants elevated privileges.
UPDATE users
SET role = UPPER(TRIM(role))
WHERE role IS NOT NULL
  AND UPPER(TRIM(role)) IN ('SUPER_ADMIN', 'ADMIN', 'ORGANIZER', 'EVENT_STAFF', 'STUDENT');

UPDATE users
SET role = 'STUDENT'
WHERE role IS NULL
   OR TRIM(role) = ''
   OR UPPER(TRIM(role)) NOT IN ('SUPER_ADMIN', 'ADMIN', 'ORGANIZER', 'EVENT_STAFF', 'STUDENT');
