# Task Breakdown: Screening Room Video List & Detail

## Overview
Total Tasks: 5 Task Groups, ~40 sub-tasks

This feature delivers the first paginated endpoint in the codebase along with two frontend views: an infinite-scrolling video list at `/screening` and a video detail page with an HTML5 player at `/screening/:videoId`. The backend provides paginated video queries, authorization, and stubbed pre-signed S3 URLs. Seed data scaffolds the full entity chain from Organization down to StorageObject.

## Task List

### Seed Data Layer

#### Task Group 1: Seed Data for Videos
**Dependencies:** None (seed data must exist before backend integration tests can run)

- [x] 1.0 Complete seed data layer
  - [x] 1.1 Review existing seed data in `R__seed_data.sql` to understand current orgs, users, projects, and foreign key chains
    - Locate file at `crud-logic-service/src/main/resources/db/changelog/seed/R__seed_data.sql`
    - Identify existing Acme Corp org UUID (`20..002`), existing user accounts (`client2@acme.local`, `producer@rivvy.local`, `client@rivvy.local`), and any existing project rows
    - Understand the full FK chain: Organization -> Project -> Brief -> BriefItem -> BriefItemDeliverable -> Video -> VideoVersion -> StorageObject
  - [x] 1.2 Insert Project row for Acme Corp
    - UUID: `70000000-0000-0000-0000-000000000001`
    - Linked to Acme Corp org (`20000000-0000-0000-0000-000000000002`)
    - Use `ON CONFLICT (id) DO NOTHING`
  - [x] 1.3 Insert Brief, BriefItem, and BriefItemDeliverable scaffolding rows
    - Brief UUID: `80000000-0000-0000-0000-000000000001` for Acme Corp
    - BriefItem UUID: `81000000-0000-0000-0000-000000000001`
    - BriefItemDeliverable UUID: `82000000-0000-0000-0000-000000000001`
    - All using `ON CONFLICT (id) DO NOTHING`
  - [x] 1.4 Insert 4 Video rows under the Acme Corp project
    - Video 1: "Brand Launch Teaser" -- will have approved, completed version
    - Video 2: "Product Demo" -- will have completed but unapproved version
    - Video 3: "Behind the Scenes" -- will have processing version
    - Video 4: "Social Cutdown" -- zero versions (edge case)
    - Use staggered `created_at` timestamps (1 day apart) to produce clear descending sort order
  - [x] 1.5 Insert StorageObject rows for each VideoVersion
    - Provider: `s3`, bucket: `rivvy-portal-dev`
    - Object keys like `videos/<uuid>.mp4`
  - [x] 1.6 Insert VideoVersion rows with varied states
    - Video 1 version: `isCurrent=true`, `transcodeStatus=COMPLETED`, `isApproved=true`
    - Video 2 version: `isCurrent=true`, `transcodeStatus=COMPLETED`, `isApproved=false`
    - Video 3 version: `transcodeStatus=PROCESSING`, `isApproved=false`
    - Video 4: no versions inserted
    - Each version references its corresponding StorageObject
  - [x] 1.7 Verify seed data loads without errors
    - Run the application or Flyway migration to confirm `R__seed_data.sql` executes successfully
    - Verify no constraint violations or duplicate key errors

**Acceptance Criteria:**
- `R__seed_data.sql` runs cleanly with all new inserts using `ON CONFLICT (id) DO NOTHING`
- 4 videos exist under Acme Corp project with the specified version states
- FK chain is intact: Project -> Brief -> BriefItem -> BriefItemDeliverable -> Video -> VideoVersion -> StorageObject
- Staggered `created_at` timestamps produce a deterministic sort order

---

### Backend Repository Layer

#### Task Group 2: Repository Queries and DTOs
**Dependencies:** Task Group 1 (seed data must exist for repository tests)

