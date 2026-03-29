--liquibase formatted sql

--changeset rivvy:023-add-lockout-fields-to-user-account
ALTER TABLE user_account
    ADD COLUMN failed_attempts_count INT NOT NULL DEFAULT 0,
    ADD COLUMN first_failed_attempt_at TIMESTAMPTZ,
    ADD COLUMN last_failed_attempt_at TIMESTAMPTZ,
    ADD COLUMN locked_until TIMESTAMPTZ;
