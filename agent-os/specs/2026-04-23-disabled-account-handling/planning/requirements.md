# Spec Requirements: Disabled Account Handling

## Initial Description
This story defines disabled account handling for password-based sign-in on the Login screen in the Rivvy Portal. When a UserAccount is in disabled status, the system must block authentication, show a specific disabled-account message with recovery guidance, and count the attempt toward failed-login handling. In addition, if a user already has an authenticated session and their account later becomes disabled, that session must be invalidated so they can no longer access the portal. This applies consistently across Rivvy Admin, Rivvy Producer, and Client users.

## Requirements Discussion

### First Round Questions

**Q1:** I assume the disabled-account error message should be something like "Your account has been disabled. Please contact your administrator or support@rivvy.com for assistance." Is that the right tone and content, or would you prefer different wording?
**Answer:** Perfect wording

**Q2:** For session invalidation when an account becomes disabled, I'm thinking this should happen at the next API request when the authentication middleware validates the token. Is that correct, or do you need immediate real-time invalidation (e.g., via WebSocket push)?
**Answer:** Correct

**Q3:** I assume disabled-account sign-in attempts should increment the failed login counter the same way incorrect passwords do. Should a disabled account eventually trigger the lockout mechanism if someone keeps trying, or should disabled accounts be exempt from lockout since they're already blocked?
**Answer:** A disabled account should eventually trigger the lockout mechanism if someone keeps trying

**Q4:** For the Login screen recovery guidance, I'm thinking a link or text that says "Contact Support" would be sufficient. Do you want an actual clickable support link, or just instructional text?
**Answer:** Just instructional text

**Q5:** The spec mentions counting disabled-account attempts toward "failed-login handling." I assume this means there's already existing failed-login tracking and lockout logic in place. Can you confirm this exists and point me to where it's implemented?
**Answer:** It exists - scan codebase during execution to find it

**Q6:** Should the disabled-account message differentiate between user types (Admin/Producer/Client), or use the same generic message for all?
**Answer:** Use the same generic message for all

**Q7:** Is there anything else about the disabled account behavior that should happen beyond blocking sign-in and showing the message - for example, should we log these attempts differently, send notifications, or trigger any other side effects?
**Answer:** Just block sign-in and show the message

### Existing Code to Reference

**Similar Features Identified:**
No similar existing features identified for reference. The spec-writer should scan the codebase during execution to locate:
- Existing failed-login tracking and lockout logic
- Login screen/form components
- Authentication service or API endpoints
- Session validation middleware

### Follow-up Questions
No follow-up questions needed.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A

## Requirements Summary

### Functional Requirements
- Block authentication for UserAccount records with status = 'disabled'
- Display error message: "Your account has been disabled. Please contact your administrator or support@rivvy.com for assistance."
- Show instructional text for recovery guidance (not a clickable link)
- Count disabled-account sign-in attempts toward failed-login tracking
- Disabled accounts can eventually trigger lockout mechanism if attempts continue
- Invalidate existing authenticated sessions at next API request when account becomes disabled
- Apply same behavior across all user types (Rivvy Admin, Rivvy Producer, Client)
- No special logging, notifications, or other side effects beyond blocking and message display

### Reusability Opportunities
- Locate and integrate with existing failed-login tracking and lockout mechanisms
- Follow existing Login screen error message display patterns
- Use existing session validation middleware/authentication guards

### Scope Boundaries
**In Scope:**
- Password-based sign-in handling for disabled UserAccount records on the Login screen
- Blocking session creation when a disabled account submits credentials
- Showing a specific disabled-account message on the Login screen
- Showing recovery guidance for disabled users (instructional text)
- Counting disabled-account sign-in attempts toward failed-login handling
- Triggering lockout mechanism for repeated disabled-account attempts
- Invalidating an already authenticated session if the account becomes disabled
- Applying disabled-account handling consistently across Rivvy Admin, Rivvy Producer, and Client users

**Out of Scope:**
- Successful sign-in behavior for active accounts
- Session inactivity timeout and remember-me behavior
- Email trimming and case-insensitive email matching
- Post-login destination or role-based routing after successful authentication
- Definition of general failed-login and lockout rules beyond the requirement that disabled-account attempts count toward them
- SSO or external-auth sign-in flows implied by authProvider
- Admin workflows for disabling or re-enabling accounts
- Special logging, notifications, or other side effects
- Clickable support links (text only)
- Differentiated messaging by user type

### Technical Considerations
- Disabled status is represented by UserAccount.status = 'disabled'
- Session invalidation enforced at next authenticated request/token refresh
- Must integrate with existing failed-login and lockout mechanisms (to be located during implementation)
- Same disabled-account message for all user types
- Recovery guidance is instructional text only, not interactive elements
