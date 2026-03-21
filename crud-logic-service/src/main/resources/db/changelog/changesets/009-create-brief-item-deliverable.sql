--liquibase formatted sql

--changeset rivvy:009-create-brief-item-deliverable
CREATE TABLE brief_item_deliverable (
    id                       UUID PRIMARY KEY,
    brief_item_id            UUID NOT NULL REFERENCES brief_item(id),
    platform                 TEXT NOT NULL,
    aspect_ratio             TEXT NOT NULL,
    max_duration_ms          INT,
    target_width             INT,
    target_height            INT,
    frame_rate_numerator     INT,
    frame_rate_denominator   INT,
    audio_required           BOOLEAN NOT NULL,
    captions_required        BOOLEAN NOT NULL,
    localization_required    BOOLEAN NOT NULL,
    locales                  TEXT[],
    deliverable_notes        TEXT,
    extras                   JSONB NOT NULL DEFAULT '{}',
    order_index              INT NOT NULL,
    is_locked                BOOLEAN NOT NULL,
    locked_at                TIMESTAMPTZ,
    locked_by                UUID REFERENCES user_account(id)
);
