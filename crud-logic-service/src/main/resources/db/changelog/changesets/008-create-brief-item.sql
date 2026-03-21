--liquibase formatted sql

--changeset rivvy:008-create-brief-item
CREATE TABLE brief_item (
    id                    UUID PRIMARY KEY,
    brief_id              UUID NOT NULL REFERENCES brief(id),
    title                 TEXT NOT NULL,
    description           TEXT,
    expected_duration_ms  INT,
    primary_aspect_ratio  TEXT,
    audience              TEXT,
    order_index           INT NOT NULL
);
