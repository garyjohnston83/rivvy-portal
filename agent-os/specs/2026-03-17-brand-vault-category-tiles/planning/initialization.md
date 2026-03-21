# Spec Initialization: Brand Vault Category Tiles

## Raw Idea

On the Brand Vault screen (/vault), render a grid of four category tiles—Logos, Fonts, Guidelines, Visuals. Each tile shows a category label, an icon, and counts derived from BrandAsset records: a required org-wide active-asset count, and, when a current project context exists, a secondary per-project active-asset count. Counts are based on distinct BrandAsset rows (not versions), include only status=active, and exclude archived. Tiles are read-only and non-clickable in this story; listing and filtering are handled by separate planned stories.

## In Scope
- Render four tiles (Logos, Fonts, Guidelines, Visuals) on Brand Vault (/vault)
- Show category label and icon per tile
- Show org-wide active BrandAsset count per tile
- Show current-project active BrandAsset count per tile when a project context exists; otherwise omit the project count
- Compute counts on BrandAsset (not BrandAssetVersion) with status=active; exclude archived
- Loading and non-blocking error states
- Accessible labels including category and counts
- Backend API to provide counts for the current org and optional current project

## Out of Scope
- Clickable tiles or deep links to lists (handled by the planned list story)
- Any filtering UI or project selector (handled by the planned filter story)
- Uploading/editing assets, search, or inline previews
- Admin/category management or changing category taxonomy
- Displaying categories beyond the four specified

## Sibling Items (handled separately — do NOT implement these)
- [PLANNED] List assets by category (download-only)
- [PLANNED] Filter by scope and project

## Assumptions
- Category-to-assetType mapping: Logos=logo, Fonts=font, Guidelines=guideline, Visuals=image+footage.
- Assets with asset_type not in the mapping (e.g., audio, other) are not represented by tiles in this story.
- Counts include only BrandAsset rows where status=active; archived are excluded.
- Org context (orgId) is available from authenticated session/app state; optional project context (projectId) may be present from global state or route.
- Authorization rules are enforced server-side so counts reflect only assets the requester can access.
- Icons will be proposed and mapped in the UI; placeholders are acceptable initially.

## Acceptance Criteria
- On /vault, exactly four tiles appear labeled Logos, Fonts, Guidelines, and Visuals; tiles render even when counts are zero.
- Each tile displays an icon aligned with the category (placeholder acceptable until final set is approved).
- Org-wide count per tile equals the number of distinct BrandAsset records with the mapped asset types and status=active for the current organization; BrandAssetVersion records are not counted.
- If a current project context exists, a secondary per-project count is shown per tile for that project's active assets; if no project context exists, only org-wide counts are shown.
- Tiles are non-clickable and do not navigate.
- While counts load, a visible loading state is shown; on failure, a non-blocking error state appears and the page remains usable.
- Each tile has an accessible name that includes the category and presented counts (e.g., via aria-label), and counts are announced to screen readers.

## Date Initialized
2026-03-17
