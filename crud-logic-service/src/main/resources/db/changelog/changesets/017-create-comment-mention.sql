--liquibase formatted sql

--changeset rivvy:017-create-comment-mention
CREATE TABLE comment_mention (
    id                  UUID PRIMARY KEY,
    comment_id          UUID NOT NULL REFERENCES review_comment(id),
    mentioned_user_id   UUID NOT NULL REFERENCES user_account(id)
);
