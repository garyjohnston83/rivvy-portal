# Task Breakdown: Login Authentication

## Overview
Total Tasks: 38

This breakdown implements end-to-end login authentication for Rivvy Portal: database migration for password storage, Spring Security integration with JDBC-backed sessions, a login REST API with role-based routing, and a functional React login form. The work is organized into five task groups by specialization, ordered to respect dependencies between the database, backend security, API, session, and frontend layers.

## Task List

### Database Layer

#### Task Group 1: Password Hash Migration and Seed Data
**Dependencies:** None

- [x] 1.0 Complete password hash database migration and seed data updates
  - [x] 1.1 Write 4 focused tests for the password hash migration and seed data
    - Test that the `password_hash` column exists on the `user_account` table after migration
    - Test that `UserAccount` entity can be persisted and retrieved with a non-null `passwordHash` value
    - Test that `UserAccountRepository.findByEmail` returns the correct user (case-insensitive match)
    - Test that `findByEmail` returns empty for a non-existent email address
  - [x] 1.2 Create Liquibase migration `022-add-password-hash-to-user-account.sql`
    - File: `src/main/resources/db/changelog/changesets/022-add-password-hash-to-user-account.sql`
    - Use `--liquibase formatted sql` header with `--changeset rivvy:022-add-password-hash-to-user-account`
    - Add column: `ALTER TABLE user_account ADD COLUMN password_hash TEXT;`
    - Column is nullable (existing users may not have a local password)
  - [x] 1.3 Register the new changeset in `db.changelog-master.yaml`
    - Insert the `022` changeset include entry after the `021-add-video-latest-version-fk.sql` entry
    - Must appear before the seed data include (`seed/R__seed_data.sql`)
  - [x] 1.4 Add `passwordHash` field to the `UserAccount` JPA entity
    - File: `src/main/java/com/rivvystudios/portal/model/UserAccount.java`
    - Add field: `private String passwordHash;`
    - Annotate with `@Column(name = "password_hash")`
    - Add getter and setter methods
  - [x] 1.5 Add `findByEmail` method to `UserAccountRepository`
    - File: `src/main/java/com/rivvystudios/portal/repository/UserAccountRepository.java`
    - Add: `Optional<UserAccount> findByEmail(String email);`
    - The `email` column is `citext` so PostgreSQL handles case-insensitive matching natively
  - [x] 1.6 Update seed data with bcrypt password hashes
    - File: `src/main/resources/db/changelog/seed/R__seed_data.sql`
    - Change the three `user_account` INSERT statements from `ON CONFLICT (id) DO NOTHING` to `ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash`
    - Add `password_hash` column to each INSERT with a bcrypt hash of `password123`
    - Generate the hash using BCrypt (cost factor 10); all three seed users share the same dev password
  - [x] 1.7 Ensure database layer tests pass
    - Run ONLY the 4 tests written in 1.1
    - Verify migrations run successfully via Testcontainers PostgreSQL
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 4 tests written in 1.1 pass
- Migration `022` adds `password_hash` column to `user_account`
- `UserAccount` entity maps the new column correctly
- `findByEmail` returns users with case-insensitive matching
- Seed data re-runs apply bcrypt hashes to all three seed users

---

### Backend Security Layer

#### Task Group 2: Spring Security Configuration and UserDetailsService
**Dependencies:** Task Group 1

