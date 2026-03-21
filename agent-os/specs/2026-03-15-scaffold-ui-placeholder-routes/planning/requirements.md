# Spec Requirements: Scaffold UI Placeholder Routes

## Initial Description
Scaffold seven public routes in the Rivvy Portal UI (React/TS with React Router) that each render a distinct, minimal placeholder page. Routes: /login, /profile, /dashboard, /new-brief, /vault, /screening, /admin. Each page shows a visible title matching its screen name and includes a unique data-testid for automated verification. A shared minimal layout may be used. No authentication, role-aware routing, global navigation, or business logic is included.

**Additionally:** Since the frontend project does not yet exist, this spec also covers bootstrapping the base Vite + React + TypeScript project (package.json, vite.config.ts, tsconfig.json, index.html, main.tsx, etc.).

## Requirements Discussion

### First Round Questions

**Q1:** The tech stack specifies React 19.x + Vite 5.x + TypeScript 5.x, but the frontend project doesn't exist yet. Should this spec also include creating the base Vite/React project, or is that a separate spec?
**Answer:** Yes, the frontend project doesn't exist. This spec includes bootstrapping the base Vite + React + TypeScript project.

**Q2:** The mission doc notes that /dashboard renders differently based on user role. Since this spec excludes role-aware routing, should we render a single "Dashboard" placeholder?
**Answer:** User deferred to implementer's judgment. Decision: Single "Dashboard" placeholder at /dashboard with no role branching (consistent with out-of-scope exclusion of role-aware routing).

**Q3:** For the shared minimal layout — simple wrapper with Outlet and basic centered container, no header/sidebar/navigation?
**Answer:** User deferred to implementer's judgment. Decision: Simple layout wrapper rendering an Outlet with minimal centered styling. No header, sidebar, or navigation.

**Q4:** CSS Modules vs plain vanilla CSS for placeholders?
**Answer:** User deferred to implementer's judgment. Decision: Plain vanilla CSS or minimal inline styles. No need for CSS Modules infrastructure for placeholders.

**Q5:** Should placeholders include any subtitle or descriptive text beyond the title?
**Answer:** User deferred to implementer's judgment. Decision: Title heading only. Keep it minimal — this is a technical foundation.

**Q6:** Anything else to exclude?
**Answer:** Nothing additional beyond what's already captured.

### Existing Code to Reference
No similar existing features identified for reference. The frontend is being created from scratch. The backend exists at `crud-logic-service/` (Spring Boot) but is not relevant to this spec.

### Follow-up Questions
None required. The scope is well-defined and the user confirmed this is a straightforward technical foundation.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A — placeholder pages require no design guidance.

## Requirements Summary

### Functional Requirements
- Bootstrap a new Vite 5.x + React 19.x + TypeScript 5.x project with correct configuration
- Install React Router as a dependency
- Configure client-side routing with BrowserRouter
- Create 7 placeholder page components, each with:
  - A visible title rendered as a heading element
  - A unique `data-testid` attribute on the page container
- Route table:
  | Path | Title |
  |------|-------|
  | /login | Login |
  | /profile | Profile |
  | /dashboard | Dashboard |
  | /new-brief | New Brief |
  | /vault | Brand Vault |
  | /screening | Screening Room |
  | /admin | Admin |
- Create a shared minimal layout component wrapping all routes via React Router's Outlet
- Ensure direct navigation (deep links) to each path loads the correct page without redirects or errors

### Reusability Opportunities
- None — this is greenfield scaffolding

### Scope Boundaries
**In Scope:**
- Vite + React + TypeScript project initialization (package.json, vite.config.ts, tsconfig.json, index.html, main.tsx)
- React Router installation and BrowserRouter setup
- 7 placeholder page components with titles and data-testid attributes
- Shared minimal layout component
- Vite dev server config supporting client-side routing for deep links

**Out of Scope:**
- Global navigation or menus
- Authentication, role-aware routing, or guards
- API calls, state management, or integration with Rivvy Portal Services
- Styling beyond minimal readability
- Redirect logic, 404/500 handling changes, or error boundary work
- Automated test code
- Updates to architecture model entities or UI workflow diagrams

### Technical Considerations
- Tech stack per product docs: React 19.x, TypeScript 5.x, Vite 5.x
- Styling: CSS Modules + Vanilla CSS (per tech-stack.md), but plain CSS sufficient for placeholders
- Testing tools: Vitest + React Testing Library (per tech-stack.md), but no test code in this spec
- The Vite dev server must be configured to handle client-side routing (history API fallback) so deep links work
- The frontend project location within the repo needs to be determined during implementation (likely a top-level directory alongside `crud-logic-service/`)
