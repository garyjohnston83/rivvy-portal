# Task Breakdown: Client Dashboard

## Overview
Total Tasks: 26

This feature is entirely frontend work within the `rivvy-portal-ui` project. There are no database, API, or backend changes required. The work breaks into three logical groups: a reusable tile component, the dashboard page rewrite, and the shared layout (header/footer). All files live under `rivvy-portal-ui/src/`.

## Task List

### Frontend Components

#### Task Group 1: DashboardTile Reusable Component
**Dependencies:** None

- [x] 1.0 Complete DashboardTile component
  - [x] 1.1 Write 4 focused tests for DashboardTile component
    - Test that the component renders the `label` prop as a heading element
    - Test that the component renders the `description` prop as secondary text beneath the label
    - Test that the component renders as a React Router `Link` with the correct `to` prop (verify the rendered anchor `href`)
    - Test that the component applies card-like styling (border, border-radius, box-shadow present on the rendered element)
    - Place tests in `src/components/DashboardTile.test.tsx`
    - Use the existing vitest + @testing-library/react + MemoryRouter pattern from `LoginPage.test.tsx`
  - [x] 1.2 Create `src/components/DashboardTile.tsx`
    - Define props interface: `label: string`, `description: string`, `to: string`
    - Render as a React Router `Link` element (import from `react-router-dom`) so it is semantically an anchor and participates in client-side routing
    - Display `label` inside a heading element (e.g., `<h2>`) as prominent text
    - Display `description` as a `<p>` element beneath the heading
    - Default export the component as a simple functional component
  - [x] 1.3 Style DashboardTile with inline styles object
    - Create a `styles` object typed as `Record<string, React.CSSProperties>` at the bottom of the file, matching the codebase convention used in `LoginPage.tsx`
    - Card appearance: `border: '1px solid #ddd'`, `borderRadius: '8px'`, `backgroundColor: '#fff'`, `boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'`, `padding: '1.5rem'`
    - Remove default anchor underline and color via `textDecoration: 'none'`, `color: 'inherit'`
    - Set `display: 'block'` on the Link so it fills its grid cell
    - Label heading: `marginTop: 0`, `marginBottom: '0.5rem'`, `fontSize: '1.25rem'`
    - Description text: `margin: 0`, `fontSize: '0.875rem'`, `color: '#666'`
  - [x] 1.4 Add hover and focus-visible indicators
    - Because inline styles cannot use pseudo-selectors (`:hover`, `:focus-visible`), use `onMouseEnter`/`onMouseLeave` with a state variable to toggle a hover style (e.g., `borderColor: '#1976d2'` or `boxShadow` shift), OR inject a minimal `<style>` tag scoped to a class name
    - Ensure a clearly visible focus indicator on `:focus-visible` (outline or box-shadow) for keyboard navigation -- if using the inline approach, also handle `onFocus`/`onBlur`
    - Use the primary accent color `#1976d2` for the active/focus/hover indicator, consistent with `LoginPage.tsx`
  - [x] 1.5 Ensure DashboardTile tests pass
    - Run ONLY the tests in `src/components/DashboardTile.test.tsx`
    - Verify all 4 tests pass
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 4 tests written in 1.1 pass
- `DashboardTile` renders as a React Router `Link` with label, description, and correct `to` path
- Card styling matches the pattern from `LoginPage.tsx` (border, radius, shadow, white background)
- Hover and focus-visible states produce a clearly visible indicator
- Inline `styles` object pattern is used (typed as `Record<string, React.CSSProperties>`)
- Component is in `src/components/DashboardTile.tsx` with a default export

---

#### Task Group 2: DashboardPage Tile Grid
**Dependencies:** Task Group 1

