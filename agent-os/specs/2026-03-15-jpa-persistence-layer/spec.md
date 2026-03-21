# Specification: JPA Persistence Layer

## Goal
Implement the Rivvy Portal persistence layer by mapping all 19 physical database tables to JPA entities with Spring Data repositories, managing the schema via Liquibase, seeding reference data, and configuring Docker Compose with Postgres 16 for local development.

## User Stories
- As a backend developer, I want JPA entities and repositories for all 19 tables so that I can build service-layer features against a type-safe persistence API.
- As a backend developer, I want Liquibase-managed schema migrations and seed data so that every environment starts with an identical, version-controlled database.
- As a backend developer, I want a Docker Compose setup with Postgres 16 so that I can run the full stack locally without external infrastructure.

## Specific Requirements

**JPA Entity Modeling**
- Create 19 JPA entity classes in `com.rivvystudios.portal.model`, one per physical table
- Use `@GeneratedValue(strategy = GenerationType.UUID)` on every entity ID field
- Map all timestamps to `java.time.Instant` with `@Column(columnDefinition = "timestamptz")`
- Map all FK columns as unidirectional `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn`; no bidirectional `@OneToMany` collections
- Map `video.latest_approved_version_id` as a plain `@Column` of type `UUID`, not a JPA relationship, to avoid circular dependency with `video_version`
- Map `review_comment.parent_comment_id` as a self-referencing `@ManyToOne` to `ReviewComment`
- Map `brief.references` with an explicit `@Column(name = "\"references\"")` since `references` is a reserved word in some SQL contexts

**Postgres-Specific Type Mappings**
- Map `user_account.email` (CITEXT) to `String` with `@Column(columnDefinition = "citext")`
- Map all JSONB columns (`brief.references`, `brief.metadata`, `brief_item_deliverable.extras`, `brand_asset.tags`) using Hypersistence Utils `@Type(JsonType.class)` to `Map<String, Object>`
- Map `brief_item_deliverable.locales` (TEXT[]) using Hypersistence Utils `@Type(StringArrayType.class)` to `String[]`
- Enable the CITEXT extension in the first Liquibase changeset before any table creation

**Enum Definitions**
- Create 12 enum classes in `com.rivvystudios.portal.model.enums`
- All enums persisted via `@Enumerated(EnumType.STRING)` on the entity field
- `OrganizationStatus`: ACTIVE, INACTIVE, SUSPENDED
- `UserAccountStatus`: ACTIVE, INACTIVE, SUSPENDED, PENDING
- `ProjectStatus`: DRAFT, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED
- `BriefStatus`: DRAFT, SUBMITTED, IN_REVIEW, APPROVED, REJECTED, CANCELLED
- `BriefPriority`: LOW, MEDIUM, HIGH, URGENT
- `BrandAssetStatus`: DRAFT, ACTIVE, ARCHIVED
- `BrandAssetVisibility`: PRIVATE, INTERNAL, PUBLIC
- `BrandAssetType`: LOGO, FONT, COLOR_PALETTE, IMAGE, VIDEO, DOCUMENT, OTHER
- `UploadStatus`: PENDING, UPLOADING, COMPLETED, FAILED
- `TranscodeStatus`: PENDING, PROCESSING, COMPLETED, FAILED
- `ApprovalRequestStatus`: PENDING, APPROVED, REJECTED, CANCELLED
- `ApprovalEventType`: SUBMITTED, APPROVED, REJECTED, REVISION_REQUESTED, CANCELLED, COMMENT_ADDED

**Spring Data Repositories**
- Create one `JpaRepository<EntityName, UUID>` interface per entity in `com.rivvystudios.portal.repository`
- Basic CRUD only; no custom query methods in this spec
- 19 repository interfaces total

**Liquibase Schema Management**
- Master changelog at `src/main/resources/db/changelog/db.changelog-master.yaml`
- Changeset 1: `CREATE EXTENSION IF NOT EXISTS citext`
- Changesets 2-20: Create tables using raw SQL changesets in FK-dependency order: (1) role, (2) organization, (3) user_account, (4) organization_member, (5) org_role_assignment, (6) brief, (7) brief_item, (8) brief_item_deliverable, (9) project, (10) storage_object, (11) brand_asset, (12) brand_asset_version, (13) video, (14) video_version, (15) review_comment, (16) comment_mention, (17) producer_assignment, (18) approval_request, (19) approval_event
- After `video_version` table is created, add a separate changeset to add the FK constraint `video.latest_approved_version_id -> video_version.id`
- All DDL uses raw SQL for Postgres-specific types (CITEXT, JSONB, TEXT[], TIMESTAMPTZ, NUMERIC)
- No unique constraints beyond primary keys
- All FK constraints explicitly defined in DDL

