--liquibase formatted sql

--changeset rivvy:001-enable-citext
CREATE EXTENSION IF NOT EXISTS citext;
