--liquibase formatted sql

--changeset rivvy:007-create-brief
CREATE TABLE brief (
    id                    UUID PRIMARY KEY,
    org_id                UUID NOT NULL REFERENCES organization(id),
    submitted_by          UUID NOT NULL REFERENCES user_account(id),
    title                 TEXT NOT NULL,
    description           TEXT,
    status                TEXT NOT NULL,
    priority              TEXT NOT NULL,
    desired_due_date      DATE,
    budget                NUMERIC(12,2),
    creative_direction    TEXT,
    "references"          JSONB NOT NULL DEFAULT '{}',
    metadata              JSONB NOT NULL DEFAULT '{}',
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ
);
