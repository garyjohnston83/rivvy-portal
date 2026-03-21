# Specification: New Brief Draft

## Goal
Allow a Client user to create and edit a draft Brief on the `/new-brief` page, with the draft auto-created on page entry, autosaved as the user edits, and deletable via a Cancel action -- all scoped to the Client's organization and visible to assigned Producers as read-only.

## User Stories
- As a Client, I want a draft Brief to be automatically created when I navigate to `/new-brief` so that I can immediately start filling in project details without a separate "create" step.
- As a Client, I want my edits to be autosaved after I stop typing so that I never lose work, and I want to be able to cancel and delete the draft if I change my mind.
- As a Producer assigned to a Client organization, I want to view a Client's draft Brief (read-only) so that I have early visibility into incoming work.

## Specific Requirements

**POST /api/briefs -- Create Draft Endpoint**
- Accepts no request body; all initial values are set server-side
- Resolves the authenticated user via `SecurityContextHolder.getContext().getAuthentication().getName()` (email), then looks up `UserAccount` via `UserAccountRepository.findByEmail()`
- Resolves orgId by querying `OrganizationMemberRepository.findByUserAccount()` and taking the first membership's `Organization`
- Sets `submittedBy` to the resolved `UserAccount` entity, `organization` to the resolved `Organization` entity
- Sets defaults: `status=BriefStatus.DRAFT`, `priority=BriefPriority.NORMAL`, `title="Untitled Brief"`, `metadata={}`, `references={}`, `createdAt=Instant.now()`
- Returns the created Brief as a `BriefResponse` DTO with HTTP 201

**GET /api/briefs/{id} -- Retrieve Brief Endpoint**
- Looks up Brief by UUID via `BriefRepository.findById()`; returns 404 if not found
- Authorization: the authenticated user must be either (a) a member of the Brief's `organization` (via `OrganizationMemberRepository`), or (b) a Producer assigned to that org (via `ProducerAssignmentRepository`)
- Returns `BriefResponse` DTO with HTTP 200

**PUT /api/briefs/{id} -- Update Brief Endpoint**
- Accepts a `BriefUpdateRequest` DTO body with optional fields: `title` (String), `description` (String), `priority` (String mapping to `BriefPriority` enum), `desiredDueDate` (ISO date string mapping to `LocalDate`), `budget` (BigDecimal), `creativeDirection` (String)
- Does NOT accept `metadata`, `references`, `status`, `orgId`, or `submittedBy` -- these are immutable via this endpoint
- Authorization: only Client members of the Brief's owning organization may update; Producers get 403
- Sets `updatedAt=Instant.now()` on each save; persists via `BriefRepository.save()`
- Returns updated `BriefResponse` DTO with HTTP 200

**DELETE /api/briefs/{id} -- Delete Draft Endpoint**
- Looks up Brief by UUID; returns 404 if not found
- Only allows deletion if `status == BriefStatus.DRAFT`; otherwise returns 409 Conflict
- Authorization: only Client members of the Brief's owning organization may delete
- Deletes via `BriefRepository.delete()` and returns HTTP 204 No Content

**Backend Service and Authorization Layer**
- Create a `BriefService` class in the `service` package following constructor injection pattern used by `AuthController`
- `BriefService` encapsulates all business logic: org resolution, authorization checks, default value assignment, CRUD delegation to `BriefRepository`
- For authorization, `BriefService` checks org membership via `OrganizationMemberRepository.findByUserAccount()` and producer assignment via a new `ProducerAssignmentRepository.findByProducerMember_UserAccountAndClientOrg()` query method
- `BriefController` is a thin `@RestController` annotated `@RequestMapping("/api/briefs")` that delegates to `BriefService`
- Add `/api/briefs/**` to `SecurityConfig` as requiring authentication (already covered by `.anyRequest().authenticated()`) and add CSRF exclusion for `/api/briefs/**` alongside existing `/api/auth/**`

**BriefPriority Enum Alignment**
- The existing `BriefPriority` enum has values `LOW, MEDIUM, HIGH, URGENT`; the UI dropdown should only present `NORMAL, HIGH, URGENT`
- Add a `NORMAL` value to the `BriefPriority` enum (or rename `MEDIUM` to `NORMAL`) so the default `priority=NORMAL` is valid; the enum must include at least `NORMAL`, `HIGH`, `URGENT`
- The `LOW` and `MEDIUM` values may remain in the enum for backward compatibility but are not exposed in the UI dropdown

**Brief DTOs**
- `BriefResponse`: fields `id` (UUID), `orgId` (UUID, extracted from `brief.getOrganization().getId()`), `submittedById` (UUID), `title`, `description`, `status` (String), `priority` (String), `desiredDueDate` (String, ISO format), `budget` (BigDecimal), `creativeDirection`, `metadata` (Map), `references` (Map), `createdAt` (String, ISO instant), `updatedAt` (String, ISO instant)
- `BriefUpdateRequest`: optional fields `title`, `description`, `priority`, `desiredDueDate`, `budget`, `creativeDirection` -- all nullable to support partial updates
- DTOs follow the existing pattern: plain Java classes with no-arg constructor, all-args constructor, getters, and setters in `controller.dto` package

