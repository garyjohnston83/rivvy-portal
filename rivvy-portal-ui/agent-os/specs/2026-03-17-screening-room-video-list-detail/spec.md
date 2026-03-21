# Specification: Screening Room Video List & Detail

## Goal

Enable users to browse all videos for a selected project in an infinite-scrolling list at `/screening`, select a video to deep-link to `/screening/:videoId`, and play the latest version of that video using a native HTML5 player -- delivering the first paginated endpoint in the codebase along with the corresponding frontend views.

## User Stories

- As a client, I want to see all videos for my project in a scrollable list so that I can quickly find and select a video to review.
- As a client, I want to open a specific video by its deep link and watch the latest version so that I can review the deliverable my producer shared with me.
- As a producer assigned to a client org, I want to access the same screening room views so that I can verify deliverables alongside my client.

## Specific Requirements

**VideoRepository paginated query**
- Add a method `Page<Video> findByProjectId(UUID projectId, Pageable pageable)` to the existing `VideoRepository` interface; Spring Data JPA derives the query automatically
- This is the first use of `Pageable`/`Page<T>` in the codebase; no additional configuration is needed because `spring-boot-starter-data-jpa` already includes Spring Data Web support and auto-registers `PageableHandlerMethodArgumentResolver`
- Default sort: `createdAt` descending, secondary sort `title` ascending (applied in the service layer via `PageRequest.of(page, size, Sort.by(...))` so the controller does not expose arbitrary sort params)
- Page size fixed at 25

**VideoVersionRepository lookup queries**
- Add `Optional<VideoVersion> findByVideoIdAndIsCurrentTrue(UUID videoId)` -- returns the current version for a video
- Add `Optional<VideoVersion> findFirstByVideoIdOrderByVersionNumberDesc(UUID videoId)` -- fallback to highest version number when no `isCurrent=true` exists
- Add `boolean existsByVideoIdAndIsApprovedTrue(UUID videoId)` -- checks if any version of a video is approved, used to compute the approved badge on the list endpoint
- Add `List<VideoVersion> findByVideoIdIn(List<UUID> videoIds)` -- batch-fetch versions for all videos on a page to avoid N+1 queries in the list endpoint

**VideoService**
- Constructor-inject `VideoRepository`, `VideoVersionRepository`, `UserAccountRepository`, `OrganizationMemberRepository`, `ProducerAssignmentRepository`, `ProjectRepository`
- Authorization logic: resolve user by email, get memberships, load project by `projectId`, verify the project's org matches one of the user's memberships OR that the user is an assigned producer for that org (same pattern as `BriefService.getBriefById`); throw `ResponseStatusException(FORBIDDEN)` on failure, `ResponseStatusException(NOT_FOUND)` for missing project
- `getVideosByProject(String email, UUID projectId, int page)` -- authorize, then query `VideoRepository.findByProjectId` with `PageRequest.of(page, 25, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("title")))`; batch-fetch all `VideoVersion` rows for the page's video IDs; for each video compute `currentVersionNumber` (from `isCurrent=true` version, else highest `versionNumber`, else `null`) and `isApproved` (any version with `isApproved=true`); return a `Page<VideoListItemResponse>` (use `Page.map(...)`)
- `getVideoDetail(String email, UUID videoId)` -- load `Video` by ID (404 if missing), authorize via the video's project org, find the latest version (`isCurrent=true` else highest `versionNumber`), generate a stubbed playback URL if `transcodeStatus == COMPLETED`, return a `VideoDetailResponse`
- Stubbed playback URL: if the latest version's `transcodeStatus` is `COMPLETED`, return a hardcoded string like `https://storage.example.com/stub-presigned/<storageObjectId>?token=stub`; otherwise return `null`; this will be replaced with real S3 pre-signed URL generation in a future story

**VideoController**
- `@RestController` at `@RequestMapping("/api/videos")`; constructor-inject `VideoService`
- `GET /api/videos?projectId={uuid}&page=0` -- extract email from `SecurityContextHolder`, delegate to `videoService.getVideosByProject`, return `ResponseEntity.ok(page)` where `page` is a `Page<VideoListItemResponse>`; the `projectId` query param is `@RequestParam UUID projectId` (required); `page` is `@RequestParam(defaultValue = "0") int page`
- `GET /api/videos/{videoId}` -- extract email from `SecurityContextHolder`, delegate to `videoService.getVideoDetail`, return `ResponseEntity.ok(response)`
- Follow the thin controller pattern from `BriefController` -- no business logic in the controller

