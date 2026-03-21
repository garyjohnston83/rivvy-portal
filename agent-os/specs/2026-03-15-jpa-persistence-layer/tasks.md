# Task Breakdown: JPA Persistence Layer

## Overview
Total Tasks: 12 groups, 74 sub-tasks

This spec adds the complete persistence layer to a bare Spring Boot 3.4.1 skeleton at `crud-logic-service/`. It covers Maven dependencies, Docker Compose for Postgres 16, 12 enum classes, 19 JPA entities, 19 Spring Data repositories, Liquibase schema management with raw SQL DDL, seed data, and Testcontainers-based integration tests.

**Base path:** `crud-logic-service/`
**Java source root:** `crud-logic-service/src/main/java/com/rivvystudios/portal/`
**Test source root:** `crud-logic-service/src/test/java/com/rivvystudios/portal/`
**Resources root:** `crud-logic-service/src/main/resources/`

---

## Task List

### Group 1: Project Setup & Dependencies
**Dependencies:** None
**Complexity:** Medium

- [x] 1.0 Complete project setup and dependency configuration
  - [x] 1.1 Add Maven dependencies to `crud-logic-service/pom.xml`
    - Add `spring-boot-starter-data-jpa` (version managed by Spring Boot BOM)
    - Add `org.postgresql:postgresql` with `<scope>runtime</scope>` (version managed by BOM)
    - Add `org.liquibase:liquibase-core` (version managed by BOM)
    - Add `io.hypersistence:hypersistence-utils-hibernate-63` with explicit version (e.g., `3.9.0`) in a new `<properties>` entry
    - Add `org.testcontainers:postgresql` with `<scope>test</scope>`
    - Add `org.testcontainers:junit-jupiter` with `<scope>test</scope>`
    - Add `org.springframework.boot:spring-boot-testcontainers` with `<scope>test</scope>`
    - Append new dependencies after the existing `spring-boot-starter-test` block, following the same XML formatting conventions
  - [x] 1.2 Create `crud-logic-service/docker-compose.yml`
    - Define a `postgres` service using image `postgres:16`
    - Set environment variables: `POSTGRES_DB=rivvy_portal`, `POSTGRES_USER=rivvy`, `POSTGRES_PASSWORD=rivvy`
    - Map port `5432:5432`
    - Create a named volume `pgdata` mounted to `/var/lib/postgresql/data` for persistence across restarts
  - [x] 1.3 Update `crud-logic-service/src/main/resources/application.yaml`
    - Add `spring.datasource.url: jdbc:postgresql://localhost:5432/rivvy_portal`
    - Add `spring.datasource.username: rivvy`
    - Add `spring.datasource.password: rivvy`
    - Add `spring.jpa.hibernate.ddl-auto: validate`
    - Add `spring.jpa.properties.hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect`
    - Add `spring.liquibase.change-log: classpath:db/changelog/db.changelog-master.yaml`
    - Preserve existing `server.port` and `management.endpoints` configuration

**Acceptance Criteria:**
- `pom.xml` contains all 7 new dependencies with correct scopes
- `docker-compose.yml` starts a Postgres 16 container accessible on port 5432
- `application.yaml` has datasource, JPA, and Liquibase configuration alongside existing settings
- `mvn compile` succeeds with the new dependencies (requires Postgres for full startup, but compilation should work)

---

### Group 2: Enum Definitions
**Dependencies:** Group 1 (needs JPA dependency on classpath)
**Complexity:** Small

- [x] 2.0 Create all 12 enum classes
  - [x] 2.1 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/OrganizationStatus.java`
    - Values: `ACTIVE`, `INACTIVE`, `SUSPENDED`
  - [x] 2.2 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/UserAccountStatus.java`
    - Values: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING`
  - [x] 2.3 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/ProjectStatus.java`
    - Values: `DRAFT`, `ACTIVE`, `ON_HOLD`, `COMPLETED`, `ARCHIVED`
  - [x] 2.4 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BriefStatus.java`
    - Values: `DRAFT`, `SUBMITTED`, `IN_REVIEW`, `APPROVED`, `REJECTED`, `CANCELLED`
  - [x] 2.5 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BriefPriority.java`
    - Values: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
  - [x] 2.6 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BrandAssetStatus.java`
    - Values: `DRAFT`, `ACTIVE`, `ARCHIVED`
  - [x] 2.7 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BrandAssetVisibility.java`
    - Values: `PRIVATE`, `INTERNAL`, `PUBLIC`
  - [x] 2.8 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BrandAssetType.java`
    - Values: `LOGO`, `FONT`, `COLOR_PALETTE`, `IMAGE`, `VIDEO`, `DOCUMENT`, `OTHER`
  - [x] 2.9 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/UploadStatus.java`
    - Values: `PENDING`, `UPLOADING`, `COMPLETED`, `FAILED`
  - [x] 2.10 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/TranscodeStatus.java`
    - Values: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
  - [x] 2.11 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/ApprovalRequestStatus.java`
    - Values: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`
  - [x] 2.12 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/ApprovalEventType.java`
    - Values: `SUBMITTED`, `APPROVED`, `REJECTED`, `REVISION_REQUESTED`, `CANCELLED`, `COMMENT_ADDED`

