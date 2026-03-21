--liquibase formatted sql

--changeset rivvy:004-create-user-account
CREATE TABLE user_account (
    id                    UUID PRIMARY KEY,
    email                 CITEXT NOT NULL,
    first_name            TEXT,
    last_name             TEXT,
    auth_provider         TEXT,
    external_subject_id   TEXT,
    status                TEXT NOT NULL,
    last_login_at         TIMESTAMPTZ,
    default_org_id        UUID REFERENCES organization(id),
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ
);