- [x] 2.0 Complete DashboardPage rewrite
  - [x] 2.1 Write 5 focused tests for DashboardPage
    - Test that the page renders with `data-testid="page-dashboard"` on the outermost wrapper div
    - Test that exactly 3 DashboardTile instances are rendered (by checking for three link elements with the expected routes)
    - Test that tiles appear in the correct order: "New Brief" first, "Screening Room" second, "Brand Vault" third (verify DOM order)
    - Test that each tile links to the correct route (`/new-brief`, `/screening`, `/vault`)
    - Test that each tile displays the correct description text ("Submit a new creative project request", "Review and comment on video deliverables", "Manage your brand assets and guidelines")
    - Place tests in `src/pages/DashboardPage.test.tsx`
    - Use the existing vitest + @testing-library/react + MemoryRouter pattern
  - [x] 2.2 Replace DashboardPage contents with tile grid
    - Remove the existing `<h1>Dashboard</h1>` placeholder
    - Retain the outermost `<div data-testid="page-dashboard">` wrapper
    - Import `DashboardTile` from `../components/DashboardTile`
    - Render exactly 3 `DashboardTile` instances inside the wrapper:
      - `label="New Brief"` `description="Submit a new creative project request"` `to="/new-brief"`
      - `label="Screening Room"` `description="Review and comment on video deliverables"` `to="/screening"`
      - `label="Brand Vault"` `description="Manage your brand assets and guidelines"` `to="/vault"`
    - Keep the component as a simple functional component with a default export
  - [x] 2.3 Apply CSS Grid layout to the tile container
    - Add a container div (or apply directly to the `data-testid` div) with inline grid styles
    - `display: 'grid'`, `gridTemplateColumns: 'repeat(4, 1fr)'`, `gap: '1.5rem'`
    - Only 3 tiles fill the first 3 cells; the fourth cell remains empty naturally (no placeholder element)
    - No heading, welcome message, or summary widgets above the grid
  - [x] 2.4 Add responsive single-column layout for small viewports
    - Since inline styles cannot use `@media` queries, inject a `<style>` tag or use `window.matchMedia` with state to switch `gridTemplateColumns` to `'1fr'` below approximately 768px
    - Alternatively, add a scoped `<style>` block at the component level with a class-based media query (e.g., `.dashboard-grid { ... } @media (max-width: 768px) { .dashboard-grid { grid-template-columns: 1fr; } }`)
    - Ensure the grid container remains centered within the existing `RootLayout` max-width constraint (960px)
  - [x] 2.5 Ensure DashboardPage tests pass
    - Run ONLY the tests in `src/pages/DashboardPage.test.tsx`
    - Verify all 5 tests pass
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 5 tests written in 2.1 pass
- `data-testid="page-dashboard"` is preserved on the outermost div
- Exactly 3 tiles render in the correct order with correct labels, descriptions, and routes
- CSS Grid uses `repeat(4, 1fr)` with the fourth cell naturally empty
- Grid collapses to single column below ~768px
- No heading, welcome message, or widgets above the tile grid
- No modifications to `App.tsx` routing

---

#### Task Group 3: Shared Header and Footer in RootLayout
**Dependencies:** None (can be done in parallel with Task Groups 1-2)

- [x] 3.0 Complete shared header and footer
  - [x] 3.1 Write 5 focused tests for RootLayout header and footer
    - Test that a `<header>` element is rendered containing the text "Rivvy Portal" as a link
    - Test that the "Rivvy Portal" link navigates to `/dashboard` (verify `href` attribute)
    - Test that the header contains a "Sign out" text element
    - Test that a `<footer>` element is rendered containing "Rivvy Studios" copyright text
    - Test that the `<Outlet />` content appears between the header and footer (render a child route and verify DOM ordering)
    - Place tests in `src/layouts/RootLayout.test.tsx`
    - Use the vitest + @testing-library/react + MemoryRouter pattern; configure MemoryRouter with a child route to test Outlet rendering
  - [x] 3.2 Add header element to RootLayout
    - Add a `<header>` element above the existing `<Outlet />`
    - Render "Rivvy Portal" as a React Router `Link` to `/dashboard`
    - Include a "Sign out" text element (non-functional -- a `<span>` or plain text; no click handler needed)
    - Use flexbox or similar layout so the brand link is on the left and "Sign out" is on the right
  - [x] 3.3 Add footer element to RootLayout
    - Add a `<footer>` element below the existing `<Outlet />`
    - Display a simple copyright line, e.g., "Rivvy Studios" (current year is acceptable but not required)
    - Center the text within the footer
  - [x] 3.4 Style header and footer with inline styles
    - Create a `styles` object typed as `Record<string, React.CSSProperties>` matching the codebase convention
    - Header: visually distinct from page content (e.g., `borderBottom: '1px solid #ddd'`, `paddingBottom: '1rem'`, `marginBottom: '1.5rem'`), flex layout with `justifyContent: 'space-between'`, `alignItems: 'center'`
    - Brand link: use `textDecoration: 'none'`, `color: '#1976d2'`, `fontWeight: 700`, `fontSize: '1.25rem'`
    - Sign out text: `color: '#666'`, `fontSize: '0.875rem'`
    - Footer: visually distinct (e.g., `borderTop: '1px solid #ddd'`, `paddingTop: '1rem'`, `marginTop: '1.5rem'`), `textAlign: 'center'`, `color: '#999'`, `fontSize: '0.875rem'`
    - Preserve the existing wrapper div with `maxWidth: '960px'`, `margin: '0 auto'`, `padding: '1rem'`
  - [x] 3.5 Ensure RootLayout tests pass
    - Run ONLY the tests in `src/layouts/RootLayout.test.tsx`
    - Verify all 5 tests pass
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 5 tests written in 3.1 pass
- `<header>` renders above `<Outlet />` with "Rivvy Portal" link to `/dashboard` and "Sign out" text
- `<footer>` renders below `<Outlet />` with "Rivvy Studios" copyright text
- Header and footer are visually distinct from page content (borders or background differentiation)
- Inline `styles` object pattern is used consistently
- Existing `RootLayout` wrapper constraints (max-width 960px, padding, centering) are preserved
- No functional sign-out logic is implemented (placeholder only)

