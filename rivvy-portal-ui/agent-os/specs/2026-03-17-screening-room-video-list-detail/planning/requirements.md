# Spec Requirements: Screening Room Video List & Detail

## Initial Description

On Screening Room, users view all accessible videos for a selected project and open a video to see details. The list at /screening is infinite-scrolling and sorted by Video.createdAt (desc). Each list item shows title, current version number, and an Approved (completed) badge if any VideoVersion.isApproved. Selecting a video deep-links to /screening/:videoId, opening a detail view with the latest version selected by default and playable if its transcodeStatus is ready. If a video has no versions, show sensible defaults (no player, clear message, and a placeholder for version number). Version history/switching is out of scope and handled by a separate story.

## Requirements Discussion

### First Round Questions

**Q1:** For the API design, I'm planning two endpoints: (a) `GET /api/videos?projectId={uuid}&page=0&size=25&sort=createdAt,desc` returning a paginated list with video title, current version number, and isApproved flag pre-computed by the backend, and (b) `GET /api/videos/{videoId}` returning the full detail including the latest version's playback URL (pre-signed S3 URL). Does that match your expectations, or would you prefer a different structure?
**Answer:** Matches expectations.

**Q2:** For video playback, I'm assuming a native HTML5 `<video>` element is sufficient for this story — no need for a third-party player library. The backend generates a short-lived pre-signed S3 URL for the StorageObject, and the frontend sets it as the `<video src>`. Is that correct?
**Answer:** Correct.

**Q3:** For the pre-signed S3 URL generation: since the project doesn't currently have AWS SDK integration, should we add the AWS S3 SDK and implement real pre-signed URL generation, or stub/mock this for now?
**Answer:** Stub/mock this for now. (Wire up real S3 in a later story.)

