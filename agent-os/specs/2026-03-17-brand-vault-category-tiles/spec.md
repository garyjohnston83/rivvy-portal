# Specification: Brand Vault Category Tiles

## Goal
Replace the placeholder VaultPage with a four-tile category grid (Logos, Fonts, Guidelines, Visuals) backed by a new backend count endpoint, so users landing on /vault immediately see how many active brand assets their organization holds in each category.

## User Stories
- As an org member, I want to see a count of active brand assets per category on the Vault page so that I have an at-a-glance summary of my organization's brand library.
- As an assigned producer, I want to view the same category counts for a client organization so that I can assess their available brand assets before starting work.

## Specific Requirements

**Update BrandAssetType enum**
- The existing `BrandAssetType` enum at `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BrandAssetType.java` currently defines `LOGO, FONT, COLOR_PALETTE, IMAGE, VIDEO, DOCUMENT, OTHER`
- Replace all existing values with exactly four values: `LOGOS, FONTS, GUIDELINES, VISUALS` (these are the canonical asset_type values stored in the database, per the requirements discussion)
- The `BrandAsset.assetType` field uses `@Enumerated(EnumType.STRING)`, so the stored column value will be the enum name (e.g., `LOGOS`, `FONTS`, etc.)
- If any existing seed data or test data references the old enum values, update those references to use the new values

**Backend repository: count queries on BrandAssetRepository**
- Add a JPQL query method to `BrandAssetRepository` (at `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BrandAssetRepository.java`) that counts BrandAsset rows grouped by `assetType`, filtered by `organization` and `status = ACTIVE`
- The method should use a `@Query` annotation with a JPQL query equivalent to: `SELECT b.assetType, COUNT(b) FROM BrandAsset b WHERE b.organization.id = :orgId AND b.status = 'ACTIVE' GROUP BY b.assetType`
- Return type should be `List<Object[]>` where each element is `[BrandAssetType, Long]`
- Add a second query method for the optional project-scoped count that additionally filters by `b.project.id = :projectId`
- Both queries count distinct `BrandAsset` rows only (not `BrandAssetVersion` rows)

**Backend service layer: BrandAssetService**
- Create a new `BrandAssetService` class in `crud-logic-service/src/main/java/com/rivvystudios/portal/service/BrandAssetService.java`
- Inject `BrandAssetRepository`, `UserAccountRepository`, `OrganizationMemberRepository`, and `ProducerAssignmentRepository` (same dependencies as `BriefService`)
- Implement a method `getCategoryCounts(String email, UUID projectId)` that: (1) resolves `UserAccount` by email, (2) resolves org from the user's first `OrganizationMember` membership, (3) calls the repository count query with `orgId` and `status=ACTIVE`, (4) optionally calls the project-scoped count query if `projectId` is non-null
- Authorization: allow access if the user is a direct org member OR is a producer assigned to the org (reuse the `BriefService.getBriefById` authorization pattern using `OrganizationMemberRepository.findByUserAccount` and `ProducerAssignmentRepository.existsByProducerMemberAndClientOrg`)
- Throw `ResponseStatusException(FORBIDDEN)` if not authorized, `ResponseStatusException(NOT_FOUND)` if user not found, `ResponseStatusException(BAD_REQUEST)` if user has no org membership
- Return a DTO/response object containing a map of all four categories to their org counts, plus an optional map of project counts (null when projectId was not provided)

**Backend DTO: BrandAssetCountsResponse**
- Create `BrandAssetCountsResponse` in `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/BrandAssetCountsResponse.java`
- Fields: `Map<String, Long> orgCounts` (keys: `logos`, `fonts`, `guidelines`, `visuals`) and `Map<String, Long> projectCounts` (nullable, same keys)
- Always return all four keys in `orgCounts`, defaulting to `0` for categories with no matching rows
- `projectCounts` is null when no `projectId` was provided; otherwise same structure with defaults of `0`

