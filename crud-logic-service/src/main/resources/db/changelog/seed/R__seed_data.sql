--liquibase formatted sql

--changeset rivvy:seed-data runOnChange:true

-- Roles
INSERT INTO role (id, code, display_name, description) VALUES
('10000000-0000-0000-0000-000000000001', 'RIVVY_ADMIN', 'Rivvy Admin', 'Full administrative access')
ON CONFLICT (id) DO NOTHING;

INSERT INTO role (id, code, display_name, description) VALUES
('10000000-0000-0000-0000-000000000002', 'RIVVY_PRODUCER', 'Rivvy Producer', 'Producer role for managing video production')
ON CONFLICT (id) DO NOTHING;

INSERT INTO role (id, code, display_name, description) VALUES
('10000000-0000-0000-0000-000000000003', 'CLIENT', 'Client', 'Client role for submitting briefs and reviewing content')
ON CONFLICT (id) DO NOTHING;

-- Organization
INSERT INTO organization (id, name, slug, status, created_at) VALUES
('20000000-0000-0000-0000-000000000001', 'Rivvy Studios', 'rivvy-studios', 'ACTIVE', NOW())
ON CONFLICT (id) DO NOTHING;

-- Second Organization (client org for authorization testing)
INSERT INTO organization (id, name, slug, status, created_at) VALUES
('20000000-0000-0000-0000-000000000002', 'Acme Corp', 'acme-corp', 'ACTIVE', NOW())
ON CONFLICT (id) DO NOTHING;

-- User Accounts (password: password123, bcrypt cost factor 10)
INSERT INTO user_account (id, email, first_name, last_name, auth_provider, status, default_org_id, created_at, password_hash) VALUES
('30000000-0000-0000-0000-000000000001', 'admin@rivvy.local', 'Rivvy', 'Admin', 'LOCAL', 'ACTIVE', '20000000-0000-0000-0000-000000000001', NOW(), '$2a$10$qcrMrgReF0vAxTi0RTxwTe0tbAX4JpMtCIrSqHADQFa9ttyfnhzoC')
ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash;

INSERT INTO user_account (id, email, first_name, last_name, auth_provider, status, default_org_id, created_at, password_hash) VALUES
('30000000-0000-0000-0000-000000000002', 'producer@rivvy.local', 'Rivvy', 'Producer', 'LOCAL', 'ACTIVE', '20000000-0000-0000-0000-000000000001', NOW(), '$2a$10$qcrMrgReF0vAxTi0RTxwTe0tbAX4JpMtCIrSqHADQFa9ttyfnhzoC')
ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash;

INSERT INTO user_account (id, email, first_name, last_name, auth_provider, status, default_org_id, created_at, password_hash) VALUES
('30000000-0000-0000-0000-000000000003', 'client@rivvy.local', 'Rivvy', 'Client', 'LOCAL', 'ACTIVE', '20000000-0000-0000-0000-000000000001', NOW(), '$2a$10$qcrMrgReF0vAxTi0RTxwTe0tbAX4JpMtCIrSqHADQFa9ttyfnhzoC')
ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash;

-- Client user for Acme Corp (used in brief authorization tests)
INSERT INTO user_account (id, email, first_name, last_name, auth_provider, status, default_org_id, created_at, password_hash) VALUES
('30000000-0000-0000-0000-000000000004', 'client2@acme.local', 'Acme', 'Client', 'LOCAL', 'ACTIVE', '20000000-0000-0000-0000-000000000002', NOW(), '$2a$10$qcrMrgReF0vAxTi0RTxwTe0tbAX4JpMtCIrSqHADQFa9ttyfnhzoC')
ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash;