**Q4:** For the `/screening/:videoId` route: is this a completely separate page/component (VideoDetailPage) or a split-view/overlay on the list?
**Answer:** The screening screen will allow the client to view multiple videos (if there were multiple videos requested in the project brief). The deep link allows the user to be shown which of the many videos is to be displayed. (The `/screening/:videoId` deep link selects a specific video for display from the project's video list.)

**Q5:** For authorization, I'm assuming the same pattern as BriefService/BrandAssetService — resolve the user's org from their membership, verify the project belongs to that org, then return only videos for that project. Producers assigned to the org can also access. Is that correct?
**Answer:** Correct.

**Q6:** For seed data: should I create seed data with a few sample videos with versions in various states (ready, transcoding, approved) for testing and demo purposes?
**Answer:** Yes please.

### Existing Code to Reference

**Similar Features Identified:**
- Service: `BrandAssetService` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/service/BrandAssetService.java` (authorization pattern, org resolution)
- Controller: `BrandAssetController` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BrandAssetController.java` (REST endpoint pattern)
- Controller: `BriefController` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BriefController.java` (thin controller pattern)
- Tests: `BriefControllerTests` — Path: `crud-logic-service/src/test/java/com/rivvystudios/portal/brief/BriefControllerTests.java` (integration test pattern)
- Tests: `BrandAssetControllerTests` — Path: `crud-logic-service/src/test/java/com/rivvystudios/portal/brandasset/BrandAssetControllerTests.java` (integration test pattern)
- Page: `VaultPage` — Path: `rivvy-portal-ui/src/pages/VaultPage.tsx` (fetch + grid layout with loading/error states)
- Component: `DashboardTile` — Path: `rivvy-portal-ui/src/components/DashboardTile.tsx` (clickable card navigation)
- Page: `ScreeningPage` — Path: `rivvy-portal-ui/src/pages/ScreeningPage.tsx` (existing stub placeholder to replace)
- Styles: `LoginPage` — Path: `rivvy-portal-ui/src/pages/LoginPage.tsx` (inline styles pattern, `Record<string, React.CSSProperties>`)
- Theme: `theme.ts` — Path: `rivvy-portal-ui/src/theme.ts` (design tokens: colors, fonts)
- Layout: `RootLayout` — Path: `rivvy-portal-ui/src/layouts/RootLayout.tsx` (authenticated layout wrapper)
- Routing: `App.tsx` — Path: `rivvy-portal-ui/src/App.tsx` (route definitions)
- Seed data: `R__seed_data.sql` — Path: `crud-logic-service/src/main/resources/db/changelog/seed/R__seed_data.sql` (existing seed data for orgs, users, projects)
- Entity: `Video` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Video.java` (existing JPA entity, if present)
- Entity: `VideoVersion` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/VideoVersion.java` (existing JPA entity, if present)
- Entity: `StorageObject` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/StorageObject.java` (existing JPA entity, if present)
- Entity: `Project` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Project.java` (existing JPA entity, if present)

No infinite scroll or paginated list patterns exist in the codebase yet — this will be the first paginated endpoint.

### Follow-up Questions
None required.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A

## Requirements Summary

### Functional Requirements
- Replace ScreeningPage stub with a video list view at `/screening?projectId={uuid}`
- Add new route `/screening/:videoId` for video detail view
- Video list: paginated (page size 25), sorted by `Video.createdAt` descending (ties by `title` ascending), with infinite scroll (auto-load at ~80% scroll depth)
- Each list item shows: video title, current version number (from `isCurrent=true` VideoVersion, or highest `versionNumber`, or "—" if no versions), and an "Approved" badge if any `VideoVersion.isApproved=true`
- Clicking a list item navigates to `/screening/:videoId`
- Video detail view: shows video title, "Approved" badge if applicable, and the latest version's video player
- Latest version = `isCurrent=true` if present, else highest `versionNumber`; player renders only if `transcodeStatus=ready`
- If video has no versions: no player, "No version available" message, no version number displayed
- Empty state when project has zero videos
- Deep links to `/screening/:videoId` work directly without prior list navigation
- Loading and non-blocking error states on both list and detail views
- Backend: `GET /api/videos?projectId={uuid}&page=0&size=25` — paginated video list with pre-computed current version number and isApproved flag
- Backend: `GET /api/videos/{videoId}` — full video detail with latest version info and playback URL
- Playback URL: stubbed/mocked pre-signed S3 URL for now (not real AWS integration)
- Native HTML5 `<video>` element for playback — no third-party player library
- Server-side authorization: org members and assigned producers can access; others get 403
- Seed data: sample videos with versions in various states (ready, transcoding, approved, no versions)

### Reusability Opportunities
- `BrandAssetService` / `BriefService` authorization pattern (org resolution, producer assignment check)
- `BrandAssetController` / `BriefController` thin controller pattern
- `VaultPage` fetch + loading/error state pattern
- `DashboardTile` clickable card component for list items (or new `VideoListItem` component)
- `theme.ts` design tokens for consistent styling
- Integration test patterns from `BriefControllerTests` and `BrandAssetControllerTests`

### Scope Boundaries
**In Scope:**
- Backend: VideoService, VideoController, paginated list endpoint, detail endpoint, stubbed playback URL, repository queries, seed data
- Frontend: ScreeningPage rewrite with video list + infinite scroll, VideoDetailPage with player, new route in App.tsx
- Authorization (same pattern as existing services)
- Loading/error/empty states
- Accessibility (aria-labels, semantic HTML)

**Out of Scope:**
- Version history UI and switching between versions (planned: version switching story)
- Commenting UI and moderation
- Uploading/managing versions and transcode workflows
- Approval workflow actions (beyond displaying badge)
- Project selection/switching controls
- Advanced list filters, search, or alternative sort modes
- Real AWS S3 pre-signed URL integration (stubbed for now)
- Third-party video player library

### Technical Considerations
- Full-stack feature: Spring Boot backend + React frontend
- Backend: Spring Boot 3.4.1, Java 21, Spring Security (session-based auth), Spring Data JPA
- Frontend: React 19.x, TypeScript 5.x, Vite 5.x, react-router-dom v7
- First paginated endpoint in the codebase — use Spring Data's `Pageable` / `Page<T>` for backend pagination
- Infinite scroll on frontend: Intersection Observer API or scroll event listener
- Pre-signed S3 URL stubbed — return a placeholder/mock URL from the backend; real S3 integration deferred
- Native HTML5 `<video>` element for playback
- New route `/screening/:videoId` needs to be added to `App.tsx`
- Follow inline styles pattern (`Record<string, React.CSSProperties>`) with theme tokens
- Vitest + React Testing Library for frontend tests; JUnit 5 + Testcontainers for backend tests
- Seed data needs: Project, Video, VideoVersion, StorageObject, BriefItemDeliverable rows (VideoVersion references all of these)
- Database tables involved: `video`, `video_version`, `storage_object`, `project`, `organization`, `organization_member`, `producer_assignment`