- [x] 2.0 Complete repository and DTO layer
  - [x] 2.1 Write 4-6 focused tests for repository query methods
    - Test `VideoRepository.findByProjectId` returns paginated results sorted correctly
    - Test `VideoVersionRepository.findByVideoIdAndIsCurrentTrue` returns the current version
    - Test `VideoVersionRepository.findFirstByVideoIdOrderByVersionNumberDesc` returns fallback version
    - Test `VideoVersionRepository.existsByVideoIdAndIsApprovedTrue` returns correct boolean
    - Test `VideoVersionRepository.findByVideoIdIn` batch-fetches versions for multiple video IDs
    - Use `@DataJpaTest` or equivalent with Testcontainers
  - [x] 2.2 Add paginated query to `VideoRepository`
    - Add method: `Page<Video> findByProjectId(UUID projectId, Pageable pageable)`
    - Spring Data JPA derives the query automatically
    - This is the first use of `Pageable`/`Page<T>` in the codebase
  - [x] 2.3 Add lookup queries to `VideoVersionRepository`
    - `Optional<VideoVersion> findByVideoIdAndIsCurrentTrue(UUID videoId)`
    - `Optional<VideoVersion> findFirstByVideoIdOrderByVersionNumberDesc(UUID videoId)`
    - `boolean existsByVideoIdAndIsApprovedTrue(UUID videoId)`
    - `List<VideoVersion> findByVideoIdIn(List<UUID> videoIds)`
  - [x] 2.4 Create `VideoListItemResponse` DTO
    - Package: `com.rivvystudios.portal.controller.dto`
    - Fields: `id` (UUID), `title` (String), `currentVersionNumber` (Integer, nullable), `isApproved` (boolean)
    - Plain Java class with getters/setters following the `BriefResponse` pattern
  - [x] 2.5 Create `VideoDetailResponse` DTO
    - Package: `com.rivvystudios.portal.controller.dto`
    - Fields: `id` (UUID), `title` (String), `description` (String, nullable), `currentVersionNumber` (Integer, nullable), `isApproved` (boolean), `transcodeStatus` (String, nullable), `playbackUrl` (String, nullable), `createdAt` (String, ISO-8601)
    - Plain Java class with getters/setters following the `BriefResponse` pattern
  - [x] 2.6 Ensure repository tests pass
    - Run ONLY the 4-6 tests written in 2.1
    - Verify all derived query methods work correctly against seeded data
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 4-6 repository tests pass
- `VideoRepository.findByProjectId` returns a `Page<Video>` sorted by `createdAt` desc, `title` asc
- All `VideoVersionRepository` query methods return correct results
- DTOs compile and follow the existing `BriefResponse` pattern
- No N+1 query risk: batch-fetch method `findByVideoIdIn` is available

---

### Backend Service & Controller Layer

#### Task Group 3: VideoService, VideoController, and Backend Integration Tests
**Dependencies:** Task Group 2 (repositories and DTOs must exist)

