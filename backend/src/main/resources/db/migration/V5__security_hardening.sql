ALTER TABLE users ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN locked_until TIMESTAMP(6) NULL;
CREATE INDEX idx_users_locked_until ON users(locked_until);
