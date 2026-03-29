# Task Breakdown: Failed Sign-in Handling

## Overview
Total Tasks: 4 Task Groups
Technology Stack: Java 21, Spring Boot 4.x, Spring Security, PostgreSQL, Liquibase

## Task List

### Database Layer

#### Task Group 1: Database Schema and Entity Updates
**Dependencies:** None

- [x] 1.0 Complete database layer for lockout tracking
  - [x] 1.1 Write 4-6 focused tests for UserAccount lockout fields
    - Test lockout field persistence (failedAttemptsCount, firstFailedAttemptAt, lastFailedAttemptAt, lockedUntil)
    - Test lockout field defaults and nullable constraints
    - Test timestamp field handling for Instant type
    - Skip exhaustive validation testing
  - [x] 1.2 Create Liquibase migration 023-add-lockout-fields-to-user-account.sql
    - Add `failed_attempts_count` column (INT, default 0, not null)
    - Add `first_failed_attempt_at` column (TIMESTAMPTZ, nullable)
    - Add `last_failed_attempt_at` column (TIMESTAMPTZ, nullable)
    - Add `locked_until` column (TIMESTAMPTZ, nullable)
    - Follow existing timestamp pattern from previous migrations
  - [x] 1.3 Update db.changelog-master.yaml
    - Add include entry for 023-add-lockout-fields-to-user-account.sql
    - Follow existing pattern (line 2-47)
  - [x] 1.4 Update UserAccount.java JPA entity
    - Add `private Integer failedAttemptsCount` field with @Column annotation
    - Add `private Instant firstFailedAttemptAt` field with @Column(columnDefinition = "timestamptz")
    - Add `private Instant lastFailedAttemptAt` field with @Column(columnDefinition = "timestamptz")
    - Add `private Instant lockedUntil` field with @Column(columnDefinition = "timestamptz")
    - Add getter and setter methods following existing code style
  - [x] 1.5 Ensure database layer tests pass
    - Run ONLY the 4-6 tests written in 1.1
    - Verify migration runs successfully against test database
    - Verify new fields are persisted correctly

**Acceptance Criteria:**
- The 4-6 tests written in 1.1 pass
- Liquibase migration executes without errors
- UserAccount entity properly maps to updated user_account table
- New lockout fields default to 0/null appropriately

### Configuration Layer

#### Task Group 2: Environment Configuration and Properties
**Dependencies:** None

- [x] 2.0 Complete configuration for lockout policy
  - [x] 2.1 Write 2-3 focused tests for lockout configuration
    - Test environment variable loading with defaults
    - Test configuration injection into service/controller
    - Skip exhaustive configuration scenarios
  - [x] 2.2 Add lockout properties to application.yaml
    - Add `auth.lockout.threshold: ${AUTH_LOCKOUT_THRESHOLD:5}`
    - Add `auth.lockout.window-minutes: ${AUTH_LOCKOUT_WINDOW_MINUTES:15}`
    - Add `auth.lockout.duration-minutes: ${AUTH_LOCKOUT_DURATION_MINUTES:30}`
    - Follow existing configuration pattern
  - [x] 2.3 Create LockoutProperties configuration class (optional approach)
    - Use @ConfigurationProperties(prefix = "auth.lockout") if using class-based approach
    - Or use @Value annotations directly in service/controller
    - Include validation for positive integer values
  - [x] 2.4 Ensure configuration tests pass
    - Run ONLY the 2-3 tests written in 2.1
    - Verify default values load correctly
    - Verify environment variable override works

**Acceptance Criteria:**
- The 2-3 tests written in 2.1 pass
- Configuration properties load from application.yaml with defaults
- Environment variables override defaults when set
- Properties are injectable into services/controllers

### Backend Security Layer

#### Task Group 3: Authentication Logic and Lockout Service
**Dependencies:** Task Groups 1, 2

- [x] 3.0 Complete authentication security logic
  - [x] 3.1 Write 6-8 focused tests for lockout and authentication logic
    - Test lockout check before authentication (locked user rejected)
    - Test lockout expiry auto-clearing (expired lockout proceeds)
    - Test failed attempt counter increment and window tracking
    - Test lockout trigger when threshold reached
    - Test disabled account with correct/incorrect password behavior
    - Test unknown email timing attack prevention (dummy bcrypt)
    - Skip exhaustive edge case testing
  - [x] 3.2 Refactor AuthController.login() method
    - Inject BCryptPasswordEncoder (reuse bean from SecurityConfig)
    - Inject UserAccountRepository (already present)
    - Add lockout configuration properties via constructor injection
    - Implement pre-authentication lockout check logic before authenticationManager.authenticate()
  - [x] 3.3 Implement lockout check logic
    - Check if user exists via UserAccountRepository.findByEmail()
    - If user not found: perform dummy BCrypt operation, return generic error
    - If user found and locked_until is future: return generic error (frozen counters)
    - If user found and locked_until is past: clear lockout fields (set to null/0)
  - [x] 3.4 Implement disabled/pending account handling
    - Check UserAccountStatus before authentication
    - For INACTIVE/SUSPENDED: verify password with BCryptPasswordEncoder.matches()
      - If correct: return "Your account has been disabled. Please contact support."
      - If incorrect: return generic error
    - For PENDING: return generic error without password check
  - [x] 3.5 Implement failed attempt tracking
    - On authentication failure for known ACTIVE user: increment failedAttemptsCount
    - Update lastFailedAttemptAt to current Instant
    - If firstFailedAttemptAt is null or outside window: reset to current Instant
    - Calculate if failures exceed threshold within window
    - If threshold exceeded: set lockedUntil to current time + duration
  - [x] 3.6 Implement counter reset on success
    - After authenticationManager.authenticate() succeeds (after line 86 in AuthController)
    - Reset failedAttemptsCount = 0
    - Set firstFailedAttemptAt, lastFailedAttemptAt, lockedUntil to null
    - Save via userAccountRepository.save()
  - [x] 3.7 Add comprehensive audit logging
    - Log failed attempts with reason: "wrong_password", "unknown_email", "disabled_account", "invited_account", "account_locked"
    - Log lockout started with user ID and threshold
    - Log lockout expired when auto-clearing
    - Log successful login with counter reset
    - Use SLF4J Logger (LoggerFactory.getLogger(AuthController.class))
    - Ensure no passwords or sensitive data in logs
  - [x] 3.8 Ensure backend security tests pass
    - Run ONLY the 6-8 tests written in 3.1
    - Verify lockout logic works correctly
    - Verify timing attack prevention via dummy bcrypt
    - Verify error messages match spec exactly

