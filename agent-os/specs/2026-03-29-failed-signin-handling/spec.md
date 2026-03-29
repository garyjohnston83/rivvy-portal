# Specification: Failed Sign-in Handling

## Goal
Implement enumeration-safe failed sign-in handling with environment-configurable account lockout, preventing attackers from discovering valid accounts while protecting against brute-force attacks through per-user lockout tracking persisted in the database.

## User Stories
- As a system administrator, I want failed login attempts to return generic error messages so that attackers cannot enumerate valid email addresses
- As a security engineer, I want configurable lockout thresholds to prevent brute-force attacks without impacting legitimate users
- As a disabled account holder, I want to receive a clear message explaining my account status when I provide correct credentials

## Specific Requirements

**Enumeration-Safe Error Responses**
- All failed authentication attempts return HTTP 401 with generic message: "Invalid email or password"
- Unknown email addresses mimic known-user timing via dummy BCrypt hash operation (using BCryptPasswordEncoder bean)
- Exception: Disabled accounts (INACTIVE/SUSPENDED status) with correct password reveal "Your account has been disabled. Please contact support."
- Pending accounts (invited users) always return generic error regardless of password correctness
- Unknown email attempts must never mutate database counters or create audit logs for non-existent users
- Frontend LoginPage.tsx already displays error from `data.error` field in 401 response body

**Database Schema for Lockout Tracking**
- Add four new columns to `user_account` table via Liquibase migration (023-add-lockout-fields-to-user-account.sql)
- `failed_attempts_count` (INT, default 0, not null): Count of failed attempts in current window
- `first_failed_attempt_at` (TIMESTAMPTZ, nullable): Timestamp of first failed attempt in current window
- `last_failed_attempt_at` (TIMESTAMPTZ, nullable): Timestamp of most recent failed attempt
- `locked_until` (TIMESTAMPTZ, nullable): Timestamp when lockout expires; null means not locked
- Update UserAccount.java JPA entity with corresponding Java fields (Integer, Instant types)

**Environment-Configurable Lockout Policy**
- Define three environment variables loaded via Spring Boot's `@Value` annotation or `@ConfigurationProperties`
- `AUTH_LOCKOUT_THRESHOLD` (integer, default 5): Number of failed attempts before lockout
- `AUTH_LOCKOUT_WINDOW_MINUTES` (integer, default 15): Time window in minutes to count failures
- `AUTH_LOCKOUT_DURATION_MINUTES` (integer, default 30): Lockout duration in minutes
- Add properties to application.yaml with `${ENV_VAR:default}` syntax for environment override
- Configuration should be injectable into service/controller via constructor or @Value

**Lockout Logic Implementation**
- Check lockout status before calling authenticationManager.authenticate() in AuthController.login()
- If `locked_until` is not null and in the future, reject immediately with generic error (frozen counter, no increment)
- If `locked_until` is in the past, auto-clear lockout fields (set to null/0) and proceed with authentication
- On authentication failure for known user: increment `failed_attempts_count`, update `last_failed_attempt_at`
- If `first_failed_attempt_at` is null or outside window, reset it to current timestamp
- If failures reach threshold within window, set `locked_until` to current time plus lockout duration
- Lockout logic runs per-user (UserAccount email), not per-IP or per-organization

**Counter Reset on Success**
- On successful authentication (after authenticationManager.authenticate() succeeds), reset all lockout fields to null/0
- Update `last_login_at` (already implemented in AuthController.login() at line 84)
- Reset happens in same transaction as session creation
- Fields to reset: `failed_attempts_count` = 0, `first_failed_attempt_at` = null, `last_failed_attempt_at` = null, `locked_until` = null

**Password Verification for Disabled Accounts**
- For accounts with status INACTIVE or SUSPENDED, check if password is correct using BCryptPasswordEncoder.matches()
- If password correct: return 401 with "Your account has been disabled. Please contact support."
- If password incorrect: return 401 with generic "Invalid email or password"
- Do not increment lockout counters for disabled accounts (they cannot authenticate anyway)
- For PENDING status (invited users), always return generic error without checking password

