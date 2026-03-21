# Task Breakdown: Scaffold UI Placeholder Routes

## Overview
Total Tasks: 4 task groups, 20 sub-tasks

This is a greenfield frontend scaffold. There is no existing frontend code. The `rivvy-portal-ui` directory will be created alongside the existing `crud-logic-service` directory at the project root. Automated test code is explicitly out of scope per the spec; verification tasks focus on build validation and manual route confirmation.

## Task List

### Project Initialization

#### Task Group 1: Vite + React + TypeScript Project Bootstrap
**Dependencies:** None

- [x] 1.0 Complete project initialization
  - [x] 1.1 Scaffold a new Vite project in the `rivvy-portal-ui` directory
    - Run Vite's project creation with the `react-ts` template to produce the base project structure
    - Target directory: `rivvy-portal-ui/` at the repo root (alongside `crud-logic-service/`)
    - Vite version must be 5.x
  - [x] 1.2 Verify and adjust dependency versions in `package.json`
    - React and ReactDOM must be 19.x
    - TypeScript must be 5.x
    - Install `react-router-dom` (latest stable version compatible with React 19)
    - Run `npm install` to confirm all dependencies resolve without errors
  - [x] 1.3 Configure TypeScript with strict mode
    - Verify `tsconfig.json` has `"strict": true` enabled
    - Verify `tsconfig.node.json` exists and is correctly configured for Vite's Node-side config
    - Ensure `tsconfig.json` includes JSX support set to `"react-jsx"`
  - [x] 1.4 Configure `vite.config.ts`
    - Ensure the React plugin (`@vitejs/plugin-react`) is applied
    - Confirm Vite dev server supports HTML5 history API fallback for SPA routing (default behavior, but verify no conflicting configuration)
  - [x] 1.5 Set up `index.html` with root mount point
    - Ensure `index.html` exists at `rivvy-portal-ui/` root with a `<div id="root"></div>` element
    - Ensure the script entry points to `src/main.tsx`
  - [x] 1.6 Verify the project builds and dev server starts without errors
    - Run `npm run build` to confirm TypeScript compilation and Vite build succeed
    - Run `npm run dev` briefly to confirm the dev server starts

**Acceptance Criteria:**
- `rivvy-portal-ui/` directory exists at the project root with all standard Vite project files: `package.json`, `vite.config.ts`, `tsconfig.json`, `tsconfig.node.json`, `index.html`
- `npm install` completes without errors
- `npm run build` completes without errors
- `npm run dev` starts the Vite dev server without errors
- Dependency versions match: Vite 5.x, React 19.x, ReactDOM 19.x, TypeScript 5.x, react-router-dom installed

---

### Layout and Component Scaffolding

#### Task Group 2: Shared Layout and Placeholder Page Components
**Dependencies:** Task Group 1

- [x] 2.0 Complete layout and page component scaffolding
  - [x] 2.1 Create the shared layout component at `src/layouts/RootLayout.tsx`
    - Import and render `<Outlet />` from `react-router-dom`
    - Apply minimal centered container styling using inline styles or a simple CSS approach
    - Container should have a max-width constraint and horizontal centering (e.g., `max-width: 960px; margin: 0 auto; padding: 1rem`)
    - No header, sidebar, footer, or navigation elements
  - [x] 2.2 Create `src/pages/LoginPage.tsx`
    - Render a `<div>` with `data-testid="page-login"` containing an `<h1>` with text "Login"
    - No business logic, API calls, or interactive elements
  - [x] 2.3 Create `src/pages/ProfilePage.tsx`
    - Render a `<div>` with `data-testid="page-profile"` containing an `<h1>` with text "Profile"
    - No business logic, API calls, or interactive elements
  - [x] 2.4 Create `src/pages/DashboardPage.tsx`
    - Render a `<div>` with `data-testid="page-dashboard"` containing an `<h1>` with text "Dashboard"
    - No business logic, API calls, or interactive elements
  - [x] 2.5 Create `src/pages/NewBriefPage.tsx`
    - Render a `<div>` with `data-testid="page-new-brief"` containing an `<h1>` with text "New Brief"
    - No business logic, API calls, or interactive elements
  - [x] 2.6 Create `src/pages/VaultPage.tsx`
    - Render a `<div>` with `data-testid="page-vault"` containing an `<h1>` with text "Brand Vault"
    - No business logic, API calls, or interactive elements
  - [x] 2.7 Create `src/pages/ScreeningPage.tsx`
    - Render a `<div>` with `data-testid="page-screening"` containing an `<h1>` with text "Screening Room"
    - No business logic, API calls, or interactive elements
  - [x] 2.8 Create `src/pages/AdminPage.tsx`
    - Render a `<div>` with `data-testid="page-admin"` containing an `<h1>` with text "Admin"
    - No business logic, API calls, or interactive elements

**Acceptance Criteria:**
- `src/layouts/RootLayout.tsx` exists and renders an `<Outlet />` inside a centered container
- All 7 page components exist in `src/pages/` with correct filenames
- Each page component renders an `<h1>` with the exact title from the route table
- Each page component has the correct `data-testid` attribute on its outermost element
- No component contains business logic, state management, or API calls

---

### Routing Integration

#### Task Group 3: React Router Configuration and Entry Point Wiring
**Dependencies:** Task Group 2

