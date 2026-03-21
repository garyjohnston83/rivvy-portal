# Spec Initialization: Login Authentication

## Raw Idea

On the Rivvy Portal UI Login screen (/login), users enter email, password, and optional Remember me. Rivvy Portal Services (Portal API) authenticates against a local password store. If the account is ACTIVE and credentials are valid, the user is signed in. Post-auth redirect behavior: if login was triggered by a protected deep link, return to the originally requested URL; otherwise route by role—global Admin to /admin; Client or Producer to /dashboard. When Remember me is checked, the session is sliding and persists across browser restarts for up to 180 days since last activity (and is invalidated upon explicit sign-out). Without Remember me, the session uses a 30-minute idle-based timeout.

## In Scope
- Login form in Rivvy Portal UI (/login) with email, password, and Remember me
- Local password authentication via Rivvy Portal Services (Portal API)
- Role-based routing: global Admin -> /admin; Client or Producer -> /dashboard
- Deep-link return: redirect to originally requested protected URL after successful sign-in
- Session behavior: Remember me = sliding 180-day persistence across restarts; No Remember me = 30-minute idle-based timeout
- Secure session cookie issuance and server-side session store

## Out of Scope
- User registration, password reset, and account recovery
- SSO or MFA flows
- Account lockout, rate limiting, and CAPTCHA
- Role management or assignment changes
- Admin or dashboard feature content beyond routing
- Organization switching UX
- Sign-out UI/API (session model will support revocation but delivery is separate)

## Assumptions
- Active account means UserAccount.status = 'ACTIVE' only.
- Global Admin takes precedence over any other roles for routing.
- Client and Producer roles both route to /dashboard.
- Non-remember session timeout is 30 minutes of inactivity (sliding).
- Remember me is sliding up to 180 days since last activity and is invalidated on explicit sign-out across devices.
- If no valid deep link is present (missing, invalid, or external), fall back to role-based landing.
- Local password store and hashing (e.g., bcrypt) are already in place for UserAccount.

## Acceptance Criteria
- Given an ACTIVE account with valid email and password, when I submit the form on /login, then I am signed in and routed to /admin if I have the global Admin role, otherwise to /dashboard if I am a Client or Producer.
- Given I checked Remember me, when I sign in successfully, then my session persists across browser restarts and remains valid with activity for up to 180 days since my last activity, and otherwise expires.
- Given I did not check Remember me, when I sign in successfully, then my session expires after 30 minutes of inactivity.
- Given I attempted to access a protected URL and was redirected to /login, when I sign in successfully, then I am redirected to the originally requested URL.
- Given my account is not ACTIVE, when I submit valid-looking credentials, then I am not signed in and receive an appropriate error.
- Given my credentials are invalid, when I submit the form, then I am not signed in and receive a generic invalid credentials error.

## Architecture Context
- The architecture model defines the Login ui_screen at /login
- Portal API (REST) is exposed by Rivvy Portal Services
- Relevant physical tables: user_account (with status, email, auth_provider, external_subject_id), role (code, display_name), org_role_assignment (member_id, role_id), organization_member (org_id, user_id)
- The user_account table currently has no password_hash column — this will need to be added

## Date Initialized
2026-03-16
