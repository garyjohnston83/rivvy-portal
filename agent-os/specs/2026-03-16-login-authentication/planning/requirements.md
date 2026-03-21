# Spec Requirements: Login Authentication

## Initial Description
On the Rivvy Portal UI Login screen (/login), users enter email, password, and optional Remember me. Rivvy Portal Services (Portal API) authenticates against a local password store. If the account is ACTIVE and credentials are valid, the user is signed in. Post-auth redirect behavior: if login was triggered by a protected deep link, return to the originally requested URL; otherwise route by role—global Admin to /admin; Client or Producer to /dashboard. When Remember me is checked, the session is sliding and persists across browser restarts for up to 180 days since last activity (and is invalidated upon explicit sign-out). Without Remember me, the session uses a 30-minute idle-based timeout.

## Requirements Discussion

### First Round Questions

**Q1:** The `user_account` table currently has no `password_hash` column. Should this spec add a `password_hash` column to `user_account` via a Liquibase migration, using bcrypt for hashing? Or a separate `user_credential` table?
**Answer:** Yes, add password_hash column to user_account with bcrypt.

**Q2:** For local password auth, should we use Spring Security's session-based auth with a custom `UserDetailsService` — not JWT or OAuth2? Session cookie as sole auth token?
**Answer:** Correct.

**Q3:** For the server-side session store, in-memory or JDBC-backed via `spring-session-jdbc`?
**Answer:** JDBC-backed.

**Q4:** What are the expected role codes? Assumed ADMIN, PRODUCER, CLIENT.
**Answer:** Check the database. The actual role codes from seed data are: `RIVVY_ADMIN`, `RIVVY_PRODUCER`, `CLIENT`.

**Q5:** For deep-link return after login — should the backend also be aware of the return URL, or purely frontend routing?
**Answer:** Backend should also be aware of the return URL.

**Q6:** Since sign-out UI/API is out of scope, should we still build server-side session revocation infrastructure?
**Answer:** Defer entirely. Session model will support revocation but delivery is separate.

**Q7:** Login form UI — simple centered card with email, password, Remember me, Submit. No branding/logo/forgot password. Should there be a loading spinner?
**Answer:** Correct. Add loading spinner during submission.

**Q8:** Anything else to exclude?
**Answer:** No.

### Existing Code to Reference

**Similar Features Identified:**
- Entity: `UserAccount` - Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/UserAccount.java`
  - Status enum: ACTIVE, INACTIVE, SUSPENDED, PENDING
  - Currently has `authProvider` and `externalSubjectId` fields but NO `password_hash`
- Entity: `Role` - Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Role.java`
  - Codes: RIVVY_ADMIN, RIVVY_PRODUCER, CLIENT
- Entity: `OrgRoleAssignment` - Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/OrgRoleAssignment.java`
  - Links OrganizationMember to Role
- Entity: `OrganizationMember` - Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/OrganizationMember.java`
  - Links UserAccount to Organization
- Repository: `UserAccountRepository` - Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/UserAccountRepository.java`
- Repository: `OrgRoleAssignmentRepository` - Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/OrgRoleAssignmentRepository.java`
- Seed data: `crud-logic-service/src/main/resources/db/changelog/seed/R__seed_data.sql`
- Application config: `crud-logic-service/src/main/resources/application.yaml`
- POM: `crud-logic-service/pom.xml` (Spring Boot 3.4.1, Java 21 — currently NO spring-boot-starter-security dependency)

**Key finding:** No Spring Security is currently in the project. No SecurityFilterChain, no auth filters, no security config. This is entirely greenfield auth work.

### Follow-up Questions
None required.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A

## Requirements Summary

### Functional Requirements
- **Login Form (Frontend):** Email input, password input, Remember me checkbox, Submit button, loading spinner during submission
- **Authentication Endpoint (Backend):** POST to Portal API accepting email, password, and rememberMe flag
- **Password Storage:** Add `password_hash` column to `user_account` table via Liquibase migration; use bcrypt
- **Credential Validation:** Spring Security `UserDetailsService` backed by `user_account` table; only ACTIVE accounts can authenticate
- **Session Management:** JDBC-backed sessions via `spring-session-jdbc`
  - Remember me checked: sliding 180-day session, persists across browser restarts
  - Remember me unchecked: 30-minute idle timeout
- **Role-Based Routing:** After successful login:
  - RIVVY_ADMIN role → redirect to `/admin`
  - RIVVY_PRODUCER or CLIENT role → redirect to `/dashboard`
  - RIVVY_ADMIN takes precedence if user has multiple roles
- **Deep-Link Return:** Backend aware of return URL; if user attempted a protected URL before login, redirect there post-auth instead of role-based landing; validate return URL is internal
- **Error Handling:**
  - Invalid credentials → generic "Invalid email or password" error
  - Inactive account → appropriate error (not revealing account status details)
- **Loading State:** Spinner on submit button during authentication request

### Reusability Opportunities
- Existing JPA entities (UserAccount, Role, OrgRoleAssignment, OrganizationMember) and their repositories
- Existing Liquibase migration pattern for schema changes
- Existing seed data pattern for reference data

### Scope Boundaries
**In Scope:**
- Login form in Rivvy Portal UI (/login) with email, password, Remember me, loading spinner
- Spring Security integration (add dependency, configure SecurityFilterChain)
- Custom UserDetailsService backed by user_account table
- Liquibase migration to add password_hash to user_account
- JDBC-backed session store (spring-session-jdbc)
- Session cookie with configurable timeout (30min idle vs 180-day sliding)
- POST /api/auth/login endpoint (or Spring Security form-login equivalent)
- Role-based post-login routing (RIVVY_ADMIN → /admin, others → /dashboard)
- Deep-link return URL handling (backend-aware)
- Generic error messages for invalid credentials and inactive accounts

**Out of Scope:**
- User registration, password reset, account recovery
- SSO or MFA flows
- Account lockout, rate limiting, CAPTCHA
- Role management or assignment changes
- Admin or dashboard feature content beyond routing
- Organization switching UX
- Sign-out UI/API and session revocation infrastructure (deferred)

### Technical Considerations
- Backend: Spring Boot 3.4.1, Java 21, Maven — need to add `spring-boot-starter-security` and `spring-session-jdbc` dependencies
- Frontend: React 19.x, TypeScript 5.x, Vite 5.x — existing placeholder LoginPage.tsx needs to become a real form
- Role lookup path: UserAccount → OrganizationMember → OrgRoleAssignment → Role
- RIVVY_ADMIN is a "global" role — need to clarify if it's org-scoped or truly global (currently org_role_assignment is per-org-member)
- The frontend currently has no HTTP client setup — will need fetch/axios for API calls
- CORS configuration will be needed since frontend (port 5200) and backend (port 8080) are on different ports
- Session cookie must be configured for cross-origin if frontend/backend are on different ports in dev