**Acceptance Criteria:**
- All 12 enum files exist in `com.rivvystudios.portal.model.enums`
- Each enum is a simple Java enum with the specified values
- `mvn compile` succeeds with all enums on the classpath

---

### Group 3: JPA Entities -- Foundation (No FK Dependencies)
**Dependencies:** Group 2 (enums must exist for status fields)
**Complexity:** Medium

These four entities have no foreign key dependencies on other entities and form the foundation layer.

- [x] 3.0 Create foundation entities with no FK dependencies
  - [x] 3.1 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Role.java`
    - `@Entity` with `@Table(name = "role")`
    - Fields: `id` (UUID, `@GeneratedValue(strategy = GenerationType.UUID)`), `name` (String), `displayName` (String), `description` (String), `createdAt` (Instant, `columnDefinition = "timestamptz"`), `updatedAt` (Instant, `columnDefinition = "timestamptz"`)
    - No FK relationships
  - [x] 3.2 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Organization.java`
    - `@Entity` with `@Table(name = "organization")`
    - Fields: `id` (UUID), `name` (String), `slug` (String), `status` (`OrganizationStatus`, `@Enumerated(EnumType.STRING)`), `logoUrl` (String), `createdAt` (Instant), `updatedAt` (Instant)
    - No FK relationships
  - [x] 3.3 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/StorageObject.java`
    - `@Entity` with `@Table(name = "storage_object")`
    - Fields: `id` (UUID), `bucket` (String), `key` (String), `fileName` (String), `contentType` (String), `sizeBytes` (Long), `uploadStatus` (`UploadStatus`, `@Enumerated(EnumType.STRING)`), `uploadedById` (UUID -- plain column, not a relationship, since UserAccount may not exist yet in dependency order; alternatively use `@ManyToOne` to `UserAccount` if created in same group), `createdAt` (Instant), `updatedAt` (Instant)
    - Note: `uploadedById` references `user_account`; since `UserAccount` is in the same group, use `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn(name = "uploaded_by_id")` pointing to `UserAccount`
  - [x] 3.4 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/UserAccount.java`
    - `@Entity` with `@Table(name = "user_account")`
    - Fields: `id` (UUID), `email` (String, `@Column(columnDefinition = "citext")`), `fullName` (String), `avatarUrl` (String), `authProvider` (String), `authProviderId` (String), `status` (`UserAccountStatus`, `@Enumerated(EnumType.STRING)`), `defaultOrg` (`Organization`, `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "default_org_id")`), `createdAt` (Instant), `updatedAt` (Instant)
    - `defaultOrg` is a unidirectional `@ManyToOne` to `Organization`

**Acceptance Criteria:**
- All four entity classes compile without errors
- Each entity uses `@GeneratedValue(strategy = GenerationType.UUID)` for the ID
- All timestamps use `Instant` with `columnDefinition = "timestamptz"`
- `UserAccount.email` has `columnDefinition = "citext"`
- `UserAccount.defaultOrg` is a lazy `@ManyToOne` to `Organization`
- `StorageObject.uploadedBy` is a lazy `@ManyToOne` to `UserAccount`

---

### Group 4: JPA Entities -- Membership & Roles
**Dependencies:** Group 3 (depends on Organization, UserAccount, Role)
**Complexity:** Small

- [x] 4.0 Create membership and role assignment entities
  - [x] 4.1 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/OrganizationMember.java`
    - `@Entity` with `@Table(name = "organization_member")`
    - Fields: `id` (UUID), `organization` (`@ManyToOne` lazy to `Organization`), `userAccount` (`@ManyToOne` lazy to `UserAccount`), `isPrimary` (Boolean), `joinedAt` (Instant), `createdAt` (Instant), `updatedAt` (Instant)
  - [x] 4.2 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/OrgRoleAssignment.java`
    - `@Entity` with `@Table(name = "org_role_assignment")`
    - Fields: `id` (UUID), `organizationMember` (`@ManyToOne` lazy to `OrganizationMember`), `role` (`@ManyToOne` lazy to `Role`), `assignedAt` (Instant), `createdAt` (Instant), `updatedAt` (Instant)

**Acceptance Criteria:**
- Both entity classes compile without errors
- `OrganizationMember` has lazy `@ManyToOne` to both `Organization` and `UserAccount`
- `OrgRoleAssignment` has lazy `@ManyToOne` to both `OrganizationMember` and `Role`
- All FK columns use `@JoinColumn` with explicit column names matching the physical schema

---

### Group 5: JPA Entities -- Briefs & Deliverables
**Dependencies:** Group 3 (depends on Organization, UserAccount)
**Complexity:** Medium

