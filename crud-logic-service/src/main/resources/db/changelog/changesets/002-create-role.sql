--liquibase formatted sql

--changeset rivvy:002-create-role
CREATE TABLE role (
    id                UUID PRIMARY KEY,
    code              TEXT NOT NULL,
    display_name      TEXT,
    description       TEXT
);
