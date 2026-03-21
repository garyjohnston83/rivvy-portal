# Spec Requirements: JPA Persistence Layer

## Initial Description
Implement the Rivvy Portal persistence layer using Spring Data JPA and Liquibase: model all provided physical entities as JPA entities with unidirectional child-to-parent relationships; create repositories for CRUD; manage schema with an initial Liquibase baseline and repeatable seed changelog; map Postgres types (CITEXT, JSONB, TEXT[]); use enums for status/event_type fields; seed roles (RIVVY_ADMIN, RIVVY_PRODUCER, CLIENT), organization Rivvy Studios (slug rivvy-studios), and one default user per role with org membership and role assignments. UUIDs are generated in JPA at runtime; Liquibase assigns UUIDs for seed data.

## Requirements Discussion

### First Round Questions

**Q1:** I assume the JPA entities should live in `com.rivvystudios.portal.model` and repositories in `com.rivvystudios.portal.repository`. Is that correct, or do you prefer a different package layout?
**Answer:** Correct.

**Q2:** The existing pom.xml uses Spring Boot 3.4.1. I'll add `spring-boot-starter-data-jpa`, `postgresql` driver, `liquibase-core`, and Hypersistence Utils (`hypersistence-utils-hibernate-63`). Should I also add Testcontainers for the CRUD smoke tests, or are smoke tests expected to run against an external Postgres instance?
**Answer:** Add Testcontainers for the CRUD smoke tests.

**Q3:** For enums, I've identified status/event_type fields that need enum classes. Can you provide the valid values for each enum, or should I infer reasonable defaults?
**Answer:** Decide the values; user will manually change after if needed.

**Q4:** For the Liquibase baseline, I'm planning a single `db/changelog/db.changelog-master.yaml` with: (1) CITEXT extension, (2) a changeset per table in FK-dependency order, (3) repeatable seed changelog. Does that structure work?
**Answer:** That is fine.

**Q5:** For the three seed users, I'll generate deterministic UUIDs and use placeholder emails like `admin@rivvy.local`, `producer@rivvy.local`, `client@rivvy.local`. Are there specific names, emails, or UUIDs you'd like used instead?
**Answer:** Fine as proposed.

**Q6:** The `video` table has a `latest_approved_version_id` FK that creates a circular reference. Should this be mapped as a simple UUID column (not a JPA relationship) with the FK enforced only at the DB level?
**Answer:** Correct.

**Q7:** There's no `docker-compose.yml` in the project yet. Should I create one with a Postgres 16 service for local development?
**Answer:** Create it.

**Q8:** Is there anything explicitly NOT wanted in this persistence layer -- for example, should I avoid adding `@EntityListeners`, auditing callbacks, soft-delete filters, or any Spring Data custom query methods beyond basic CRUD?
**Answer:** No restrictions -- nothing explicitly excluded.

### Existing Code to Reference

No similar existing features identified for reference. The project is a bare Spring Boot 3.4.1 skeleton with only `spring-boot-starter-web`, `spring-boot-starter-actuator`, and `spring-boot-starter-test` dependencies. Main class is `com.rivvystudios.portal.PortalApplication`.

### Follow-up Questions

No follow-up questions were needed.

## Visual Assets

### Files Provided:
No visual assets provided.

## Requirements Summary

### Functional Requirements
- 19 JPA entities mapped to physical tables in `com.rivvystudios.portal.model`
- Spring Data JPA repositories for each entity in `com.rivvystudios.portal.repository`
- Unidirectional child-to-parent associations for all FK fields
- Liquibase baseline DDL creating all 19 tables with FK constraints
- Repeatable seed changelog inserting: 3 roles (RIVVY_ADMIN, RIVVY_PRODUCER, CLIENT), 1 organization (Rivvy Studios, slug: rivvy-studios), 3 users (one per role), 3 organization memberships, 3 org role assignments
- Enum classes for all status/event_type fields, persisted as strings
- Postgres-specific type mappings: CITEXT for user_account.email, JSONB for tags/references/metadata/extras, TEXT[] for locales
- `video.latest_approved_version_id` mapped as plain UUID column (no JPA relationship) to avoid circular dependency
- Docker Compose file with Postgres 16 for local development
- Testcontainers-based CRUD smoke tests

