--liquibase formatted sql

--changeset rivvy:016-create-review-comment
CREATE TABLE review_comment (
    id                  UUID PRIMARY KEY,
    video_version_id    UUID NOT NULL REFERENCES video_version(id),
    author_user_id      UUID NOT NULL REFERENCES user_account(id),
    parent_comment_id   UUID REFERENCES review_comment(id),
    time_ms             INT,
    text                TEXT NOT NULL,
    resolved_by         UUID REFERENCES user_account(id),
    resolved_at         TIMESTAMPTZ,
    resolution_note     TEXT,
    is_deleted          BOOLEAN NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ
);
