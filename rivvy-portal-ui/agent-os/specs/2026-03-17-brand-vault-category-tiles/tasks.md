# Task Breakdown: Brand Vault Category Tiles

## Overview
Total Tasks: 37

## Task List

### Backend Layer

#### Task Group 1: Enum Update and Repository Count Queries
**Dependencies:** None

- [x] 1.0 Complete enum update and repository count queries
  - [x] 1.1 Write 4 focused tests for the count endpoint (backend integration tests)
    - Create `crud-logic-service/src/test/java/com/rivvystudios/portal/brandasset/BrandAssetControllerTests.java`
    - Follow the `BriefControllerTests` pattern: `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Import(TestcontainersConfiguration.class)`, `MockMvc`, `JdbcTemplate`
    - Use the existing `loginAs` helper pattern to authenticate as client and producer users
    - Test 1: GET `/api/brand-assets/counts` as authenticated org member (`client2@acme.local`) returns 200 with all four category keys (`logos`, `fonts`, `guidelines`, `visuals`) present in `orgCounts`
    - Test 2: GET `/api/brand-assets/counts` as assigned producer (`producer@rivvy.local`) returns 200 with valid counts
    - Test 3: GET `/api/brand-assets/counts` unauthenticated (no session cookies) returns 401
    - Test 4: GET `/api/brand-assets/counts` returns counts reflecting only `ACTIVE` status rows (insert test data via `JdbcTemplate` with mixed statuses, verify only ACTIVE rows are counted)
  - [x] 1.2 Update `BrandAssetType` enum with new values
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BrandAssetType.java`
    - Replace existing values (`LOGO, FONT, COLOR_PALETTE, IMAGE, VIDEO, DOCUMENT, OTHER`) with exactly: `LOGOS, FONTS, GUIDELINES, VISUALS`
    - Search for any seed data or test data referencing old enum values and update them to the new values
  - [x] 1.3 Add count query methods to `BrandAssetRepository`
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BrandAssetRepository.java`
    - Add `@Query` annotated method for org-scoped count: `SELECT b.assetType, COUNT(b) FROM BrandAsset b WHERE b.organization.id = :orgId AND b.status = 'ACTIVE' GROUP BY b.assetType` returning `List<Object[]>`
    - Add a second `@Query` annotated method for project-scoped count that additionally filters by `b.project.id = :projectId`, also returning `List<Object[]>`
    - Both queries count distinct `BrandAsset` rows only (not `BrandAssetVersion`)
  - [x] 1.4 Create `BrandAssetCountsResponse` DTO
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/BrandAssetCountsResponse.java`
    - Fields: `Map<String, Long> orgCounts` (keys: `logos`, `fonts`, `guidelines`, `visuals`) and `Map<String, Long> projectCounts` (nullable, same keys)
    - Always include all four keys in `orgCounts`, defaulting missing categories to `0`
    - `projectCounts` is null when no `projectId` was provided; otherwise same four-key structure with `0` defaults
    - Include getters/setters following existing DTO patterns (e.g., `BriefResponse.java`)
  - [x] 1.5 Create `BrandAssetService`
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/service/BrandAssetService.java`
    - Annotate with `@Service`
    - Constructor-inject: `BrandAssetRepository`, `UserAccountRepository`, `OrganizationMemberRepository`, `ProducerAssignmentRepository`
    - Implement `getCategoryCounts(String email, UUID projectId)` method:
      - Resolve `UserAccount` via `userAccountRepository.findByEmail(email)`, throw `ResponseStatusException(NOT_FOUND)` if absent
      - Get org from `organizationMemberRepository.findByUserAccount(userAccount).get(0).getOrganization()`, throw `ResponseStatusException(BAD_REQUEST)` if memberships list is empty
      - Authorize: check if user is org member via `memberships.stream().anyMatch(...)`, or is assigned producer via `producerAssignmentRepository.existsByProducerMemberAndClientOrg(member, org)` -- reuse exact `BriefService.getBriefById` authorization pattern
      - Throw `ResponseStatusException(FORBIDDEN)` if neither check passes
      - Call org-scoped repository count query with `orgId`
      - If `projectId` is non-null, also call project-scoped repository count query
      - Build and return `BrandAssetCountsResponse` with all four category keys defaulting to `0`, populated from query results
  - [x] 1.6 Create `BrandAssetController`
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BrandAssetController.java`
    - Follow `BriefController` pattern: `@RestController`, `@RequestMapping("/api/brand-assets")`, constructor injection of `BrandAssetService`
    - Single endpoint: `@GetMapping("/counts")` with `@RequestParam(required = false) UUID projectId`
    - Extract email from `SecurityContextHolder.getContext().getAuthentication().getName()`
    - Delegate to `BrandAssetService.getCategoryCounts(email, projectId)` and return `ResponseEntity.ok(result)`
    - No CSRF or SecurityConfig changes needed (GET is a safe method; existing `anyRequest().authenticated()` protects this endpoint)
  - [x] 1.7 Ensure backend tests pass
    - Run ONLY the 4 tests written in 1.1 (`BrandAssetControllerTests`)
    - Verify the endpoint returns correct JSON structure with `orgCounts` containing all four keys
    - Verify authorization works for org members and producers
    - Verify unauthenticated requests return 401
    - Verify only ACTIVE-status rows are counted
    - Do NOT run the entire backend test suite at this stage

**Acceptance Criteria:**
- `BrandAssetType` enum contains exactly `LOGOS, FONTS, GUIDELINES, VISUALS`
- Repository count queries return `List<Object[]>` with `[BrandAssetType, Long]` tuples
- `BrandAssetService` resolves org from user membership and enforces authorization (org member or assigned producer)
- `GET /api/brand-assets/counts` returns 200 with `{ orgCounts: { logos: N, fonts: N, guidelines: N, visuals: N }, projectCounts: null }`
- All 4 backend integration tests pass
- Only ACTIVE-status `BrandAsset` rows are counted

---

### Frontend Layer

#### Task Group 2: VaultCategoryTile Component
**Dependencies:** None (can proceed in parallel with Task Group 1)

- [x] 2.0 Complete VaultCategoryTile component
  - [x] 2.1 Write 5 focused tests for VaultCategoryTile
    - Create `rivvy-portal-ui/src/components/VaultCategoryTile.test.tsx`
    - Follow `DashboardTile.test.tsx` pattern: `renderTile` helper, `@testing-library/react`, Vitest
    - Test 1: Renders the label prop as visible text
    - Test 2: Renders the orgCount value
    - Test 3: Renders projectCount when provided
    - Test 4: Does not render project count text when projectCount prop is omitted
    - Test 5: Has correct `aria-label` including category name and counts
  - [x] 2.2 Add `react-icons` dependency
    - Add `react-icons` as a production dependency in `rivvy-portal-ui/package.json`
    - Run `npm install` to update `package-lock.json`
    - Verify icons from `react-icons/fi` (Feather) set are importable: `FiImage` (Logos), `FiType` (Fonts), `FiBookOpen` (Guidelines), `FiCamera` (Visuals)
  - [x] 2.3 Create `VaultCategoryTile` component
    - File: `rivvy-portal-ui/src/components/VaultCategoryTile.tsx`
    - Props interface: `{ icon: React.ReactNode; label: string; orgCount: number; projectCount?: number }`
    - Render a non-clickable `<div>` (not a `<Link>`) with card styling matching `DashboardTile`: `border: '1px solid #ddd'`, `borderRadius: '8px'`, `boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'`, `padding: '1.5rem'`, `backgroundColor: '#fff'`
    - Use `Record<string, React.CSSProperties>` pattern for inline styles (same as `DashboardTile` and `LoginPage`)
    - Layout: icon at top (with `aria-hidden="true"`), category label below, org count displayed prominently, optional project count as secondary text when provided
    - Add `aria-label` on the tile div (e.g., `"Logos: 12 org assets"` or `"Logos: 12 org assets, 3 project assets"`)
    - No hover/focus interaction styles (tiles are non-clickable)
  - [x] 2.4 Ensure VaultCategoryTile tests pass
    - Run ONLY the 5 tests written in 2.1
    - Verify component renders correctly with all prop variations
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- `react-icons` is installed as a production dependency
- `VaultCategoryTile` renders a non-clickable card div with icon, label, org count, and optional project count
- Card styling matches `DashboardTile` visual style (border, borderRadius, boxShadow, padding, white background)
- `aria-label` includes category name and count values
- Icons have `aria-hidden="true"`
- All 5 component tests pass