**Backend API endpoint: GET /api/brand-assets/counts**
- Create a new `BrandAssetController` in `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BrandAssetController.java`
- Follow the `BriefController` pattern: `@RestController`, `@RequestMapping("/api/brand-assets")`, constructor injection of `BrandAssetService`
- Single endpoint: `@GetMapping("/counts")` with `@RequestParam(required = false) UUID projectId`
- Extract email from `SecurityContextHolder.getContext().getAuthentication().getName()` (same as `BriefController`)
- Delegate to `BrandAssetService.getCategoryCounts(email, projectId)` and return `ResponseEntity.ok(result)`
- No CSRF exemption needed in `SecurityConfig` because GET is a safe method; the existing `anyRequest().authenticated()` rule already protects this endpoint

**Frontend: add react-icons dependency**
- Add `react-icons` as a production dependency in `rivvy-portal-ui/package.json`
- Use icons from the `react-icons/fi` (Feather) set for a clean, consistent look: `FiImage` for Logos, `FiType` for Fonts, `FiBookOpen` for Guidelines, `FiCamera` for Visuals (or similar appropriate mappings)

**Frontend: VaultCategoryTile component**
- Create `rivvy-portal-ui/src/components/VaultCategoryTile.tsx` as a reusable component
- Props interface: `{ icon: React.ReactNode; label: string; orgCount: number; projectCount?: number }`
- Renders a non-clickable `<div>` (not a `<Link>`) with card styling matching the `DashboardTile` visual style (border, borderRadius 8px, boxShadow, padding 1.5rem, white background)
- Layout within tile: icon at top, category label below it, org count displayed prominently, optional project count displayed as secondary text when provided
- Use inline styles via `Record<string, React.CSSProperties>` pattern (same as `DashboardTile` and `LoginPage`)
- Add `aria-label` on the tile div that includes the category name and counts (e.g., "Logos: 12 org assets" or "Logos: 12 org assets, 3 project assets")
- No hover/focus interaction styles since tiles are non-clickable

**Frontend: VaultPage rewrite**
- Rewrite `rivvy-portal-ui/src/pages/VaultPage.tsx` to replace the placeholder content
- Preserve `data-testid="page-vault"` on the outermost `<div>`
- On mount, fetch `GET /api/brand-assets/counts` with `credentials: 'include'` (same fetch pattern as `LoginPage`)
- Use `useState` for loading (boolean), error (string), and counts data; use `useEffect` for the fetch call
- projectId is not passed in this story (dormant); when a future project selector provides it, append `?projectId={value}` to the fetch URL
- Render a 4-column CSS Grid using an inline `<style>` tag with a class name (same pattern as `DashboardPage`): `grid-template-columns: repeat(4, 1fr)`, `gap: 1.5rem`, responsive `@media (max-width: 768px)` collapsing to `grid-template-columns: 1fr`
- Render four `VaultCategoryTile` instances in fixed order: Logos, Fonts, Guidelines, Visuals
- Each tile receives its icon (from react-icons), label, orgCount from the fetched data, and projectCount only when available

**Loading and error states**
- While the fetch is in progress, display a loading indicator (simple text "Loading..." or a spinner) within the `data-testid="page-vault"` div
- On fetch failure (network error or non-200 response), display a non-blocking error message (e.g., a styled alert div with `role="alert"`) but still render the page structure; do not crash or show a blank page
- On success with zero counts, render all four tiles with count values of `0`

**Accessibility**
- Each `VaultCategoryTile` must have an `aria-label` that includes the category name and count values so screen readers announce meaningful content
- The error message div must use `role="alert"` so screen readers announce it
- Icons must have `aria-hidden="true"` since the label text already conveys the category meaning

**Backend tests**
- Create `crud-logic-service/src/test/java/com/rivvystudios/portal/brandasset/BrandAssetControllerTests.java`
- Follow the `BriefControllerTests` pattern: `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Import(TestcontainersConfiguration.class)`, use `MockMvc` and `JdbcTemplate`
- Use the `loginAs` helper pattern to authenticate as client and producer users
- Test cases: (1) GET /api/brand-assets/counts as authenticated org member returns 200 with all four category keys, (2) GET as assigned producer returns 200, (3) GET unauthenticated returns 401, (4) GET as user from a different org returns 403, (5) counts reflect only ACTIVE status rows