**Audit Logging**
- Use SLF4J logger (org.slf4j.Logger) injected via LoggerFactory.getLogger(AuthController.class)
- Log failed attempts: `logger.warn("Failed login attempt for user {} - reason: {}", email, reason)`
- Reasons: "wrong_password", "unknown_email", "disabled_account", "invited_account", "account_locked"
- Log lockout started: `logger.warn("Account locked for user {} - threshold {} reached", userId, threshold)`
- Log lockout ended: `logger.info("Account lockout expired for user {} at {}", userId, lockedUntil)`
- Log successful sign-in with counter reset: `logger.info("Successful login for user {} - lockout counters reset", userId)`
- Do not log passwords, password hashes, or sensitive authentication details

**Status Enum Mapping**
- Map existing UserAccountStatus enum values to spec terminology
- ACTIVE = active accounts that can authenticate normally
- INACTIVE or SUSPENDED = disabled accounts (special message if correct password)
- PENDING = invited accounts (always generic error, no password check)
- Check status before authentication attempt to determine handling path

**Frontend Error Display**
- LoginPage.tsx (line 32) already extracts and displays `data.error` from 401 response
- No frontend changes required - backend must send correct error message in response body
- Verify error messages match exactly: "Invalid email or password" or "Your account has been disabled. Please contact support."
- Frontend displays error in red error box (line 87, role="alert")

**Timing Attack Prevention**
- For unknown email addresses, perform dummy BCrypt.matches() with dummy value to match real check timing
- Use same BCryptPasswordEncoder bean injected from SecurityConfig
- Example: `passwordEncoder.matches("dummy", "$2a$10$dummyHashValue...")`
- Ensures consistent response time regardless of whether email exists in database

## Visual Design
No visual mockups provided. Use existing LoginPage.tsx error styling.

## Existing Code to Leverage

**AuthController.java (crud-logic-service/src/main/java/com/rivvystudios/portal/controller/AuthController.java)**
- Extend login() method at line 51-112 to add lockout checks before authenticationManager.authenticate()
- Reuse existing error response pattern: `ResponseEntity.status(401).body(Map.of("error", "message"))` (line 109-110)
- Inject new LockoutService or implement lockout logic directly in controller
- Reuse existing UserAccountRepository injection and save pattern (line 81-86)
- Add lockout counter reset after successful authentication (after line 86)

**SecurityConfig.java (crud-logic-service/src/main/java/com/rivvystudios/portal/config/SecurityConfig.java)**
- Reuse existing BCryptPasswordEncoder bean (line 69-71) for password verification and dummy timing operations
- Inject PasswordEncoder into AuthController or lockout service
- No changes required to SecurityConfig itself

**UserAccount.java (crud-logic-service/src/main/java/com/rivvystudios/portal/model/UserAccount.java)**
- Add four new fields for lockout tracking with JPA annotations
- Follow existing pattern: @Column annotations with columnDefinition for timestamps (line 49-50, 56-57, 59-60)
- Use Instant type for timestamp fields (line 16 import already present)
- Add getter/setter methods following existing code style (line 62-156)

**PortalUserDetailsService.java (crud-logic-service/src/main/java/com/rivvystudios/portal/security/PortalUserDetailsService.java)**
- Reference enabled flag logic (line 42) that checks UserAccountStatus.ACTIVE
- Use similar pattern for status checking in lockout logic
- Reuse UserAccountRepository.findByEmail() pattern (line 39-40)

**Liquibase Migration Pattern (crud-logic-service/src/main/resources/db/changelog/db.changelog-master.yaml)**
- Create new changeset file: 023-add-lockout-fields-to-user-account.sql
- Add include entry in master yaml following existing pattern (line 2-47)
- Use ALTER TABLE user_account ADD COLUMN syntax
- Follow existing timestamp column pattern: `created_at timestamptz` (from other migrations)

## Out of Scope
- Successful sign-in flow and session establishment (already implemented)
- Role-based post-login routing (already implemented in AuthController.computeRedirectUrl())
- Admin or support tools to manually clear lockout status or view lockout history
- Forgot password functionality
- Invitation completion or account activation flows
- SSO/OIDC authentication integration
- CAPTCHA, multi-factor authentication (MFA), or challenge-response systems
- Per-IP rate limiting or global rate limiting across all users
- Background scheduled jobs for lockout expiry (handled inline during authentication)
- UI changes beyond error message content (no new pages, modals, or components)
- Lockout history or audit trail tables (basic logging only)
