--liquibase formatted sql

--changeset rivvy:003-create-organization
CREATE TABLE organization (
    id                UUID PRIMARY KEY,
    name              TEXT NOT NULL,
    slug              TEXT NOT NULL,
    status            TEXT NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ
);
