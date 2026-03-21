# Spec Requirements: Spring Boot Maven Service Skeleton

## Initial Description

Create a single-module, empty Java 21 Spring Boot 3.x Maven service for Rivvy Portal Services (within RS Service Tier) using coordinates com.rivvystudios:portal:v0.0.1 and base package com.rivvystudios.portal. Include starters: web, actuator, test. Provide standard Maven layout, minimal application.yaml (default profile only), and an executable JAR that builds and starts locally. No business endpoints or integrations. Place the project in repo folder crud-logic-service.

## Requirements Discussion

### First Round Questions

**Q1: [q1-springboot-version]** I notice the tech-stack.md specifies Spring Boot 4.x, but your spec requests Spring Boot 3.x. Should we use Spring Boot 3.x as specified in the initial requirements (which is the latest stable Spring Boot version as of early 2026), or align with tech-stack.md and use Spring Boot 4.x?
**Answer:** Use Spring Boot 3.x

**Q2: [q2-maven-wrapper]** For the Maven Wrapper (mvnw), I assume we should include it with standard configuration to allow builds without requiring a pre-installed Maven. Is that correct?
**Answer:** Correct

**Q3: [q3-folder-location]** The spec says the project should be placed in folder 'crud-logic-service'. I assume this folder should be created at the repository root level (e.g., ./crud-logic-service/). Is that correct, or should it be nested elsewhere?
**Answer:** Correct

**Q4: [q4-application-yaml]** For the application.yaml configuration, I'm assuming minimal configuration: just server.port (e.g., 8080) and management.endpoints.web.exposure.include for actuator health endpoint. Should we include anything else, like application name or logging configuration?
**Answer:** Correct (minimal configuration is sufficient)

**Q5: [q5-spring-boot-parent]** For the Spring Boot parent POM version, I assume we should use the latest stable release of the Spring Boot version chosen (3.x or 4.x based on Q1). Is that acceptable?
**Answer:** Correct

**Q6: [q6-actuator-config]** The acceptance criteria mention /actuator/health should return UP. I assume the default Spring Boot Actuator configuration is sufficient (no custom health indicators needed). Correct?
**Answer:** Correct

**Q7: [q7-exclusions]** Are there any specific exclusions or additional features you want to explicitly avoid in this skeleton beyond what's listed in "Out of Scope"? For example, should we avoid including spring-boot-devtools or any IDE-specific files?
**Answer:** None (no additional exclusions)

**Q8: [q-reuse-similar-code]** Are there existing Spring Boot services in your codebase with similar structure or Maven configuration we should reference? For example: existing pom.xml files, application.yaml patterns, or Spring Boot main class naming conventions you prefer?
**Answer:** No

**Q9: [q-visual-assets]** Do you have any architectural diagrams, folder structure mockups, or example pom.xml files that could help guide this setup? If yes, please place them in: agent-os/specs/2026-02-23-spring-boot-maven-service-skeleton/planning/visuals/
**Answer:** None

### Existing Code to Reference

No similar existing features identified for reference.

### Follow-up Questions

No follow-up questions were required.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
Not applicable - no visual files were found in the visuals folder.

## Requirements Summary

### Functional Requirements

- **Spring Boot Version:** Use Spring Boot 3.x (latest stable release)
- **Java Version:** Java 21 (LTS)
- **Build Tool:** Maven with Maven Wrapper (mvnw) included
- **Maven Coordinates:**
  - groupId: `com.rivvystudios`
  - artifactId: `portal`
  - version: `v0.0.1`
- **Base Package:** `com.rivvystudios.portal`
- **Project Location:** Repository root level folder named `crud-logic-service`
- **Dependencies:**
  - `spring-boot-starter-web` - REST API capabilities
  - `spring-boot-starter-actuator` - Health and monitoring endpoints
  - `spring-boot-starter-test` - Testing framework
- **Application Configuration:**
  - Minimal `application.yaml` with default profile only
  - Include `server.port` configuration (e.g., 8080)
  - Include `management.endpoints.web.exposure.include` for actuator health endpoint
- **Main Application Class:**
  - Located under `com.rivvystudios.portal` package
  - Standard Spring Boot application entry point
- **Packaging:** Executable JAR using `spring-boot-maven-plugin`
- **Standard Maven Layout:**
  - `src/main/java` - Java source code
  - `src/main/resources` - Application resources
  - `src/test/java` - Test source code
- **Build & Run:**
  - `mvn clean verify` succeeds on JDK 21
  - Application starts locally without errors
  - `/actuator/health` endpoint returns UP status

### Reusability Opportunities

No existing code patterns or components identified for reuse. This is a greenfield project.

### Scope Boundaries

**In Scope:**
- Empty Spring Boot 3.x Maven project skeleton
- Basic web and actuator starters only
- Minimal configuration for local development
- Maven Wrapper for build portability
- Standard project structure following Maven conventions
- Executable JAR packaging
- Basic health check endpoint via Spring Boot Actuator

**Out of Scope:**
- Business/domain controllers or endpoints beyond Spring Boot defaults
- Database configuration, repositories, or migrations
- Messaging, caching, or external system integrations
- Authentication/authorization/security setup
- API specifications or contracts
- Containerization or orchestration assets (Dockerfile, Docker Compose, Helm, Terraform, CI/CD pipelines)
- Lombok, code style/quality tooling beyond standard Maven defaults
- Additional testing scaffolding beyond `spring-boot-starter-test`
- Spring Boot DevTools or other development-time utilities
- IDE-specific configuration files

### Technical Considerations

- **Java 21 LTS:** Leverage modern Java features and long-term support
- **Spring Boot 3.x:** Use latest stable Spring Boot 3.x release for modern framework features
- **Maven Build:** Standard Maven project structure for consistency
- **Maven Wrapper:** Ensures builds work without pre-installed Maven
- **Actuator Health:** Default configuration sufficient for basic health checks
- **No Additional Dependencies:** Keep the skeleton minimal and focused
- **Executable JAR:** Standard Spring Boot packaging for easy deployment
- **Repository Naming:** Folder named `crud-logic-service` while artifactId remains `portal` (as per assumptions)
- **Architecture Context:** Project is part of RS Service Tier containing Rivvy Portal Services within the Rivvy Portal application

### Acceptance Criteria Verification

The following criteria must be met:

1. Single-module Maven project exists under folder `crud-logic-service` with coordinates:
   - groupId: `com.rivvystudios`
   - artifactId: `portal`
   - version: `v0.0.1`
2. Project configured for Java 21 and Spring Boot 3.x
3. `mvn clean verify` succeeds on JDK 21
4. Standard source layout: `src/main/java`, `src/main/resources`, `src/test/java`
5. Main class under `com.rivvystudios.portal` starts Spring application successfully
6. Dependencies included: `spring-boot-starter-web`, `spring-boot-starter-actuator`, `spring-boot-starter-test`
7. `application.yaml` exists with default configuration only (no separate environment profiles)
8. Application starts locally without errors
9. `/actuator/health` endpoint returns UP status
10. No business endpoints, external integrations, or containerization artifacts present
11. Maven Wrapper (mvnw) included for build portability
