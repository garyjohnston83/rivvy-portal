--liquibase formatted sql

--changeset rivvy:012-create-brand-asset
CREATE TABLE brand_asset (
    id                UUID PRIMARY KEY,
    org_id            UUID NOT NULL REFERENCES organization(id),
    project_id        UUID REFERENCES project(id),
    name              TEXT NOT NULL,
    description       TEXT,
    asset_type        TEXT NOT NULL,
    tags              JSONB NOT NULL DEFAULT '{}',
    visibility        TEXT NOT NULL,
    status            TEXT NOT NULL,
    created_by        UUID NOT NULL REFERENCES user_account(id),
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ
);