- [x] 2.0 Complete Spring Security configuration and custom authentication
  - [x] 2.1 Write 6 focused tests for security configuration and UserDetailsService
    - Test that `POST /api/auth/login` is accessible without authentication (returns 401 for bad creds, not 403)
    - Test that `GET /api/auth/me` returns 401 when no session is present
    - Test that a protected endpoint (e.g., `/api/auth/me`) returns 401 for unauthenticated API requests (not a redirect)
    - Test that `UserDetailsService.loadUserByUsername` returns a valid UserDetails for an ACTIVE user with correct authorities
    - Test that `UserDetailsService.loadUserByUsername` returns a disabled UserDetails for a non-ACTIVE user
    - Test that `UserDetailsService.loadUserByUsername` throws `UsernameNotFoundException` for an unknown email
  - [x] 2.2 Add Spring Security and Spring Session dependencies to `pom.xml`
    - Add `spring-boot-starter-security` dependency
    - Add `spring-session-jdbc` dependency
    - These are needed together because SecurityConfig references session configuration
  - [x] 2.3 Create `SecurityConfig` class
    - File: `src/main/java/com/rivvystudios/portal/config/SecurityConfig.java`
    - Annotate with `@Configuration` and `@EnableWebSecurity`
    - Define a `SecurityFilterChain` bean
    - Permit unauthenticated access to: `/login`, `/api/auth/login`, `/api/auth/me`, and static asset paths (`/assets/**`, `/favicon.ico`, `/index.html`, `/`)
    - All other routes require authentication
    - For API requests (path starts with `/api/`): return 401 on authentication failure (use a custom `AuthenticationEntryPoint`)
    - For page navigation requests: redirect to `/login` (default Spring behavior)
    - Disable CSRF for `/api/auth/**` endpoints
    - Configure CORS to allow origin `http://localhost:5200` with credentials enabled
    - Expose an `AuthenticationManager` bean from `AuthenticationConfiguration`
    - Configure a `BCryptPasswordEncoder` bean
  - [x] 2.4 Implement custom `UserDetailsService`
    - File: `src/main/java/com/rivvystudios/portal/security/PortalUserDetailsService.java`
    - Implement `org.springframework.security.core.userdetails.UserDetailsService`
    - Inject `UserAccountRepository`
    - Look up user by email using `findByEmail`; throw `UsernameNotFoundException` if not found
    - Map `UserAccountStatus.ACTIVE` to `enabled=true`; all other statuses to `enabled=false`
    - Traverse role path: `UserAccount` -> `OrganizationMember` (via user_id) -> `OrgRoleAssignment` (via member_id) -> `Role` (via role_id)
    - Collect roles across ALL org memberships, not just the default org
    - Grant authorities using role codes prefixed with `ROLE_` (e.g., `ROLE_RIVVY_ADMIN`)
    - Return a Spring Security `User` object with email as username, passwordHash as password, enabled flag, and granted authorities
    - Requires custom queries: add `findByUserAccount` to `OrganizationMemberRepository` and `findByOrganizationMember` to `OrgRoleAssignmentRepository` (or use a single join query)
  - [x] 2.5 Add Spring Session JDBC configuration to `application.yaml`
    - Add `spring.session.store-type: jdbc` to `application.yaml`
    - Spring Session auto-creates `SPRING_SESSION` and `SPRING_SESSION_ATTRIBUTES` tables
    - Configure session cookie: name `RIVVY_SESSION`, path `/`, `SameSite=Lax`
    - Set default session timeout to 30 minutes (`server.servlet.session.timeout: 30m`)
  - [x] 2.6 Ensure security layer tests pass
    - Run ONLY the 6 tests written in 2.1
    - Verify security filter chain permits and blocks correct endpoints
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 6 tests written in 2.1 pass
- Spring Security filter chain correctly permits public endpoints and blocks protected ones
- API endpoints return 401 (not redirects) for unauthenticated requests
- UserDetailsService correctly loads users, maps active/disabled status, and resolves role authorities across all org memberships
- BCrypt password encoder is available as a bean
- Spring Session JDBC is configured and session tables are auto-created

---

### API Layer

#### Task Group 3: Authentication Endpoints and Session Management
**Dependencies:** Task Group 2

