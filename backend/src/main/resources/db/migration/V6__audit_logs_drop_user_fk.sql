-- Audit rows are written in a separate transaction (REQUIRES_NEW). A foreign key to users makes that
-- insert wait on the caller's uncommitted/locked user row, which deadlocks until the lock timeout.
ALTER TABLE audit_logs DROP FOREIGN KEY fk_audit_user;
