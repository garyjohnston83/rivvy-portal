# Spec Initialization: Brand Vault Asset Grid

## Feature Description
In the Brand Vault (/vault), when a category is selected elsewhere (e.g., category tiles), render a read-only grid of assets for that category. Each item shows the asset name and basic metadata (description, visibility, createdAt, tags) plus a Download action that retrieves the latest BrandAssetVersion via the Portal API, which returns a time-limited pre-signed S3 URL. Archived assets are hidden by default. No category tiles, previews, uploads, edits, search, or scope/project filter UI are part of this story.

## In Scope
- Grid list of BrandAsset items filtered by selected category (logos, fonts, guidelines, visuals)
- Display per-item: name, description, visibility, createdAt, tags
- Per-item Download action limited to the latest BrandAssetVersion
- Honor authorization and asset visibility (org/project) based on provided context (no filter UI here)
- Default sorting by name (ascending) and pagination (page size 24) with API support and UI consumption
- Empty-state messaging when no assets match

## Out of Scope
- Category tiles UI (icons, counts, navigation)
- Scope/project filter UI and its behavior (separate story handles this)
- Create/edit/delete assets, tagging changes, archiving operations
- Previews/thumbnails/inline viewers
- Version history browsing or downloading older versions
- Bulk/multi-select download
- Search, advanced filtering, or custom sorting controls

## Sibling Items (handled separately — do NOT implement these)
- [IN_PROGRESS] View category tiles with icons and counts
- [PLANNED] Filter by scope and project: Provide filters to view org-scoped assets or project-scoped assets; when project-scoped, allow choosing a project.

Acceptance Criteria:
- A scope filter allows selecting: All, Org-only, or Project.
- When Project is selected, a project selector is required and limits the list to that project's assets.
- Clearing filters returns the list to the full set for the selected category.

## Assumptions
- User-facing categories: logos, fonts, guidelines, visuals.
- Category-to-assetType mapping: logos -> logo, fonts -> font, guidelines -> guideline, visuals -> {image, footage}.
- Only BrandAsset.status = 'active' are listed; archived are hidden by default.
- Download action is strictly limited to the latest BrandAssetVersion.
- Clients and Producers can view authorized assets. Access is gated by org membership; project-visible assets require valid project context and authorization.
- Without a project context, only visibility='org' assets are included. With a valid project context, include visibility='org' and visibility='project' assets where project_id matches.
- Default sorting is by BrandAsset.name ascending; default page size is 24; no user-facing controls to change these in this story.
- Portal API (ifc-mm2fbrdg-s9zlg) will provide endpoints to list assets and to generate a latest-version download URL; files are stored in AWS S3 (svc-mlxwlddw-tstrs) via StorageObject.

## Acceptance Criteria
- Given a selected category (logos, fonts, guidelines, visuals), the grid displays only assets whose BrandAsset.assetType maps to that category and that the user is authorized to view.
- Each grid item shows: name, description (if present), visibility, createdAt, and tags.
- Clicking Download initiates retrieval of a pre-signed URL for the latest BrandAssetVersion via the Portal API and results in a usable file download.
- If an asset has no versions, its Download action is disabled.
- Archived assets (status='archived') do not appear.
- Without project context, only org-visible assets are listed; with a valid project context, eligible project-visible assets for that project are included alongside org-visible assets, subject to authorization.
- The list is sorted by name A–Z and paginated with a default page size of 24; pagination works correctly.
- If no assets match, an empty-state message is displayed.
- API errors (e.g., 403/404) surface a user-friendly message without breaking the page.

## Date Created
2026-03-23
