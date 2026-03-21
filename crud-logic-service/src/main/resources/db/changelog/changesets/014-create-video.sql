--liquibase formatted sql

--changeset rivvy:014-create-video
CREATE TABLE video (
    id                            UUID PRIMARY KEY,
    project_id                    UUID NOT NULL REFERENCES project(id),
    brief_item_deliverable_id     UUID NOT NULL REFERENCES brief_item_deliverable(id),
    brief_item_id                 UUID REFERENCES brief_item(id),
    title                         TEXT NOT NULL,
    description                   TEXT,
    order_index                   INT NOT NULL,
    latest_approved_version_id    UUID,
    created_at                    TIMESTAMPTZ NOT NULL,
    updated_at                    TIMESTAMPTZ
);
