# Spec Requirements: Failed Sign-in Handling

## Initial Description

Add enumeration-safe failed sign-in handling to the Rivvy Portal. All failed attempts return a uniform 401 with a generic error, except when a disabled account supplies the correct password, in which case respond 401 with an explicit 'account disabled' message. Enforce environment-configurable lockout (threshold, time window, duration) with counters persisted in the Rivvy Portal DB. Reset counters on successful sign-in. Invited status is always denied with the generic message. Basic auditing via logs only.

## Requirements Discussion

### First Round Questions

**Q1: Environment variable naming**
I assume the lockout policy environment variables should be named like `AUTH_LOCKOUT_THRESHOLD` (e.g., 5), `AUTH_LOCKOUT_WINDOW_MINUTES` (e.g., 15), and `AUTH_LOCKOUT_DURATION_MINUTES` (e.g., 30). Is that correct, or would you prefer different naming conventions?

**Answer:** Correct

**Q2: Database fields for lockout data**
For persisting lockout data, I'm thinking we should add these fields to the `user_account` table: `failed_attempts_count` (INT), `first_failed_attempt_at` (TIMESTAMPTZ), and `locked_until` (TIMESTAMPTZ nullable). Should we also track the last failed attempt timestamp, or is this sufficient?

**Answer:** Track all those in that table

**Q3: Error messages**
I assume the generic error message should be something like "Invalid email or password" to avoid revealing whether an account exists. For the disabled account case (with correct password), should the message be "Your account has been disabled. Please contact support." or something else?

**Answer:** Yes, those messages are fine

**Q4: Timing attacks prevention**
For timing attacks prevention on unknown emails, I'm thinking the backend should perform a dummy password hash operation (e.g., bcrypt with a dummy value) to match the timing of a real check. Is that the approach you want, or would you prefer a fixed delay?

**Answer:** Yes, that's the approach I want

**Q5: Lockout clearing mechanism**
I assume the lockout logic should check if `locked_until` is in the past and auto-clear it during the sign-in attempt, rather than having a background job. Is that correct?

**Answer:** Correct

**Q6: Audit logging details**
For audit logging, I assume we should log: failed attempt with reason (wrong password, unknown email, disabled, locked), lockout started (user_id, threshold reached), and lockout ended (user_id, time expired). Should we also log successful sign-ins that clear counters?

**Answer:** Correct and successful sign-ins clear counters

**Q7: Locked account counter behavior**
When a user is locked out and attempts to sign in, should the failed attempt counter continue incrementing, or should it remain frozen until the lockout expires?

**Answer:** Remain frozen

**Q8: Disabled vs invited account behavior**
I'm assuming that when checking if a disabled account has the correct password, we should only reveal "account disabled" if the password verification succeeds. For invited accounts, we never check the password and always return the generic error. Is this the intended behavior?

**Answer:** Correct for both

**Q9: Admin override capability**
Should there be any admin/support override capability to manually clear lockout status, or is that out of scope for this spec?

**Answer:** Not in scope

**Q10: Existing authentication utilities**
Are there any existing authentication, password hashing, or session management utilities in your codebase that I should reference? For example, existing Spring Security configurations, authentication services, or password encoders? Please provide file/folder paths or names.

**Answer:** You check for this

**Q11: Similar features for code reuse**
Are there existing features in your codebase with similar patterns we should reference? For example: existing authentication/login endpoints or services, password validation or hashing utilities, error response formatting or exception handlers, environment configuration loading patterns, or audit logging utilities or services? Please provide file/folder paths or names if they exist.

**Answer:** You check for this

**Q12: Visual assets**
Do you have any design mockups, wireframes, or screenshots for the login error messages? If yes, please place them in `agent-os/specs/2026-03-29-failed-signin-handling/planning/visuals/` with descriptive names like login-error-message-mockup.png, disabled-account-error.png, or generic-error-ui.png.

**Answer:** No

### Existing Code to Reference

Based on exploration of the codebase, the following existing features and patterns were identified:

