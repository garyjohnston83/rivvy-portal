--liquibase formatted sql

--changeset rivvy:010-create-project
CREATE TABLE project (
    id                         UUID PRIMARY KEY,
    org_id                     UUID NOT NULL REFERENCES organization(id),
    brief_id                   UUID REFERENCES brief(id),
    key                        TEXT NOT NULL,
    title                      TEXT NOT NULL,
    status                     TEXT NOT NULL,
    due_date                   DATE,
    assigned_producer_user_id  UUID REFERENCES user_account(id),
    created_by                 UUID NOT NULL REFERENCES user_account(id),
    created_at                 TIMESTAMPTZ NOT NULL,
    updated_at                 TIMESTAMPTZ
);