---

#### Task Group 3: VaultPage Rewrite
**Dependencies:** Task Group 1 (backend endpoint), Task Group 2 (VaultCategoryTile component)

- [x] 3.0 Complete VaultPage rewrite
  - [x] 3.1 Write 4 focused tests for VaultPage
    - Create `rivvy-portal-ui/src/pages/VaultPage.test.tsx`
    - Follow `DashboardPage.test.tsx` pattern with `MemoryRouter` wrapper
    - Mock `fetch` using `vi.fn()` to control API responses
    - Test 1: Renders `data-testid="page-vault"` on the outermost div
    - Test 2: Renders all four category tiles (Logos, Fonts, Guidelines, Visuals) after successful fetch
    - Test 3: Shows loading state initially before fetch resolves
    - Test 4: Shows error message with `role="alert"` on fetch failure
  - [x] 3.2 Rewrite `VaultPage` with fetch and tile grid
    - File: `rivvy-portal-ui/src/pages/VaultPage.tsx`
    - Preserve `data-testid="page-vault"` on the outermost `<div>`
    - State: `useState` for `loading` (boolean, initially `true`), `error` (string), and `counts` data (orgCounts map and optional projectCounts map)
    - On mount (`useEffect`), fetch `GET /api/brand-assets/counts` with `credentials: 'include'` (same fetch pattern as `LoginPage`)
    - `projectId` is not passed in this story (dormant); when a future project selector provides it, append `?projectId={value}` to the fetch URL
    - On success, parse JSON and store counts; on failure, set error string
    - Always set `loading` to `false` in `finally` block
  - [x] 3.3 Implement 4-column CSS Grid layout
    - Use inline `<style>` tag with class name `vault-grid` (same pattern as `DashboardPage` uses `dashboard-grid`) to avoid style collisions
    - Grid: `grid-template-columns: repeat(4, 1fr)`, `gap: 1.5rem`
    - Responsive: `@media (max-width: 768px)` collapsing to `grid-template-columns: 1fr`
  - [x] 3.4 Render four VaultCategoryTile instances
    - Fixed order: Logos, Fonts, Guidelines, Visuals
    - Icon mapping from `react-icons/fi`: `FiImage` for Logos, `FiType` for Fonts, `FiBookOpen` for Guidelines, `FiCamera` for Visuals
    - Each tile receives: `icon` (React element), `label` (string), `orgCount` (from fetched `orgCounts` map, default `0`), and `projectCount` (from fetched `projectCounts` map when available)
  - [x] 3.5 Implement loading and error states
    - While fetch is in progress (`loading === true`): display `"Loading..."` text within the `data-testid="page-vault"` div
    - On fetch failure: display a styled alert div with `role="alert"` containing the error message; use error styling similar to `LoginPage` error style (red text, light red background, border-radius)
    - On failure, still render the page structure (do not crash or show blank page) -- render tiles with `0` counts as fallback
    - On success with zero counts: render all four tiles with count values of `0`
  - [x] 3.6 Ensure VaultPage tests pass
    - Run ONLY the 4 tests written in 3.1
    - Verify page renders testid, all four tiles, loading state, and error state
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- VaultPage fetches `GET /api/brand-assets/counts` on mount with `credentials: 'include'`
- 4-column CSS Grid layout with responsive single-column collapse at 768px
- Four tiles rendered in order: Logos, Fonts, Guidelines, Visuals with correct icons
- Loading state shows "Loading..." text
- Error state shows `role="alert"` message without crashing the page
- `data-testid="page-vault"` is preserved on the outermost div
- All 4 page tests pass