-- Organization Members
INSERT INTO organization_member (id, org_id, user_id, joined_at, is_primary) VALUES
('40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', NOW(), true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO organization_member (id, org_id, user_id, joined_at, is_primary) VALUES
('40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', NOW(), true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO organization_member (id, org_id, user_id, joined_at, is_primary) VALUES
('40000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000003', NOW(), true)
ON CONFLICT (id) DO NOTHING;

-- Acme Corp client member
INSERT INTO organization_member (id, org_id, user_id, joined_at, is_primary) VALUES
('40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000004', NOW(), true)
ON CONFLICT (id) DO NOTHING;

-- Role Assignments
INSERT INTO org_role_assignment (id, member_id, role_id) VALUES
('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

INSERT INTO org_role_assignment (id, member_id, role_id) VALUES
('50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002')
ON CONFLICT (id) DO NOTHING;

INSERT INTO org_role_assignment (id, member_id, role_id) VALUES
('50000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003')
ON CONFLICT (id) DO NOTHING;

-- Acme Corp client role assignment
INSERT INTO org_role_assignment (id, member_id, role_id) VALUES
('50000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003')
ON CONFLICT (id) DO NOTHING;

-- Producer Assignment: link producer (member 40..002) to Acme Corp so producer can view Acme briefs
INSERT INTO producer_assignment (id, producer_member_id, client_org_id, assigned_at) VALUES
('60000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- Screening Room seed data: Project, Brief, BriefItem, BriefItemDeliverable,
-- Videos, StorageObjects, and VideoVersions for Acme Corp
-- ============================================================================

-- Brief for Acme Corp (scaffolding for the video FK chain)
INSERT INTO brief (id, org_id, submitted_by, title, status, priority, "references", metadata, created_at) VALUES
('80000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000004',
 'Acme Brand Video Brief', 'SUBMITTED', 'NORMAL', '{}', '{}', '2026-03-10T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Project for Acme Corp (linked to Acme org and the brief above)
INSERT INTO project (id, org_id, brief_id, key, title, status, created_by, created_at) VALUES
('70000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000001',
 'ACME-001', 'Acme Brand Videos', 'ACTIVE', '30000000-0000-0000-0000-000000000004', '2026-03-10T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- BriefItem (scaffolding for BriefItemDeliverable)
INSERT INTO brief_item (id, brief_id, title, order_index) VALUES
('81000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000001', 'Brand Video Package', 0)
ON CONFLICT (id) DO NOTHING;

-- BriefItemDeliverable (scaffolding for Video FK)
INSERT INTO brief_item_deliverable (id, brief_item_id, platform, aspect_ratio, audio_required, captions_required, localization_required, extras, order_index, is_locked) VALUES
('82000000-0000-0000-0000-000000000001', '81000000-0000-0000-0000-000000000001', 'web', '16:9', true, false, false, '{}', 0, false)
ON CONFLICT (id) DO NOTHING;

-- Videos (4 rows under Acme Corp project with staggered created_at for deterministic sort order)
-- Video 1: "Brand Launch Teaser" -- most recent, will have approved+completed version
INSERT INTO video (id, project_id, brief_item_deliverable_id, brief_item_id, title, description, order_index, created_at) VALUES
('90000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', '82000000-0000-0000-0000-000000000001', '81000000-0000-0000-0000-000000000001',
 'Brand Launch Teaser', 'A 30-second teaser for the brand launch campaign', 0, '2026-03-14T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Video 2: "Product Demo" -- completed but unapproved version
INSERT INTO video (id, project_id, brief_item_deliverable_id, brief_item_id, title, description, order_index, created_at) VALUES
('90000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000001', '82000000-0000-0000-0000-000000000001', '81000000-0000-0000-0000-000000000001',
 'Product Demo', 'Full product walkthrough video', 1, '2026-03-13T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Video 3: "Behind the Scenes" -- processing version
INSERT INTO video (id, project_id, brief_item_deliverable_id, brief_item_id, title, description, order_index, created_at) VALUES
('90000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000001', '82000000-0000-0000-0000-000000000001', '81000000-0000-0000-0000-000000000001',
 'Behind the Scenes', 'Behind the scenes footage from the shoot', 2, '2026-03-12T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Video 4: "Social Cutdown" -- zero versions (edge case)
INSERT INTO video (id, project_id, brief_item_deliverable_id, brief_item_id, title, description, order_index, created_at) VALUES
('90000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000001', '82000000-0000-0000-0000-000000000001', '81000000-0000-0000-0000-000000000001',
 'Social Cutdown', 'Short-form social media cut', 3, '2026-03-11T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- StorageObjects for VideoVersions (one per version, created by the producer user)
-- StorageObject for Video 1 version
INSERT INTO storage_object (id, provider, bucket, object_key, content_type, size_bytes, created_by, created_at) VALUES
('91000000-0000-0000-0000-000000000001', 's3', 'rivvy-portal-dev', 'videos/90000000-0000-0000-0000-000000000001.mp4', 'video/mp4', 52428800, '30000000-0000-0000-0000-000000000002', '2026-03-14T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- StorageObject for Video 2 version
INSERT INTO storage_object (id, provider, bucket, object_key, content_type, size_bytes, created_by, created_at) VALUES
('91000000-0000-0000-0000-000000000002', 's3', 'rivvy-portal-dev', 'videos/90000000-0000-0000-0000-000000000002.mp4', 'video/mp4', 104857600, '30000000-0000-0000-0000-000000000002', '2026-03-13T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- StorageObject for Video 3 version
INSERT INTO storage_object (id, provider, bucket, object_key, content_type, size_bytes, created_by, created_at) VALUES
('91000000-0000-0000-0000-000000000003', 's3', 'rivvy-portal-dev', 'videos/90000000-0000-0000-0000-000000000003.mp4', 'video/mp4', 31457280, '30000000-0000-0000-0000-000000000002', '2026-03-12T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- VideoVersions with varied states
-- Video 1 version: isCurrent=true, transcodeStatus=COMPLETED, isApproved=true
INSERT INTO video_version (id, video_id, version_number, storage_object_id, uploaded_by, upload_status, transcode_status, is_current, is_approved, notes, created_at) VALUES
('92000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', 1, '91000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002',
 'COMPLETED', 'COMPLETED', true, true, 'Final approved version', '2026-03-14T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Video 2 version: isCurrent=true, transcodeStatus=COMPLETED, isApproved=false
INSERT INTO video_version (id, video_id, version_number, storage_object_id, uploaded_by, upload_status, transcode_status, is_current, is_approved, notes, created_at) VALUES
('92000000-0000-0000-0000-000000000002', '90000000-0000-0000-0000-000000000002', 1, '91000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002',
 'COMPLETED', 'COMPLETED', true, false, 'Awaiting client review', '2026-03-13T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Video 3 version: transcodeStatus=PROCESSING, isApproved=false (not yet current while processing)
INSERT INTO video_version (id, video_id, version_number, storage_object_id, uploaded_by, upload_status, transcode_status, is_current, is_approved, notes, created_at) VALUES
('92000000-0000-0000-0000-000000000003', '90000000-0000-0000-0000-000000000003', 1, '91000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002',
 'COMPLETED', 'PROCESSING', false, false, 'Upload complete, transcoding in progress', '2026-03-12T12:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Video 4: no versions inserted (zero versions edge case)

-- Update Video 1 latest_approved_version_id to point to its approved version
UPDATE video SET latest_approved_version_id = '92000000-0000-0000-0000-000000000001'
WHERE id = '90000000-0000-0000-0000-000000000001' AND latest_approved_version_id IS NULL;
