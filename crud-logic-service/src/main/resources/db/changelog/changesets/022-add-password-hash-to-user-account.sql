--liquibase formatted sql

--changeset rivvy:022-add-password-hash-to-user-account
ALTER TABLE user_account ADD COLUMN password_hash TEXT;
