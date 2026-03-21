# Specification: Spring Boot Maven Service Skeleton

## Goal
Create a minimal, executable Java 21 Spring Boot 3.x Maven service skeleton for Rivvy Portal Services with web and actuator capabilities, following standard Maven conventions and producing a runnable JAR with health check endpoint.

## User Stories
- As a backend developer, I want a working Spring Boot Maven project skeleton so that I can immediately start adding business logic without setting up infrastructure
- As a DevOps engineer, I want a service with health endpoints so that I can monitor service availability in deployment environments

## Specific Requirements

**Maven Project Structure**
- Create single-module Maven project in folder `crud-logic-service` at repository root
- Use Maven coordinates: groupId=`com.rivvystudios`, artifactId=`portal`, version=`v0.0.1`
- Include Maven Wrapper (mvnw/mvnw.cmd) for builds without pre-installed Maven
- Follow standard Maven directory layout: `src/main/java`, `src/main/resources`, `src/test/java`
- Configure `spring-boot-maven-plugin` for executable JAR packaging
- Ensure `mvn clean verify` succeeds on JDK 21

**Spring Boot Configuration**
- Use latest stable Spring Boot 3.x release as parent POM
- Configure project for Java 21 (source and target compatibility)
- Set base package to `com.rivvystudios.portal`
- Include only three starter dependencies: `spring-boot-starter-web`, `spring-boot-starter-actuator`, `spring-boot-starter-test`
- No additional dependencies beyond these three starters

**Main Application Class**
- Create Spring Boot application class in package `com.rivvystudios.portal`
- Use `@SpringBootApplication` annotation
- Implement standard `main` method to launch Spring application
- Follow Spring Boot naming conventions (e.g., `PortalApplication`)
- No business logic, controllers, or custom beans in this class

**Application Configuration**
- Create `application.yaml` in `src/main/resources` with minimal configuration
- Configure `server.port` (e.g., 8080)
- Configure `management.endpoints.web.exposure.include` to expose actuator health endpoint
- Use default profile only, no environment-specific profiles (dev/prod/test)
- No database, security, or messaging configuration

**Spring Boot Actuator**
- Use default Actuator configuration without custom health indicators
- Ensure `/actuator/health` endpoint returns UP status when application runs
- No additional actuator endpoints or custom metrics required
- Rely on spring-boot-starter-actuator defaults for health checks

**Build and Runtime Verification**
- Application must start locally without errors using `mvn spring-boot:run`
- JAR file must be executable via `java -jar target/portal-v0.0.1.jar`
- Health endpoint must be accessible at `http://localhost:8080/actuator/health`
- No runtime dependencies on external systems (databases, message brokers, etc.)

## Visual Design
No visual designs provided for this infrastructure setup.

## Existing Code to Leverage
No existing source code found in this project. This is a greenfield service skeleton.

## Out of Scope
- Business controllers, REST endpoints, or domain logic beyond Spring Boot defaults
- Database dependencies, JPA configuration, repositories, or migration scripts
- Security configuration, authentication, or authorization setup
- Messaging systems like Kafka, RabbitMQ, or JMS
- Caching frameworks like Redis or Ehcache
- External API integrations or HTTP clients
- API documentation tools like Swagger/OpenAPI
- Containerization files (Dockerfile, docker-compose.yaml)
- Infrastructure as Code (Helm charts, Terraform, Kubernetes manifests)
- CI/CD pipeline configurations (GitHub Actions, Jenkins, GitLab CI)
- Code quality tools (Checkstyle, PMD, SpotBugs, SonarQube)
- Additional libraries like Lombok, MapStruct, or utility frameworks
- Development tools like spring-boot-devtools
- IDE-specific configuration files (.idea, .vscode, .eclipse)
- Environment-specific application configuration files (application-dev.yaml, application-prod.yaml)
