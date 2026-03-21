# MISSION: Rivvy Portal

## Overview
Rivvy Portal is a collaborative web portal for Clients, Rivvy Producers, and Rivvy Admins to manage creative video projects end-to-end. It centralizes brief submissions, brand asset management (Brand Vault), video review with timecoded comments (Screening Room), approvals, and organization/user administration.

## Problem Statement
Not discussed during discovery.

## Target Audience
- Rivvy Admin
  - Involved processes: organization setup; user/role management; assigning producers to client orgs
- Rivvy Producer
  - Involved processes: upload/manage video versions; review and respond to timecoded comments; manage approvals; support new brief intake across multiple client orgs
- Client
  - Involved processes: view project videos; leave timecoded comments; upload/manage assets in Brand Vault; create/submit new briefs

## Vision & Goals
Not discussed during discovery.

## Success Criteria
Not discussed during discovery.

## Scope
### In Scope
- User roles and high-level processes
  - Rivvy Admin: organization setup; user/role management; assigning producers to client orgs
  - Rivvy Producer: upload/manage video versions; review/respond to timecoded comments; manage approvals; support new brief intake across multiple client orgs
  - Client: view project videos; leave timecoded comments; upload/manage assets in Brand Vault; create/submit new briefs
- Process activities captured (as provided during discovery)
  - organization setup (Rivvy Admin): organization setup
  - user/role management (Rivvy Admin): user/role management
  - assigning producers to client orgs (Rivvy Admin): assigning producers to client orgs
  - upload/manage video versions (Rivvy Producer): upload/manage video versions
  - review and respond to timecoded comments (Rivvy Producer): review and respond to timecoded comments
  - manage approvals (Rivvy Producer): manage approvals
  - support new brief intake across multiple client orgs (Rivvy Producer): support new brief intake across multiple client orgs
  - view project videos (Client): view project videos
  - leave timecoded comments (Client): leave timecoded comments
  - upload/manage assets in Brand Vault (Client): upload/manage assets in Brand Vault
  - create/submit new briefs (Client): create/submit new briefs
- UI screens and routes
  - Login (/login): For users to login or reset their password
  - User Profile (/profile): Basic user profile page that allows user to set key info about themselves and change password
  - Client Dashboard (/dashboard if logged in user is a client): Entry point with links to Screening Room, Brand Vault, New Brief
  - New Brief (/new-brief): Clients submit requests for new projects that can involve one or many videos; Producer views the filled-in form and can edit the brief if required
  - Brand Vault (/vault): Client uploads brand assets (e.g., logos, fonts, guidelines) and tags asset types; centralized per-organization asset library, as well as per-project assets
  - Screening Room (/screening): View current and past project videos; Client and Producers add timecoded comments; Producer uploads videos and manages versions; Clients can approve final cuts
  - Rivvy Producer Dashboard (/dashboard if logged in user is a Rivvy Producer): Producer access to Screening Room, Brand Vault, and New Brief across multiple assigned client orgs
  - Rivvy Admin (/admin): Admin creates organizations; manages users/roles and credentials; assigns producers to client organizations

### Out of Scope
- Not discussed during discovery.

## Constraints
- Not discussed during discovery.

## Delivery Plan
- Timeline, milestones, MVP definition, and phasing expectations: Not discussed during discovery.

## Additional Context
- New vs. Existing Product: Not discussed during discovery.
- Existing Documentation: Not discussed during discovery.
- Notes from discovery: The process activities provided mirror the high-level process names. The system distinguishes dashboards for Clients and Producers using the same /dashboard route based on logged-in role.