### Enum Values (architect-determined, subject to user revision)
- **OrganizationStatus**: ACTIVE, INACTIVE, SUSPENDED
- **UserAccountStatus**: ACTIVE, INACTIVE, SUSPENDED, PENDING
- **ProjectStatus**: DRAFT, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED
- **BriefStatus**: DRAFT, SUBMITTED, IN_REVIEW, APPROVED, REJECTED, CANCELLED
- **BriefPriority**: LOW, MEDIUM, HIGH, URGENT
- **BrandAssetStatus**: DRAFT, ACTIVE, ARCHIVED
- **BrandAssetVisibility**: PRIVATE, INTERNAL, PUBLIC
- **BrandAssetType**: LOGO, FONT, COLOR_PALETTE, IMAGE, VIDEO, DOCUMENT, OTHER
- **UploadStatus**: PENDING, UPLOADING, COMPLETED, FAILED
- **TranscodeStatus**: PENDING, PROCESSING, COMPLETED, FAILED
- **ApprovalRequestStatus**: PENDING, APPROVED, REJECTED, CANCELLED
- **ApprovalEventType**: SUBMITTED, APPROVED, REJECTED, REVISION_REQUESTED, CANCELLED, COMMENT_ADDED

### Reusability Opportunities
- No existing code to reuse; greenfield persistence layer
- Hypersistence Utils patterns for JSONB/TEXT[] are well-documented externally

### Scope Boundaries
**In Scope:**
- JPA entities for all 19 physical tables
- Spring Data repositories with basic CRUD
- Liquibase baseline schema + repeatable seed data
- Enum classes for status/event_type fields
- Postgres type mappings (CITEXT, JSONB, TEXT[])
- Docker Compose with Postgres 16
- Testcontainers-based CRUD smoke tests
- Hibernate set to `validate` mode
- `application.yaml` datasource and Liquibase configuration

**Out of Scope:**
- Controllers, services, or business logic beyond repositories
- Auth/OIDC integration beyond static seed data
- Media processing or external storage integration
- Historical data migrations beyond baseline/seed
- DB-level unique constraints (per directive)

### Technical Considerations
- Spring Boot 3.4.1 (existing), Java 21
- Spring Data JPA + Hibernate 6 + PostgreSQL
- Liquibase is authoritative for schema; Hibernate set to validate
- Hypersistence Utils (`hypersistence-utils-hibernate-63`) for JSONB and TEXT[]
- CITEXT extension enabled via Liquibase changeset
- All enums persisted as strings (`@Enumerated(EnumType.STRING)`)
- UUIDs generated at JPA runtime (`@GeneratedValue(strategy = GenerationType.UUID)`)
- Liquibase seed data uses hardcoded deterministic UUIDs
- Seed users: auth_provider=LOCAL, status=ACTIVE, default_org_id=Rivvy Studios org UUID
- Seed emails: admin@rivvy.local, producer@rivvy.local, client@rivvy.local
- All FKs enforced in DB; no additional unique constraints
- Circular FK (video.latest_approved_version_id) mapped as plain UUID, not JPA relationship
- Testcontainers with PostgreSQL for integration/smoke tests

### Architecture Context
- Application: Rivvy Portal (app-mlxwja0d-7swkq)
- Persistence Tier component (comp-mlxwjpdp-apue4) contains Rivvy Portal DB service
- Service Tier component (comp-mlxwjp8s-enkq6) contains Rivvy Portal Services, exposing Portal API (REST)
- 19 physical tables in rivvy_portal database
- 19 corresponding logical data entities
