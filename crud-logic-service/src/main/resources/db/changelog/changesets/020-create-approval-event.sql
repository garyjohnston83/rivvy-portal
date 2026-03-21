--liquibase formatted sql

--changeset rivvy:020-create-approval-event
CREATE TABLE approval_event (
    id                    UUID PRIMARY KEY,
    approval_request_id   UUID NOT NULL REFERENCES approval_request(id),
    actor_user_id         UUID NOT NULL REFERENCES user_account(id),
    event_type            TEXT NOT NULL,
    note                  TEXT,
    created_at            TIMESTAMPTZ NOT NULL
);
