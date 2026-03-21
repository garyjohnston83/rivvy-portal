--liquibase formatted sql

--changeset rivvy:018-create-producer-assignment
CREATE TABLE producer_assignment (
    id                  UUID PRIMARY KEY,
    producer_member_id  UUID NOT NULL REFERENCES organization_member(id),
    client_org_id       UUID NOT NULL REFERENCES organization(id),
    assigned_by         UUID REFERENCES user_account(id),
    assigned_at         TIMESTAMPTZ NOT NULL
);
