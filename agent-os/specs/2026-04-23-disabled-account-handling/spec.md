# Specification: Disabled Account Handling

## Goal
When a UserAccount has INACTIVE or SUSPENDED status, the system must block password-based authentication at login, display a specific disabled-account message with recovery guidance, count these attempts toward failed-login tracking and potential lockout, and invalidate any existing authenticated sessions at the next request validation checkpoint.

## User Stories
- As a user with a disabled account, I want to see a clear message explaining my account is disabled and how to get help, so I understand why I cannot sign in and know what action to take.
- As a system administrator, I want disabled account login attempts to count toward lockout thresholds, so accounts cannot be brute-forced even when disabled.

## Specific Requirements

**Disabled Account Detection on Login**
- Check UserAccount.status field after retrieving the user from the database
- Treat both INACTIVE and SUSPENDED statuses as "disabled" for authentication purposes
- Perform this check after lockout validation but before Spring Security authentication
- Continue to verify the password even for disabled accounts to determine which error message to show

**Disabled Account Error Message**
- Display message: "Your account has been disabled. Please contact your administrator or support@rivvy.com for assistance."
- Show this specific message only when the password is correct (intentional non-enumeration-safe behavior per product decision)
- Show generic "Invalid email or password" message when password is incorrect for a disabled account
- Return HTTP 401 status for both cases
- Frontend LoginPage already has error display mechanism at LoginPage.tsx:86-88 using the error state

**Failed Login Attempt Tracking for Disabled Accounts**
- Increment failed login counter (failedAttemptsCount) for disabled account attempts with incorrect passwords
- Use existing handleFailedAttempt() method in AuthController.java:185-211
- Track firstFailedAttemptAt, lastFailedAttemptAt, and failedAttemptsCount fields on UserAccount
- Apply existing lockout logic when threshold is reached (configured via LockoutProperties)
- Do NOT increment counter when password is correct (only show disabled message)

**Lockout Mechanism Integration**
- Disabled accounts can trigger lockout after repeated failed attempts (when password is wrong)
- Use existing lockout window (default 15 minutes) and threshold (default 5 attempts) from LockoutProperties
- Set lockedUntil timestamp when threshold is reached
- Lockout duration configured via LockoutProperties.durationMinutes (default 30 minutes)

**Session Invalidation for Disabled Accounts**
- When an account's status changes to INACTIVE or SUSPENDED while a session exists, invalidate that session
- Spring Security already checks UserDetailsService.loadUserByUsername() which sets enabled=false for non-ACTIVE accounts (PortalUserDetailsService.java:42)
- This means existing sessions will be rejected at the next authenticated request when Spring Security re-validates the user
- No additional real-time invalidation mechanism needed beyond Spring Security's built-in behavior

**Consistent Behavior Across User Types**
- Apply the same disabled account logic for all user types (RIVVY_ADMIN, RIVVY_PRODUCER, CLIENT)
- Use the same error message regardless of role
- No differentiation in lockout or session invalidation behavior

## Visual Design
No visual assets provided.

## Existing Code to Leverage

**AuthController.java (crud-logic-service/src/main/java/com/rivvystudios/portal/controller/AuthController.java)**
- Already has disabled account handling logic at lines 107-122 for INACTIVE/SUSPENDED status
- Already calls handleFailedAttempt() for wrong passwords (line 178)
- Existing message says "Your account has been disabled. Please contact support." - needs update to match new wording
- Current implementation does NOT count disabled-account attempts toward lockout - needs modification
- Uses passwordEncoder.matches() to verify password before showing disabled message

**handleFailedAttempt() method (AuthController.java:185-211)**
- Implements sliding window approach with LockoutProperties configuration
- Manages failedAttemptsCount, firstFailedAttemptAt, lastFailedAttemptAt, and lockedUntil fields
- Resets counter when outside the configured window
- Sets lockedUntil when threshold is reached

**PortalUserDetailsService.java (crud-logic-service/src/main/java/com/rivvystudios/portal/security/PortalUserDetailsService.java)**
- Sets enabled=false for non-ACTIVE accounts (line 42)
- Spring Security uses this to reject authentication attempts on protected endpoints
- Provides automatic session invalidation when account becomes disabled

**LoginPage.tsx (rivvy-portal-ui/src/pages/LoginPage.tsx)**
- Displays error messages using error state (lines 86-88)
- Already handles 401 responses and extracts error message from response body (lines 30-32)
- No frontend changes needed for error display

**UserAccount model and UserAccountStatus enum**
- UserAccountStatus has ACTIVE, INACTIVE, SUSPENDED, and PENDING values
- UserAccount has lockout tracking fields: failedAttemptsCount, firstFailedAttemptAt, lastFailedAttemptAt, lockedUntil
- Status field is persisted and queryable for status checks

## Out of Scope
- Successful sign-in behavior for active accounts
- Session inactivity timeout configuration or remember-me persistence logic
- Email normalization (trimming, case-insensitive matching) during login
- Post-login destination routing or role-based redirection after successful authentication
- Modifying the core lockout threshold, window, or duration configuration values
- SSO or external authentication provider flows
- Admin UI or API endpoints for disabling or re-enabling user accounts
- Special logging, audit trails, notifications, or alerts for disabled account attempts beyond standard login failure logging
- Clickable support links or interactive recovery workflows (text-only guidance is sufficient)
- Differentiated error messages based on user type or role
- Real-time session invalidation via WebSocket or push notifications (next-request validation is acceptable)
