--liquibase formatted sql

--changeset rivvy:019-create-approval-request
CREATE TABLE approval_request (
    id                      UUID PRIMARY KEY,
    video_version_id        UUID NOT NULL REFERENCES video_version(id),
    producer_assignment_id  UUID NOT NULL REFERENCES producer_assignment(id),
    approver_member_id      UUID NOT NULL REFERENCES organization_member(id),
    requested_by            UUID REFERENCES user_account(id),
    status                  TEXT NOT NULL,
    decision_note           TEXT,
    decided_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ
);