---

### Test Review

#### Task Group 4: Test Review and Gap Analysis
**Dependencies:** Task Groups 1, 2, 3

- [x] 4.0 Review existing tests and fill critical gaps only
  - [x] 4.1 Review tests from Task Groups 1-3
    - Review the 4 backend integration tests from Task Group 1 (BrandAssetControllerTests)
    - Review the 5 component tests from Task Group 2 (VaultCategoryTile.test.tsx)
    - Review the 4 page tests from Task Group 3 (VaultPage.test.tsx)
    - Total existing tests: 13
  - [x] 4.2 Analyze test coverage gaps for this feature only
    - Identify critical workflows that lack test coverage across the full stack
    - Focus ONLY on gaps related to the Brand Vault Category Tiles feature
    - Do NOT assess entire application test coverage
    - Prioritize: authorization edge cases, API response shape validation, component accessibility, end-to-end data flow
  - [x] 4.3 Write up to 10 additional strategic tests maximum
    - Potential gap areas to consider (add tests only where critically needed):
      - Backend: GET as user from a different org (not assigned) returns 403
      - Backend: Response includes `projectCounts: null` when no `projectId` param is provided
      - Backend: Response `orgCounts` defaults missing categories to `0` when no brand assets exist
      - Frontend component: VaultCategoryTile applies card-like styling (border, borderRadius, boxShadow) matching DashboardTile
      - Frontend component: Icon within VaultCategoryTile has `aria-hidden="true"`
      - Frontend page: VaultPage renders tiles with `0` counts when API returns empty counts
      - Frontend page: VaultPage passes correct orgCount values from fetch response to each tile
    - Add maximum of 10 new tests to fill identified critical gaps
    - Do NOT write comprehensive coverage for all scenarios
    - Skip edge cases, performance tests, and accessibility tests unless business-critical
  - [x] 4.4 Run all feature-specific tests
    - Run ONLY tests related to this feature:
      - `crud-logic-service/src/test/java/com/rivvystudios/portal/brandasset/BrandAssetControllerTests.java`
      - `rivvy-portal-ui/src/components/VaultCategoryTile.test.tsx`
      - `rivvy-portal-ui/src/pages/VaultPage.test.tsx`
    - Expected total: approximately 13-23 tests maximum
    - Do NOT run the entire application test suite
    - Verify all critical workflows pass