**Frontend NewBriefPage -- Form and Layout**
- Replace the placeholder in `rivvy-portal-ui/src/pages/NewBriefPage.tsx` with a full draft editing form
- On mount (via `useEffect`), POST to `/api/briefs` with `credentials: 'include'` to auto-create the draft; store the returned brief ID and field values in component state
- Render editable fields: `title` (text input, placeholder "Untitled Brief"), `description` (textarea), `priority` (select dropdown with options Normal/High/Urgent), `desiredDueDate` (date input), `budget` (number input), `creativeDirection` (textarea)
- Show a loading/creating state while the POST is in flight; show an error banner if the POST fails
- Include a "Cancel" button that calls DELETE `/api/briefs/{id}` then navigates to `/dashboard` via `useNavigate()`

**Frontend Autosave Logic**
- Implement debounced autosave: after the user stops editing any field for approximately 1-2 seconds, send a PUT `/api/briefs/{id}` with the current form state
- Use a `setTimeout`/`clearTimeout` pattern (or a `useRef` for the timer) inside a `useEffect` that depends on the form field values
- While the autosave PUT is in flight, show a subtle "Saving..." indicator; on success show "Saved"; on error show "Save failed"
- Do NOT autosave if the brief has not yet been created (guard on the brief ID being present)
- On navigate-away without Cancel, do nothing -- the draft persists as an orphaned draft in the database

**Frontend Styling and Testing**
- Use the inline `styles` object pattern (`Record<string, React.CSSProperties>`) matching `LoginPage.tsx` conventions: `styles.container`, `styles.card`, `styles.field`, `styles.label`, `styles.input`, etc.
- Form should be centered in a card layout similar to `LoginPage` but wider (max-width ~640px) to accommodate more fields
- Tests in `NewBriefPage.test.tsx` using Vitest + React Testing Library + MemoryRouter, following `LoginPage.test.tsx` patterns: mock `fetch` via `vi.spyOn(globalThis, 'fetch')`, mock `useNavigate`, assert on rendered fields, API calls, and navigation

## Existing Code to Leverage

**`Brief` entity (`crud-logic-service/src/main/java/com/rivvystudios/portal/model/Brief.java`)**
- JPA entity mapped to the `brief` table with all required columns already defined: `id`, `organization` (ManyToOne to Organization, column `org_id`), `submittedBy` (ManyToOne to UserAccount, column `submitted_by`), `title`, `description`, `status` (BriefStatus enum), `priority` (BriefPriority enum), `desiredDueDate` (LocalDate), `budget` (BigDecimal), `creativeDirection`, `references` (JSONB Map), `metadata` (JSONB Map), `createdAt`, `updatedAt`
- Uses `@GeneratedValue(strategy = GenerationType.UUID)` for ID generation
- `BriefRepository` (`crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BriefRepository.java`) extends `JpaRepository<Brief, UUID>` with no custom queries yet -- new query methods can be added as needed

**`AuthController` and DTO pattern (`crud-logic-service/src/main/java/com/rivvystudios/portal/controller/AuthController.java`)**
- Demonstrates the established controller pattern: `@RestController`, `@RequestMapping`, constructor-injected dependencies, `ResponseEntity<?>` return types, `Map.of("error", ...)` for error responses
- Resolves authenticated user via `SecurityContextHolder.getContext().getAuthentication().getName()` (email) then `UserAccountRepository.findByEmail()`
- DTOs (`LoginRequest`, `LoginResponse`) in `controller.dto` package use plain Java classes with no-arg and all-args constructors, getters/setters

**`OrganizationMember` and `OrganizationMemberRepository`**
- `OrganizationMember` entity links `UserAccount` to `Organization` via `user_id` and `org_id` foreign keys
- `OrganizationMemberRepository.findByUserAccount(UserAccount)` returns `List<OrganizationMember>` -- use this to resolve the authenticated user's organization (take the first result since each user belongs to one org)

**`ProducerAssignment` entity and `ProducerAssignmentRepository`**
- `ProducerAssignment` links a producer's `OrganizationMember` (`producerMember`, column `producer_member_id`) to a client `Organization` (`clientOrg`, column `client_org_id`)
- `ProducerAssignmentRepository` extends `JpaRepository<ProducerAssignment, UUID>` with no custom queries -- add a derived query method like `existsByProducerMemberAndClientOrg(OrganizationMember, Organization)` to check producer access

**`LoginPage.tsx` and `LoginPage.test.tsx` frontend patterns**
- `LoginPage.tsx` demonstrates: `fetch()` with `credentials: 'include'`, `useNavigate()` for navigation, `useState` for form state and loading/error, inline `styles` object typed as `Record<string, React.CSSProperties>`, `data-testid` on the root div
- `LoginPage.test.tsx` demonstrates: `vi.spyOn(globalThis, 'fetch')` for mocking API calls, `MemoryRouter` wrapping, `vi.mock('react-router-dom')` for mocking `useNavigate`, `userEvent.setup()` for interactions, `waitFor` for async assertions

## Out of Scope
- Submitting a Brief (transitioning status from DRAFT to SUBMITTED or any other status)
- Creating or editing BriefItems and Deliverables
- Notifications or emails triggered by draft creation or editing
- Admin visibility or admin-specific controls for Briefs
- Producer editing of draft Briefs (Producers have view-only access)
- UI for editing the `metadata` or `references` JSONB fields (backend defaults only)
- Currency formatting, locale-specific validation, or advanced validation rules for the budget field
- Orphaned draft cleanup (scheduled jobs, TTL, or garbage collection)
- Mobile-specific layout, hamburger menu, or collapsible navigation
- List/search endpoint for Briefs (only single-brief CRUD endpoints are in scope)
