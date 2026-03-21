--liquibase formatted sql

--changeset rivvy:005-create-organization-member
CREATE TABLE organization_member (
    id                UUID PRIMARY KEY,
    org_id            UUID NOT NULL REFERENCES organization(id),
    user_id           UUID NOT NULL REFERENCES user_account(id),
    invited_by        UUID REFERENCES user_account(id),
    joined_at         TIMESTAMPTZ NOT NULL,
    is_primary        BOOLEAN NOT NULL
);
