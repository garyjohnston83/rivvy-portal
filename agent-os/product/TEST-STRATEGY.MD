# Test Strategy

## Test Levels

### Frontend Unit Testing
- **Scope:** React components, utilities, hooks, and reducers; validates timecode parsing/formatting, UI logic for enabling/disabling comment inputs, and small pure functions.
- **Coverage Target:** ≥75% lines, ≥60% branches
- **Tools:** Vitest, React Testing Library, @testing-library/user-event, MSW, c8/Istanbul
- **Rationale:** Fast feedback for a single-engineer workflow; stabilizes core UI logic and parsing behaviors tied to high-risk areas (timecodes, comment state).

### Frontend Functional/UI Testing
- **Scope:** Component-level user flows with mocked APIs: create/edit/delete comments, comment box read-only on non-latest versions, timecode click behavior with a stubbed player, and approval/lock UI states.
- **Coverage Target:** Critical UI states and edge cases
- **Tools:** React Testing Library, MSW, Vitest
- **Rationale:** Covers user-centric behavior without full browser overhead, ensuring deterministic UI states before E2E validation.

### Backend Unit Testing
- **Scope:** Domain and service-level logic: versioning rules, timecode math/validation, comment locking/unlocking, and approval state transitions.
- **Coverage Target:** ≥80% lines, ≥70% branches
- **Tools:** JUnit 5, Mockito, JaCoCo
- **Rationale:** Protects critical business rules for Screening Room with fast, isolated checks.

### Backend Integration Testing (API + DB)
- **Scope:** Spring Boot API + PostgreSQL via Testcontainers; exercises all DB-touching repositories/services/controllers with happy paths and at least one failure path each. S3 interactions mocked.
- **Coverage Target:** All DB-touching repos/services: happy path + ≥1 failure path each
- **Tools:** Spring Boot Test, Testcontainers (PostgreSQL), JUnit 5, Mockito
- **Rationale:** Validates persistence and transaction boundaries with realistic PostgreSQL, reducing environment-specific defects.

### End-to-End Testing (Playwright)
- **Scope:** Browser flows on shared staging (merges to main): real login with Liquibase-seeded users; critical Screening Room journeys — (1) Producer uploads a new version; (2) Client comments on latest version and clicking a timecoded comment seeks playback; (3) Viewing a non-latest version prevents new comments and forces new comments to latest; (4) Producer marks complete (locks comments/uploads) and can be reversed. Uses real S3 test bucket.
- **Coverage Target:** Critical Screening Room flows
- **Tools:** Playwright (Chromium)
- **Rationale:** Full-stack validation of the highest-risk user journeys, including real storage interactions and authentication.

### Smoke Testing (Pre- and Post-deployment)
- **Scope:** Health check, login, upload, timecode comment seek, complete/uncomplete; S3 write/read smoke.
- **Coverage Target:** Release-blocking smoke checks
- **Tools:** Playwright (Chromium)
- **Rationale:** Provides high-confidence go/no-go signals before and immediately after deploy, with automatic rollback on failure.

### Mutation Testing (MVP)
- **Scope:** Assess effectiveness of unit tests against critical backend logic; frontend optional.
- **Coverage Target:** Backend PIT ≥60% mutation score (MVP); Frontend Stryker optional
- **Tools:** PIT (Java), Stryker (optional)
- **Rationale:** Strengthens test rigor for production hardening in MVP.

### NFR Light-Touch (MVP)
- **Scope:** Basic dependency and secret scanning; minimal performance/accessibility checks as feasible.
- **Coverage Target:** Basic checks only (MVP)
- **Tools:** OWASP Dependency Check or Snyk, git-secrets
- **Rationale:** Introduces essential safety nets for MVP without heavy investment during PoC.

## Quality Gates

### PR Gate
**Criteria:**
- ESLint and Prettier pass
- TypeScript typecheck passes
- Frontend unit/functional tests pass with coverage ≥75% lines and ≥60% branches
- Backend unit tests pass with coverage ≥80% lines and ≥70% branches
- Backend integration tests (PostgreSQL via Testcontainers; S3 mocked) pass
- PR required for all changes; self-approval allowed due to single engineer
**Enforcement:** CI pipeline blocks merge

### Merge to Main Gate (E2E on Staging)
**Criteria:**
- Playwright E2E suite (Chromium) passes against shared staging
- Real login with Liquibase-seeded users
- Real S3 test bucket reachable for upload/download
**Enforcement:** CI runs on merges to main; failure blocks deployment

### Pre-Deployment Gate
**Criteria:**
- Liquibase validate/migrate successful
- S3 write/read smoke succeeds
- Smoke suite passes: upload, timecode comment seek, complete/uncomplete
- Rollback plan in place (DB backup + app rollback)
**Enforcement:** CI/CD pre-deploy stage blocks release on failure

### Post-Deployment Smoke Gate
**Criteria:**
- Immediate post-deploy smoke: health, login, upload, comment seek, complete/uncomplete
- Automatic rollback on failure
**Enforcement:** CD pipeline monitors checks and triggers rollback

### S3 Hygiene Gate
**Criteria:**
- Unique S3 key prefix per E2E run
- Test artifacts cleaned up after runs
- Lifecycle rule to auto-delete test artifacts older than 7 days
**Enforcement:** CI job enforces cleanup; bucket lifecycle policy enforces retention

### Security Scanning Gate (MVP)
**Criteria:**
- Enable dependency scanning (OWASP Dependency Check or Snyk)
- Enable secret scanning (git-secrets)
**Enforcement:** Enabled starting MVP; CI blocks on critical findings

## Testing Principles

### Test-After Development
Tests are written after implementation; prioritize risky logic (upload/playback, timecode accuracy, completion lock).

### Flaky E2E Handling
Allow 1 retry for E2E; if still failing, fail the pipeline. Quarantine only with an issue opened and fix within 48 hours.

### E2E Data Isolation and Cleanup
Each run creates its own entities, uses a unique S3 prefix, and cleans up artifacts to avoid polluting shared staging.

### Real Services Usage Policy
Use real S3 and real login only in E2E and smoke tests; mock S3 in unit/integration tests for determinism and cost control.

### Risk-Focused Coverage
Emphasize scenarios that exercise upload/playback reliability, timecode correctness, and completion lock/unlock behavior.

### Lightweight Test Documentation
User-centric Given/When/Then test names; tests colocated with code; maintain a brief Test Notes doc listing critical E2E flows and seeded users.
