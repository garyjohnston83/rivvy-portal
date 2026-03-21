--liquibase formatted sql

--changeset rivvy:013-create-brand-asset-version
CREATE TABLE brand_asset_version (
    id                  UUID PRIMARY KEY,
    asset_id            UUID NOT NULL REFERENCES brand_asset(id),
    version_number      INT NOT NULL,
    storage_object_id   UUID NOT NULL REFERENCES storage_object(id),
    uploaded_by         UUID NOT NULL REFERENCES user_account(id),
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL
);
