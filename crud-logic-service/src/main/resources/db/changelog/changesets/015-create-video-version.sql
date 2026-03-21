--liquibase formatted sql

--changeset rivvy:015-create-video-version
CREATE TABLE video_version (
    id                  UUID PRIMARY KEY,
    video_id            UUID NOT NULL REFERENCES video(id),
    version_number      INT NOT NULL,
    storage_object_id   UUID NOT NULL REFERENCES storage_object(id),
    uploaded_by         UUID NOT NULL REFERENCES user_account(id),
    upload_status       TEXT NOT NULL,
    transcode_status    TEXT NOT NULL,
    is_current          BOOLEAN NOT NULL,
    is_approved         BOOLEAN NOT NULL,
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL
);