**Seed Data**
- Repeatable changelog file `src/main/resources/db/changelog/seed/R__seed_data.sql`
- Use `INSERT INTO ... ON CONFLICT (id) DO NOTHING` for idempotent reruns
- Use deterministic hardcoded UUIDs for all seed records
- Seed 3 roles: RIVVY_ADMIN (display_name "Rivvy Admin"), RIVVY_PRODUCER (display_name "Rivvy Producer"), CLIENT (display_name "Client")
- Seed 1 organization: name "Rivvy Studios", slug "rivvy-studios", status ACTIVE
- Seed 3 users: admin@rivvy.local, producer@rivvy.local, client@rivvy.local; all with auth_provider=LOCAL, status=ACTIVE, default_org_id pointing to Rivvy Studios
- Seed 3 organization_member records: one per user in Rivvy Studios, is_primary=true
- Seed 3 org_role_assignment records: admin user gets RIVVY_ADMIN, producer user gets RIVVY_PRODUCER, client user gets CLIENT

**Application Configuration**
- Update `application.yaml` with datasource URL `jdbc:postgresql://localhost:5432/rivvy_portal`, username, password
- Set `spring.jpa.hibernate.ddl-auto=validate` so Hibernate validates against the Liquibase-managed schema
- Set `spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml`
- Configure Hibernate dialect for PostgreSQL

**Docker Compose**
- Create `docker-compose.yml` at the `crud-logic-service/` root
- Postgres 16 service with database `rivvy_portal`, port 5432 mapped
- Environment variables for POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD
- Named volume for data persistence across container restarts

**Testing Strategy**
- Add Testcontainers PostgreSQL dependency in test scope
- Create integration test class that boots the full Spring context against a Testcontainers Postgres instance
- Verify Hibernate `validate` passes on startup (context loads without schema mismatch)
- Smoke test basic CRUD operations (create, read, update, delete) for a representative subset of entities
- Verify CITEXT, JSONB, and TEXT[] type mappings work end-to-end through entity persistence and retrieval

**Maven Dependencies**
- Add `spring-boot-starter-data-jpa` (managed by Spring Boot BOM)
- Add `org.postgresql:postgresql` runtime scope (managed by Spring Boot BOM)
- Add `org.liquibase:liquibase-core` (managed by Spring Boot BOM)
- Add `io.hypersistence:hypersistence-utils-hibernate-63` with explicit version
- Add `org.testcontainers:postgresql` and `org.testcontainers:junit-jupiter` in test scope
- Add `org.springframework.boot:spring-boot-testcontainers` in test scope

## Visual Design
No visual assets provided for this spec.

## Existing Code to Leverage

**PortalApplication main class (`com.rivvystudios.portal.PortalApplication`)**
- Bare `@SpringBootApplication` entry point at `crud-logic-service/src/main/java/com/rivvystudios/portal/PortalApplication.java`
- No changes needed to this class; Spring Boot auto-configures JPA, Liquibase, and DataSource from classpath dependencies
- Entity scanning will automatically pick up `com.rivvystudios.portal.model` since it is a sub-package of the application root

**Existing `pom.xml` (Spring Boot 3.4.1 with BOM)**
- Parent is `spring-boot-starter-parent:3.4.1`, so `spring-boot-starter-data-jpa`, `postgresql`, and `liquibase-core` versions are managed by the BOM
- Currently contains `spring-boot-starter-web`, `spring-boot-starter-actuator`, and `spring-boot-starter-test`
- New dependencies should be appended to the existing `<dependencies>` block following the same formatting conventions

**Existing `application.yaml`**
- Located at `crud-logic-service/src/main/resources/application.yaml`
- Currently configures `server.port=8080` and actuator health endpoint exposure
- Datasource, JPA, and Liquibase configuration blocks should be added to this file rather than creating separate profile-specific files

**Existing test classes**
- `PortalApplicationTests`, `ActuatorHealthEndpointTests`, and `ApplicationConfigurationTests` exist in `src/test/java/com/rivvystudios/portal/`
- These tests use `@SpringBootTest` with `RANDOM_PORT` and will need a Testcontainers Postgres instance to continue working after JPA is added
- Follow the same assertion style (AssertJ) and test class naming conventions when creating new integration tests

## Out of Scope
- REST controllers, service classes, or any business logic beyond JPA repositories
- Authentication, authorization, or OIDC/OAuth2 integration
- Media processing, transcoding pipelines, or external storage provider integration
- Custom Spring Data query methods, specifications, or projections
- Database unique constraints beyond primary keys
- Bidirectional JPA relationships or `@OneToMany` collections on parent entities
- Flyway or any schema migration tool other than Liquibase
- Database indexes beyond those implicit on primary keys and foreign keys
- Production deployment configuration, CI/CD pipelines, or cloud infrastructure
- Historical data migrations or schema versioning beyond the initial baseline and seed