- [x] 5.0 Create brief-related entities
  - [x] 5.1 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Brief.java`
    - `@Entity` with `@Table(name = "brief")`
    - Fields: `id` (UUID), `organization` (`@ManyToOne` lazy to `Organization`), `title` (String), `description` (String), `status` (`BriefStatus`, `@Enumerated(EnumType.STRING)`), `priority` (`BriefPriority`, `@Enumerated(EnumType.STRING)`), `references` (`Map<String, Object>`, `@Type(JsonType.class)`, `@Column(name = "\"references\"", columnDefinition = "jsonb")` -- note escaped column name since `references` is a reserved word), `metadata` (`Map<String, Object>`, `@Type(JsonType.class)`, `@Column(columnDefinition = "jsonb")`), `createdBy` (`@ManyToOne` lazy to `UserAccount`), `createdAt` (Instant), `updatedAt` (Instant)
  - [x] 5.2 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/BriefItem.java`
    - `@Entity` with `@Table(name = "brief_item")`
    - Fields: `id` (UUID), `brief` (`@ManyToOne` lazy to `Brief`), `title` (String), `description` (String), `sortOrder` (Integer), `createdAt` (Instant), `updatedAt` (Instant)
  - [x] 5.3 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/BriefItemDeliverable.java`
    - `@Entity` with `@Table(name = "brief_item_deliverable")`
    - Fields: `id` (UUID), `briefItem` (`@ManyToOne` lazy to `BriefItem`), `title` (String), `description` (String), `locales` (`String[]`, `@JdbcTypeCode(SqlTypes.ARRAY)`, `@Column(columnDefinition = "text[]")`), `extras` (`Map<String, Object>`, `@Type(JsonType.class)`, `@Column(columnDefinition = "jsonb")`), `assignedTo` (`@ManyToOne` lazy to `UserAccount`), `createdAt` (Instant), `updatedAt` (Instant)

**Acceptance Criteria:**
- All three entity classes compile without errors
- `Brief.references` uses `@Column(name = "\"references\"")` to escape the reserved word
- `Brief.references` and `Brief.metadata` use `@Type(JsonType.class)` with `Map<String, Object>`
- `BriefItemDeliverable.locales` uses `@JdbcTypeCode(SqlTypes.ARRAY)` with `String[]`
- `BriefItemDeliverable.extras` uses `@Type(JsonType.class)` with `Map<String, Object>`

---

### Group 6: JPA Entities -- Projects & Brand Assets
**Dependencies:** Group 3 (Organization, UserAccount, StorageObject), Group 5 (Brief)
**Complexity:** Medium

- [x] 6.0 Create project and brand asset entities
  - [x] 6.1 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Project.java`
    - `@Entity` with `@Table(name = "project")`
    - Fields: `id` (UUID), `organization` (`@ManyToOne` lazy to `Organization`), `brief` (`@ManyToOne` lazy to `Brief`), `name` (String), `description` (String), `status` (`ProjectStatus`, `@Enumerated(EnumType.STRING)`), `createdBy` (`@ManyToOne` lazy to `UserAccount`), `createdAt` (Instant), `updatedAt` (Instant)
  - [x] 6.2 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/BrandAsset.java`
    - `@Entity` with `@Table(name = "brand_asset")`
    - Fields: `id` (UUID), `organization` (`@ManyToOne` lazy to `Organization`), `project` (`@ManyToOne` lazy to `Project`), `name` (String), `description` (String), `assetType` (`BrandAssetType`, `@Enumerated(EnumType.STRING)`), `status` (`BrandAssetStatus`, `@Enumerated(EnumType.STRING)`), `visibility` (`BrandAssetVisibility`, `@Enumerated(EnumType.STRING)`), `tags` (`Map<String, Object>`, `@Type(JsonType.class)`, `@Column(columnDefinition = "jsonb")`), `createdBy` (`@ManyToOne` lazy to `UserAccount`), `createdAt` (Instant), `updatedAt` (Instant)
  - [x] 6.3 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/BrandAssetVersion.java`
    - `@Entity` with `@Table(name = "brand_asset_version")`
    - Fields: `id` (UUID), `brandAsset` (`@ManyToOne` lazy to `BrandAsset`), `storageObject` (`@ManyToOne` lazy to `StorageObject`), `versionNumber` (Integer), `createdBy` (`@ManyToOne` lazy to `UserAccount`), `createdAt` (Instant), `updatedAt` (Instant)

**Acceptance Criteria:**
- All three entity classes compile without errors
- `BrandAsset.tags` uses `@Type(JsonType.class)` mapping to `Map<String, Object>`
- `BrandAsset` has three enum fields: `assetType`, `status`, `visibility`
- `BrandAssetVersion` has lazy `@ManyToOne` to both `BrandAsset` and `StorageObject`

---

### Group 7: JPA Entities -- Videos & Versions
**Dependencies:** Group 3 (StorageObject, UserAccount), Group 5 (BriefItem, BriefItemDeliverable), Group 6 (Project)
**Complexity:** Medium

