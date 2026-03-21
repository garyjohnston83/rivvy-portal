# Spec Initialization

## Raw Idea

Implement the Rivvy Portal persistence layer using Spring Data JPA and Liquibase: model all provided physical entities as JPA entities with unidirectional child-to-parent relationships; create repositories for CRUD; manage schema with an initial Liquibase baseline and repeatable seed changelog; map Postgres types (CITEXT, JSONB, TEXT[]); use enums for status/event_type fields; seed roles (RIVVY_ADMIN, RIVVY_PRODUCER, CLIENT), organization Rivvy Studios (slug rivvy-studios), and one default user per role with org membership and role assignments. UUIDs are generated in JPA at runtime; Liquibase assigns UUIDs for seed data.

## In Scope
- JPA entities for all physical tables in models package
- Spring Data repositories for each entity
- Unidirectional child-to-parent associations for all FK fields
- Liquibase initial schema baseline and repeatable seed changelog
- Enum mappings for status/event_type fields
- Postgres-specific type mapping: CITEXT, JSONB, TEXT[]
- Startup validation (Hibernate validate), basic CRUD smoke tests

## Out of Scope
- Controllers/services/business logic beyond repositories
- Auth/OIDC integration beyond static seed data
- Media processing or external storage integration
- Historical data migrations beyond baseline/seed
- DB-level unique constraints (per directive)

## Assumptions
- Spring Boot + Spring Data JPA + Hibernate 6 + Postgres
- Liquibase is authoritative for schema; Hibernate set to validate
- Hypersistence Utils (hibernate-types) used for JSONB and TEXT[]
- CITEXT extension is enabled via Liquibase
- Enums are persisted as strings
- All FKs are enforced in the DB; no additional unique constraints
- Seed users use auth_provider=LOCAL, status=ACTIVE, default_org_id set to Rivvy Studios

## Acceptance Criteria
- All listed physical tables are represented as JPA entities with correct fields and nullability
- Relationships mapped as unidirectional child-to-parent and enforced with DB foreign keys
- Repositories exist for each entity and support CRUD
- Liquibase baseline DDL applies successfully; Hibernate validate passes
- CITEXT, JSONB, TEXT[] work end-to-end via entities
- Enums implemented for all status/event_type fields and persisted as strings
- Repeatable seed inserts roles, organization Rivvy Studios, and one default user per role with memberships and role assignments
- App boots without errors; basic repository CRUD operations succeed

## Architecture Context
19 physical data entities defined across the rivvy_portal database, with logical entity mappings for Role, BriefItem, OrgRoleAssignment, StorageObject, Video, BriefItemDeliverable, CommentMention, ApprovalRequest, VideoVersion, Project, BrandAssetVersion, ProducerAssignment, Organization, UserAccount, ReviewComment, Brief, ApprovalEvent, OrganizationMember, and BrandAsset.

## Date Initialized
2026-03-15
