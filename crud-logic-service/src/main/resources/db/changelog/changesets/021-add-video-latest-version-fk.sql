--liquibase formatted sql

--changeset rivvy:021-add-video-latest-version-fk
ALTER TABLE video ADD CONSTRAINT fk_video_latest_approved_version FOREIGN KEY (latest_approved_version_id) REFERENCES video_version(id);
