# Spec Initialization: New Brief Draft

## Raw Idea

On New Brief (/new-brief), when a Client lands on the page a Brief is auto-created with status=draft, scoped to the Client's single Organization. The draft supports editing project-level fields: title (optional until submission), description, priority (default=normal), desiredDueDate, budget, creativeDirection, metadata. submittedBy is set to the creator's user ID at draft creation. Cancel always deletes the auto-created draft so no draft remains. Visibility: Clients in the org can see the draft; Producers assigned to that org (via ProducerAssignment) have view-only access.

## In Scope
- Autosave-on-entry creation of a draft Brief on /new-brief (status=draft, org-scoped, submittedBy=creator, priority=normal)
- Editable project-level fields: title (optional in draft), description, priority, desiredDueDate, budget, creativeDirection, metadata
- Continuous autosave of field edits after initial create
- Visibility: Clients in the owning Organization can view the draft; assigned Producers have view-only access
- Cancel action that deletes the auto-created draft

## Out of Scope
- Submitting a Brief (status changes beyond draft)
- BriefItems and Deliverables creation/editing
- Notifications/emails
- Admin visibility/controls
- Producer editing of drafts
- UI for managing references content (backend defaulting only)
- Additional currency/validation rules for budget

## Assumptions
- Each user belongs to exactly one Organization; no org selector is shown.
- Title is optional in draft; backend stores a placeholder value at create time (e.g., "Untitled Brief") due to non-null title column.
- Priority defaults to normal unless changed by the Client.
- Autosave persists edits for all listed project-level fields.
- Brief.metadata and Brief.references initialize to empty objects if not provided.
- Budget accepts any numeric value without additional validation.
- Producers have view-only access to drafts for their assigned client orgs in this story.

## Acceptance Criteria
- When a Client navigates to /new-brief, a Brief is immediately persisted with status=draft, orgId set to the user's Organization, submittedBy set to the current user, priority=normal, and non-null defaults for required fields (e.g., title placeholder, metadata={}, references={}).
- The draft allows editing title, description, priority, desiredDueDate, budget, creativeDirection, and metadata; changes are autosaved.
- Title may remain empty in the UI while the Brief is in draft; it is only required at submission (submission not in scope).
- Cancel invoked from /new-brief deletes the auto-created draft so that no draft from that session exists afterward.
- Only Clients who are members of the owning Organization can retrieve and view the draft.
- Rivvy Producers assigned to the owning Organization via ProducerAssignment can view the draft but cannot edit it.
- No BriefItems or Deliverables are created or modified as part of this story.
- No emails or notifications are sent as part of this story.

## Date Initialized
2026-03-17