- [x] 3.0 Complete login and session-check API endpoints
  - [x] 3.1 Write 8 focused tests for authentication endpoints
    - Test successful login with valid credentials returns 200 with user info (email, name, roles) and a redirect URL
    - Test login with wrong password returns 401 with `{"error": "Invalid email or password"}`
    - Test login with non-existent email returns 401 with the same generic error (no email enumeration)
    - Test login with a disabled/non-ACTIVE account returns 401 with the same generic error
    - Test that successful login sets `last_login_at` on the `UserAccount`
    - Test that `rememberMe: true` sets session maxInactiveInterval to 180 days
    - Test that `rememberMe: false` (or omitted) keeps session maxInactiveInterval at 30 minutes
    - Test that `GET /api/auth/me` returns user info when a valid session exists
  - [x] 3.2 Create `AuthController` with login endpoint
    - File: `src/main/java/com/rivvystudios/portal/controller/AuthController.java`
    - Annotate with `@RestController` and `@RequestMapping("/api/auth")`
    - Implement `POST /login` accepting JSON body: `{ "email": string, "password": string, "rememberMe": boolean }`
    - Create a request DTO class `LoginRequest` with `email`, `password`, and `rememberMe` fields
    - Create a response DTO class `LoginResponse` with `redirectUrl`, `email`, `firstName`, `lastName`, and `roles` (list of role code strings)
    - Authenticate using injected `AuthenticationManager` with `UsernamePasswordAuthenticationToken`
    - Wrap authentication in try-catch; on `AuthenticationException` return 401 with `{"error": "Invalid email or password"}`
  - [x] 3.3 Implement session management in the login flow
    - On successful authentication:
      - Create `SecurityContext`, set the authentication, store in `SecurityContextHolder`
      - Save the context to the HTTP session via `HttpSessionSecurityContextRepository`
      - If `rememberMe` is true: set `session.setMaxInactiveInterval(180 * 24 * 60 * 60)` (180 days in seconds)
      - If `rememberMe` is false: set `session.setMaxInactiveInterval(1800)` (30 minutes in seconds)
    - Cookie maxAge handling for rememberMe requires a custom `SessionRepositoryFilter` or `CookieSerializer` configuration
    - In `SecurityConfig` (or a new `SessionConfig`), configure a `DefaultCookieSerializer` bean:
      - Set cookie name to `RIVVY_SESSION`
      - Set cookie path to `/`
      - Set `SameSite=Lax`
      - Note: dynamically setting `maxAge` per-request (rememberMe vs. session cookie) requires a custom approach -- either a filter that adjusts the cookie after login or a custom `CookieSerializer` wrapper
  - [x] 3.4 Implement post-login redirect URL computation
    - After successful authentication, compute the redirect URL:
      - Check for a Spring Security `SavedRequest` in the session (via `HttpSessionRequestCache`); if present, extract the redirect URL
      - Validate the saved request URL is a relative path (starts with `/`) to prevent open-redirect attacks
      - If no saved request: determine redirect by role -- `RIVVY_ADMIN` role maps to `/admin`; `RIVVY_PRODUCER` or `CLIENT` maps to `/dashboard`
      - `RIVVY_ADMIN` takes precedence if user has multiple roles
    - Include the computed redirect URL in the `LoginResponse`
  - [x] 3.5 Update `last_login_at` on successful login
    - After successful authentication, look up the `UserAccount` by email
    - Set `lastLoginAt` to `Instant.now()`
    - Save the updated entity via `UserAccountRepository`
  - [x] 3.6 Implement `GET /api/auth/me` endpoint
    - In `AuthController`, add `GET /me`
    - If the current `SecurityContext` has an authenticated principal, return user info (same shape as login response, minus redirectUrl)
    - If not authenticated, return 401 (handled automatically by the security filter chain)
    - The frontend will call this on app load to check for an existing session
  - [x] 3.7 Ensure API layer tests pass
    - Run ONLY the 8 tests written in 3.1
    - Verify login success/failure flows, session configuration, and redirect URL computation
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 8 tests written in 3.1 pass
- `POST /api/auth/login` authenticates users and returns correct response shape
- All authentication failures return identical 401 error responses (no information leakage)
- `rememberMe` flag correctly controls session timeout duration
- Role-based redirect URL is computed correctly with RIVVY_ADMIN taking precedence
- Saved request (deep-link return) takes priority over role-based routing
- `last_login_at` is updated on each successful login
- `GET /api/auth/me` returns user info for authenticated sessions and 401 otherwise

---

### Frontend Layer

#### Task Group 4: Login Form and Dev Proxy Configuration
**Dependencies:** Task Group 3

