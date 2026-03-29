# Spec Initialization: Failed Sign-in Handling

## Original Feature Description

Add enumeration-safe failed sign-in handling to the Rivvy Portal. All failed attempts return a uniform 401 with a generic error, except when a disabled account supplies the correct password, in which case respond 401 with an explicit 'account disabled' message. Enforce environment-configurable lockout (threshold, time window, duration) with counters persisted in the Rivvy Portal DB. Reset counters on successful sign-in. Invited status is always denied with the generic message. Basic auditing via logs only.

## In Scope
- Rivvy Portal Services (Portal API) sign-in failure behavior: enumeration-safe responses with HTTP 401 for all failures
- Environment-configurable lockout policy (failure threshold, time window, lockout duration)
- Persisted counters and lockout-until in DB; reset on success and after lockout expiry
- Denial of sign-in for non-active users (invited, disabled) with policy-compliant messaging
- Special-case: reveal 'account disabled' only when the supplied password is correct (still 401)
- Login screen (/login) error message mapping to generic vs disabled messaging
- Basic auditing in logs for failed attempts, lockouts start/end

## Out of Scope
- Successful sign-in/session establishment and role-based post-login routing (handled by sibling features)
- Admin flows to enable/disable accounts or manage statuses
- Forgot password, invitation completion, or SSO/OIDC flows
- CAPTCHA/MFA or per-IP/global rate limiting for /login
- Any UI outside the existing Login screen

## Sibling Items (handled separately)
- [COMPLETED] Sign in with valid credentials: As a user, I can successfully authenticate with a valid email and password, resulting in an authenticated session; post-authentication destination is handled by the role-based routing feature.
- [IN_PROGRESS] Disabled account handling

## Assumptions
- Only UserAccount.status=active can authenticate; invited and disabled cannot.
- Email normalization (trim, lowercase/case-insensitive) already exists in the auth pipeline.
- Unknown email attempts must mimic known-user failures (timing and payload) and never mutate DB counters.
- Lockout is per-user (email/UserAccount), not per-IP or per-organization.
- Implementation may add DB fields to user_account to persist counters and lockout-until.

## Acceptance Criteria
- Given an unknown email or wrong password, when I attempt to sign in, then the API responds 401 with a generic error and the UI shows a non-revealing failure message.
- Given a disabled account and the correct password, when I attempt to sign in, then the API responds 401 and the UI explicitly indicates the account is disabled (no session is created).
- Given a disabled account and an incorrect password, when I attempt to sign in, then the API responds 401 with the same generic error as other failures.
- Given an invited account (any password), when I attempt to sign in, then the API responds 401 with a generic error and no alternative setup path is shown.
- Given failed attempts for an existing user, when failures reach the configured threshold within the configured time window, then the user is locked for the configured duration.
- Given a locked account, when any sign-in attempt is made (any password), then the API responds 401 with a generic error until the lockout duration expires.
- Given a successful sign-in (active and not locked), when authentication succeeds, then any prior failed-attempt counters for that user are cleared.
- Given a lockout period has expired, when the next sign-in attempt occurs, then the account is no longer locked and counters are reset per policy.
- Given any failed sign-in attempt for a known user, when it occurs, then a log entry records the attempt outcome and increments counters; when a lockout starts or ends, then log entries record those events.
- Given environment configuration is set for threshold, time window, and lockout duration, when the service starts, then the policy is applied without code changes across environments.
