# Implementation Plan: M1 — Backend Foundation

**Branch**: `001-backend-foundation` | **Date**: 2026-11-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-backend-foundation/spec.md`

## Summary

Establish the foundational backend infrastructure for MiMi: a
running Spring Boot 4.2.0 application connected to PostgreSQL 17,
with Flyway migration infrastructure, health/readiness endpoints,
OpenAPI/Swagger documentation, structured error handling,
Testcontainers-based integration testing, Docker Compose for local
development, and baseline structured logging. No business-domain
logic or schema is introduced.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**:
- Spring Boot 4.2.0 (web, data-jpa, security, validation, actuator)
- Flyway (flyway-core, flyway-database-postgresql)
- Springdoc OpenAPI 3.2.1 (springdoc-openapi-starter-webmvc-ui)
- Lombok
- PostgreSQL JDBC driver

**Storage**: PostgreSQL 17 (Alpine) via Docker Compose; Flyway for
schema migrations; Hibernate JPA in validation mode (`ddl-auto=validate`)

**Testing**:
- JUnit 5 (spring-boot-starter-test)
- Testcontainers PostgreSQL (spring-boot-testcontainers,
  testcontainers-junit-jupiter, testcontainers-postgresql)
- Spring Security Test (spring-boot-starter-security-test)

**Target Platform**: Local development (Docker Compose + JVM)

**Project Type**: Web service (Modular Monolith, single deployable)

**Performance Goals**: Application startup within 30 seconds;
API documentation loads within 5 seconds (per SC-001, SC-006)

**Constraints**: Backend only; no business-domain schema in M1;
no authentication enforcement in M1 (Spring Security present but
all endpoints accessible); simulation only — no real financial
operations

**Scale/Scope**: Single developer; local development environment;
one PostgreSQL database instance

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Architecture and Maintainability | ✅ PASS | Single Spring Boot application, modular monolith structure. No modules introduced beyond `platform` concerns. No premature abstractions. |
| II. Financial Data Integrity | ✅ N/A | No financial operations in M1. Migration infrastructure only. |
| III. Concurrency and Transaction Safety | ✅ N/A | No financial transactions in M1. |
| IV. Idempotency | ✅ N/A | No state-changing financial commands in M1. |
| V. Security | ✅ PASS | No auth enforcement in M1 (explicitly deferred to M2). No credentials in logs (FR-010). Spring Security present but permissive. |
| VI. API and Database Standards | ✅ PASS | `/api/v1` prefix established. OpenAPI/Swagger served. Flyway for migrations. `ddl-auto=validate`. Structured error responses. |
| VII. Testing and Quality | ✅ PASS | Testcontainers PostgreSQL for integration tests. TDD methodology. No H2 substitution. |
| VIII. Observability and Operations | ✅ PASS | Structured logging. Health and readiness via Actuator. No sensitive data in logs. |
| IX. Learning-First Development | ✅ PASS | Developer approval required. Mentor mode. Incremental delivery. |

**Gate result: PASS** — no violations.

## Project Structure

### Documentation (this feature)

```text
specs/001-backend-foundation/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0: research findings
├── data-model.md        # Phase 1: data model (infrastructure only)
├── quickstart.md        # Phase 1: validation guide
├── contracts/           # Phase 1: API contracts
│   └── api-endpoints.md # Health, readiness, docs, error contracts
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/milki/mimi/
│   │   ├── MiMiApplication.java          # (exists) Application entry point
│   │   └── platform/
│   │       ├── api/
│   │       │   └── GlobalExceptionHandler.java  # Structured error responses
│   │       └── config/
│   │           ├── SecurityConfig.java           # Permit-all for M1
│   │           ├── OpenApiConfig.java            # Springdoc configuration
│   │           └── ActuatorConfig.java           # Health/readiness config
│   └── resources/
│       ├── application.yaml              # (exists) Main configuration
│       ├── application-local.yaml        # Local development profile
│       └── db/migration/
│           └── V1__init_flyway.sql       # Baseline migration (empty schema)
├── test/
│   ├── java/com/milki/mimi/
│   │   ├── MiMiApplicationTests.java     # (exists) Context load test
│   │   ├── TestcontainersConfiguration.java  # (exists) TC config
│   │   ├── TestMiMiApplication.java      # (exists) TC dev runner
│   │   └── platform/
│   │       ├── HealthEndpointIntegrationTest.java
│   │       ├── FlywayMigrationIntegrationTest.java
│   │       ├── OpenApiIntegrationTest.java
│   │       └── ErrorHandlingIntegrationTest.java
│   └── resources/
│       └── application-test.yaml         # Test profile configuration
├── docker-compose.yml                    # (exists) PostgreSQL service
├── .env.example                          # Template for local env vars
└── README.md                             # Local setup instructions
```

**Structure Decision**: The existing flat `com.milki.mimi` package
is preserved. A `platform` sub-package is introduced for
cross-cutting infrastructure concerns (error handling, security
configuration, OpenAPI configuration). This follows the modular
monolith structure defined in PROJECT_CONTEXT.md where `platform`
handles "cross-cutting technical capabilities such as configuration,
error handling, observability, and API infrastructure." Business
modules (identity, wallet, ledger, etc.) are NOT created in M1.

## Complexity Tracking

No Constitution Check violations. No complexity justifications required.
