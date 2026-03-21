# Specification: Client Dashboard

## Goal
Replace the placeholder Dashboard screen at `/dashboard` with a tile-based navigation hub for authenticated Client users, and add a shared header/nav and footer to the application layout.

## User Stories
- As a Client user, I want to see clearly labeled navigation tiles on my dashboard so that I can quickly access New Brief, Screening Room, and Brand Vault features.
- As a Client user, I want a consistent header and footer on every authenticated page so that I always have access to site-wide navigation.

## Specific Requirements

**Dashboard tile grid layout**
- Render exactly 3 tiles in a CSS Grid with 4 columns (`grid-template-columns: repeat(4, 1fr)`)
- Tiles appear in this fixed order: New Brief, Screening Room, Brand Vault
- The fourth grid cell remains empty; no placeholder or hidden element fills it
- No heading, welcome message, or summary widgets above the tile grid
- On smaller viewports (below ~768px), the grid should collapse to a single column so tiles stack vertically
- The grid container should be centered within the existing `RootLayout` max-width constraint (960px)

**Reusable DashboardTile component**
- Create a new file `src/components/DashboardTile.tsx`
- Props: `label: string`, `description: string`, `to: string` (the route path)
- Renders as a React Router `Link` element so it participates in client-side routing and is semantically an anchor
- Each tile displays the `label` as a prominent heading and `description` as secondary subtext beneath it
- Style the tile as a card (border, border-radius, padding, subtle box-shadow) consistent with the card pattern used in `LoginPage.tsx`
- Use the inline `styles` object pattern (typed as `Record<string, React.CSSProperties>`) matching the existing codebase convention -- no CSS modules or external stylesheets
- Tile should have a visible hover/focus indicator (e.g., border-color change or box-shadow shift)

**Tile definitions and navigation**
- Tile 1 -- Label: "New Brief", Description: "Submit a new creative project request", Route: `/new-brief`
- Tile 2 -- Label: "Screening Room", Description: "Review and comment on video deliverables", Route: `/screening`
- Tile 3 -- Label: "Brand Vault", Description: "Manage your brand assets and guidelines", Route: `/vault`
- All subtext is placeholder copy that the user will modify later

**Keyboard accessibility**
- Each tile must be keyboard-focusable (inherent from `Link`/`<a>` element)
- Tab order must follow the visual left-to-right order (New Brief, Screening Room, Brand Vault)
- Enter and Space must activate navigation (inherent from `Link`/`<a>`)
- Focus indicator must be clearly visible (use outline or box-shadow on `:focus-visible`)

**Replace DashboardPage placeholder**
- Replace the contents of `src/pages/DashboardPage.tsx` with the tile grid
- Retain the `data-testid="page-dashboard"` attribute on the outermost wrapper div
- Import and render three `DashboardTile` instances with the defined tile data
- Keep the component as a simple functional component with a default export, matching the existing page pattern

**Shared header and footer in RootLayout**
- Add a `<header>` element above the `<Outlet />` in `RootLayout.tsx`
- The header should display the application name "Rivvy Portal" as a link to `/dashboard`
- Include a minimal nav with a placeholder "Sign out" text element (non-functional -- just a `<span>` or static text, not wired to any logout logic)
- Add a `<footer>` element below the `<Outlet />` with a simple copyright line (e.g., "Rivvy Studios")
- Header and footer should be visually distinct from page content (e.g., subtle background color or border)
- Use the same inline styles pattern (`Record<string, React.CSSProperties>`) as the rest of the codebase

**No routing changes**
- The `/dashboard` route already exists in `App.tsx` and points to `DashboardPage` -- no modifications to `App.tsx` are needed
- Destination pages (`/new-brief`, `/screening`, `/vault`) already exist as placeholder pages -- do not modify them

## Visual Design
No visual assets provided. Follow the existing card-style aesthetic established in `LoginPage.tsx` (white background, 1px border, 8px border-radius, subtle box-shadow). Tiles should feel like clickable cards within a clean grid.

## Existing Code to Leverage

**`LoginPage.tsx` -- card styling pattern**
- Uses an inline `styles` object typed as `Record<string, React.CSSProperties>` for all styling
- Card style: `border: '1px solid #ddd'`, `borderRadius: '8px'`, `backgroundColor: '#fff'`, `boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'`
- The `DashboardTile` card should replicate this visual treatment for consistency
- Color palette reference: primary blue `#1976d2` can be used for hover/focus accent

**`RootLayout.tsx` -- shared layout wrapper**
- Currently a minimal wrapper: centered `<div>` at `maxWidth: '960px'` with `padding: '1rem'` and an `<Outlet />`
- Header and footer should be added inside this component, wrapping the `<Outlet />`
- The max-width container approach should be preserved; header/footer live within or span the same container

**`DashboardPage.tsx` -- placeholder to replace**
- Currently renders a simple `<div>` with `data-testid="page-dashboard"` and an `<h1>`
- The `data-testid` must be preserved; the `<h1>` is removed per requirements (no heading above tiles)
- Default export pattern must be maintained

**`App.tsx` -- route definitions**
- All routes are children of `RootLayout` via a parent `<Route element={<RootLayout />}>`
- The `/dashboard` route already exists -- no changes needed
- Destination routes `/new-brief`, `/screening`, `/vault` already exist as placeholder pages

**`LoginPage.test.tsx` -- testing patterns**
- Uses `@testing-library/react` with `vitest`, `MemoryRouter` for routing context
- Uses `screen.getByRole`, `screen.getByLabelText`, `screen.getByTestId` query patterns
- Follow this same test setup pattern for any new tests on the dashboard or tile component

## Out of Scope
- Producer-specific or Admin dashboard layouts or tile sets
- Feature work on destination pages (`/new-brief`, `/screening`, `/vault`) beyond navigating to them
- Role-based gating, feature flags, or conditional tile visibility
- Analytics, telemetry, or event tracking
- Localization, theming, or dark mode support
- Dynamic badges, counters, notifications, or any backend data integration
- Multi-organization context switching or org selector UI
- Functional sign-out logic (placeholder text only in header)
- Modifications to `App.tsx` routing configuration
- Mobile hamburger menu or collapsible navigation patterns
