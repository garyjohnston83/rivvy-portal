# Spec Initialization: Scaffold UI Placeholder Routes

## Raw Idea

Scaffold seven public routes in the Rivvy Portal UI (React/TS with React Router) that each render a distinct, minimal placeholder page. Routes: /login, /profile, /dashboard, /new-brief, /vault, /screening, /admin. Each page shows a visible title matching its screen name and includes a unique data-testid for automated verification. A shared minimal layout may be used. No authentication, role-aware routing, global navigation, or business logic is included.

## In Scope
- Add React Router routes in Rivvy Portal UI for /login, /profile, /dashboard, /new-brief, /vault, /screening, /admin
- Create distinct placeholder pages for each route rendered within a shared minimal layout
- Display a visible title on each page matching: Login, Profile, Dashboard, New Brief, Brand Vault, Screening Room, Admin
- Include a unique data-testid on each placeholder page
- Ensure direct navigation to each path loads the corresponding page without redirects or errors

## Out of Scope
- Global navigation or menus
- Authentication, role-aware routing, or guards
- API calls, state management, or integration with Rivvy Portal Services
- Styling beyond minimal readability
- Redirect logic, 404/500 handling changes, or error boundary work
- Automated test code
- Conventions or placement rules for data-testid attributes
- Updates to architecture model entities or UI workflow diagrams

## Assumptions
- The development server/build setup supports client-side routing for deep links to all listed paths.
- The implementer will choose the element and naming for each data-testid while ensuring uniqueness.
- Titles should match exactly: Login, Profile, Dashboard, New Brief, Brand Vault, Screening Room, Admin.

## Acceptance Criteria
- Navigating to /login renders a placeholder with visible title 'Login' and a unique data-testid; no redirects or guards occur.
- Navigating to /profile renders a placeholder with visible title 'Profile' and a unique data-testid; no redirects or guards occur.
- Navigating to /dashboard renders a placeholder with visible title 'Dashboard' and a unique data-testid; no redirects or guards occur.
- Navigating to /new-brief renders a placeholder with visible title 'New Brief' and a unique data-testid; no redirects or guards occur.
- Navigating to /vault renders a placeholder with visible title 'Brand Vault' and a unique data-testid; no redirects or guards occur.
- Navigating to /screening renders a placeholder with visible title 'Screening Room' and a unique data-testid; no redirects or guards occur.
- Navigating to /admin renders a placeholder with visible title 'Admin' and a unique data-testid; no redirects or guards occur.
- Each placeholder page uses a distinct data-testid value not reused by other pages.
- No global navigation, role-aware routing, or business logic is introduced by this work.
- Placeholders may render within a shared layout but must remain visually distinguishable by their titles.

## Architecture Context
- The architecture model already defines 3 ui_screens: Brand Vault (/vault), Login (/login), New Brief (/new-brief)
- This spec adds 4 additional screens: Profile (/profile), Dashboard (/dashboard), Screening Room (/screening), Admin (/admin)
- All routes belong to the "Rivvy Portal UI" service within the "RS UI Tier" app component

## Date Initialized
2026-03-15