**Frontend tests**
- Create `rivvy-portal-ui/src/components/VaultCategoryTile.test.tsx` following the `DashboardTile.test.tsx` pattern
- Test cases: renders label, renders orgCount, renders projectCount when provided, omits projectCount when not provided, applies card styling, has correct aria-label
- Create `rivvy-portal-ui/src/pages/VaultPage.test.tsx` following the `DashboardPage.test.tsx` pattern
- Test cases: renders `data-testid="page-vault"`, renders all four tiles after successful fetch, shows loading state initially, shows error message on fetch failure

## Existing Code to Leverage

**DashboardTile component (`rivvy-portal-ui/src/components/DashboardTile.tsx`)**
- Card styling pattern with inline `Record<string, React.CSSProperties>`: border `1px solid #ddd`, borderRadius `8px`, boxShadow, padding `1.5rem`, white background
- VaultCategoryTile should replicate this visual style but render a `<div>` instead of a `<Link>` since tiles are non-clickable
- Test file `DashboardTile.test.tsx` provides the pattern for component-level tests with a `renderTile` helper and style assertions

**DashboardPage layout (`rivvy-portal-ui/src/pages/DashboardPage.tsx`)**
- 4-column CSS Grid via inline `<style>` tag with class name `dashboard-grid`: `grid-template-columns: repeat(4, 1fr)`, `gap: 1.5rem`
- Responsive breakpoint at 768px collapsing to single column
- VaultPage should use the same grid technique with its own class name (e.g., `vault-grid`) to avoid style collisions
- `data-testid` attribute on outermost div (`page-dashboard` pattern; VaultPage uses `page-vault`)

**BriefService authorization pattern (`crud-logic-service/src/main/java/com/rivvystudios/portal/service/BriefService.java`)**
- Resolves `UserAccount` via `userAccountRepository.findByEmail(email)`, then gets org from `organizationMemberRepository.findByUserAccount(userAccount).get(0).getOrganization()`
- Checks org membership with stream `.anyMatch(m -> m.getOrganization().getId().equals(...))`
- Falls back to producer check via `producerAssignmentRepository.existsByProducerMemberAndClientOrg(member, org)`
- BrandAssetService should replicate this exact authorization flow, resolving the target org from the user's own membership (not from a resource like Brief)

**BriefController endpoint pattern (`crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BriefController.java`)**
- Thin controller: extracts email from `SecurityContextHolder.getContext().getAuthentication().getName()`, delegates entirely to service
- Uses `@RestController`, `@RequestMapping("/api/...")`, constructor injection
- BrandAssetController should follow this identical pattern

**BriefControllerTests integration test pattern (`crud-logic-service/src/test/java/com/rivvystudios/portal/brief/BriefControllerTests.java`)**
- Uses `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Import(TestcontainersConfiguration.class)`
- `loginAs(email)` helper returns session cookies; tests pass cookies to subsequent requests
- Uses `JdbcTemplate` for test data setup (e.g., inserting BrandAsset rows with specific asset_type and status values)
- Asserts JSON response structure with `jsonPath("$.field").value(expected)`

## Out of Scope
- Clickable tiles or navigation to asset list views (planned: asset list story)
- Filtering UI, search, or project selector dropdown (planned: filter story)
- Uploading, editing, deleting, or previewing brand assets
- Admin category management or taxonomy changes
- Displaying categories beyond Logos, Fonts, Guidelines, Visuals
- Asset types not represented by the four tiles
- Functional project context (projectId is accepted by the API but no UI project selector exists yet)
- BrandAssetVersion counting or version-level display
- Pagination, sorting, or any list-level interactions on the Vault page
- Real-time or WebSocket-based count updates
