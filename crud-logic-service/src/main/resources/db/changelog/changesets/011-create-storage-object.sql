--liquibase formatted sql

--changeset rivvy:011-create-storage-object
CREATE TABLE storage_object (
    id                UUID PRIMARY KEY,
    provider          TEXT NOT NULL,
    bucket            TEXT NOT NULL,
    object_key        TEXT NOT NULL,
    version_id        TEXT,
    region            TEXT,
    size_bytes        BIGINT,
    content_type      TEXT,
    etag              TEXT,
    sha256            TEXT,
    width             INT,
    height            INT,
    duration_ms       INT,
    created_by        UUID NOT NULL REFERENCES user_account(id),
    created_at        TIMESTAMPTZ NOT NULL
);