**Authentication & Security:**
- `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/AuthController.java` - Existing login endpoint at `/api/auth/login` that handles authentication using Spring Security's `AuthenticationManager`
- `crud-logic-service/src/main/java/com/rivvystudios/portal/config/SecurityConfig.java` - Spring Security configuration with `BCryptPasswordEncoder` bean for password hashing
- `crud-logic-service/src/main/java/com/rivvystudios/portal/security/PortalUserDetailsService.java` - Implements Spring Security's `UserDetailsService` to load users and check `UserAccountStatus.ACTIVE` for the `enabled` flag

**Data Models:**
- `crud-logic-service/src/main/java/com/rivvystudios/portal/model/UserAccount.java` - JPA entity with fields: `id`, `email` (citext), `passwordHash`, `status` (enum), `lastLoginAt`, `createdAt`, `updatedAt`. This entity needs to be extended with lockout fields.
- `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/UserAccountStatus.java` - Enum with values: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING`. Note: The spec refers to "disabled" and "invited" statuses, which need to be mapped to these existing values.
- `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/UserAccountRepository.java` - JPA repository for UserAccount with `findByEmail()` method

**DTOs:**
- `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/LoginRequest.java` - Contains `email`, `password`, `rememberMe` fields
- `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/LoginResponse.java` - Response DTO for successful login

**Frontend:**
- `rivvy-portal-ui/src/pages/LoginPage.tsx` - React component for the login page at `/login` route. Currently displays error messages from the API's `error` field in the 401 response body. The UI already has error styling and displays: `data.error || 'Invalid email or password'`

**Configuration:**
- `crud-logic-service/src/main/resources/application.yaml` - Spring Boot configuration file where environment-based properties can be added
- Spring Boot's standard `@Value` annotation or `@ConfigurationProperties` can be used for environment variables

**Testing:**
- `crud-logic-service/src/test/java/com/rivvystudios/portal/auth/AuthControllerTests.java` - Existing test suite for authentication
- `crud-logic-service/src/test/java/com/rivvystudios/portal/auth/LoginIntegrationTests.java` - Integration tests for login flow
- `rivvy-portal-ui/src/pages/LoginPage.test.tsx` - Frontend tests for login page

### Visual Assets

No visual assets provided.

## Requirements Summary

### Functional Requirements

**Authentication Failure Handling:**
- All failed sign-in attempts must return HTTP 401 with a generic error message: "Invalid email or password"
- Exception: Disabled accounts with correct password return HTTP 401 with message: "Your account has been disabled. Please contact support."
- Invited/pending accounts always return the generic error regardless of password correctness
- Unknown email addresses must mimic the timing of known user checks (via dummy bcrypt operation) and return the generic error

**Lockout Policy:**
- Environment-configurable lockout policy with three parameters:
  - `AUTH_LOCKOUT_THRESHOLD`: Number of failed attempts before lockout (e.g., 5)
  - `AUTH_LOCKOUT_WINDOW_MINUTES`: Time window to count failures (e.g., 15 minutes)
  - `AUTH_LOCKOUT_DURATION_MINUTES`: Duration of lockout (e.g., 30 minutes)
- Lockout is per-user (based on UserAccount/email), not per-IP or per-organization
- When failures reach threshold within the time window, user is locked until the lockout duration expires
- Locked accounts return HTTP 401 with generic error (no special message revealing lockout status)
- Failed attempt counters remain frozen during lockout (do not increment)

**Counter Management:**
- Persist the following fields in `user_account` table:
  - `failed_attempts_count` (INT): Count of failed attempts
  - `first_failed_attempt_at` (TIMESTAMPTZ): Timestamp of first failed attempt in current window
  - `last_failed_attempt_at` (TIMESTAMPTZ): Timestamp of most recent failed attempt
  - `locked_until` (TIMESTAMPTZ, nullable): Timestamp when lockout expires
- Unknown email attempts must NOT mutate database counters
- Counters are reset to zero on successful authentication
- Lockout status is auto-cleared during sign-in attempt if `locked_until` is in the past

**Audit Logging:**
- Log failed sign-in attempts with reason: wrong password, unknown email, disabled account, invited account, or locked account
- Log lockout started events with user_id and threshold reached
- Log lockout ended events when lockout expires
- Log successful sign-ins that clear counters

**Status Mapping:**
- Map spec terminology to existing `UserAccountStatus` enum:
  - "active" = `ACTIVE` (can authenticate)
  - "disabled" = `INACTIVE` or `SUSPENDED` (cannot authenticate, but special message if correct password)
  - "invited" = `PENDING` (cannot authenticate, always generic error)

**Frontend Display:**
- Login page (`/login`) already displays error messages from API 401 responses
- Generic error: "Invalid email or password"
- Disabled account error: "Your account has been disabled. Please contact support."
- No changes needed to UI beyond ensuring correct error messages are passed through

### Reusability Opportunities

**Existing Components:**
- Reuse `AuthController` structure for handling login requests
- Leverage existing `BCryptPasswordEncoder` bean from `SecurityConfig` for password verification
- Extend `PortalUserDetailsService` or create a new service for lockout checking logic
- Use existing `UserAccountRepository` pattern for lockout data persistence
- Follow existing error response pattern: `Map.of("error", "message")` for 401 responses
- Reuse `LoginRequest` DTO structure
- Frontend `LoginPage.tsx` already handles error display correctly

**Backend Patterns:**
- Use Spring Boot's `@Value` or `@ConfigurationProperties` for environment variable injection
- Use SLF4J logger (standard in Spring Boot) for audit logging
- Use Liquibase (already configured) for database migration to add lockout fields
- Follow existing JPA entity patterns for adding fields to `UserAccount`

### Scope Boundaries

**In Scope:**
- Enumeration-safe error responses for all failed sign-in attempts
- Environment-configurable lockout policy (threshold, window, duration)
- Database persistence of lockout counters and lockout-until timestamp in `user_account` table
- Special case: reveal "account disabled" message only when disabled account supplies correct password
- Timing attack prevention via dummy bcrypt operation for unknown emails
- Auto-clearing of lockout when duration expires
- Reset counters on successful authentication
- Basic audit logging for failures, lockout start/end, and successful sign-ins
- Frozen counters during lockout period
- Generic error for invited/pending accounts regardless of password

**Out of Scope:**
- Successful sign-in flow and session establishment (already implemented in sibling feature)
- Role-based post-login routing (already implemented)
- Admin/support tools to manually clear lockout status
- Forgot password flows
- Invitation completion flows
- SSO/OIDC authentication
- CAPTCHA or MFA
- Per-IP rate limiting or global rate limiting
- Background jobs for lockout expiry (handled inline during authentication)
- Any UI changes beyond error message content

### Technical Considerations

**Integration Points:**
- Modify `AuthController.login()` method to implement lockout logic before calling `authenticationManager.authenticate()`
- Extend `UserAccount` JPA entity with four new fields for lockout tracking
- Create Liquibase migration to add lockout columns to `user_account` table
- Potentially refactor `PortalUserDetailsService` or create a separate service to handle lockout checks and counter updates
- Environment variables loaded via Spring Boot configuration (`application.yaml` or OS environment)
- Frontend requires no changes beyond ensuring backend sends correct error messages

**Existing System Constraints:**
- Email normalization (trim, case-insensitive) already exists in authentication pipeline via `citext` column type
- Spring Security's `AuthenticationManager` is the entry point for authentication
- `PortalUserDetailsService.loadUserByUsername()` currently checks `UserAccountStatus.ACTIVE` for the `enabled` flag
- Current error response format: `ResponseEntity.status(401).body(Map.of("error", "message"))`
- Database migrations managed via Liquibase

**Technology Stack:**
- Backend: Java 21, Spring Boot 4.x, Spring Security, JPA/Hibernate, PostgreSQL
- Frontend: React 19, TypeScript, Vite
- Password hashing: BCrypt (via Spring Security's `BCryptPasswordEncoder`)
- Session management: Spring Session JDBC
- Configuration: Spring Boot YAML + environment variables
- Database migrations: Liquibase

**Status Enum Clarification:**
- Spec uses "disabled" and "invited" terminology
- Existing `UserAccountStatus` enum has: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING`
- Recommended mapping:
  - "active" → `ACTIVE`
  - "disabled" → `INACTIVE` or `SUSPENDED` (clarify which one or treat both as disabled)
  - "invited" → `PENDING`

**Performance Considerations:**
- Dummy bcrypt operation for unknown emails will add latency (by design for security)
- Database updates for counter increments will occur on each failed attempt for known users
- Lockout status check is a simple timestamp comparison (efficient)