**Acceptance Criteria:**
- All feature-specific tests pass (approximately 13-23 tests total)
- Critical authorization, data flow, and accessibility workflows are covered
- No more than 10 additional tests added beyond the original 13
- Testing focused exclusively on Brand Vault Category Tiles feature requirements

---

## Execution Order

Recommended implementation sequence:

1. **Task Group 1 (Backend Layer)** and **Task Group 2 (VaultCategoryTile Component)** -- these two groups can be executed in parallel since they have no dependencies on each other
   - Task Group 1 builds the enum, repository queries, service, DTO, and controller
   - Task Group 2 adds `react-icons` and builds the reusable tile component
2. **Task Group 3 (VaultPage Rewrite)** -- depends on both Task Group 1 (the API endpoint must exist for the fetch) and Task Group 2 (the VaultCategoryTile component must exist for rendering)
3. **Task Group 4 (Test Review and Gap Analysis)** -- depends on all previous groups being complete; reviews all tests and fills critical gaps

## File Manifest

### Files Created (New)
- `crud-logic-service/src/test/java/com/rivvystudios/portal/brandasset/BrandAssetControllerTests.java`
- `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/BrandAssetCountsResponse.java`
- `crud-logic-service/src/main/java/com/rivvystudios/portal/service/BrandAssetService.java`
- `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BrandAssetController.java`
- `rivvy-portal-ui/src/components/VaultCategoryTile.tsx`
- `rivvy-portal-ui/src/components/VaultCategoryTile.test.tsx`
- `rivvy-portal-ui/src/pages/VaultPage.test.tsx`

### Files Modified (Existing)
- `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BrandAssetType.java` -- replace enum values
- `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BrandAssetRepository.java` -- add count query methods
- `rivvy-portal-ui/package.json` -- add `react-icons` dependency
- `rivvy-portal-ui/src/pages/VaultPage.tsx` -- full rewrite from placeholder to tile grid