---

### Testing

#### Task Group 4: Test Review and Gap Analysis
**Dependencies:** Task Groups 1-3

- [x] 4.0 Review existing tests and fill critical gaps only
  - [x] 4.1 Review tests from Task Groups 1-3
    - Review the 4 tests written for DashboardTile (Task 1.1)
    - Review the 5 tests written for DashboardPage (Task 2.1)
    - Review the 5 tests written for RootLayout (Task 3.1)
    - Total existing tests: 14 tests
  - [x] 4.2 Analyze test coverage gaps for this feature only
    - Identify any critical user workflows that lack test coverage
    - Focus ONLY on gaps related to the Client Dashboard feature requirements
    - Prioritize integration-level checks (e.g., DashboardPage rendering within RootLayout, tile navigation end-to-end)
    - Do NOT assess entire application test coverage
  - [x] 4.3 Write up to 7 additional strategic tests to fill gaps
    - Potential gap areas to evaluate (write tests only if genuinely missing from Task Groups 1-3):
      - Keyboard accessibility: Tab order follows visual order (New Brief, Screening Room, Brand Vault) -- verify tab sequence across tiles
      - Integration: DashboardPage renders correctly inside RootLayout (header, tile grid, footer all present in correct order)
      - Tile count: Verify no fourth tile or placeholder element exists in the fourth grid cell
      - Responsive behavior: If testable, verify grid container has the responsive class/style mechanism in place
      - Existing LoginPage tests still pass (regression check -- run `LoginPage.test.tsx` only, do not write new tests for it)
      - Header link: Clicking "Rivvy Portal" navigates to `/dashboard`
      - Tile descriptions are visible and not hidden (verify they are in the accessible DOM)
    - Place additional tests in `src/__tests__/client-dashboard-integration.test.tsx` or alongside existing test files as appropriate
    - Add a maximum of 7 new tests; skip any that duplicate coverage from Task Groups 1-3
  - [x] 4.4 Run all feature-specific tests
    - Run ONLY the tests related to this feature:
      - `src/components/DashboardTile.test.tsx` (4 tests)
      - `src/pages/DashboardPage.test.tsx` (5 tests)
      - `src/layouts/RootLayout.test.tsx` (5 tests)
      - Any new integration tests from 4.3 (up to 7 tests)
      - `src/pages/LoginPage.test.tsx` (5 existing tests -- regression check)
    - Expected total: approximately 19-26 tests
    - Do NOT run the entire application test suite
    - Verify all tests pass

**Acceptance Criteria:**
- All feature-specific tests pass (approximately 19-26 tests total)
- Critical user workflows for the Client Dashboard feature are covered
- No more than 7 additional tests added when filling in testing gaps
- Existing LoginPage tests still pass (no regressions from RootLayout changes)
- Testing focused exclusively on this spec's feature requirements

---

## Execution Order

Recommended implementation sequence:

1. **Task Group 1: DashboardTile Component** -- No dependencies; creates the foundational reusable component
2. **Task Group 3: RootLayout Header/Footer** -- No dependencies on Task Group 1; can be developed in parallel with Task Group 1
3. **Task Group 2: DashboardPage Tile Grid** -- Depends on Task Group 1 (imports DashboardTile); can proceed once Task Group 1 is complete
4. **Task Group 4: Test Review and Gap Analysis** -- Depends on all prior groups; runs after all components are implemented

Task Groups 1 and 3 are independent and can be executed in parallel if multiple developers are available.

## Key Files

| File | Action |
|------|--------|
| `src/components/DashboardTile.tsx` | **Create** -- New reusable tile component |
| `src/components/DashboardTile.test.tsx` | **Create** -- Tests for DashboardTile |
| `src/pages/DashboardPage.tsx` | **Modify** -- Replace placeholder with tile grid |
| `src/pages/DashboardPage.test.tsx` | **Create** -- Tests for DashboardPage |
| `src/layouts/RootLayout.tsx` | **Modify** -- Add header and footer around Outlet |
| `src/layouts/RootLayout.test.tsx` | **Create** -- Tests for RootLayout |
| `src/App.tsx` | **No changes** -- Routing already exists |

## Out of Scope Reminders
- No modifications to `App.tsx` routing
- No functional sign-out logic (placeholder text only)
- No destination page changes (NewBriefPage, ScreeningPage, VaultPage stay as-is)
- No mobile hamburger menu
- No dynamic data, badges, or counters
- No role-based gating or feature flags