- [x] 3.0 Complete backend service and controller layer
  - [x] 3.1 Write 8 focused integration tests in `VideoControllerTests.java`
    - New file in `com.rivvystudios.portal.video` test package
    - Follow exact structure of `BriefControllerTests`: `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Import(TestcontainersConfiguration.class)`
    - Reuse `loginAs` helper pattern (POST to `/api/auth/login`, return cookies)
    - Test 1: `GET /api/videos?projectId={acmeProjectId}&page=0` as `client2@acme.local` returns 200 with `$.content` containing seeded videos
    - Test 2: Same endpoint as `producer@rivvy.local` (assigned to Acme) returns 200
    - Test 3: Same endpoint unauthenticated returns 401
    - Test 4: Same endpoint as `client@rivvy.local` (Rivvy Studios member, NOT Acme) returns 403
    - Test 5: `GET /api/videos/{videoId}` for a seeded video as `client2@acme.local` returns 200 with `title`, `playbackUrl`, and `transcodeStatus`
    - Test 6: `GET /api/videos/{nonExistentUuid}` returns 404
    - Test 7: Verify list response `isApproved` is `true` for the approved video and `false` for unapproved ones
    - Test 8: Verify pagination fields `totalElements`, `totalPages`, `number`, `last` are present in list response
  - [x] 3.2 Create `VideoService` with constructor injection
    - Inject: `VideoRepository`, `VideoVersionRepository`, `UserAccountRepository`, `OrganizationMemberRepository`, `ProducerAssignmentRepository`, `ProjectRepository`
    - Implement private authorization method following `BriefService.getBriefById` pattern:
      - Resolve user by email
      - Get org memberships
      - Load project by `projectId`, throw `NOT_FOUND` if missing
      - Verify project's org matches user's membership OR user is an assigned producer
      - Throw `FORBIDDEN` on failure
  - [x] 3.3 Implement `VideoService.getVideosByProject(String email, UUID projectId, int page)`
    - Authorize user for the project
    - Query `VideoRepository.findByProjectId` with `PageRequest.of(page, 25, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("title")))`
    - Batch-fetch all `VideoVersion` rows for the page's video IDs via `findByVideoIdIn`
    - For each video compute `currentVersionNumber` (from `isCurrent=true` version, else highest `versionNumber`, else `null`)
    - Compute `isApproved` (any version with `isApproved=true`)
    - Return `Page<VideoListItemResponse>` using `Page.map(...)`
  - [x] 3.4 Implement `VideoService.getVideoDetail(String email, UUID videoId)`
    - Load `Video` by ID, throw `NOT_FOUND` if missing
    - Authorize via the video's project org (same auth pattern)
    - Find latest version: `isCurrent=true` else highest `versionNumber`
    - Generate stubbed playback URL if `transcodeStatus == COMPLETED`:
      - Return `https://storage.example.com/stub-presigned/<storageObjectId>?token=stub`
    - Otherwise return `null` for playback URL
    - Build and return `VideoDetailResponse`
  - [x] 3.5 Create `VideoController`
    - `@RestController` at `@RequestMapping("/api/videos")`
    - Constructor-inject `VideoService`
    - `GET /api/videos?projectId={uuid}&page=0`:
      - Extract email from `SecurityContextHolder`
      - `@RequestParam UUID projectId` (required)
      - `@RequestParam(defaultValue = "0") int page`
      - Return `ResponseEntity.ok(videoService.getVideosByProject(email, projectId, page))`
    - `GET /api/videos/{videoId}`:
      - Extract email from `SecurityContextHolder`
      - `@PathVariable UUID videoId`
      - Return `ResponseEntity.ok(videoService.getVideoDetail(email, videoId))`
    - Follow thin controller pattern from `BriefController` -- no business logic
  - [x] 3.6 Ensure all 8 backend integration tests pass
    - Run ONLY the 8 tests written in 3.1
    - Verify authorization, pagination, detail endpoint, and error cases
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- All 8 integration tests pass
- Authorization correctly permits org members and assigned producers, rejects others with 403, returns 401 for unauthenticated
- Paginated list endpoint returns correct sort order, version numbers, and approved flags
- Detail endpoint returns stubbed playback URL for COMPLETED videos, null otherwise
- 404 returned for non-existent videos
- Pagination metadata (`totalElements`, `totalPages`, `number`, `last`) present in list response
- No N+1 queries: versions are batch-fetched

---

### Frontend Layer

#### Task Group 4: ScreeningPage, VideoDetailPage, and Route Registration
**Dependencies:** Task Group 3 (API endpoints must be defined so frontend can target them)

