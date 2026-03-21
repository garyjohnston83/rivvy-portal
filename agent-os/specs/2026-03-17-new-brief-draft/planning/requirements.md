# Spec Requirements: New Brief Draft

## Initial Description

On New Brief (/new-brief), when a Client lands on the page a Brief is auto-created with status=draft, scoped to the Client's single Organization. The draft supports editing project-level fields: title (optional until submission), description, priority (default=normal), desiredDueDate, budget, creativeDirection, metadata. submittedBy is set to the creator's user ID at draft creation. Cancel always deletes the auto-created draft so no draft remains. Visibility: Clients in the org can see the draft; Producers assigned to that org (via ProducerAssignment) have view-only access.

## Requirements Discussion

### First Round Questions

**Q1:** The `brief` table already exists with all required columns from the JPA persistence layer spec. I'm assuming the `Brief` JPA entity and `BriefRepository` already exist and we should build on them rather than creating new entities. Is that correct?
**Answer:** Yes, build on top of them.

**Q2:** For autosave timing: I'm assuming a debounced approach — after the user stops typing/editing a field for ~1-2 seconds, the frontend sends a PATCH/PUT to persist the changes. Is that the right pattern, or do you prefer save-on-blur (when the user leaves a field)?
**Answer:** After the user stops typing/editing a field for ~1-2 seconds.

**Q3:** For the priority field: the `brief.priority` column is TEXT. I'm assuming we should use a fixed set of values like `low`, `normal`, `high`, `urgent` and present them as a dropdown/select in the UI. What priority values should be available?
**Answer:** normal, high, urgent.

**Q4:** For the metadata field: this is a JSONB column. Since there's no UI for managing references (out of scope), I assume metadata is also just initialized to `{}` and not editable in the UI for now — or should we provide a simple key-value editor for metadata?
**Answer:** Initialized to `{}` — not editable in the UI for now.

**Q5:** For the API design: I'm planning 4 REST endpoints under the Portal API — `POST /api/briefs` (create draft), `PUT /api/briefs/{id}` (update draft fields), `DELETE /api/briefs/{id}` (cancel/delete draft), and `GET /api/briefs/{id}` (retrieve draft). Does that match your expectations, or should the URL structure differ?
**Answer:** Yes.

**Q6:** When the user navigates away from /new-brief without clicking Cancel (e.g., clicks "Rivvy Portal" header link or browser back), should the draft persist in the database (orphaned draft) or should we attempt cleanup? I'm assuming orphaned drafts are acceptable for now and cleanup is a future concern.
**Answer:** The draft should persist in the database (orphaned draft).

**Q7:** For resolving the user's organization: I'm assuming we derive orgId from the authenticated user's membership via `organization_member` (since each user belongs to exactly one org). The backend resolves this from the session — the frontend does not send an orgId. Correct?
**Answer:** Correct.

### Existing Code to Reference

**Similar Features Identified:**
- Entity: `Brief` JPA entity — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Brief.java`
- Repository: `BriefRepository` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BriefRepository.java`
- Auth Controller pattern: `AuthController` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/AuthController.java`
- Auth DTOs pattern: `LoginRequest`, `LoginResponse` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/`
- Security Config: `SecurityConfig` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/config/SecurityConfig.java`
- Frontend login form: `LoginPage.tsx` — Path: `rivvy-portal-ui/src/pages/LoginPage.tsx` (form pattern, inline styles, API call pattern)
- Frontend page placeholder: `NewBriefPage.tsx` — Path: `rivvy-portal-ui/src/pages/NewBriefPage.tsx` (page to be replaced)
- UserAccount entity: `UserAccount.java` — Path: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/UserAccount.java`
- OrganizationMember entity: for resolving user's org

### Follow-up Questions
None required.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A

## Requirements Summary

### Functional Requirements
- Auto-create a Brief with status=draft when a Client navigates to /new-brief
- Draft is scoped to the Client's single Organization (resolved from session via organization_member)
- submittedBy is set to the authenticated user's ID at draft creation
- Priority defaults to `normal`; available values are `normal`, `high`, `urgent`
- Title defaults to a placeholder (e.g., "Untitled Brief") due to non-null column; UI shows it as empty/editable
- `metadata` and `references` JSONB fields initialize to `{}`
- Editable fields in the UI: title, description, priority (dropdown), desiredDueDate (date picker), budget (numeric input), creativeDirection (text area)
- `metadata` is NOT editable in the UI — initialized to `{}` only
- `references` is NOT editable in the UI — initialized to `{}` only
- Autosave: debounced at ~1-2 seconds after the user stops editing a field, sends PUT to persist changes
- Cancel button deletes the auto-created draft via DELETE endpoint and navigates back to /dashboard
- Navigating away without Cancel leaves the draft persisted (orphaned draft is acceptable)
- Visibility: Clients in the owning Organization can view the draft
- Visibility: Producers assigned to the owning Organization via ProducerAssignment have view-only access (cannot edit)
- 4 REST endpoints: POST /api/briefs (create), GET /api/briefs/{id} (retrieve), PUT /api/briefs/{id} (update), DELETE /api/briefs/{id} (delete)
- Backend resolves orgId from the authenticated user's organization membership — frontend does not send orgId

### Reusability Opportunities
- Existing `Brief` JPA entity and `BriefRepository` — build on top of them
- `AuthController` pattern for new `BriefController`
- DTO pattern from `LoginRequest`/`LoginResponse` for Brief request/response DTOs
- `LoginPage.tsx` form pattern for the New Brief form UI (inline styles, API call pattern)
- `SecurityConfig` for endpoint authorization rules
- `PortalUserDetailsService` for resolving authenticated user details

### Scope Boundaries
**In Scope:**
- Backend: BriefController, BriefService, Brief DTOs, endpoint authorization
- Frontend: Replace NewBriefPage placeholder with draft form, autosave logic, cancel/delete
- Auto-create draft on page entry
- Debounced autosave of field edits
- Cancel deletes draft and navigates to /dashboard
- Authorization: Clients in org can view/edit their drafts; assigned Producers can view only

**Out of Scope:**
- Submitting a Brief (status changes beyond draft)
- BriefItems and Deliverables creation/editing
- Notifications/emails
- Admin visibility/controls
- Producer editing of drafts
- UI for managing references or metadata content
- Additional currency/validation rules for budget
- Orphaned draft cleanup
- Mobile hamburger menu or collapsible navigation

### Technical Considerations
- Full-stack feature: Spring Boot backend + React frontend
- Backend: Spring Boot 3.4.1, Java 21, Spring Security (session-based auth), Spring Data JPA
- Frontend: React 19.x, TypeScript 5.x, Vite 5.x, react-router-dom v7
- Existing session-based auth (RIVVY_SESSION cookie) — endpoints must be authenticated
- Use existing `Brief` JPA entity and `BriefRepository` from JPA persistence layer spec
- Resolve user's org via `OrganizationMember` lookup from authenticated user
- Frontend uses inline `styles` object pattern (Record<string, React.CSSProperties>) — no CSS modules
- Frontend testing: Vitest + React Testing Library + MemoryRouter
- Backend testing: JUnit 5 + Spring Boot Test + Testcontainers
- Vite dev proxy: `/api/**` → `http://localhost:8080`
- Priority values enum: `normal`, `high`, `urgent`
