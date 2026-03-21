# Spec Initialization: Spring Boot Maven Service Skeleton

## Original Feature Description

Create a single-module, empty Java 21 Spring Boot 3.x Maven service for Rivvy Portal Services (within RS Service Tier) using coordinates com.rivvystudios:portal:v0.0.1 and base package com.rivvystudios.portal. Include starters: web, actuator, test. Provide standard Maven layout, minimal application.yaml (default profile only), and an executable JAR that builds and starts locally. No business endpoints or integrations. Place the project in repo folder crud-logic-service.

## In Scope
- Single-module Spring Boot 3.x (Java 21) Maven project skeleton for Rivvy Portal Services under RS Service Tier
- Maven coordinates: groupId=com.rivvystudios, artifactId=portal, version=v0.0.1
- Base package: com.rivvystudios.portal with a bootstrapping application class
- Standard Maven layout: src/main/java, src/main/resources, src/test/java
- Baseline dependencies: spring-boot-starter-web, spring-boot-starter-actuator, spring-boot-starter-test
- Executable JAR packaging with Spring Boot Maven plugin
- application.yaml with default (no separate env profiles)
- Project builds and runs locally with no business logic
- Repository location: folder crud-logic-service

## Out of Scope
- Business/domain controllers or endpoints beyond Spring Boot defaults
- Database configuration, repositories, or migrations
- Messaging, caching, or external system integrations
- Authentication/authorization/security setup
- API specifications or contracts
- Containerization or orchestration assets (Dockerfile, Compose, Helm, Terraform, pipelines)
- Lombok, code style/quality tooling, or additional testing scaffolding beyond spring-boot-starter-test

## Assumptions
- ArtifactId remains 'portal' even though the repository folder is named 'crud-logic-service'.
- Packaging is an executable JAR using spring-boot-maven-plugin.
- Only the default application.yaml is required; no dev/prod profiles are needed now.
- Maven Wrapper (mvnw) is acceptable to include by default.
- No security, database, or messaging dependencies are required in this initial setup.

## Acceptance Criteria
- A single-module Maven project exists under the folder crud-logic-service with groupId=com.rivvystudios, artifactId=portal, version=v0.0.1.
- Project is configured for Java 21 and Spring Boot 3.x; mvn clean verify succeeds on JDK 21.
- Source layout includes src/main/java, src/main/resources, and src/test/java.
- A main class under com.rivvystudios.portal starts the Spring application successfully.
- Dependencies include spring-boot-starter-web, spring-boot-starter-actuator, and spring-boot-starter-test.
- application.yaml exists with default configuration only (no separate environment profiles).
- When running locally, the application starts without errors and exposes Actuator health at /actuator/health returning UP.
- No business endpoints, external integrations, or containerization artifacts are present.

## Architecture Context
Here is the part of the architecture that directly applies to this spec and it must be reviewed as part of the spec above. It is expressed as a JSON payload about business, application, data, behavioural and/or UI architecture:

```json
{
  "entities": [
    {
      "id": 1,
      "name": "RS Service Tier",
      "type": "appComponents",
      "fields": {
        "applicationId": "app-mlxwja0d-7swkq"
      }
    },
    {
      "id": 2,
      "name": "Rivvy Portal",
      "type": "applications",
      "fields": {
        "appType": "",
        "status": ""
      }
    },
    {
      "id": 3,
      "name": "Rivvy Portal Services",
      "type": "services",
      "fields": {
        "applicationId": "app-mlxwja0d-7swkq",
        "serviceType": ""
      }
    }
  ],
  "diagrams": [],
  "relationships": [
    {
      "id": 4,
      "type": "contains",
      "label": "contains",
      "from": {
        "type": "appComponents",
        "eid": 1,
        "name": "comp-mlxwjp8s-enkq6"
      },
      "to": {
        "type": "services",
        "eid": 3,
        "name": "Rivvy Portal Services"
      },
      "fields": {}
    },
    {
      "id": 5,
      "type": "contains",
      "label": "contains",
      "from": {
        "type": "applications",
        "eid": 2,
        "name": "app-mlxwja0d-7swkq"
      },
      "to": {
        "type": "appComponents",
        "eid": 1,
        "name": "comp-mlxwjp8s-enkq6"
      },
      "fields": {}
    }
  ]
}
```

## Date Created
2026-02-23