**Acceptance Criteria:**
- The 6-8 tests written in 3.1 pass
- Lockout check runs before authentication and blocks locked users
- Expired lockouts auto-clear during authentication attempt
- Failed attempts increment counters and trigger lockout at threshold
- Successful authentication resets all lockout fields
- Disabled accounts show special message only with correct password
- Pending accounts always show generic error
- Unknown emails perform dummy bcrypt for timing consistency
- Audit logs capture all required events without sensitive data

### Integration Testing

#### Task Group 4: Integration Tests and Gap Analysis
**Dependencies:** Task Groups 1-3

- [x] 4.0 Review and fill critical integration testing gaps
  - [x] 4.1 Review existing tests from Task Groups 1-3
    - Review the 4-6 database tests from Task 1.1
    - Review the 2-3 configuration tests from Task 2.1
    - Review the 6-8 security logic tests from Task 3.1
    - Total existing tests: approximately 12-17 tests
  - [x] 4.2 Analyze integration test coverage gaps
    - Identify critical end-to-end authentication workflows lacking coverage
    - Focus on integration points between lockout logic and Spring Security
    - Focus on database transaction boundaries for counter updates
    - Identify timing attack scenarios not covered
    - Skip unit test gaps and focus on integration workflows
  - [x] 4.3 Write up to 8 additional integration tests maximum
    - Full lockout workflow: 5 failed attempts → lockout → rejection → expiry → success
    - Disabled account workflow: disabled user with correct password → special error
    - Unknown email workflow: unknown email → dummy bcrypt timing → generic error
    - Counter reset workflow: failed attempts → successful login → counters reset
    - Concurrent login attempts during lockout window (race condition)
    - Window expiry reset: failed attempt outside window → counter reset
    - Status transitions: active → suspended during lockout
    - Focus on database consistency and transaction isolation
  - [x] 4.4 Run feature-specific tests only
    - Run ONLY tests related to failed sign-in handling (tests from 1.1, 2.1, 3.1, and 4.3)
    - Expected total: approximately 20-25 tests maximum
    - Verify all critical authentication workflows pass
    - Verify database state is consistent across scenarios
    - Do NOT run entire application test suite

**Acceptance Criteria:**
- All feature-specific tests pass (approximately 20-25 tests total)
- End-to-end lockout workflow verified via integration tests
- Disabled account special error message verified
- Unknown email timing attack prevention verified
- Counter reset on success verified
- No more than 8 additional tests added when filling gaps
- Database transaction boundaries respected in all scenarios

## Execution Order

Recommended implementation sequence:
1. **Database Layer (Task Group 1)** - Foundation for lockout tracking
2. **Configuration Layer (Task Group 2)** - Can be done in parallel with Task Group 1
3. **Backend Security Layer (Task Group 3)** - Core authentication logic (requires 1 & 2)
4. **Integration Testing (Task Group 4)** - Final validation (requires 1-3)

## Implementation Notes

### Security Considerations
- **Timing attacks**: Unknown email must execute dummy BCrypt.matches() to match known user timing
- **Enumeration prevention**: Generic error for all failures except disabled+correct password
- **Counter integrity**: Unknown emails must never mutate database counters
- **Lockout bypass prevention**: Disabled/pending accounts don't participate in lockout (always return error)

### Database Transaction Boundaries
- Counter updates must be atomic within authentication transaction
- Failed attempt tracking and lockout setting must happen in single transaction
- Counter reset on success must be part of session creation transaction

### Testing Strategy
- Focus on security-critical paths: lockout logic, timing attacks, status handling
- Use @Transactional tests for database layer
- Use @SpringBootTest for integration tests with real authentication flow
- Mock BCryptPasswordEncoder for unit tests, use real encoder for integration tests
- Verify audit logs without exposing sensitive data

### Frontend Compatibility
- No frontend changes required
- Backend must return error messages in exact format: `Map.of("error", "message")`
- LoginPage.tsx already displays `data.error` field from 401 response
- Error messages must match exactly: "Invalid email or password" or "Your account has been disabled. Please contact support."
