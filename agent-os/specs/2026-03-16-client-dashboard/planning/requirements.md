# Spec Requirements: Client Dashboard

## Initial Description
Add a new Dashboard screen at /dashboard for authenticated Client users. It renders exactly three tiles—New Brief, Screening Room, Brand Vault—in that order within a 4-up grid. Each tile includes a label and short descriptive subtext and navigates to: New Brief -> /new-brief, Screening Room -> /screening, Brand Vault -> /vault. No tiles are hidden or disabled for Client users in the PoC. No summary widgets appear. Standard header/nav and footer remain. Users belong to a single org; no org selector.

## Requirements Discussion

### First Round Questions

**Q1:** The current frontend has a `RootLayout.tsx` that's just a centered container with no header/nav/footer. Should this spec add a basic header and footer to the shared layout, or defer that?
**Answer:** Yes, good assumption — add a basic header/nav and footer to the shared layout as part of this spec.

**Q2:** Do you have the initial tile subtext copy ready, or should I use sensible placeholder text?
**Answer:** No copy ready. Add sensible text and the user will modify after.

**Q3:** Should the dashboard page show a heading like "Dashboard" or "Welcome" above the tiles, or just the tiles?
**Answer:** Just the tiles. No heading above.

**Q4:** Should each tile be a reusable `DashboardTile` component in `src/components/` for future reuse by Producer dashboard, or inline in DashboardPage?
**Answer:** Reusable component in `src/components/`.

**Q5:** Anything else to exclude?
**Answer:** No.

### Existing Code to Reference

**Similar Features Identified:**
- Component: `LoginPage.tsx` - Path: `rivvy-portal-ui/src/pages/LoginPage.tsx` — card-style centered layout pattern
- Layout: `RootLayout.tsx` - Path: `rivvy-portal-ui/src/layouts/RootLayout.tsx` — shared layout wrapper (needs header/footer added)
- Router: `App.tsx` - Path: `rivvy-portal-ui/src/App.tsx` — route definitions (dashboard route already exists)
- Existing page: `DashboardPage.tsx` - Path: `rivvy-portal-ui/src/pages/DashboardPage.tsx` — placeholder to be replaced

### Follow-up Questions
None required.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A

## Requirements Summary

### Functional Requirements
- Replace the placeholder `DashboardPage.tsx` with a 4-up grid containing exactly 3 tiles
- Tile definitions (in order):
  1. **New Brief** — subtext: sensible placeholder (e.g., "Submit a new creative project request") — navigates to `/new-brief`
  2. **Screening Room** — subtext: sensible placeholder (e.g., "Review and comment on video deliverables") — navigates to `/screening`
  3. **Brand Vault** — subtext: sensible placeholder (e.g., "Manage your brand assets and guidelines") — navigates to `/vault`
- Create a reusable `DashboardTile` component in `src/components/` for future reuse
- Each tile is keyboard-focusable and activates via Enter/Space; tab order matches visual order
- No heading above the tiles — just the tile grid
- No summary widgets, badges, counters, or backend integrations
- Add a basic header/nav and footer to the shared `RootLayout.tsx`
- No tiles are hidden or disabled for Client users

### Reusability Opportunities
- `DashboardTile` component — reusable for Producer dashboard in a future spec
- Header/footer in `RootLayout.tsx` — shared across all authenticated pages

### Scope Boundaries
**In Scope:**
- Replace DashboardPage placeholder with tile grid
- Create reusable DashboardTile component
- Add basic header/nav and footer to RootLayout
- 4-up CSS grid layout (3 tiles in 4 columns)
- Keyboard accessibility (focusable tiles, Enter/Space activation)
- Sensible placeholder subtext for each tile

**Out of Scope:**
- Producer-specific or Admin dashboard experiences
- Feature work on destination pages beyond navigation
- Role-based gating/feature flags beyond stated Client behavior
- Analytics/telemetry
- Localization or theming changes
- Dynamic badges/counters or backend integrations
- Multi-organization context switching
- Sign-out functionality (just a placeholder link in header if needed)

### Technical Considerations
- Frontend only — no backend changes needed
- React 19.x, TypeScript 5.x, Vite 5.x
- Use React Router's `Link` or `useNavigate` for tile navigation
- CSS Grid for the 4-up layout; responsive behavior on smaller viewports
- Tiles should use semantic HTML (e.g., `<a>` or `role="link"`) for accessibility
- The `/dashboard` route already exists in `App.tsx` — no routing changes needed
