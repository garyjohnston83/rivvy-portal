# Spec Initialization: Client Dashboard

## Raw Idea

Add a new Dashboard screen at /dashboard for authenticated Client users. It renders exactly three tiles—New Brief, Screening Room, Brand Vault—in that order within a 4-up grid. Each tile includes a label and short descriptive subtext and navigates to: New Brief -> /new-brief, Screening Room -> /screening, Brand Vault -> /vault. No tiles are hidden or disabled for Client users in the PoC. No summary widgets appear. Standard header/nav and footer remain. Users belong to a single org; no org selector.

## In Scope
- Create Dashboard UI screen at /dashboard for Client users
- Render exactly three tiles labeled New Brief, Screening Room, Brand Vault
- Maintain tile order: New Brief (1st), Screening Room (2nd), Brand Vault (3rd)
- Include short descriptive subtext under each tile label (initial copy provided)
- Tile navigation targets: /new-brief, /screening, /vault
- Use a 4-up grid layout (three tiles occupying available columns)
- Include standard header/nav and footer
- Exclude summary widgets from the Dashboard
- Ensure keyboard accessibility and logical tab order

## Out of Scope
- Producer-specific or Admin dashboard experiences
- Feature work on destination pages beyond navigation
- Role-based gating/feature flags beyond stated Client behavior
- Analytics/telemetry
- Localization or theming changes
- Dynamic badges/counters or backend integrations
- Multi-organization context switching

## Assumptions
- Initial descriptive subtext will be refined later without layout changes.
- 4-up grid is four columns on desktop; responsive behavior follows existing design system.
- Existing Rivvy Portal UI components/styles are reused; no new design tokens or icons needed.
- Accessibility follows portal standards (focusable tiles, Enter/Space activation).

## Acceptance Criteria
- Given an authenticated Client user visits /dashboard, exactly three tiles are visible with labels: New Brief, Screening Room, Brand Vault.
- Tiles render in the order: New Brief (first), Screening Room (second), Brand Vault (third) across supported viewports.
- Each tile includes a visible descriptive subtext beneath its label.
- Clicking New Brief navigates to /new-brief in the same tab.
- Clicking Screening Room navigates to /screening in the same tab.
- Clicking Brand Vault navigates to /vault in the same tab.
- No tiles are hidden or disabled for Client users in the PoC.
- No summary widgets (e.g., Recent Videos, Draft Briefs) appear on the Dashboard.
- Tiles are keyboard-focusable and activate via Enter/Space; tab order matches visual order.
- Standard header/nav and footer are present on the Dashboard.

## Date Initialized
2026-03-16