- [x] 4.0 Complete frontend pages and routing
  - [x] 4.1 Write 8 focused frontend tests in `src/__tests__/screening-room.test.tsx`
    - Use Vitest + React Testing Library
    - Use `MemoryRouter` with initial entries and mock `fetch` via `vi.fn()`
    - Test 1: ScreeningPage renders loading state initially
    - Test 2: ScreeningPage renders video list items after successful fetch
    - Test 3: ScreeningPage renders "No videos found for this project" empty state
    - Test 4: ScreeningPage renders error message on fetch failure
    - Test 5: Clicking a video card navigates to `/screening/:videoId`
    - Test 6: VideoDetailPage renders video title and `<video>` player when `playbackUrl` is present
    - Test 7: VideoDetailPage renders "No version available" when `currentVersionNumber` is null
    - Test 8: VideoDetailPage renders "Video is processing..." when `transcodeStatus` is not `COMPLETED`
  - [x] 4.2 Rewrite `ScreeningPage.tsx` with video list and infinite scroll
    - Read `projectId` from URL query string via `useSearchParams`
    - If `projectId` missing, display "No project selected" message
    - On mount and `projectId` change, fetch `GET /api/videos?projectId={projectId}&page=0` with `credentials: 'include'`
    - Store response as video items list plus pagination metadata (`totalPages`, `number`, `last`)
    - Follow `VaultPage` fetch + loading/error pattern with `useState` for `loading`, `error`, and data; `useEffect` with `cancelled` flag
    - Extend with pagination state: `page`, `hasMore`, `items` array that appends on subsequent fetches
    - Loading state: "Loading..." text (same style as `VaultPage`)
    - Error state: non-blocking error banner with `role="alert"` and `colors.error` styling, above the list, does not clear existing items
    - Empty state: "No videos found for this project"
    - Keep `data-testid="page-screening"` on root element
  - [x] 4.3 Implement video list card rendering
    - Render vertical list of clickable video cards
    - Each card shows: video title, "V{currentVersionNumber}" badge (or dash if null), green "Approved" badge if `isApproved`
    - Clicking a card navigates to `/screening/{videoId}` via `useNavigate`
    - Card styling: dark background (`colors.black` or slightly lighter), `1px solid ${colors.border}`, `borderRadius: '2px'`
    - Hover effect: `DashboardTile` pattern with `useState` for `hovered`/`focused`, border color changes to `colors.orange`
    - Inline styles with `Record<string, React.CSSProperties>` pattern using `colors` and `fonts` from `theme.ts`
  - [x] 4.4 Implement infinite scroll with IntersectionObserver
    - Place a sentinel `<div>` after the last video card
    - Use `IntersectionObserver` with `rootMargin: '0px 0px 200px 0px'` to trigger slightly before reaching the end
    - When sentinel enters viewport, fetch next page and append results to existing items
    - Stop observing when `last` is true (no more pages)
    - Show "Loading more..." at the bottom during subsequent page fetches
  - [x] 4.5 Create `VideoDetailPage.tsx`
    - New file at `src/pages/VideoDetailPage.tsx`
    - Read `videoId` from URL path param via `useParams`
    - On mount, fetch `GET /api/videos/{videoId}` with `credentials: 'include'`
    - Display video title as `<h1>`, "Approved" badge if applicable, "V{currentVersionNumber}" if present
    - Video player: if `playbackUrl` is non-null and `transcodeStatus` is `COMPLETED`, render native `<video>` with `controls`, `src={playbackUrl}`, `width="100%"`, `style={{ maxWidth: '800px' }}`, `preload="metadata"`
    - If `transcodeStatus` is not `COMPLETED` but a version exists, show "Video is processing..."
    - If no version exists (`currentVersionNumber` is null), show "No version available" and no player
    - Back link/button to navigate to `/screening`
    - Loading and error states following same pattern as ScreeningPage
    - `data-testid="page-video-detail"` on root element
    - Inline styles with theme tokens
  - [x] 4.6 Register new route in `App.tsx`
    - Add `import VideoDetailPage from './pages/VideoDetailPage'`
    - Add `<Route path="/screening/:videoId" element={<VideoDetailPage />} />` inside the `<Route element={<RootLayout />}>` block, after the existing `/screening` route
    - Existing `/screening` route remains unchanged
  - [x] 4.7 Ensure all 8 frontend tests pass
    - Run ONLY the 8 tests written in 4.1
    - Verify loading, error, empty, list rendering, navigation, player, and edge case states
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- All 8 frontend tests pass
- ScreeningPage displays video list with correct badges and hover interactions
- Infinite scroll loads additional pages and stops when no more pages exist
- VideoDetailPage renders video player for COMPLETED videos, processing message, or no-version message as appropriate
- Navigation between list and detail works correctly
- Route `/screening/:videoId` is registered and renders `VideoDetailPage`
- All components use inline styles with theme tokens from `theme.ts`
- `data-testid` attributes present on both page root elements

---

### Test Review & Integration

#### Task Group 5: Test Review, Gap Analysis, and Full Feature Verification
**Dependencies:** Task Groups 1-4