- [x] 7.0 Create video-related entities
  - [x] 7.1 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Video.java`
    - `@Entity` with `@Table(name = "video")`
    - Fields: `id` (UUID), `project` (`@ManyToOne` lazy to `Project`), `briefItemDeliverable` (`@ManyToOne` lazy to `BriefItemDeliverable`), `briefItem` (`@ManyToOne` lazy to `BriefItem`), `title` (String), `description` (String), `latestApprovedVersionId` (UUID, `@Column(name = "latest_approved_version_id")` -- plain UUID column, NOT a JPA relationship to avoid circular dependency with `VideoVersion`), `createdAt` (Instant), `updatedAt` (Instant)
    - CRITICAL: `latestApprovedVersionId` must be a plain `UUID` field with `@Column`, not a `@ManyToOne` relationship
  - [x] 7.2 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/VideoVersion.java`
    - `@Entity` with `@Table(name = "video_version")`
    - Fields: `id` (UUID), `video` (`@ManyToOne` lazy to `Video`), `storageObject` (`@ManyToOne` lazy to `StorageObject`), `versionNumber` (Integer), `uploadStatus` (`UploadStatus`, `@Enumerated(EnumType.STRING)`), `transcodeStatus` (`TranscodeStatus`, `@Enumerated(EnumType.STRING)`), `durationSeconds` (`BigDecimal` or `Double`), `fileSizeBytes` (Long), `createdBy` (`@ManyToOne` lazy to `UserAccount`), `createdAt` (Instant), `updatedAt` (Instant)

**Acceptance Criteria:**
- Both entity classes compile without errors
- `Video.latestApprovedVersionId` is a plain `UUID` column, not a JPA relationship
- `VideoVersion` has lazy `@ManyToOne` to `Video`, `StorageObject`, and `UserAccount`
- `VideoVersion` uses `@Enumerated(EnumType.STRING)` for both `uploadStatus` and `transcodeStatus`

---

### Group 8: JPA Entities -- Reviews & Approvals
**Dependencies:** Group 4 (OrganizationMember), Group 7 (VideoVersion), Group 3 (UserAccount, Organization)
**Complexity:** Large

This is the final entity group. These entities sit at the top of the dependency tree.

