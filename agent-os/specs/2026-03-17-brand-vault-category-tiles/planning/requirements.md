# Spec Requirements: Brand Vault Category Tiles

## Initial Description

On the Brand Vault screen (/vault), render a grid of four category tiles—Logos, Fonts, Guidelines, Visuals. Each tile shows a category label, an icon, and counts derived from BrandAsset records: a required org-wide active-asset count, and, when a current project context exists, a secondary per-project active-asset count. Counts are based on distinct BrandAsset rows (not versions), include only status=active, and exclude archived. Tiles are read-only and non-clickable in this story; listing and filtering are handled by separate planned stories.

## Requirements Discussion

### First Round Questions

**Q1:** The `brand_asset.asset_type` column is TEXT. I'm assuming the exact string values stored are lowercase: `logo`, `font`, `guideline`, `image`, `footage`. The Visuals tile aggregates both `image` and `footage` counts. Is that correct, or are the stored values different?
**Answer:** The asset_type values are: `logos`, `fonts`, `guidelines`, `visuals`. (Each tile maps 1:1 to a single asset_type value — Visuals maps to `visuals`, not to `image`+`footage` as originally assumed.)

**Q2:** For the API design: I'm planning a single endpoint like `GET /api/brand-assets/counts?projectId={optional}` that returns all four category counts in one response (org-wide counts always, plus project counts if projectId is provided). The backend resolves orgId from the session. Does that match your expectations?
**Answer:** Yes.

**Q3:** For the "current project context": since there's no project selector in scope for this story, I'm assuming projectId will NOT be available in this initial implementation — meaning the UI will always show only org-wide counts for now. The project count display is wired up but dormant until a future story provides projectId.
**Answer:** Correct.

**Q4:** For icons: since there's no icon library in the project currently, should I add a lightweight icon library like `react-icons`, or keep it simple with inline SVG/text placeholders?
**Answer:** Add a lightweight icon library.

**Q5:** The tile grid layout: I'm assuming the same 4-column CSS Grid pattern used in DashboardPage. Should the VaultCategoryTile be a new reusable component in `src/components/`, or is it specific enough to live inline in VaultPage?
**Answer:** Reusable component in `src/components/`.

**Q6:** For authorization: I'm assuming the same pattern as BriefService — resolve the user's org from their membership, then count only BrandAssets where `org_id` matches. Producers assigned to the org can also see counts.
**Answer:** Correct.

### Existing Code to Reference

**Similar Features Identified:**
- Component: `DashboardTile` — Path: `rivvy-portal-ui/src/components/DashboardTile.tsx` (card tile pattern, reusable component structure)
- Page: `DashboardPage` — Path: `rivvy-portal-ui/src/pages/DashboardPage.tsx` (4-column CSS Grid layout, responsive collapse)
- Controller: `BriefController` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BriefController.java` (REST endpoint pattern)
- Service: `BriefService` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/service/BriefService.java` (org resolution, authorization pattern)
- Entity: `BrandAsset` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/BrandAsset.java` (existing JPA entity)
- Repository: `BrandAssetRepository` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BrandAssetRepository.java` (existing repo to extend with count queries)
- Page placeholder: `VaultPage` — Path: `rivvy-portal-ui/src/pages/VaultPage.tsx` (placeholder to replace)
- Styles pattern: `LoginPage.tsx` — Path: `rivvy-portal-ui/src/pages/LoginPage.tsx` (inline styles as `Record<string, React.CSSProperties>`)

### Follow-up Questions
None required.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A

## Requirements Summary

### Functional Requirements
- Replace VaultPage placeholder with a 4-tile category grid on /vault
- Four tiles: Logos, Fonts, Guidelines, Visuals — each mapping 1:1 to a `brand_asset.asset_type` value (`logos`, `fonts`, `guidelines`, `visuals`)
- Each tile shows: category icon, category label, org-wide active-asset count
- Each tile optionally shows a per-project active-asset count when projectId is available (dormant for now — no project selector in this story)
- Counts are based on distinct `BrandAsset` rows with `status=active` for the user's org; `BrandAssetVersion` records are not counted
- Tiles are read-only and non-clickable — no navigation on click
- Loading state shown while counts are fetched; non-blocking error state on failure (page remains usable)
- Each tile has an accessible name including category and counts (aria-label)
- Single backend endpoint: `GET /api/brand-assets/counts?projectId={optional}` returns all four category counts
- Backend resolves orgId from authenticated user's org membership (same as BriefService pattern)
- Authorization: org members and assigned producers can see counts
- Add a lightweight icon library (e.g., `react-icons`) for category icons
- Create `VaultCategoryTile` as a reusable component in `src/components/`

### Reusability Opportunities
- `DashboardTile` component pattern for tile structure and styling
- `DashboardPage` CSS Grid layout (repeat(4, 1fr), responsive single-column below 768px)
- `BriefService` authorization pattern (org resolution from session, producer assignment check)
- `BriefController` endpoint pattern (thin controller delegating to service)
- `BrandAsset` JPA entity and `BrandAssetRepository` — extend with count queries

### Scope Boundaries
**In Scope:**
- Backend: BrandAssetService, BrandAssetController, count endpoint, count query methods on repository
- Frontend: VaultCategoryTile reusable component, VaultPage rewrite with tile grid
- Icon library addition (e.g., react-icons)
- Loading/error states
- Accessibility (aria-labels with counts)

**Out of Scope:**
- Clickable tiles or navigation to asset lists (planned: list story)
- Filtering UI or project selector (planned: filter story)
- Uploading/editing/searching/previewing assets
- Admin/category management
- Categories beyond Logos, Fonts, Guidelines, Visuals
- Asset types not in the four-tile mapping (e.g., audio, other)
- Functional project context (no project selector exists yet)

### Technical Considerations
- Full-stack feature: Spring Boot backend + React frontend
- Backend: Spring Boot 3.4.1, Java 21, Spring Security (session-based auth), Spring Data JPA
- Frontend: React 19.x, TypeScript 5.x, Vite 5.x, react-router-dom v7
- New dependency: lightweight icon library (e.g., `react-icons`)
- Use existing `BrandAsset` entity and `BrandAssetRepository`
- Count query: `SELECT asset_type, COUNT(*) FROM brand_asset WHERE org_id = ? AND status = 'active' GROUP BY asset_type`
- Optional project count: add `AND project_id = ?` when projectId is provided
- CSRF exclusion may not be needed for GET-only endpoint (GET is safe method)
- Follow inline styles pattern (`Record<string, React.CSSProperties>`)
- Vitest + React Testing Library for frontend tests; JUnit 5 + Testcontainers for backend tests
