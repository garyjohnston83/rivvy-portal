# Spec Initialization: Disabled Account Handling

## Feature Description

This story defines disabled account handling for password-based sign-in on the Login screen in the Rivvy Portal. When a UserAccount is in disabled status, the system must block authentication, show a specific disabled-account message with recovery guidance, and count the attempt toward failed-login handling. In addition, if a user already has an authenticated session and their account later becomes disabled, that session must be invalidated so they can no longer access the portal. This applies consistently across Rivvy Admin, Rivvy Producer, and Client users.

## In Scope
- Password-based sign-in handling for disabled UserAccount records on the Login screen
- Blocking session creation when a disabled account submits credentials
- Showing a specific disabled-account message on the Login screen
- Showing recovery guidance for disabled users such as contacting an administrator or support
- Counting disabled-account sign-in attempts toward failed-login handling
- Invalidating an already authenticated session if the account becomes disabled
- Applying disabled-account handling consistently across Rivvy Admin, Rivvy Producer, and Client users

## Out of Scope
- Successful sign-in behavior for active accounts
- Session inactivity timeout and remember-me behavior
- Email trimming and case-insensitive email matching
- Post-login destination or role-based routing after successful authentication
- Definition of general failed-login and lockout rules beyond the requirement that disabled-account attempts count toward them
- SSO or external-auth sign-in flows implied by authProvider
- Admin workflows for disabling or re-enabling accounts

## Sibling Items (handled separately — do NOT implement these)
- [COMPLETED] Sign in with valid credentials: As a user, I can successfully authenticate with a valid email and password, resulting in an authenticated session; post-authentication destination is handled by the role-based routing feature.

## Assumptions
- Disabled is represented by UserAccount.status = disabled in the existing account model
- The specific disabled-account message is an intentional product choice even though it is not enumeration-safe
- Recovery guidance can be satisfied by Login-screen copy instructing the user to contact an administrator or support
- Forced session invalidation can be enforced at the next authenticated request, token refresh, or equivalent session validation checkpoint already used by the application
- Existing failed-login and lockout mechanisms already exist and this story integrates disabled-account attempts into those mechanisms rather than redefining them

## Acceptance Criteria
- Given a user account in disabled status and the correct password, when I submit the Login form, then no authenticated session is established.
- Given a user account in disabled status, when I submit the Login form, then I am shown a message indicating that my account is disabled.
- Given a user account in disabled status, when I am shown the disabled-account message, then the Login screen includes recovery guidance such as contacting an administrator or support.
- Given a user account in disabled status, when I attempt password-based sign-in on the Login screen, then the attempt counts toward failed-login handling.
- Given I have an authenticated session and my account is changed to disabled, when the system next validates my access, then my session is invalidated and I can no longer access the portal.
- Given a disabled Rivvy Admin, Rivvy Producer, or Client account, when that user attempts password-based sign-in on the Login screen, then the same disabled-account handling rules apply.

## Architecture Context

The architecture model shows the following business processes:
- user/role management
- organization setup

These processes are relevant to this feature as disabled account handling is part of user/role management and affects how users authenticate across the portal.

---

**Spec Path:** `/app/workspace/rivvy-studios/rivvy-portal/agent-os/specs/2026-04-23-disabled-account-handling`
**Created:** 2026-04-23