- [x] 8.0 Create review and approval entities
  - [x] 8.1 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/ReviewComment.java`
    - `@Entity` with `@Table(name = "review_comment")`
    - Fields: `id` (UUID), `videoVersion` (`@ManyToOne` lazy to `VideoVersion`), `author` (`@ManyToOne` lazy to `UserAccount`, `@JoinColumn(name = "author_id")`), `parentComment` (`@ManyToOne` lazy to `ReviewComment`, `@JoinColumn(name = "parent_comment_id")` -- self-referencing), `body` (String), `timecodeSeconds` (`BigDecimal` or `Double`), `createdAt` (Instant), `updatedAt` (Instant)
    - IMPORTANT: `parentComment` is a self-referencing `@ManyToOne` to `ReviewComment` itself
  - [x] 8.2 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/CommentMention.java`
    - `@Entity` with `@Table(name = "comment_mention")`
    - Fields: `id` (UUID), `reviewComment` (`@ManyToOne` lazy to `ReviewComment`), `mentionedUser` (`@ManyToOne` lazy to `UserAccount`, `@JoinColumn(name = "mentioned_user_id")`), `createdAt` (Instant)
  - [x] 8.3 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/ProducerAssignment.java`
    - `@Entity` with `@Table(name = "producer_assignment")`
    - Fields: `id` (UUID), `organizationMember` (`@ManyToOne` lazy to `OrganizationMember`), `organization` (`@ManyToOne` lazy to `Organization`), `assignedBy` (`@ManyToOne` lazy to `UserAccount`, `@JoinColumn(name = "assigned_by_id")`), `assignedAt` (Instant), `createdAt` (Instant), `updatedAt` (Instant)
  - [x] 8.4 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/ApprovalRequest.java`
    - `@Entity` with `@Table(name = "approval_request")`
    - Fields: `id` (UUID), `videoVersion` (`@ManyToOne` lazy to `VideoVersion`), `producerAssignment` (`@ManyToOne` lazy to `ProducerAssignment`), `requestedBy` (`@ManyToOne` lazy to `UserAccount`, `@JoinColumn(name = "requested_by_id")`), `reviewerMember` (`@ManyToOne` lazy to `OrganizationMember`, `@JoinColumn(name = "reviewer_member_id")`), `status` (`ApprovalRequestStatus`, `@Enumerated(EnumType.STRING)`), `createdAt` (Instant), `updatedAt` (Instant)
  - [x] 8.5 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/model/ApprovalEvent.java`
    - `@Entity` with `@Table(name = "approval_event")`
    - Fields: `id` (UUID), `approvalRequest` (`@ManyToOne` lazy to `ApprovalRequest`), `eventType` (`ApprovalEventType`, `@Enumerated(EnumType.STRING)`), `comment` (String), `performedBy` (`@ManyToOne` lazy to `UserAccount`, `@JoinColumn(name = "performed_by_id")`), `createdAt` (Instant)

**Acceptance Criteria:**
- All five entity classes compile without errors
- `ReviewComment.parentComment` is a self-referencing `@ManyToOne` to `ReviewComment`
- `ApprovalRequest.status` uses `@Enumerated(EnumType.STRING)` with `ApprovalRequestStatus`
- `ApprovalEvent.eventType` uses `@Enumerated(EnumType.STRING)` with `ApprovalEventType`
- All `@JoinColumn` annotations use explicit column names matching the physical schema
- `mvn compile` succeeds with all 19 entities on the classpath

---

### Group 9: Spring Data Repositories
**Dependencies:** Groups 3-8 (all entities must exist)
**Complexity:** Small

Create one `JpaRepository<Entity, UUID>` interface per entity. Basic CRUD only -- no custom query methods.

- [x] 9.0 Create all 19 repository interfaces
  - [x] 9.1 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/RoleRepository.java`
    - `public interface RoleRepository extends JpaRepository<Role, UUID> {}`
  - [x] 9.2 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/OrganizationRepository.java`
    - `public interface OrganizationRepository extends JpaRepository<Organization, UUID> {}`
  - [x] 9.3 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/StorageObjectRepository.java`
    - `public interface StorageObjectRepository extends JpaRepository<StorageObject, UUID> {}`
  - [x] 9.4 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/UserAccountRepository.java`
    - `public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {}`
  - [x] 9.5 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/OrganizationMemberRepository.java`
    - `public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {}`
  - [x] 9.6 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/OrgRoleAssignmentRepository.java`
    - `public interface OrgRoleAssignmentRepository extends JpaRepository<OrgRoleAssignment, UUID> {}`
  - [x] 9.7 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BriefRepository.java`
    - `public interface BriefRepository extends JpaRepository<Brief, UUID> {}`
  - [x] 9.8 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BriefItemRepository.java`
    - `public interface BriefItemRepository extends JpaRepository<BriefItem, UUID> {}`
  - [x] 9.9 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BriefItemDeliverableRepository.java`
    - `public interface BriefItemDeliverableRepository extends JpaRepository<BriefItemDeliverable, UUID> {}`
  - [x] 9.10 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/ProjectRepository.java`
    - `public interface ProjectRepository extends JpaRepository<Project, UUID> {}`
  - [x] 9.11 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BrandAssetRepository.java`
    - `public interface BrandAssetRepository extends JpaRepository<BrandAsset, UUID> {}`
  - [x] 9.12 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BrandAssetVersionRepository.java`
    - `public interface BrandAssetVersionRepository extends JpaRepository<BrandAssetVersion, UUID> {}`
  - [x] 9.13 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/VideoRepository.java`
    - `public interface VideoRepository extends JpaRepository<Video, UUID> {}`
  - [x] 9.14 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/VideoVersionRepository.java`
    - `public interface VideoVersionRepository extends JpaRepository<VideoVersion, UUID> {}`
  - [x] 9.15 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/ReviewCommentRepository.java`
    - `public interface ReviewCommentRepository extends JpaRepository<ReviewComment, UUID> {}`
  - [x] 9.16 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/CommentMentionRepository.java`
    - `public interface CommentMentionRepository extends JpaRepository<CommentMention, UUID> {}`
  - [x] 9.17 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/ProducerAssignmentRepository.java`
    - `public interface ProducerAssignmentRepository extends JpaRepository<ProducerAssignment, UUID> {}`
  - [x] 9.18 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/ApprovalRequestRepository.java`
    - `public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {}`
  - [x] 9.19 Create `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/ApprovalEventRepository.java`
    - `public interface ApprovalEventRepository extends JpaRepository<ApprovalEvent, UUID> {}`

**Acceptance Criteria:**
- All 19 repository interfaces exist in `com.rivvystudios.portal.repository`
- Each extends `JpaRepository<EntityName, UUID>`
- No custom query methods -- basic CRUD only
- `mvn compile` succeeds

---

### Group 10: Liquibase Schema Baseline
**Dependencies:** Group 1 (Liquibase dependency and configuration must be in place)
**Complexity:** Large

All DDL uses raw SQL changesets for Postgres-specific types. Tables are created in FK-dependency order.

- [x] 10.0 Create Liquibase schema baseline
  - [x] 10.1 Create master changelog at `crud-logic-service/src/main/resources/db/changelog/db.changelog-master.yaml`
    - Include all changeset files in order
    - Include seed data changelog as a repeatable changeset reference
  - [x] 10.2 Create changeset: Enable CITEXT extension
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/001-enable-citext.sql`
    - SQL: `CREATE EXTENSION IF NOT EXISTS citext;`
    - This MUST be the first changeset, before any table creation
  - [x] 10.3 Create changeset: Create `role` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/002-create-role.sql`
    - Columns: `id UUID PRIMARY KEY`, `code TEXT NOT NULL`, `display_name TEXT`, `description TEXT`
  - [x] 10.4 Create changeset: Create `organization` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/003-create-organization.sql`
    - Columns: `id UUID PRIMARY KEY`, `name TEXT NOT NULL`, `slug TEXT NOT NULL`, `status TEXT NOT NULL`, `created_at TIMESTAMPTZ NOT NULL`, `updated_at TIMESTAMPTZ`
  - [x] 10.5 Create changeset: Create `user_account` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/004-create-user-account.sql`
    - Columns: `id UUID PRIMARY KEY`, `email CITEXT NOT NULL`, `first_name TEXT`, `last_name TEXT`, `auth_provider TEXT`, `external_subject_id TEXT`, `status TEXT NOT NULL`, `last_login_at TIMESTAMPTZ`, `default_org_id UUID REFERENCES organization(id)`, `created_at TIMESTAMPTZ NOT NULL`, `updated_at TIMESTAMPTZ`
  - [x] 10.6 Create changeset: Create `organization_member` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/005-create-organization-member.sql`
    - FK to `organization(id)` and `user_account(id)`
  - [x] 10.7 Create changeset: Create `org_role_assignment` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/006-create-org-role-assignment.sql`
    - FK to `organization_member(id)` and `role(id)`
  - [x] 10.8 Create changeset: Create `brief` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/007-create-brief.sql`
    - JSONB columns for `"references"` and `metadata`
    - Use `"references"` (quoted) as column name since it is a reserved word
    - FK to `organization(id)` and `user_account(id)` (submitted_by)
  - [x] 10.9 Create changeset: Create `brief_item` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/008-create-brief-item.sql`
    - FK to `brief(id)`
  - [x] 10.10 Create changeset: Create `brief_item_deliverable` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/009-create-brief-item-deliverable.sql`
    - `TEXT[]` column for `locales`, JSONB column for `extras`
    - FK to `brief_item(id)` and `user_account(id)` (locked_by)
  - [x] 10.11 Create changeset: Create `project` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/010-create-project.sql`
    - FK to `organization(id)`, `brief(id)`, and `user_account(id)` (created_by, assigned_producer_user_id)
  - [x] 10.12 Create changeset: Create `storage_object` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/011-create-storage-object.sql`
    - FK to `user_account(id)` (created_by)
  - [x] 10.13 Create changeset: Create `brand_asset` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/012-create-brand-asset.sql`
    - JSONB column for `tags`
    - FK to `organization(id)`, `project(id)`, and `user_account(id)` (created_by)
  - [x] 10.14 Create changeset: Create `brand_asset_version` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/013-create-brand-asset-version.sql`
    - FK to `brand_asset(id)`, `storage_object(id)`, and `user_account(id)` (uploaded_by)
  - [x] 10.15 Create changeset: Create `video` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/014-create-video.sql`
    - Include `latest_approved_version_id UUID` column WITHOUT FK constraint (FK added later in 10.21)
    - FK to `project(id)`, `brief_item_deliverable(id)`, `brief_item(id)`
  - [x] 10.16 Create changeset: Create `video_version` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/015-create-video-version.sql`
    - Columns for `upload_status`, `transcode_status` (TEXT), `is_current` (BOOLEAN), `is_approved` (BOOLEAN)
    - FK to `video(id)`, `storage_object(id)`, and `user_account(id)` (uploaded_by)
  - [x] 10.17 Create changeset: Create `review_comment` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/016-create-review-comment.sql`
    - Self-referencing FK: `parent_comment_id UUID REFERENCES review_comment(id)`
    - FK to `video_version(id)` and `user_account(id)` (author_user_id, resolved_by)
  - [x] 10.18 Create changeset: Create `comment_mention` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/017-create-comment-mention.sql`
    - FK to `review_comment(id)` and `user_account(id)` (mentioned_user_id)
  - [x] 10.19 Create changeset: Create `producer_assignment` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/018-create-producer-assignment.sql`
    - FK to `organization_member(id)` (producer_member_id), `organization(id)` (client_org_id), and `user_account(id)` (assigned_by)
  - [x] 10.20 Create changeset: Create `approval_request` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/019-create-approval-request.sql`
    - FK to `video_version(id)`, `producer_assignment(id)`, `organization_member(id)` (approver_member_id), `user_account(id)` (requested_by)
  - [x] 10.21 Create changeset: Create `approval_event` table
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/020-create-approval-event.sql`
    - FK to `approval_request(id)` and `user_account(id)` (actor_user_id)
  - [x] 10.22 Create changeset: Add deferred FK for `video.latest_approved_version_id`
    - File: `crud-logic-service/src/main/resources/db/changelog/changesets/021-add-video-latest-version-fk.sql`
    - SQL: `ALTER TABLE video ADD CONSTRAINT fk_video_latest_approved_version FOREIGN KEY (latest_approved_version_id) REFERENCES video_version(id);`
    - This MUST come after both `video` and `video_version` tables are created

**Acceptance Criteria:**
- Master changelog YAML references all changesets in correct order
- CITEXT extension is enabled before any table creation
- All 19 tables created in FK-dependency order
- Deferred FK for `video.latest_approved_version_id -> video_version(id)` is in a separate changeset after both tables exist
- All FK constraints are explicitly defined in DDL
- JSONB columns used for `brief.references`, `brief.metadata`, `brief_item_deliverable.extras`, `brand_asset.tags`
- `TEXT[]` used for `brief_item_deliverable.locales`
- `CITEXT` used for `user_account.email`
- `TIMESTAMPTZ` used for all timestamp columns
- Column name `"references"` is quoted in DDL

---

### Group 11: Seed Data
**Dependencies:** Group 10 (all tables must exist)
**Complexity:** Medium

- [x] 11.0 Create repeatable seed data changelog
  - [x] 11.1 Create `crud-logic-service/src/main/resources/db/changelog/seed/R__seed_data.sql`
    - Use `INSERT INTO ... ON CONFLICT (id) DO NOTHING` for idempotent reruns
    - Use deterministic hardcoded UUIDs for all seed records
  - [x] 11.2 Seed 3 roles
    - `RIVVY_ADMIN` with display_name "Rivvy Admin"
    - `RIVVY_PRODUCER` with display_name "Rivvy Producer"
    - `CLIENT` with display_name "Client"
  - [x] 11.3 Seed 1 organization
    - Name: "Rivvy Studios", slug: "rivvy-studios", status: `ACTIVE`
  - [x] 11.4 Seed 3 user accounts
    - `admin@rivvy.local` (first_name: "Rivvy", last_name: "Admin"), auth_provider: LOCAL, status: ACTIVE, default_org_id: Rivvy Studios UUID
    - `producer@rivvy.local` (first_name: "Rivvy", last_name: "Producer"), auth_provider: LOCAL, status: ACTIVE, default_org_id: Rivvy Studios UUID
    - `client@rivvy.local` (first_name: "Rivvy", last_name: "Client"), auth_provider: LOCAL, status: ACTIVE, default_org_id: Rivvy Studios UUID
  - [x] 11.5 Seed 3 organization_member records
    - One per user in Rivvy Studios organization, `is_primary = true`
  - [x] 11.6 Seed 3 org_role_assignment records
    - Admin user gets RIVVY_ADMIN role
    - Producer user gets RIVVY_PRODUCER role
    - Client user gets CLIENT role
  - [x] 11.7 Reference the seed file from the master changelog
    - Add the repeatable changeset reference in `db.changelog-master.yaml` pointing to `seed/R__seed_data.sql`

**Acceptance Criteria:**
- Seed SQL file uses `ON CONFLICT (id) DO NOTHING` for all inserts
- All UUIDs are deterministic and hardcoded (not generated at runtime)
- Running Liquibase multiple times does not create duplicate records
- Seed data correctly references FK relationships (e.g., user default_org_id points to org UUID, org_member points to both user and org UUIDs)

---

### Group 12: Integration Tests
**Dependencies:** Groups 1-11 (full persistence layer must be in place)
**Complexity:** Large

- [x] 12.0 Complete integration test suite
  - [x] 12.1 Create Testcontainers base test configuration
    - File: `crud-logic-service/src/test/java/com/rivvystudios/portal/TestcontainersConfiguration.java`
    - Define a `@TestConfiguration` class that creates a `PostgreSQLContainer<?>` bean using `postgres:16` image
    - Use `@ServiceConnection` annotation (Spring Boot 3.1+ Testcontainers support) so that datasource properties are auto-configured
    - Alternatively, use `@DynamicPropertySource` to set `spring.datasource.url`, `username`, `password` from the container
  - [x] 12.2 Update existing test classes to work with Testcontainers
    - Modify `PortalApplicationTests.java` to import the Testcontainers configuration (e.g., `@Import(TestcontainersConfiguration.class)`)
    - Modify `ActuatorHealthEndpointTests.java` to import the Testcontainers configuration
    - Modify `ApplicationConfigurationTests.java` to import the Testcontainers configuration
    - Verify all 3 existing test classes still pass with the Postgres-backed context
  - [x] 12.3 Write 2-4 tests: Context and Hibernate validation
    - File: `crud-logic-service/src/test/java/com/rivvystudios/portal/persistence/SchemaValidationTests.java`
    - Test that the Spring context loads successfully (proves Hibernate `validate` passes against the Liquibase-managed schema)
    - Test that all 19 repository beans are present in the application context
    - Test that seed data is loaded (query `RoleRepository.findAll()` and verify 3 roles exist)
  - [x] 12.4 Write 2-4 tests: CRUD smoke tests for representative entities
    - File: `crud-logic-service/src/test/java/com/rivvystudios/portal/persistence/EntityCrudSmokeTests.java`
    - Test create + read for `Organization` (simple entity, no FK)
    - Test create + read for `UserAccount` with `Organization` FK (verifies lazy loading and FK relationship)
    - Test update for an entity (e.g., change `Organization.name` and verify)
    - Test delete for an entity (e.g., delete a created `Organization`)
  - [x] 12.5 Write 2-3 tests: Postgres-specific type mapping verification
    - File: `crud-logic-service/src/test/java/com/rivvystudios/portal/persistence/PostgresTypeMappingTests.java`
    - Test CITEXT: save a `UserAccount` with mixed-case email, retrieve it, verify case-insensitive behavior
    - Test JSONB: save a `Brief` with `references` and `metadata` as `Map<String, Object>`, retrieve it, verify map contents are preserved
    - Test TEXT[]: save a `BriefItemDeliverable` with `locales` as `String[]`, retrieve it, verify array contents
  - [x] 12.6 Run all feature-specific tests and verify they pass
    - Run the 3 updated existing tests + the 6-11 new tests (approximately 9-14 tests total)
    - Verify all tests pass against the Testcontainers Postgres instance
    - Verify Hibernate `validate` mode does not throw schema mismatch errors

**Acceptance Criteria:**
- Testcontainers configuration boots a Postgres 16 container for all `@SpringBootTest` classes
- All 3 existing test classes (`PortalApplicationTests`, `ActuatorHealthEndpointTests`, `ApplicationConfigurationTests`) continue to pass
- Context load test proves Hibernate `validate` passes (no schema mismatch)
- CRUD smoke tests demonstrate create, read, update, delete operations work through JPA repositories
- CITEXT test confirms case-insensitive email storage
- JSONB test confirms `Map<String, Object>` round-trips through Postgres JSONB columns
- TEXT[] test confirms `String[]` round-trips through Postgres TEXT[] columns
- All tests run against Testcontainers -- no external Postgres dependency

---

## Execution Order

```
Group 1:  Project Setup & Dependencies          (no dependencies)
Group 2:  Enum Definitions                       (depends on Group 1)
Group 3:  JPA Entities -- Foundation             (depends on Group 2)
Group 4:  JPA Entities -- Membership & Roles     (depends on Group 3)
Group 5:  JPA Entities -- Briefs & Deliverables  (depends on Group 3)
Group 6:  JPA Entities -- Projects & Assets      (depends on Groups 3, 5)
Group 7:  JPA Entities -- Videos & Versions      (depends on Groups 3, 5, 6)
Group 8:  JPA Entities -- Reviews & Approvals    (depends on Groups 3, 4, 7)
Group 9:  Spring Data Repositories               (depends on Groups 3-8)
Group 10: Liquibase Schema Baseline              (depends on Group 1)
Group 11: Seed Data                              (depends on Group 10)
Group 12: Integration Tests                      (depends on Groups 1-11)
```

**Parallelization opportunities:**
- Groups 4 and 5 can be executed in parallel (both depend only on Group 3)
- Groups 9 and 10 can be executed in parallel (9 depends on entities, 10 depends on Group 1 config)
- Group 11 depends only on Group 10, so it can proceed as soon as Liquibase changesets are done

**Critical path:** Group 1 -> Group 2 -> Group 3 -> Groups 4+5 -> Group 6 -> Group 7 -> Group 8 -> Group 9 -> Group 12

---

## Key Technical Notes

1. **No bidirectional relationships**: All JPA associations are unidirectional `@ManyToOne` from child to parent. No `@OneToMany` collections on parent entities.
2. **Hibernate validate mode**: Hibernate does NOT manage schema creation. Liquibase is authoritative. Hibernate only validates that entities match the Liquibase-created schema.
3. **Circular FK avoidance**: `Video.latestApprovedVersionId` is a plain `UUID` column, not a JPA relationship, to avoid circular dependency between `Video` and `VideoVersion`.
4. **Reserved word escaping**: `Brief.references` must use `@Column(name = "\"references\"")` because `references` is a reserved SQL keyword.
5. **Hypersistence Utils**: JSONB columns use `@Type(JsonType.class)` from `hypersistence-utils-hibernate-63`. TEXT[] columns use Hibernate 6 native `@JdbcTypeCode(SqlTypes.ARRAY)` for proper schema validation compatibility.
6. **Testcontainers with @ServiceConnection**: Spring Boot 3.1+ provides `@ServiceConnection` for Testcontainers, which auto-configures datasource properties from the container.
7. **Docker API proxy**: Tests require a Docker API version proxy (`docker-api-version-proxy.js`) running on port 23750 to work around Docker Desktop v29.2.1's broken un-versioned API handling. The proxy rewrites all API version prefixes to `/v1.44/` before forwarding to the Docker Desktop named pipe. Configure via `DOCKER_HOST=tcp://127.0.0.1:23750` and `tc.host=tcp://127.0.0.1:23750` in `~/.testcontainers.properties`.
