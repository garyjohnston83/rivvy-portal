# Spec Initialization: Blocked and Failed Sign-In Handling

## Feature Description

On the Rivvy Portal Login screen (/login) and Portal API, sign-in must deny authentication for UserAccount records with status disabled or invited, returning a unified 401 response with a single generic, non-enumerative error message. A Remember me control on the Login UI (unchecked by default) enables extended sessions: when selected, authenticated sessions persist for up to 180 days of inactivity (rolling). If an account is disabled after sign-in, all existing sessions are immediately invalidated upon the next request. No additional audit store beyond application logs is required.

## In Scope
- Blocking sign-in for disabled and invited UserAccount records with a single generic 401 response via Portal API and reflected in the UI
- Remember me option on the Login screen (unchecked by default) that extends session inactivity timeout to 180 days with rolling refresh
- Immediate session invalidation behavior when a user becomes disabled (enforced by auth middleware on subsequent requests)
- Consistent generic error messaging for disabled/invited sign-in attempts

## Out of Scope
- General invalid-credential enumeration-safety and temporary lockout (handled by sibling story)
- Password reset/forgot password flows and emails
- SSO/OIDC flows
- CAPTCHA, device/IP-based rate limiting, or broader security hardening
- UI redesign beyond adding Remember me and showing a generic error
- Dedicated audit storage beyond application logs

## Sibling Items (handled separately — do NOT implement these)
- [COMPLETED] Sign in with valid credentials: As a user, I can successfully authenticate with a valid email and password, resulting in an authenticated session; post-authentication destination is handled by the role-based routing feature.

## Assumptions
- UserAccount.status values active|invited|disabled are authoritative for eligibility.
- Generic error string: "Invalid email or password." returned with HTTP 401 for disabled/invited attempts.
- Remember me is a rolling inactivity timeout of 180 days (no separate absolute lifetime cap).
- Non-Remember me sessions retain the existing 8-hour inactivity timeout from the completed sign-in story.
- Session storage already exists from the completed sign-in story; this change only adjusts TTL behavior and adds status checks.
- Session invalidation on disablement is acceptable to enforce at request time via auth middleware (and optionally via a helper to revoke stored sessions when available).
- Remember me applies to all roles.

## Acceptance Criteria
- Given a disabled user account, when correct credentials are submitted on /login, then the API responds 401 with a single generic error message, no session is created, and the UI shows the same generic message.
- Given an invited (not yet activated) user account, when correct credentials are submitted, then the API responds 401 with the same generic error message, no session is created, and the UI shows the same generic message.
- Given any disabled or invited account, when credentials are incorrect, then the API still responds 401 with the same generic error message and the UI shows the same generic message.
- Given an active account and Remember me is unchecked at sign-in, then the session follows the existing 8-hour inactivity timeout (unchanged baseline).
- Given an active account and Remember me is checked at sign-in, then the authenticated session remains valid across browser restarts and inactivity up to 180 days; if inactivity exceeds 180 days, the user must sign in again.
- Given Remember me is checked, when the user closes and reopens the browser within 180 days, then the user remains signed in without re-authenticating.
- Given an active session (with or without Remember me), when the user's account status changes to disabled, then all of the user's active sessions are revoked on the next request and subsequent authenticated requests return 401, requiring sign-in.
- Given disabled or invited sign-in attempts, when observing API responses, then all such attempts return HTTP 401 with the same generic error payload (no reason codes or fields indicating disabled/invited).
- Given the Login screen, when it renders, then a Remember me checkbox is displayed and is unchecked by default.

## Spec Path
`/app/workspace/rivvy-studios/rivvy-portal/agent-os/specs/2026-03-29-blocked-and-failed-signin-handling`

## Date Created
2026-03-29
