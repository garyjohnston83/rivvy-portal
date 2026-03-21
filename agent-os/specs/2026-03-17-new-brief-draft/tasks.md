# Task Breakdown: New Brief Draft

## Overview
Total Tasks: 7 task groups, 46 sub-tasks

This feature allows a Client user to create and edit a draft Brief on the `/new-brief` page. The draft is auto-created on page entry, autosaved as the user edits, and deletable via Cancel. The draft is scoped to the Client's organization and visible to assigned Producers as read-only.

## Key Files

| File | Purpose |
|------|---------|
| `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BriefPriority.java` | Enum to modify -- add NORMAL value |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/model/Brief.java` | Existing JPA entity -- no changes needed |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/BriefRepository.java` | Existing repo -- no changes needed |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/ProducerAssignmentRepository.java` | Existing repo -- add derived query method |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/OrganizationMemberRepository.java` | Existing repo -- already has `findByUserAccount()` |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/UserAccountRepository.java` | Existing repo -- already has `findByEmail()` |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/BriefResponse.java` | New DTO -- response shape for all endpoints |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/BriefUpdateRequest.java` | New DTO -- request body for PUT endpoint |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/service/BriefService.java` | New service -- all business logic |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BriefController.java` | New controller -- thin REST layer |
| `crud-logic-service/src/main/java/com/rivvystudios/portal/config/SecurityConfig.java` | Existing config -- add CSRF exclusion |
| `rivvy-portal-ui/src/pages/NewBriefPage.tsx` | Existing placeholder -- replace with full form |
| `rivvy-portal-ui/src/pages/NewBriefPage.test.tsx` | New test file -- component tests |
| `crud-logic-service/src/test/java/com/rivvystudios/portal/brief/BriefServiceTests.java` | New test file -- service unit tests |
| `crud-logic-service/src/test/java/com/rivvystudios/portal/brief/BriefControllerTests.java` | New test file -- integration tests |
| `crud-logic-service/src/main/resources/db/changelog/seed/R__seed_data.sql` | Existing seed data -- add ProducerAssignment + client org for authorization tests |

---

## Task List

### Backend: Enum and Repository Prep

#### Task Group 1: BriefPriority Enum Update and Repository Query Methods
**Dependencies:** None

- [x] 1.0 Complete enum and repository prep
  - [x] 1.1 Add `NORMAL` value to `BriefPriority` enum
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/model/enums/BriefPriority.java`
    - Add `NORMAL` between `LOW` and `MEDIUM` (or after `MEDIUM`) so the enum contains at least: `LOW, MEDIUM, NORMAL, HIGH, URGENT`
    - Keep `LOW` and `MEDIUM` for backward compatibility with any existing data; they simply will not be exposed in the UI dropdown
  - [x] 1.2 Add derived query method to `ProducerAssignmentRepository`
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/repository/ProducerAssignmentRepository.java`
    - Add method: `boolean existsByProducerMemberAndClientOrg(OrganizationMember producerMember, Organization clientOrg)`
    - This enables checking whether a producer user has view access to a brief's owning organization
  - [x] 1.3 Add seed data for integration test authorization scenarios
    - File: `crud-logic-service/src/main/resources/db/changelog/seed/R__seed_data.sql`
    - Add a second Organization (e.g., `Acme Corp`, id `20000000-0000-0000-0000-000000000002`) to represent a client org distinct from Rivvy Studios
    - Add a new client user (e.g., `client2@acme.local`, id `30000000-0000-0000-0000-000000000004`) as a member of Acme Corp with CLIENT role
    - Add a `ProducerAssignment` row linking the existing producer member (`40000000-0000-0000-0000-000000000002`) to Acme Corp, so the producer can view Acme briefs
    - This data is needed so integration tests can verify: client can CRUD their own org's briefs, producer can GET but not PUT/DELETE, and users from other orgs get 403

**Acceptance Criteria:**
- `BriefPriority.NORMAL` compiles and is a valid enum constant
- `ProducerAssignmentRepository.existsByProducerMemberAndClientOrg()` resolves as a valid Spring Data derived query
- Seed data includes a client org, client user, and producer assignment for authorization testing
- Application starts successfully with the updated enum and seed data (Liquibase changelog applies cleanly)

---

### Backend: DTOs

#### Task Group 2: Brief Request and Response DTOs
**Dependencies:** Task Group 1 (BriefPriority enum must have NORMAL)

- [x] 2.0 Complete DTO layer
  - [x] 2.1 Create `BriefResponse` DTO
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/BriefResponse.java`
    - Package: `com.rivvystudios.portal.controller.dto`
    - Follow the `LoginResponse.java` pattern: plain Java class, no-arg constructor, all-args constructor, getters, setters
    - Fields: `id` (UUID), `orgId` (UUID), `submittedById` (UUID), `title` (String), `description` (String), `status` (String), `priority` (String), `desiredDueDate` (String, ISO format e.g., `"2026-04-15"`), `budget` (BigDecimal), `creativeDirection` (String), `metadata` (Map<String, Object>), `references` (Map<String, Object>), `createdAt` (String, ISO instant e.g., `"2026-03-17T10:30:00Z"`), `updatedAt` (String, ISO instant or null)
    - Note: `orgId` is extracted from `brief.getOrganization().getId()`, `submittedById` from `brief.getSubmittedBy().getId()`; `status` and `priority` are the enum `.name()` values; dates are formatted to ISO strings
  - [x] 2.2 Create `BriefUpdateRequest` DTO
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/dto/BriefUpdateRequest.java`
    - Package: `com.rivvystudios.portal.controller.dto`
    - Follow the same pattern: plain Java class, no-arg constructor, all-args constructor, getters, setters
    - Fields (all nullable to support partial updates): `title` (String), `description` (String), `priority` (String -- maps to `BriefPriority` enum), `desiredDueDate` (String -- ISO date string to parse into `LocalDate`), `budget` (BigDecimal), `creativeDirection` (String)
    - Does NOT include: `metadata`, `references`, `status`, `orgId`, `submittedBy` -- these are immutable via this endpoint

**Acceptance Criteria:**
- `BriefResponse` has all 14 fields with constructors, getters, and setters
- `BriefUpdateRequest` has all 6 optional fields with constructors, getters, and setters
- Both DTOs compile and follow the established DTO pattern in `controller.dto` package

---

### Backend: Service Layer

#### Task Group 3: BriefService Business Logic
**Dependencies:** Task Group 1 (enum + repository), Task Group 2 (DTOs)

- [x] 3.0 Complete service layer
  - [x] 3.1 Write 6 focused unit tests for `BriefService`
    - File: `crud-logic-service/src/test/java/com/rivvystudios/portal/brief/BriefServiceTests.java`
    - Use JUnit 5 with Mockito (`@ExtendWith(MockitoExtension.class)`) to mock repositories
    - Test 1: `createDraft_setsDefaultValues` -- verify status=DRAFT, priority=NORMAL, title="Untitled Brief", metadata={}, references={}, createdAt is set
    - Test 2: `createDraft_resolvesOrgFromUserMembership` -- verify org is taken from first OrganizationMember result
    - Test 3: `updateBrief_appliesPartialFieldUpdates` -- verify only non-null fields are applied to the entity, updatedAt is set
    - Test 4: `updateBrief_producerGetsForbidden` -- verify a producer member (not a client org member) receives a 403-equivalent exception
    - Test 5: `deleteBrief_onlyAllowedForDraftStatus` -- verify deletion succeeds for DRAFT status and throws for non-DRAFT
    - Test 6: `getBrief_producerWithAssignmentCanView` -- verify a producer assigned to the brief's org can retrieve the brief
  - [x] 3.2 Create `BriefService` class
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/service/BriefService.java`
    - Package: `com.rivvystudios.portal.service`
    - Annotate with `@Service`; use constructor injection (following `AuthController` pattern)
    - Inject: `BriefRepository`, `UserAccountRepository`, `OrganizationMemberRepository`, `ProducerAssignmentRepository`
  - [x] 3.3 Implement `createDraft(String email)` method
    - Resolve `UserAccount` via `UserAccountRepository.findByEmail(email)`; throw `ResponseStatusException(404)` if not found
    - Resolve org via `OrganizationMemberRepository.findByUserAccount(userAccount)`; take first result's `.getOrganization()`; throw `ResponseStatusException(400)` if no memberships
    - Create new `Brief` entity with defaults: `status=BriefStatus.DRAFT`, `priority=BriefPriority.NORMAL`, `title="Untitled Brief"`, `description=null`, `metadata=Map.of()`, `references=Map.of()`, `createdAt=Instant.now()`, `submittedBy=userAccount`, `organization=org`
    - Save via `BriefRepository.save()` and return the persisted entity
  - [x] 3.4 Implement `getBriefById(UUID id, String email)` method
    - Look up Brief via `BriefRepository.findById(id)`; throw `ResponseStatusException(404)` if not found
    - Resolve authenticated user and their org membership
    - Authorization check: user must be (a) a member of the brief's organization (via `OrganizationMemberRepository.findByUserAccount()` checking if any membership's org matches brief's org), OR (b) a producer assigned to the brief's org (via `ProducerAssignmentRepository.existsByProducerMemberAndClientOrg()`)
    - If neither condition is met, throw `ResponseStatusException(403)`
    - Return the Brief entity
  - [x] 3.5 Implement `updateBrief(UUID id, BriefUpdateRequest request, String email)` method
    - Look up Brief via `BriefRepository.findById(id)`; throw 404 if not found
    - Authorization: only client members of the brief's owning org may update; resolve user's memberships and verify at least one membership's org matches the brief's org; if not, throw `ResponseStatusException(403)` -- producers and non-members both get 403
    - Apply non-null fields from `BriefUpdateRequest` to the entity:
      - `title` -> `brief.setTitle()`
      - `description` -> `brief.setDescription()`
      - `priority` -> `brief.setPriority(BriefPriority.valueOf(request.getPriority()))` (uppercase)
      - `desiredDueDate` -> `brief.setDesiredDueDate(LocalDate.parse(request.getDesiredDueDate()))`
      - `budget` -> `brief.setBudget()`
      - `creativeDirection` -> `brief.setCreativeDirection()`
    - Set `updatedAt=Instant.now()`
    - Save via `BriefRepository.save()` and return the updated entity
  - [x] 3.6 Implement `deleteBrief(UUID id, String email)` method
    - Look up Brief via `BriefRepository.findById(id)`; throw 404 if not found
    - Authorization: same as update -- only client org members may delete; throw 403 otherwise
    - Status guard: if `brief.getStatus() != BriefStatus.DRAFT`, throw `ResponseStatusException(409, "Only draft briefs can be deleted")`
    - Delete via `BriefRepository.delete(brief)`
  - [x] 3.7 Implement helper method `toResponse(Brief brief)` to convert entity to `BriefResponse` DTO
    - Extract `orgId` from `brief.getOrganization().getId()`
    - Extract `submittedById` from `brief.getSubmittedBy().getId()`
    - Format `status` and `priority` via `.name()`
    - Format `desiredDueDate` via `desiredDueDate.toString()` (ISO format) or null
    - Format `createdAt` and `updatedAt` via `.toString()` (ISO instant) or null
    - Return populated `BriefResponse`
  - [x] 3.8 Ensure service layer tests pass
    - Run ONLY the 6 tests written in 3.1: `BriefServiceTests`
    - Verify all mocked repository interactions behave as expected
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- All 6 `BriefServiceTests` pass
- `createDraft` sets correct defaults and resolves org from authenticated user
- `updateBrief` applies only non-null fields and rejects producer/non-member access
- `deleteBrief` enforces DRAFT-only deletion and org membership authorization
- `getBriefById` allows org members and assigned producers, rejects others
- `toResponse` correctly maps entity fields to DTO

