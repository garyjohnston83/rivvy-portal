# Specification: Scaffold UI Placeholder Routes

## Goal
Bootstrap a new Vite + React + TypeScript frontend project for Rivvy Portal and scaffold seven placeholder page routes with minimal layout, providing the foundational UI structure for all subsequent frontend development.

## User Stories
- As a frontend developer, I want a working Vite/React/TypeScript project with routing infrastructure so that I can immediately start building feature pages without setup overhead
- As a QA engineer, I want each placeholder page to have a unique data-testid so that I can write automated tests targeting specific routes before feature content is added

## Specific Requirements

**Vite + React + TypeScript Project Initialization**
- Create a new frontend project in a top-level `rivvy-portal-ui` directory alongside the existing `crud-logic-service` directory
- Use Vite 5.x as the build tool with the React plugin
- Configure TypeScript 5.x with strict mode enabled
- Install React 19.x and ReactDOM 19.x as dependencies
- Produce standard project files: `package.json`, `vite.config.ts`, `tsconfig.json`, `tsconfig.node.json`, `index.html`
- The project must start without errors via the standard Vite dev command

**Entry Point and HTML Shell**
- Create `index.html` at the project root with a `<div id="root">` mount point
- Create `src/main.tsx` as the application entry point that renders the React app into the root div
- BrowserRouter from React Router must wrap the application at the top level in `main.tsx`

**React Router Setup**
- Install `react-router-dom` as a dependency (latest stable version compatible with React 19)
- Define all seven routes in a centralized route configuration within `src/App.tsx`
- Use a parent route with the shared layout component and nest all seven page routes as children
- Each route path must match the route table exactly: `/login`, `/profile`, `/dashboard`, `/new-brief`, `/vault`, `/screening`, `/admin`

**Shared Layout Component**
- Create a `src/layouts/RootLayout.tsx` component that renders React Router's `<Outlet />`
- Apply minimal centered container styling (e.g., max-width constraint and horizontal centering) using plain inline styles or a simple CSS file
- No header, sidebar, footer, or navigation elements

**Placeholder Page Components**
- Create one component per route in a `src/pages/` directory, with filenames matching the page name (e.g., `LoginPage.tsx`, `DashboardPage.tsx`)
- Each component renders a single `<h1>` heading element containing the page title as specified in the route table
- Each component's outermost container element must include a `data-testid` attribute with a unique value derived from the route name (e.g., `data-testid="page-login"`, `data-testid="page-dashboard"`)
- Use plain inline styles or minimal CSS for readability; no CSS Modules infrastructure needed for placeholders
- No business logic, API calls, state management, or interactive elements

**Route-to-Page Mapping**
- `/login` renders LoginPage with title "Login" and `data-testid="page-login"`
- `/profile` renders ProfilePage with title "Profile" and `data-testid="page-profile"`
- `/dashboard` renders DashboardPage with title "Dashboard" and `data-testid="page-dashboard"`
- `/new-brief` renders NewBriefPage with title "New Brief" and `data-testid="page-new-brief"`
- `/vault` renders VaultPage with title "Brand Vault" and `data-testid="page-vault"`
- `/screening` renders ScreeningPage with title "Screening Room" and `data-testid="page-screening"`
- `/admin` renders AdminPage with title "Admin" and `data-testid="page-admin"`

**Vite Dev Server Configuration**
- Configure Vite's dev server to support HTML5 history API fallback so that direct navigation (deep links) to any route loads the correct page without 404 errors
- This is typically handled by Vite's default SPA behavior, but must be verified for all seven routes

**Project Structure Convention**
- Follow a flat, simple directory structure: `src/pages/` for page components, `src/layouts/` for layout components
- Keep `App.tsx` as the route definition hub
- Do not introduce feature folders, barrel exports, or advanced patterns at this stage

## Visual Design
No visual assets were provided. Placeholder pages require no design guidance.

## Existing Code to Leverage
No existing frontend source code found in this project. This is a greenfield frontend scaffold. The backend exists at `crud-logic-service/` (Spring Boot) but is not relevant to this spec.

## Out of Scope
- Global navigation, header, sidebar, footer, or menu components
- Authentication, login form logic, session management, or auth guards
- Role-aware routing, role-based redirects, or conditional rendering by user role
- API calls, HTTP clients, state management libraries, or integration with Rivvy Portal Services backend
- Styling beyond minimal readability (no design system, theme, or CSS Modules setup)
- Redirect logic, catch-all routes, 404 pages, or error boundary components
- Automated test code (Vitest, React Testing Library, or any test files)
- Updates to architecture model entities, UI workflow diagrams, or backend code
- Containerization (Dockerfile), CI/CD pipeline configuration, or deployment scripts
- Code quality tooling (ESLint, Prettier) beyond what Vite scaffolds by default
