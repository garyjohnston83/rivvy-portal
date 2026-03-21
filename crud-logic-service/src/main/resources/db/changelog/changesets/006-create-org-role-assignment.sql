--liquibase formatted sql

--changeset rivvy:006-create-org-role-assignment
CREATE TABLE org_role_assignment (
    id                UUID PRIMARY KEY,
    member_id         UUID NOT NULL REFERENCES organization_member(id),
    role_id           UUID NOT NULL REFERENCES role(id)
);