- [x] 4.0 Complete the login page UI and Vite dev proxy
  - [x] 4.1 Write 5 focused tests for the login form component
    - Test that the form renders email input, password input, Remember me checkbox, and Submit button
    - Test that submitting with valid input calls `POST /api/auth/login` with correct JSON payload including `rememberMe`
    - Test that a 401 response displays the error message "Invalid email or password" in the form
    - Test that the Submit button shows a loading state and inputs are disabled while the request is in flight
    - Test that a successful response triggers navigation to the redirect URL from the response
  - [x] 4.2 Configure Vite dev proxy for API requests
    - File: `rivvy-portal-ui/vite.config.ts`
    - Add a `server.proxy` entry to forward `/api/**` requests to `http://localhost:8080`
    - Enable `changeOrigin: true` so the Host header matches the backend
    - This ensures cookies (session) work correctly during development without CORS issues
  - [x] 4.3 Replace `LoginPage.tsx` with functional login form
    - File: `rivvy-portal-ui/src/pages/LoginPage.tsx`
    - Centered card layout (use a simple `<div>` with CSS for centering; no external UI library required)
    - Email input: `type="email"`, `required`, labeled "Email"
    - Password input: `type="password"`, `required`, labeled "Password"
    - "Remember me" checkbox: unchecked by default, labeled "Remember me"
    - Submit button: labeled "Sign in"
    - Use React state to manage form field values, loading state, and error message
  - [x] 4.4 Implement form submission and API integration
    - On form submit, prevent default and POST to `/api/auth/login` with `{ email, password, rememberMe }` as JSON
    - Set `Content-Type: application/json` and include `credentials: 'include'` in the fetch options
    - While request is in flight: show a loading indicator on/over the Submit button and disable all form inputs
    - On success (200): read `redirectUrl` from the response JSON and navigate using `useNavigate()` from `react-router-dom`
    - On 401: read the `error` field from the response JSON and display it below the form fields (above the Submit button)
    - On network error: display a generic "Unable to connect. Please try again." message
  - [x] 4.5 Add basic styling for the login form
    - Center the card vertically and horizontally on the page
    - Stack form fields vertically with appropriate spacing
    - Style the error message in a visible color (e.g., red)
    - Style the loading/disabled state to visually indicate the form is processing
    - Follow any existing CSS patterns in the project; no external CSS framework needed
  - [x] 4.6 Ensure frontend tests pass
    - Run ONLY the 5 tests written in 4.1
    - Verify form rendering, submission, error display, and navigation
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 5 tests written in 4.1 pass
- Vite dev proxy forwards `/api/**` to `http://localhost:8080` with credentials
- Login form renders all required fields (email, password, Remember me checkbox, Sign in button)
- Form submits correct JSON payload to `POST /api/auth/login`
- Loading state disables inputs and shows indicator during request
- 401 errors display the error message in the form
- Successful login navigates to the redirect URL from the response

---

### Testing

#### Task Group 5: Test Review and Gap Analysis
**Dependencies:** Task Groups 1-4

- [x] 5.0 Review existing tests and fill critical gaps only
  - [x] 5.1 Review tests from Task Groups 1-4
    - Review the 4 tests written by database layer (Task 1.1)
    - Review the 6 tests written by security layer (Task 2.1)
    - Review the 8 tests written by API layer (Task 3.1)
    - Review the 5 tests written by frontend layer (Task 4.1)
    - Total existing tests: approximately 23 tests
  - [x] 5.2 Analyze test coverage gaps for login authentication feature only
    - Identify critical end-to-end user workflows that lack test coverage
    - Focus ONLY on gaps related to login authentication requirements
    - Do NOT assess entire application test coverage
    - Prioritize integration workflows: login -> session creation -> redirect; session persistence across requests; deep-link return flow
  - [x] 5.3 Write up to 10 additional strategic tests maximum
    - Suggested gap areas (write tests only where genuinely needed):
      - End-to-end: login with RIVVY_ADMIN user and verify redirect to `/admin`
      - End-to-end: login with RIVVY_PRODUCER user and verify redirect to `/dashboard`
      - End-to-end: login with CLIENT user and verify redirect to `/dashboard`
      - Integration: verify `GET /api/auth/me` works with the session cookie set by a prior login
      - Integration: verify that a saved request (deep-link) overrides role-based redirect
      - Security: verify return URL validation rejects absolute URLs (open-redirect prevention)
      - Session: verify rememberMe cookie behavior (persistent vs. session-scoped)
      - Security: verify CORS headers are present for cross-origin requests from `http://localhost:5200`
    - Do NOT write comprehensive coverage for all scenarios
    - Skip edge cases, performance tests, and accessibility tests unless business-critical
  - [x] 5.4 Run feature-specific tests only
    - Run ONLY tests related to login authentication (tests from 1.1, 2.1, 3.1, 4.1, and 5.3)
    - Expected total: approximately 25-33 tests maximum
    - Do NOT run the entire application test suite
    - Verify all critical login workflows pass