- [x] 5.0 Review existing tests and fill critical gaps only
  - [x] 5.1 Review tests from Task Groups 2-4
    - Review the 4-6 repository tests from Task Group 2 (sub-task 2.1)
    - Review the 8 integration tests from Task Group 3 (sub-task 3.1)
    - Review the 8 frontend tests from Task Group 4 (sub-task 4.1)
    - Total existing tests: approximately 20-22 tests
  - [x] 5.2 Analyze test coverage gaps for this feature only
    - Identify critical user workflows that lack test coverage
    - Focus ONLY on gaps related to Screening Room feature requirements
    - Prioritize end-to-end workflows over unit test gaps
    - Key areas to evaluate:
      - Infinite scroll pagination (fetching page 1+ after page 0)
      - Video with zero versions in the list response (currentVersionNumber is null)
      - Stubbed playback URL format correctness
      - Sort order verification in the paginated list
      - Authorization edge cases (producer access specifically)
  - [x] 5.3 Write up to 10 additional strategic tests maximum to fill gaps
    - Focus on integration points and cross-layer workflows
    - Possible gap-filling tests (choose based on analysis):
      - Backend: Verify sort order of videos in paginated response matches `createdAt` desc, `title` asc
      - Backend: Verify video with zero versions returns `null` for `currentVersionNumber` and `false` for `isApproved` in list
      - Backend: Verify stubbed playback URL contains the storage object ID
      - Backend: Verify `GET /api/videos?projectId={uuid}&page=1` returns empty content when all videos fit on page 0
      - Frontend: Verify infinite scroll sentinel triggers next page fetch
      - Frontend: Verify "No project selected" message when `projectId` query param is missing
      - Frontend: Verify back navigation from VideoDetailPage to ScreeningPage
      - Frontend: Verify video card displays dash when `currentVersionNumber` is null
    - Do NOT write comprehensive coverage for all scenarios
    - Skip edge cases, performance tests, and accessibility tests unless business-critical
  - [x] 5.4 Run all feature-specific tests
    - Run ONLY tests related to the Screening Room feature (tests from 2.1, 3.1, 4.1, and 5.3)
    - Expected total: approximately 22-32 tests maximum
    - Do NOT run the entire application test suite
    - Verify all critical workflows pass
  - [x] 5.5 Perform manual smoke check of seed data and endpoint responses
    - Verify seed data is present by querying the database or hitting the list endpoint
    - Confirm the 4 seeded videos appear in the correct sort order
    - Confirm the detail endpoint returns the stubbed playback URL for the COMPLETED video
    - Confirm the detail endpoint returns null playback URL for the PROCESSING video

**Acceptance Criteria:**
- All feature-specific tests pass (approximately 22-32 tests total)
- Critical user workflows for the Screening Room feature are covered
- No more than 10 additional tests added when filling in testing gaps
- Testing focused exclusively on this spec's feature requirements
- Seed data produces expected results through the full stack

---

## Execution Order

Recommended implementation sequence:

1. **Seed Data Layer (Task Group 1)** -- Must come first because backend integration tests depend on seeded data existing in the database. The FK chain (Project -> Brief -> BriefItem -> BriefItemDeliverable -> Video -> VideoVersion -> StorageObject) must be in place before any queries can be tested.

2. **Backend Repository Layer (Task Group 2)** -- Repository query methods and DTOs are the foundation for the service layer. This is also where the first-ever `Pageable`/`Page<T>` usage is introduced to the codebase.

3. **Backend Service & Controller Layer (Task Group 3)** -- Depends on repositories and DTOs from Task Group 2. Implements authorization, business logic (version resolution, batch-fetching, stubbed URLs), and exposes the two REST endpoints that the frontend will consume.

4. **Frontend Layer (Task Group 4)** -- Depends on Task Group 3 so that the API contract is finalized. Implements ScreeningPage rewrite with infinite scroll, VideoDetailPage with HTML5 player, and route registration.

5. **Test Review & Integration (Task Group 5)** -- Runs after all implementation is complete. Reviews all tests from prior groups, identifies critical gaps, adds up to 10 additional tests, and performs a final verification pass.

## Key Technical Notes

- **First paginated endpoint**: Task Group 2 introduces `Pageable`/`Page<T>` for the first time. No additional Spring configuration is needed since `spring-boot-starter-data-jpa` auto-registers `PageableHandlerMethodArgumentResolver`.
- **N+1 prevention**: The `findByVideoIdIn` batch-fetch query in Task Group 2 is critical for list performance. The service layer in Task Group 3 must use it rather than querying versions per-video.
- **Stubbed S3 URLs**: The playback URL is a hardcoded string pattern (`https://storage.example.com/stub-presigned/<storageObjectId>?token=stub`), not a real AWS integration. This will be replaced in a future story.
- **Authorization pattern reuse**: `VideoService` replicates the authorization pattern from `BriefService`/`BrandAssetService` exactly, but resolves the target org from `Project.organization` rather than from a `Brief`.
- **Infinite scroll is new to the codebase**: No existing pattern to follow. The `IntersectionObserver` approach with a sentinel div and `rootMargin` pre-fetch is specified in the spec.
- **No visual mockups provided**: Follow the existing design language from `DashboardTile`, `VaultPage`, and `LoginPage` (dark background, cream accents, Playfair Display headings, Space Mono body, orange interactive accents, 2px border radius).