---

### Backend: Controller and Security Config

#### Task Group 4: BriefController REST Endpoints and Security Configuration
**Dependencies:** Task Group 3 (BriefService must be complete)

- [x] 4.0 Complete controller and security layer
  - [x] 4.1 Write 8 focused integration tests for `BriefController`
    - File: `crud-logic-service/src/test/java/com/rivvystudios/portal/brief/BriefControllerTests.java`
    - Use `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Import(TestcontainersConfiguration.class)` -- same pattern as `AuthControllerTests.java`
    - Inject `MockMvc` and `JdbcTemplate`
    - Each test authenticates by first calling `POST /api/auth/login` and extracting session cookies (follow `AuthControllerTests.getAuthMeReturnsUserInfoWhenSessionExists` pattern)
    - Test 1: `postBriefs_asClient_creates201WithDefaults` -- login as `client2@acme.local`, POST `/api/briefs`, expect 201 with `status=DRAFT`, `priority=NORMAL`, `title=Untitled Brief`
    - Test 2: `getBrief_asClient_returns200` -- create a brief, then GET `/api/briefs/{id}`, expect 200 with correct fields
    - Test 3: `putBrief_asClient_updates200` -- create a brief, then PUT `/api/briefs/{id}` with `{"title":"My Project"}`, expect 200 with updated title
    - Test 4: `deleteBrief_asClient_returns204` -- create a brief, then DELETE `/api/briefs/{id}`, expect 204; subsequent GET returns 404
    - Test 5: `putBrief_asProducer_returns403` -- login as `producer@rivvy.local`, attempt PUT on a brief owned by Acme Corp, expect 403
    - Test 6: `getBrief_asProducer_assignedToOrg_returns200` -- login as `producer@rivvy.local` (who has ProducerAssignment to Acme Corp), GET a brief owned by Acme Corp, expect 200
    - Test 7: `deleteBrief_nonDraftStatus_returns409` -- create a brief, manually update its status to SUBMITTED via JdbcTemplate, then attempt DELETE, expect 409
    - Test 8: `postBriefs_unauthenticated_returns401` -- call POST `/api/briefs` without session cookies, expect 401
  - [x] 4.2 Create `BriefController` class
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/controller/BriefController.java`
    - Package: `com.rivvystudios.portal.controller`
    - Annotate: `@RestController`, `@RequestMapping("/api/briefs")`
    - Constructor-inject `BriefService`
    - Resolve authenticated user email via `SecurityContextHolder.getContext().getAuthentication().getName()` in each handler method
  - [x] 4.3 Implement `POST /api/briefs` endpoint
    - Method signature: `@PostMapping` returning `ResponseEntity<BriefResponse>`
    - No request body -- all defaults set server-side
    - Call `briefService.createDraft(email)` to get the Brief entity
    - Call `briefService.toResponse(brief)` to convert to DTO
    - Return `ResponseEntity.status(201).body(response)`
  - [x] 4.4 Implement `GET /api/briefs/{id}` endpoint
    - Method signature: `@GetMapping("/{id}")` with `@PathVariable UUID id`, returning `ResponseEntity<BriefResponse>`
    - Call `briefService.getBriefById(id, email)`
    - Return `ResponseEntity.ok(briefService.toResponse(brief))`
  - [x] 4.5 Implement `PUT /api/briefs/{id}` endpoint
    - Method signature: `@PutMapping("/{id}")` with `@PathVariable UUID id`, `@RequestBody BriefUpdateRequest request`, returning `ResponseEntity<BriefResponse>`
    - Call `briefService.updateBrief(id, request, email)`
    - Return `ResponseEntity.ok(briefService.toResponse(updatedBrief))`
  - [x] 4.6 Implement `DELETE /api/briefs/{id}` endpoint
    - Method signature: `@DeleteMapping("/{id}")` with `@PathVariable UUID id`, returning `ResponseEntity<Void>`
    - Call `briefService.deleteBrief(id, email)`
    - Return `ResponseEntity.noContent().build()`
  - [x] 4.7 Update `SecurityConfig` to add CSRF exclusion for `/api/briefs/**`
    - File: `crud-logic-service/src/main/java/com/rivvystudios/portal/config/SecurityConfig.java`
    - In the `.csrf()` config block, add `.ignoringRequestMatchers("/api/briefs/**")` alongside the existing `"/api/auth/**"`
    - No changes needed for endpoint authorization since `.anyRequest().authenticated()` already covers `/api/briefs/**`
  - [x] 4.8 Ensure controller integration tests pass
    - Run ONLY the 8 tests written in 4.1: `BriefControllerTests`
    - Verify all CRUD operations, authorization enforcement, and status guard work end-to-end against Testcontainers PostgreSQL
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- All 8 `BriefControllerTests` pass
- POST `/api/briefs` returns 201 with correct default values
- GET `/api/briefs/{id}` returns 200 for authorized users (client org member or assigned producer), 403 for others, 404 for missing
- PUT `/api/briefs/{id}` returns 200 for client org members, 403 for producers/non-members
- DELETE `/api/briefs/{id}` returns 204 for draft briefs owned by client org members, 409 for non-draft, 403 for unauthorized
- Unauthenticated requests return 401
- CSRF does not block POST/PUT/DELETE to `/api/briefs/**`

---

### Frontend: Form UI and Page Layout

#### Task Group 5: NewBriefPage Form Component
**Dependencies:** Task Group 4 (API endpoints must be available for the form to call)

- [x] 5.0 Complete NewBriefPage form UI
  - [x] 5.1 Write 6 focused component tests for `NewBriefPage`
    - File: `rivvy-portal-ui/src/pages/NewBriefPage.test.tsx`
    - Follow `LoginPage.test.tsx` pattern: `vi.spyOn(globalThis, 'fetch')`, `vi.mock('react-router-dom')` for `useNavigate`, `MemoryRouter` wrapping, `userEvent.setup()`, `waitFor`
    - Test 1: `renders form fields after draft creation` -- mock POST `/api/briefs` to return a draft, assert that title input, description textarea, priority select, date input, budget input, creative direction textarea, and Cancel button are all rendered
    - Test 2: `calls POST /api/briefs on mount to auto-create draft` -- mock fetch, render component, verify fetch was called with `POST`, `/api/briefs`, `credentials: 'include'`
    - Test 3: `shows loading state while draft is being created` -- use a pending fetch promise, assert "Creating brief..." or similar loading indicator appears
    - Test 4: `shows error banner if draft creation fails` -- mock fetch to return 500, assert error message is displayed
    - Test 5: `populates form fields from server response` -- mock POST to return a brief with `title: "Untitled Brief"`, `priority: "NORMAL"`, verify form fields show these values
    - Test 6: `priority dropdown has Normal, High, Urgent options` -- render the component (after draft creation), verify select element contains exactly 3 options with correct labels
  - [x] 5.2 Replace `NewBriefPage.tsx` placeholder with full draft form
    - File: `rivvy-portal-ui/src/pages/NewBriefPage.tsx`
    - Import: `useState`, `useEffect`, `useRef`, `useNavigate` from react/react-router-dom
    - Component state: `briefId` (string | null), `formData` (object with title, description, priority, desiredDueDate, budget, creativeDirection), `loading` (boolean), `error` (string), `saveStatus` (string: '', 'saving', 'saved', 'error')
    - Keep `data-testid="page-new-brief"` on root div
  - [x] 5.3 Implement auto-create draft on mount
    - In a `useEffect` with empty dependency array, call `POST /api/briefs` with `{ method: 'POST', credentials: 'include' }`
    - On success (201): extract the `BriefResponse` JSON, store `briefId` in state, populate `formData` from the response (title, priority, etc.)
    - While in flight: show a centered loading indicator ("Creating brief...")
    - On failure: show an error banner (follow `LoginPage` error style pattern)
  - [x] 5.4 Render the draft editing form fields
    - `title`: `<input type="text">` with label "Title", placeholder "Untitled Brief"
    - `description`: `<textarea>` with label "Description", placeholder "Describe your project..."
    - `priority`: `<select>` with label "Priority" and three `<option>` elements: Normal (value="NORMAL"), High (value="HIGH"), Urgent (value="URGENT")
    - `desiredDueDate`: `<input type="date">` with label "Desired Due Date"
    - `budget`: `<input type="number" step="0.01" min="0">` with label "Budget"
    - `creativeDirection`: `<textarea>` with label "Creative Direction", placeholder "Describe the creative vision..."
    - Each field should use `onChange` to update `formData` state
  - [x] 5.5 Implement Cancel button
    - Render a "Cancel" button below the form fields
    - On click: call `DELETE /api/briefs/{briefId}` with `{ method: 'DELETE', credentials: 'include' }`
    - On success (204): call `navigate('/dashboard')` via `useNavigate()`
    - On failure: show an error message but still navigate to `/dashboard` (draft may be orphaned -- acceptable per spec)
    - Guard: do nothing if `briefId` is null (draft not yet created)
  - [x] 5.6 Apply inline styles following `LoginPage.tsx` pattern
    - Use `const styles: Record<string, React.CSSProperties> = { ... }` at the bottom of the file
    - `styles.container`: centered flex layout, `minHeight: '80vh'`
    - `styles.card`: `maxWidth: '640px'` (wider than LoginPage's 400px), same border/shadow pattern
    - `styles.field`, `styles.label`, `styles.input`: reuse LoginPage style values
    - `styles.textarea`: same as input but with explicit `minHeight: '80px'`, `resize: 'vertical'`
    - `styles.select`: same as input
    - `styles.cancelButton`: distinct secondary style (e.g., gray background or outlined)
    - `styles.saveIndicator`: subtle text style for autosave status ("Saving...", "Saved", "Save failed")
  - [x] 5.7 Ensure form UI tests pass
    - Run ONLY the 6 tests written in 5.1: `NewBriefPage.test.tsx`
    - Verify form renders correctly after mock draft creation
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- All 6 `NewBriefPage` form tests pass
- NewBriefPage auto-creates a draft on mount via POST `/api/briefs`
- All 6 form fields (title, description, priority, desiredDueDate, budget, creativeDirection) are rendered and editable
- Priority dropdown shows exactly Normal, High, Urgent options
- Cancel button calls DELETE and navigates to `/dashboard`
- Loading and error states are displayed appropriately
- Inline styles follow the established `LoginPage.tsx` pattern with a wider card (640px)

---

### Frontend: Autosave Logic

#### Task Group 6: Debounced Autosave Behavior
**Dependencies:** Task Group 5 (form UI and state must be in place)

- [x] 6.0 Complete autosave logic
  - [x] 6.1 Write 4 focused tests for autosave behavior
    - File: `rivvy-portal-ui/src/pages/NewBriefPage.test.tsx` (append to existing test file from 5.1)
    - Test 1: `autosaves via PUT after user stops editing for ~1-2s` -- mock timers with `vi.useFakeTimers()`, type into title field, advance timers by 2000ms, verify fetch was called with PUT `/api/briefs/{id}` and body containing the updated title
    - Test 2: `does not autosave if briefId is null` -- render without mocking the POST to succeed (leave briefId null), type into a field, advance timers, verify no PUT call was made
    - Test 3: `shows "Saving..." indicator during autosave` -- trigger an autosave with a pending PUT promise, assert "Saving..." text is visible
    - Test 4: `shows "Saved" indicator after successful autosave` -- trigger autosave, resolve the PUT, assert "Saved" text is visible
  - [x] 6.2 Implement debounced autosave with `useEffect` and `useRef`
    - File: `rivvy-portal-ui/src/pages/NewBriefPage.tsx` (add to existing component)
    - Use a `useRef<ReturnType<typeof setTimeout> | null>` to track the debounce timer
    - In a `useEffect` that depends on `formData` values and `briefId`:
      - Guard: if `briefId` is null, return early
      - Clear any existing timer via `clearTimeout(timerRef.current)`
      - Set a new timer: `timerRef.current = setTimeout(() => { performAutosave(); }, 1500)`
      - Return a cleanup function that clears the timer
    - Skip the initial trigger (when form is first populated from the POST response) by using a `useRef<boolean>` flag that skips the first effect run after briefId is set
  - [x] 6.3 Implement `performAutosave()` function
    - Build the request body from current `formData` state -- include all fields (title, description, priority, desiredDueDate, budget, creativeDirection)
    - Call `PUT /api/briefs/{briefId}` with `{ method: 'PUT', headers: { 'Content-Type': 'application/json' }, credentials: 'include', body: JSON.stringify(requestBody) }`
    - Before the call: set `saveStatus` to `'saving'`
    - On success (200): set `saveStatus` to `'saved'`
    - On failure: set `saveStatus` to `'error'`
  - [x] 6.4 Render autosave status indicator
    - Display a subtle text indicator near the top of the form card or below the title:
      - `saveStatus === 'saving'` -> "Saving..."
      - `saveStatus === 'saved'` -> "Saved"
      - `saveStatus === 'error'` -> "Save failed"
      - `saveStatus === ''` -> render nothing
    - Use `styles.saveIndicator` for styling (small font, muted color)
  - [x] 6.5 Ensure autosave tests pass
    - Run ONLY the 4 tests written in 6.1 plus the 6 from 5.1 (total 10 tests in `NewBriefPage.test.tsx`)
    - Verify debounced PUT calls fire correctly
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- All 4 autosave tests pass (10 total in `NewBriefPage.test.tsx`)
- Autosave fires approximately 1.5 seconds after the user stops editing
- Autosave sends PUT with current form state to `/api/briefs/{id}`
- Autosave is guarded: does not fire if `briefId` is null
- Save status indicator shows "Saving...", "Saved", or "Save failed" appropriately
- Debounce timer is properly cleaned up on unmount

---

### Testing: Review and Gap Analysis

#### Task Group 7: Test Review and Critical Gap Fill
**Dependencies:** Task Groups 1-6 (all implementation complete)

- [x] 7.0 Review existing tests and fill critical gaps only
  - [x] 7.1 Review tests from all prior task groups
    - Review the 6 unit tests from Task 3.1 (`BriefServiceTests`)
    - Review the 8 integration tests from Task 4.1 (`BriefControllerTests`)
    - Review the 10 component tests from Tasks 5.1 + 6.1 (`NewBriefPage.test.tsx`)
    - Total existing tests for this feature: 24 tests
  - [x] 7.2 Analyze test coverage gaps for THIS feature only
    - Identify any critical user workflows that lack test coverage
    - Focus ONLY on gaps related to the New Brief Draft feature requirements
    - Do NOT assess entire application test coverage
    - Prioritize end-to-end workflows and edge cases that could cause production issues
    - Likely gaps to evaluate:
      - Cancel button behavior (DELETE + navigate) as a full flow test
      - Partial update behavior (sending only some fields in PUT)
      - Concurrent autosave debounce resets (typing resets the timer)
      - Error recovery (failed autosave does not break subsequent saves)
      - GET by non-existent UUID returns 404
  - [x] 7.3 Write up to 10 additional strategic tests to fill identified gaps
    - Add tests to the existing test files (`BriefControllerTests.java`, `NewBriefPage.test.tsx`)
    - Focus on integration points and user workflow completeness
    - Do NOT write comprehensive coverage for all scenarios
    - Skip edge cases, performance tests, and accessibility tests unless business-critical
  - [x] 7.4 Run all feature-specific tests
    - Run backend tests: `BriefServiceTests` + `BriefControllerTests`
    - Run frontend tests: `NewBriefPage.test.tsx`
    - Expected total: approximately 24-34 tests maximum
    - Do NOT run the entire application test suite
    - Verify all critical workflows pass

**Acceptance Criteria:**
- All feature-specific tests pass (approximately 24-34 tests total)
- Critical user workflows for this feature are covered: create draft, edit fields, autosave, cancel/delete, producer read-only access, authorization enforcement
- No more than 10 additional tests added beyond the original 24
- Testing focused exclusively on the New Brief Draft feature requirements

---

## Execution Order

Recommended implementation sequence:

```
1. Task Group 1: Enum and Repository Prep        (no dependencies)
2. Task Group 2: Brief DTOs                       (depends on 1)
3. Task Group 3: BriefService Business Logic      (depends on 1, 2)
4. Task Group 4: BriefController + Security       (depends on 3)
5. Task Group 5: NewBriefPage Form UI             (depends on 4)
6. Task Group 6: Autosave Logic                   (depends on 5)
7. Task Group 7: Test Review and Gap Analysis     (depends on 1-6)
```

Task Groups 1 and 2 can potentially be developed in parallel since DTOs do not depend on the enum change at compile time (only at runtime when NORMAL is referenced in the service layer). However, sequential execution is recommended for clarity.

Task Groups 5 and 6 are separated because the autosave logic layer builds on top of the form UI and state management established in Group 5. Implementing them together in one pass is acceptable if the developer prefers.

---

## Out of Scope Reminders

The following items are explicitly out of scope for this feature and should NOT be implemented:

- **Brief submission** -- No status transitions beyond DRAFT (no SUBMITTED, IN_REVIEW, etc.)
- **BriefItems and Deliverables** -- No creation or editing of line items within a brief
- **Notifications/emails** -- No triggers on draft creation or editing
- **Admin visibility** -- No admin-specific brief views or controls
- **Producer editing** -- Producers have view-only access; no PUT/DELETE for producers
- **Metadata/references UI** -- These JSONB fields are initialized to `{}` server-side only
- **Currency formatting** -- Budget is a plain number input; no locale-specific formatting or validation
- **Orphaned draft cleanup** -- No scheduled jobs, TTL, or garbage collection for abandoned drafts
- **Mobile layout** -- No hamburger menu, collapsible navigation, or mobile-specific breakpoints
- **List/search endpoint** -- Only single-brief CRUD endpoints (POST, GET, PUT, DELETE); no list/filter/search API