**Acceptance Criteria:**
- All feature-specific tests pass (approximately 25-33 tests total)
- Critical end-to-end login workflows are covered (admin redirect, producer redirect, client redirect)
- Deep-link return URL flow is tested
- Open-redirect prevention is verified
- Session persistence behavior for both rememberMe modes is tested
- No more than 10 additional tests added
- Testing focused exclusively on login authentication feature requirements

---

## Execution Order

Recommended implementation sequence:

1. **Database Layer** (Task Group 1) -- No dependencies. Adds the `password_hash` column, updates the entity and repository, seeds bcrypt hashes. All subsequent groups depend on this.
2. **Backend Security Layer** (Task Group 2) -- Depends on Group 1. Adds Spring Security and Spring Session dependencies, configures the security filter chain, implements UserDetailsService with role resolution, and configures JDBC-backed sessions.
3. **API Layer** (Task Group 3) -- Depends on Group 2. Builds the `AuthController` with login and session-check endpoints, implements session timeout logic for rememberMe, computes role-based redirect URLs, and updates `last_login_at`.
4. **Frontend Layer** (Task Group 4) -- Depends on Group 3. Configures the Vite dev proxy, replaces the placeholder LoginPage with a functional form, handles submission/error/loading states, and navigates on success.
5. **Test Review and Gap Analysis** (Task Group 5) -- Depends on Groups 1-4. Reviews all tests written during development, identifies critical gaps in end-to-end workflows, and adds up to 10 targeted integration tests.

## Key Files Modified or Created

### New Files
- `src/main/resources/db/changelog/changesets/022-add-password-hash-to-user-account.sql` -- Liquibase migration
- `src/main/java/com/rivvystudios/portal/config/SecurityConfig.java` -- Spring Security filter chain and beans
- `src/main/java/com/rivvystudios/portal/security/PortalUserDetailsService.java` -- Custom UserDetailsService
- `src/main/java/com/rivvystudios/portal/controller/AuthController.java` -- Login and session-check endpoints
- `src/main/java/com/rivvystudios/portal/controller/dto/LoginRequest.java` -- Request DTO
- `src/main/java/com/rivvystudios/portal/controller/dto/LoginResponse.java` -- Response DTO

### Modified Files
- `pom.xml` -- Add spring-boot-starter-security and spring-session-jdbc dependencies
- `src/main/resources/application.yaml` -- Add Spring Session and cookie configuration
- `src/main/resources/db/changelog/db.changelog-master.yaml` -- Register changeset 022
- `src/main/resources/db/changelog/seed/R__seed_data.sql` -- Add bcrypt password hashes to seed users
- `src/main/java/com/rivvystudios/portal/model/UserAccount.java` -- Add passwordHash field
- `src/main/java/com/rivvystudios/portal/repository/UserAccountRepository.java` -- Add findByEmail method
- `src/main/java/com/rivvystudios/portal/repository/OrganizationMemberRepository.java` -- Add findByUserAccount query (for role resolution)
- `src/main/java/com/rivvystudios/portal/repository/OrgRoleAssignmentRepository.java` -- Add findByOrganizationMember query (for role resolution)
- `rivvy-portal-ui/vite.config.ts` -- Add dev proxy for `/api/**`
- `rivvy-portal-ui/src/pages/LoginPage.tsx` -- Replace placeholder with functional login form
