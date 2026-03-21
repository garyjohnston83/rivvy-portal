# Specification: Login Authentication

## Goal
Implement end-to-end login authentication for Rivvy Portal, allowing users to sign in with email and password on the `/login` screen, with session persistence controlled by a Remember me option, and role-based post-login routing.

## User Stories
- As a portal user, I want to sign in with my email and password so that I can access my dashboard or admin area based on my role.
- As a returning user, I want to check "Remember me" so that my session persists across browser restarts for up to 180 days without re-authenticating.
- As a user who was redirected to login from a protected page, I want to be returned to my originally requested URL after successful authentication.

## Specific Requirements

**Password Storage Migration**
- Add a `password_hash` column (type TEXT, nullable) to the `user_account` table via a new Liquibase changeset (e.g., `022-add-password-hash-to-user-account.sql`)
- Register the new changeset in `db.changelog-master.yaml` after the last existing entry (021) but before the seed data include
- Add `passwordHash` field to the `UserAccount` JPA entity mapped to `password_hash`
- Update seed data (`R__seed_data.sql`) to include bcrypt-hashed passwords for the three seed users (use a known dev password such as `password123`)
- Use `ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash` to ensure seed re-runs apply the hash

**Spring Security Integration**
- Add `spring-boot-starter-security` dependency to `pom.xml`
- Create a `SecurityConfig` class with a `SecurityFilterChain` bean
- Permit unauthenticated access to `/login`, `/api/auth/login`, and static assets
- All other routes require authentication; unauthenticated requests to protected URLs should return 401 (API) or redirect to `/login` (page navigation)
- Disable CSRF for the `/api/auth/**` endpoints (session cookie is SameSite-based protection)
- Configure CORS to allow the frontend origin (`http://localhost:5200`) with credentials enabled

**Custom UserDetailsService**
- Implement `UserDetailsService` backed by `UserAccountRepository`
- Look up user by email (case-insensitive; the `email` column is `citext`)
- Add a `findByEmail(String email)` method to `UserAccountRepository`
- Map `UserAccountStatus.ACTIVE` to an enabled Spring Security account; all other statuses map to disabled
- Load the user's roles by traversing `UserAccount` -> `OrganizationMember` -> `OrgRoleAssignment` -> `Role` and grant authorities using role codes (e.g., `ROLE_RIVVY_ADMIN`)
- Collect roles across all org memberships for the user, not just the default org

**Login Endpoint**
- Expose `POST /api/auth/login` accepting a JSON body with `email` (string), `password` (string), and `rememberMe` (boolean)
- Authenticate using Spring Security's `AuthenticationManager` with `UsernamePasswordAuthenticationToken`
- On success: create a `SecurityContext`, set it on the session, update `last_login_at` on the `UserAccount`, and return a JSON response containing the redirect URL and user info (roles, email, name)
- On failure (bad credentials or disabled account): return HTTP 401 with a generic JSON error body `{"error": "Invalid email or password"}` -- do not reveal whether the email exists or the account is disabled
- Expose `GET /api/auth/me` to let the frontend check if the current session is authenticated; return user info if authenticated, 401 if not

**Session Management (JDBC-backed)**
- Add `spring-session-jdbc` dependency to `pom.xml`
- Spring Session auto-creates its tables (`SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES`) -- enable via `spring.session.store-type=jdbc` in `application.yaml`
- Configure the session cookie name, path (`/`), and `SameSite=Lax`
- When `rememberMe` is true: set session `maxInactiveInterval` to 180 days and configure the cookie with `maxAge` so it persists across browser restarts
- When `rememberMe` is false: set session `maxInactiveInterval` to 30 minutes and use a session-scoped cookie (no `maxAge`, cleared on browser close)
- The session timeout is sliding (resets on each request) -- this is the default Spring Session behavior

**Role-Based Post-Login Routing**
- After successful authentication, the backend computes the redirect URL and returns it in the login response JSON
- If a `returnUrl` query parameter was captured (see Deep-Link Return), use that as the redirect target
- Otherwise: if the user holds `RIVVY_ADMIN` role (takes precedence), redirect to `/admin`; if the user holds `RIVVY_PRODUCER` or `CLIENT`, redirect to `/dashboard`
- The frontend reads the redirect URL from the login response and performs client-side navigation via `react-router-dom`

