# Task Breakdown: Spring Boot Maven Service Skeleton

## Overview
Total Tasks: 4 task groups with 21 sub-tasks

## Task List

### Project Structure and Build Configuration

#### Task Group 1: Maven Project Setup
**Dependencies:** None

- [x] 1.0 Complete Maven project structure and build configuration
  - [x] 1.1 Create project directory structure
    - Create `crud-logic-service` folder at repository root
    - Create standard Maven layout: `src/main/java`, `src/main/resources`, `src/test/java`
    - Create package structure: `src/main/java/com/rivvystudios/portal`
  - [x] 1.2 Create pom.xml with Spring Boot configuration
    - Set Maven coordinates: groupId=`com.rivvystudios`, artifactId=`portal`, version=`v0.0.1`
    - Configure Spring Boot parent POM (latest stable 3.x release)
    - Set Java 21 source and target compatibility
    - Add dependency: `spring-boot-starter-web`
    - Add dependency: `spring-boot-starter-actuator`
    - Add dependency: `spring-boot-starter-test` with test scope
  - [x] 1.3 Configure spring-boot-maven-plugin
    - Add plugin configuration for executable JAR packaging
    - Ensure proper main class configuration
    - Set up repackaging goal for jar execution
  - [x] 1.4 Add Maven Wrapper
    - Generate Maven Wrapper files (mvnw, mvnw.cmd, .mvn/wrapper/)
    - Use standard Maven Wrapper configuration
    - Ensure wrapper works without pre-installed Maven
  - [x] 1.5 Verify Maven build configuration
    - Run `mvn clean verify` on JDK 21
    - Confirm build succeeds without errors
    - Verify executable JAR is created in target/

**Acceptance Criteria:**
- Project structure follows standard Maven layout
- `pom.xml` contains correct coordinates and three starter dependencies only
- Maven Wrapper files are present and functional
- `mvn clean verify` succeeds on JDK 21
- Executable JAR artifact is generated

### Application Implementation

#### Task Group 2: Spring Boot Application Class
**Dependencies:** Task Group 1

- [x] 2.0 Complete Spring Boot application implementation
  - [x] 2.1 Write 2-4 focused tests for application startup
    - Test that Spring application context loads successfully
    - Test that application starts without errors
    - Test that web server starts on configured port
    - Limit to 2-4 highly focused tests maximum
  - [x] 2.2 Create main application class
    - Create `PortalApplication.java` in package `com.rivvystudios.portal`
    - Add `@SpringBootApplication` annotation
    - Implement standard `main` method with `SpringApplication.run()`
    - Keep class minimal with no business logic, controllers, or custom beans
  - [x] 2.3 Ensure application startup tests pass
    - Run ONLY the 2-4 tests written in 2.1
    - Verify Spring context loads without errors
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-4 tests written in 2.1 pass
- `PortalApplication` class exists in correct package
- Class has proper `@SpringBootApplication` annotation
- Application can be instantiated and started

### Configuration

#### Task Group 3: Application Configuration
**Dependencies:** Task Group 2

- [x] 3.0 Complete application configuration
  - [x] 3.1 Write 2-3 focused tests for configuration
    - Test that server.port is configured correctly
    - Test that actuator health endpoint is exposed
    - Limit to 2-3 highly focused tests maximum
  - [x] 3.2 Create application.yaml
    - Create file in `src/main/resources/`
    - Configure `server.port: 8080`
    - Configure `management.endpoints.web.exposure.include: health`
    - Use default profile only, no environment-specific profiles
  - [x] 3.3 Verify configuration is minimal
    - Ensure no database configuration present
    - Ensure no security configuration present
    - Ensure no messaging configuration present
    - Confirm only server.port and actuator exposure settings exist
  - [x] 3.4 Ensure configuration tests pass
    - Run ONLY the 2-3 tests written in 3.1
    - Verify configuration properties load correctly
    - Do NOT run the entire test suite at this stage

**Acceptance Criteria:**
- The 2-3 tests written in 3.1 pass
- `application.yaml` exists with minimal configuration
- Server port is configured to 8080
- Actuator health endpoint is exposed
- No additional configuration beyond requirements

### Verification and Testing

#### Task Group 4: Build, Runtime, and Integration Verification
**Dependencies:** Task Groups 1-3

- [x] 4.0 Verify complete service skeleton functionality
  - [x] 4.1 Review existing tests from Task Groups 2-3
    - Review the 2-4 tests written in Task 2.1 (application startup)
    - Review the 2-3 tests written in Task 3.1 (configuration)
    - Total existing tests: approximately 4-7 tests
  - [x] 4.2 Write 2-5 integration tests for health endpoint
    - Test that application starts via `mvn spring-boot:run`
    - Test that `/actuator/health` endpoint returns HTTP 200
    - Test that health response contains `"status":"UP"`
    - Test that JAR file is executable via `java -jar`
    - Limit to 2-5 highly focused integration tests maximum
  - [x] 4.3 Verify Maven build lifecycle
    - Run `mvn clean verify` and confirm success
    - Verify all tests pass (approximately 6-12 tests total)
    - Confirm executable JAR is created: `target/portal-v0.0.1.jar`
  - [x] 4.4 Verify runtime execution with mvn
    - Start application using `mvn spring-boot:run`
    - Confirm application starts without errors
    - Verify startup logs show Spring Boot banner and initialization
    - Verify server starts on port 8080
  - [x] 4.5 Verify JAR execution
    - Run `java -jar target/portal-v0.0.1.jar`
    - Confirm application starts successfully
    - Verify no runtime dependencies required
  - [x] 4.6 Test actuator health endpoint
    - Access `http://localhost:8080/actuator/health`
    - Verify HTTP 200 response
    - Verify JSON response contains `{"status":"UP"}`
    - Test both mvn and JAR execution modes
  - [x] 4.7 Verify no out-of-scope artifacts
    - Confirm no business controllers exist
    - Confirm no database configuration present
    - Confirm no Dockerfile or docker-compose.yaml
    - Confirm no CI/CD pipeline files
    - Confirm no IDE-specific files (.idea, .vscode, .eclipse)
    - Confirm no additional dependencies beyond three starters

**Acceptance Criteria:**
- All tests pass (approximately 6-12 tests total from groups 2-4)
- `mvn clean verify` succeeds on JDK 21
- Application starts via `mvn spring-boot:run` without errors
- Executable JAR runs via `java -jar target/portal-v0.0.1.jar`
- `/actuator/health` returns HTTP 200 with `{"status":"UP"}`
- No out-of-scope artifacts or configuration present
- Service skeleton is minimal and ready for business logic addition

## Execution Order

Recommended implementation sequence:
1. Project Structure and Build Configuration (Task Group 1)
2. Application Implementation (Task Group 2)
3. Configuration (Task Group 3)
4. Verification and Testing (Task Group 4)

## Notes

- This is a greenfield project with no existing code to leverage
- Focus on keeping the skeleton minimal - only three dependencies
- Ensure Maven Wrapper is included for build portability
- No visual designs apply to this infrastructure setup
- Total test count should be approximately 6-12 tests maximum
- All verification should confirm absence of out-of-scope features