**DTO classes**
- `VideoListItemResponse`: fields `id` (UUID), `title` (String), `currentVersionNumber` (Integer, nullable), `isApproved` (boolean); plain Java class with getters/setters following the `BriefResponse` pattern
- `VideoDetailResponse`: fields `id` (UUID), `title` (String), `description` (String, nullable), `currentVersionNumber` (Integer, nullable), `isApproved` (boolean), `transcodeStatus` (String, nullable -- the enum name of the latest version's transcode status, or null if no versions), `playbackUrl` (String, nullable), `createdAt` (String -- ISO-8601)
- Both DTOs in `com.rivvystudios.portal.controller.dto` package

**Seed data for videos**
- Add to the existing `R__seed_data.sql` file; all new rows use `ON CONFLICT (id) DO NOTHING`
- Requires a Project row for Acme Corp: insert a project with a known UUID (e.g., `70000000-...001`) linked to Acme Corp org (`20..002`), with a brief, brief_item, and brief_item_deliverable to satisfy the foreign keys on the `video` table
- Create a Brief (`80000000-...001`) for Acme Corp, a BriefItem (`81000000-...001`), and a BriefItemDeliverable (`82000000-...001`) as scaffolding for the video rows
- Insert 4 Video rows under that project: (1) "Brand Launch Teaser" with a version that is `isCurrent=true`, `transcodeStatus=COMPLETED`, `isApproved=true`; (2) "Product Demo" with a version that is `isCurrent=true`, `transcodeStatus=COMPLETED`, `isApproved=false`; (3) "Behind the Scenes" with a version that has `transcodeStatus=PROCESSING`, `isApproved=false`; (4) "Social Cutdown" with zero versions
- For each VideoVersion, insert a corresponding StorageObject row (provider=`s3`, bucket=`rivvy-portal-dev`, object_key like `videos/<uuid>.mp4`)
- Use `created_at` timestamps that produce a clear descending sort order (e.g., stagger by 1 day each)

**ScreeningPage rewrite (video list)**
- Replace the existing stub `ScreeningPage.tsx` with a full implementation
- Read `projectId` from the URL query string (`useSearchParams` from react-router-dom); if missing, display a message "No project selected"
- On mount (and when `projectId` changes), fetch `GET /api/videos?projectId={projectId}&page=0` with `credentials: 'include'`; store the response as a list of video items, plus pagination metadata (`totalPages`, `number` i.e. current page, `last` boolean)
- Render a vertical list of clickable video cards; each card shows: video title, "V{currentVersionNumber}" badge (or dash if null), and a green "Approved" badge if `isApproved` is true
- Clicking a card navigates to `/screening/{videoId}` via `useNavigate`
- Infinite scroll: use an `IntersectionObserver` on a sentinel `<div>` placed after the last card; when the sentinel enters the viewport (~80% threshold not needed -- just use `rootMargin: '0px 0px 200px 0px'` to trigger slightly before reaching the end), fetch the next page and append results; stop observing when `last` is true
- Loading state: show "Loading..." text (same style as `VaultPage`) on initial load; show a smaller "Loading more..." at the bottom of the list during subsequent page fetches
- Error state: show a non-blocking error banner (same style as `VaultPage` error) above the list; do not clear existing items on error
- Empty state: if the first page returns zero items, show "No videos found for this project"
- Keep `data-testid="page-screening"` on the root element
- Use inline styles with `Record<string, React.CSSProperties>` pattern; use `colors` and `fonts` from `theme.ts`
- Video cards: dark background (`colors.black` or slightly lighter), `1px solid ${colors.border}`, `borderRadius: '2px'`, hover effect similar to `DashboardTile` (border color changes to `colors.orange`)

**VideoDetailPage (new component)**
- New file `src/pages/VideoDetailPage.tsx`
- Read `videoId` from the URL path param (`useParams` from react-router-dom)
- On mount, fetch `GET /api/videos/{videoId}` with `credentials: 'include'`
- Display: video title as an `<h1>`, "Approved" badge if applicable, "V{currentVersionNumber}" if present
- Video player: if `playbackUrl` is non-null and `transcodeStatus` is `COMPLETED`, render a native `<video>` element with `controls`, `src={playbackUrl}`, `width="100%"`, `style={{ maxWidth: '800px' }}`; include `preload="metadata"`
- If `transcodeStatus` is not `COMPLETED` but a version exists, show "Video is processing..." message
- If no version exists (`currentVersionNumber` is null), show "No version available" and no player
- Include a back link/button to navigate to `/screening` (preserving the `projectId` query param is not required for this story but would be a nice-to-have)
- Loading and error states following the same pattern as ScreeningPage
- `data-testid="page-video-detail"` on the root element
- Inline styles with theme tokens

**Route registration in App.tsx**
- Add `import VideoDetailPage from './pages/VideoDetailPage'`
- Add `<Route path="/screening/:videoId" element={<VideoDetailPage />} />` inside the `<Route element={<RootLayout />}>` block, after the existing `/screening` route
- The existing `/screening` route remains unchanged (still renders the rewritten `ScreeningPage`)

**Backend integration tests**
- New file `VideoControllerTests.java` in package `com.rivvystudios.portal.video` (test source), following the exact same structure as `BriefControllerTests`
- Use `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Import(TestcontainersConfiguration.class)`; inject `MockMvc` and `JdbcTemplate`
- Reuse the `loginAs` helper pattern (POST to `/api/auth/login` with email/password, return cookies)
- Tests to include: (1) `GET /api/videos?projectId={acmeProjectId}&page=0` as `client2@acme.local` returns 200 with a JSON array in `$.content` containing the seeded videos; (2) same endpoint as `producer@rivvy.local` (assigned to Acme) returns 200; (3) same endpoint unauthenticated returns 401; (4) same endpoint as `client@rivvy.local` (member of Rivvy Studios, NOT Acme) returns 403; (5) `GET /api/videos/{videoId}` for a seeded video as `client2@acme.local` returns 200 with `title`, `playbackUrl`, and `transcodeStatus` fields; (6) `GET /api/videos/{nonExistentUuid}` returns 404; (7) verify the list response `isApproved` flag is `true` for the seeded approved video and `false` for unapproved ones; (8) verify pagination fields `totalElements`, `totalPages`, `number`, `last` are present in the list response

**Frontend tests**
- New file `src/__tests__/screening-room.test.tsx` using Vitest + React Testing Library
- Use `MemoryRouter` with initial entry at `/screening?projectId={uuid}` and mock `fetch` via `vi.fn()`
- Tests to include: (1) renders loading state initially; (2) renders video list items after successful fetch; (3) renders "No videos found" empty state; (4) renders error message on fetch failure; (5) clicking a video card navigates to `/screening/:videoId`; (6) VideoDetailPage renders video title and player when `playbackUrl` is present; (7) VideoDetailPage renders "No version available" when `currentVersionNumber` is null; (8) VideoDetailPage renders "Video is processing..." when `transcodeStatus` is not `COMPLETED`

## Visual Design

No visual mockups were provided. Follow the existing design language established by `DashboardTile`, `VaultPage`, and `LoginPage`: dark background with cream/light accent cards, `Playfair Display` headings, `Space Mono` body text, orange interactive accents, minimal borders at 2px radius.

## Existing Code to Leverage

**BriefService / BrandAssetService authorization pattern**
- Both services resolve the user by email via `UserAccountRepository.findByEmail`, get memberships via `OrganizationMemberRepository.findByUserAccount`, then check org membership or producer assignment via `ProducerAssignmentRepository.existsByProducerMemberAndClientOrg`
- `VideoService` should replicate this exact pattern, but resolve the target org from the `Project.organization` rather than from a `Brief`
- The `ResponseStatusException` error handling pattern (NOT_FOUND, FORBIDDEN, BAD_REQUEST) should be replicated exactly

**BriefController thin controller pattern**
- Controllers extract `email` from `SecurityContextHolder.getContext().getAuthentication().getName()` and pass it to the service
- Controllers return `ResponseEntity.ok(...)` or `ResponseEntity.status(201).body(...)` -- no business logic
- `VideoController` should follow this pattern identically

**VaultPage fetch + loading/error state pattern**
- Uses `useState` for `loading`, `error`, and data state; `useEffect` with a `cancelled` flag for cleanup
- Loading renders a simple text message; error renders a `role="alert"` div with `colors.error` styling
- `ScreeningPage` should follow this pattern, extended with pagination state (`page`, `hasMore`, `items` array that appends on subsequent fetches)

**DashboardTile hover interaction pattern**
- Uses `useState` for `hovered` and `focused` states; applies `borderColor: colors.orange` and a box-shadow on hover/focus
- Video list cards should use this same hover pattern for visual consistency

**BriefControllerTests / BrandAssetControllerTests integration test pattern**
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Import(TestcontainersConfiguration.class)` with `MockMvc`
- `loginAs(email)` helper performs a real login via `/api/auth/login` and returns session cookies
- Tests assert HTTP status codes and JSON paths using `jsonPath(...).value(...)` and `jsonPath(...).exists()`
- The seeded data (orgs, users, roles, producer assignments) is already present and should be relied on for authorization tests

## Out of Scope

- Version history UI and switching between versions of a video
- Commenting, annotations, or moderation on videos
- Uploading new versions or triggering transcode workflows
- Approval workflow actions (approving/rejecting) -- only the display badge is in scope
- Project selection or switching controls (the `projectId` query param is assumed to be provided externally)
- Search, filtering, or alternative sort modes on the video list
- Real AWS S3 pre-signed URL generation (stubbed/mocked only)
- Third-party video player libraries (use native HTML5 `<video>` only)
- Responsive/mobile-specific layout optimizations beyond basic readability
- Thumbnail generation or preview images for videos in the list