**Deep-Link Return URL Handling**
- When Spring Security redirects an unauthenticated request to `/login`, preserve the originally requested URL
- Use Spring Security's `SavedRequest` mechanism (default via `HttpSessionRequestCache`) so the backend knows the intended destination
- The login endpoint checks for a saved request; if present, it takes priority over role-based routing
- Validate the return URL is a relative path (starts with `/`) to prevent open-redirect attacks

**Login Form (Frontend)**
- Replace the placeholder `LoginPage.tsx` with a functional login form
- Centered card layout containing: email input (type `email`, required), password input (type `password`, required), "Remember me" checkbox (unchecked by default), and a Submit button
- On submit, POST to `/api/auth/login` with `{ email, password, rememberMe }` as JSON
- Display a loading spinner on/over the Submit button while the request is in flight; disable the form inputs during loading
- On success, navigate to the redirect URL from the response using `useNavigate()` from `react-router-dom`
- On 401 error, display the error message ("Invalid email or password") below the form or above the Submit button
- No branding, logo, or forgot-password link
- Add a Vite dev proxy configuration in `vite.config.ts` to forward `/api/**` requests to `http://localhost:8080` so that cookies work without CORS issues in development

**Error Handling**
- Backend returns HTTP 401 for all authentication failures with `{"error": "Invalid email or password"}`
- Do not differentiate between "user not found", "wrong password", or "account disabled" in the API response
- Frontend displays the error string from the response body beneath the form fields

## Visual Design
No visual assets provided. The login form should be a simple centered card with email, password, Remember me, Submit, and inline error display. No branding or decorative elements.

## Existing Code to Leverage

**UserAccount Entity and Repository (`model/UserAccount.java`, `repository/UserAccountRepository.java`)**
- Existing JPA entity maps to `user_account` table with email, status, lastLoginAt, and authProvider fields
- Repository currently has no custom query methods; needs a `findByEmail` method added
- The `passwordHash` field must be added to the entity to map the new column
- The `lastLoginAt` field already exists and should be updated on each successful login

**Role, OrgRoleAssignment, and OrganizationMember Entities**
- Role lookup path: `UserAccount` -> `OrganizationMember` (via `user_id`) -> `OrgRoleAssignment` (via `member_id`) -> `Role` (via `role_id`)
- `OrgRoleAssignmentRepository` and `OrganizationMemberRepository` exist but have no custom queries; will need query methods to find memberships by user and assignments by member
- Role codes from seed data are `RIVVY_ADMIN`, `RIVVY_PRODUCER`, `CLIENT`

**Liquibase Changelog Pattern (`db/changelog/`)**
- Changesets use `--liquibase formatted sql` header with `--changeset rivvy:<name>` convention
- Master changelog is YAML-based, includes changesets in numbered order then seed data last
- New migration should follow the naming pattern `022-add-password-hash-to-user-account.sql`

**Frontend Routing and Layout (`App.tsx`, `RootLayout.tsx`, `LoginPage.tsx`)**
- React Router v7 with `BrowserRouter` is already configured in `main.tsx`
- `LoginPage.tsx` is a placeholder that needs to be replaced with the full form implementation
- `RootLayout.tsx` wraps all routes with a centered container; login form renders within this layout
- Routes for `/admin` and `/dashboard` already exist and are the post-login targets

**Application Configuration (`application.yaml`, `pom.xml`)**
- Spring Boot 3.4.1 with Java 21, Maven build
- PostgreSQL datasource already configured on port 5432 with `rivvy_portal` database
- No Spring Security or Spring Session dependencies exist yet; both must be added to `pom.xml`
- Vite dev server runs on port 5200; backend on port 8080

## Out of Scope
- User registration, sign-up flow, or account creation UI
- Password reset, forgot password, or account recovery functionality
- SSO, OAuth2, OpenID Connect, or any external identity provider integration
- Multi-factor authentication (MFA) or two-step verification
- Account lockout after failed attempts, brute-force rate limiting, or CAPTCHA
- Role management, role assignment changes, or admin role-editing UI
- Sign-out UI, sign-out API endpoint, or session revocation infrastructure (deferred to a separate spec)
- Admin page content or dashboard page content beyond routing to those paths
- Organization switching UX or multi-org session context
- Password complexity validation rules or password strength enforcement