- [x] 3.0 Complete routing integration
  - [x] 3.1 Configure `src/main.tsx` as the application entry point
    - Import `BrowserRouter` from `react-router-dom`
    - Wrap `<App />` with `<BrowserRouter>` inside `ReactDOM.createRoot`
    - Render into the `#root` div element
    - Remove any Vite scaffold boilerplate that is not needed (e.g., default counter component, default CSS imports)
  - [x] 3.2 Define all 7 routes in `src/App.tsx`
    - Import `Routes`, `Route` from `react-router-dom`
    - Import `RootLayout` from `src/layouts/RootLayout`
    - Import all 7 page components from `src/pages/`
    - Create a parent `<Route>` using `RootLayout` as the `element` prop
    - Nest all 7 page routes as children of the layout route
    - Route path mapping:
      - `/login` -> `<LoginPage />`
      - `/profile` -> `<ProfilePage />`
      - `/dashboard` -> `<DashboardPage />`
      - `/new-brief` -> `<NewBriefPage />`
      - `/vault` -> `<VaultPage />`
      - `/screening` -> `<ScreeningPage />`
      - `/admin` -> `<AdminPage />`
  - [x] 3.3 Remove Vite scaffold boilerplate files
    - Delete the default `src/App.css` if it contains Vite demo styles (or replace with empty/minimal content)
    - Delete or clear `src/index.css` if it contains Vite demo styles
    - Remove `src/assets/` directory if it only contains the default Vite logo
    - Remove any default counter component file (e.g., if Vite scaffold created one)
  - [x] 3.4 Verify the project builds successfully after routing integration
    - Run `npm run build` to confirm no TypeScript errors or import issues
    - Confirm the build produces output in `dist/`

**Acceptance Criteria:**
- `src/main.tsx` wraps the app in `<BrowserRouter>` and mounts to `#root`
- `src/App.tsx` defines all 7 routes nested under a parent layout route using `RootLayout`
- Each route path matches the route table exactly: `/login`, `/profile`, `/dashboard`, `/new-brief`, `/vault`, `/screening`, `/admin`
- No Vite scaffold boilerplate (demo counter, demo logos) remains in the source
- `npm run build` succeeds with zero errors

---

### Verification

#### Task Group 4: Build Validation and Route Verification
**Dependencies:** Task Group 3

- [x] 4.0 Complete build validation and route verification
  - [x] 4.1 Run a clean install and production build
    - Run `rm -rf node_modules && npm install` to verify a clean install
    - Run `npm run build` to confirm the production build succeeds
    - Verify the `dist/` directory contains `index.html` and bundled JS assets
  - [x] 4.2 Start the dev server and verify all 7 routes render correctly
    - Start `npm run dev` and confirm the server starts
    - Verify each route loads the correct page title when navigated to directly (deep link):
      - `/login` shows "Login"
      - `/profile` shows "Profile"
      - `/dashboard` shows "Dashboard"
      - `/new-brief` shows "New Brief"
      - `/vault` shows "Brand Vault"
      - `/screening` shows "Screening Room"
      - `/admin` shows "Admin"
    - Confirm no route produces a 404 or blank page on direct navigation
  - [x] 4.3 Verify `data-testid` attributes are present in the rendered DOM
    - For each of the 7 routes, inspect the DOM to confirm the correct `data-testid` is present:
      - `page-login`, `page-profile`, `page-dashboard`, `page-new-brief`, `page-vault`, `page-screening`, `page-admin`
    - Confirm each `data-testid` value is unique across all pages
  - [x] 4.4 Verify project structure matches the spec conventions
    - Confirm directory structure:
      - `rivvy-portal-ui/src/pages/` contains exactly 7 page component files
      - `rivvy-portal-ui/src/layouts/` contains `RootLayout.tsx`
      - `rivvy-portal-ui/src/App.tsx` is the route definition hub
      - `rivvy-portal-ui/src/main.tsx` is the entry point
    - Confirm no feature folders, barrel exports, or advanced patterns were introduced

**Acceptance Criteria:**
- Clean install (`npm install`) and production build (`npm run build`) both succeed with zero errors
- All 7 routes render the correct placeholder page with the exact title from the route table
- Direct navigation (deep links) to all 7 routes works without 404 errors or redirects
- All 7 `data-testid` attributes are present and unique in the rendered DOM
- Project structure follows the flat convention specified in the spec
- No automated test files, navigation components, auth guards, or out-of-scope artifacts exist

## Execution Order

Recommended implementation sequence:

1. **Project Initialization** (Task Group 1) -- Bootstrap the Vite + React + TypeScript project and install all dependencies. This is the foundation for everything else.
2. **Layout and Component Scaffolding** (Task Group 2) -- Create the shared layout and all 7 placeholder page components. These are standalone files with no routing dependency, so they can be created before wiring routes.
3. **Routing Integration** (Task Group 3) -- Wire up `main.tsx` with BrowserRouter, define all routes in `App.tsx`, and clean up scaffold boilerplate. This connects all the pieces from Task Groups 1 and 2.
4. **Build Validation and Route Verification** (Task Group 4) -- Verify the complete build, all routes, data-testid attributes, and project structure conventions. This is the final validation pass.

## Notes

- **No automated tests**: The spec explicitly places automated test code (Vitest, React Testing Library) out of scope. Task Group 4 focuses on build validation and manual/dev-server verification instead.
- **No visual design**: No visual assets were provided. Placeholder pages use minimal inline styles for readability only.
- **Greenfield project**: There is no existing frontend code to reference or migrate. All files are created from scratch.
- **Single specialization**: This spec is entirely frontend work (React/TypeScript/Vite). There are no backend, database, or API tasks.
