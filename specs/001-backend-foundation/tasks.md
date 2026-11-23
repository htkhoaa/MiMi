# Tasks: M1 — Backend Foundation

**Input**: Design documents from `specs/001-backend-foundation/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Required per Constitution Principle VII (TDD: Red → Green → Refactor).

**Organization**: Tasks grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Environment configuration and project scaffolding

- [ ] T001 Create `.env.example` with documented environment variables (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) in project root
- [ ] T002 Verify `.gitignore` includes `.env` (already present — confirm, do not duplicate)
- [ ] T003 [P] Create `src/main/resources/db/migration/` directory structure for Flyway migrations

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Create baseline migration `src/main/resources/db/migration/V1__init_flyway.sql` with comment-only content (no business tables) per research.md R2
- [ ] T005 [P] Create `SecurityConfig` in `src/main/java/com/milki/mimi/platform/config/SecurityConfig.java` — configure `SecurityFilterChain` to permit all requests without authentication per research.md R1
- [ ] T006 [P] Update `src/test/java/com/milki/mimi/TestcontainersConfiguration.java` — pin PostgreSQL image to `postgres:17-alpine` instead of `postgres:latest` per research.md R6

**Checkpoint**: Foundation ready — application can start, connect to PostgreSQL, and run Flyway. User story implementation can now begin.

---

## Phase 3: User Story 1 — Application Startup and Database Connectivity (Priority: P1) 🎯 MVP

**Goal**: Application starts, connects to PostgreSQL, and uses environment-based configuration.

**Independent Test**: Start the application with a running PostgreSQL container. Verify it starts without errors and connects to the database.

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T007 [US1] Write integration test in `src/test/java/com/milki/mimi/platform/ApplicationStartupIntegrationTest.java` — verify application context loads with Testcontainers PostgreSQL, database connection is established, and Flyway migration tracking table exists

### Implementation for User Story 1

- [ ] T008 [US1] Verify existing `src/main/resources/application.yaml` configuration is correct — `ddl-auto=validate`, `flyway.enabled=true`, datasource uses `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` with defaults (already configured — confirm, extend if needed)
- [ ] T009 [US1] Run `ApplicationStartupIntegrationTest` — confirm test passes (GREEN)

**Checkpoint**: Application starts, connects to PostgreSQL, runs Flyway baseline migration. Independently testable.

---

## Phase 4: User Story 2 — Database Migration Infrastructure (Priority: P1)

**Goal**: Flyway migration pipeline is verified — initializes on empty database, is idempotent on restart, and rejects tampered migrations.

**Independent Test**: Run integration tests that verify Flyway initializes, is idempotent, and validates migration checksums.

### Tests for User Story 2

- [ ] T010 [US2] Write integration test in `src/test/java/com/milki/mimi/platform/FlywayMigrationIntegrationTest.java` — verify: (a) Flyway schema history table is created, (b) baseline migration V1 is recorded as applied with `success=true`, (c) re-running the application does not re-apply migrations

### Implementation for User Story 2

- [ ] T011 [US2] Run `FlywayMigrationIntegrationTest` — confirm tests pass (GREEN). If any test fails, fix configuration in `application.yaml` or `V1__init_flyway.sql` and re-run

**Checkpoint**: Flyway migration infrastructure verified. Migration pipeline works for future milestones.

---

## Phase 5: User Story 3 — Health and Readiness Monitoring (Priority: P1)

**Goal**: Health, liveness, and readiness endpoints return correct status reflecting database state.

**Independent Test**: Query `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` and verify correct responses per contracts/api-endpoints.md.

### Tests for User Story 3

- [ ] T012 [US3] Write integration test in `src/test/java/com/milki/mimi/platform/HealthEndpointIntegrationTest.java` — verify: (a) `GET /actuator/health` returns 200 with `"status":"UP"` and `db` component details, (b) `GET /actuator/health/liveness` returns 200, (c) `GET /actuator/health/readiness` returns 200

### Implementation for User Story 3

- [ ] T013 [US3] Configure Actuator endpoints in `src/main/resources/application.yaml` — expose health, liveness, and readiness endpoints; show health details (`management.endpoint.health.show-details`, `management.endpoint.health.probes.enabled`, `management.endpoints.web.exposure.include`) per research.md R4
- [ ] T014 [US3] Run `HealthEndpointIntegrationTest` — confirm tests pass (GREEN)

**Checkpoint**: Health and readiness endpoints operational. Database health reflected in responses.

---

## Phase 6: User Story 4 — API Documentation Access (Priority: P2)

**Goal**: Swagger UI and OpenAPI JSON spec are accessible and display available endpoints.

**Independent Test**: Navigate to Swagger UI and request the OpenAPI JSON document.

### Tests for User Story 4

- [ ] T015 [US4] Write integration test in `src/test/java/com/milki/mimi/platform/OpenApiIntegrationTest.java` — verify: (a) `GET /v3/api-docs` returns 200 with valid JSON containing OpenAPI version, (b) `GET /swagger-ui.html` returns a redirect or 200

### Implementation for User Story 4

- [ ] T016 [US4] Create `OpenApiConfig` in `src/main/java/com/milki/mimi/platform/config/OpenApiConfig.java` — configure API info metadata (title: "MiMi", version: "0.0.1", description) per research.md R5
- [ ] T017 [US4] Run `OpenApiIntegrationTest` — confirm tests pass (GREEN)

**Checkpoint**: Swagger UI and OpenAPI spec accessible. All exposed endpoints documented.

---

## Phase 7: User Story 5 — Automated Integration Testing with Isolated Database (Priority: P1)

**Goal**: Integration tests run with an isolated Testcontainers PostgreSQL instance, independent of external database.

**Independent Test**: Run the test suite with no external PostgreSQL running. Verify all tests pass.

### Tests for User Story 5

- [ ] T018 [US5] Write integration test in `src/test/java/com/milki/mimi/platform/TestcontainersIntegrationTest.java` — verify: (a) tests connect to an isolated PostgreSQL container (not `localhost:5432`), (b) the container is a PostgreSQL 17 instance, (c) Flyway migrations run against the test container

### Implementation for User Story 5

- [ ] T019 [US5] Create `src/test/resources/application-test.yaml` with test-specific configuration (disable unnecessary features, set `ddl-auto=validate`, configure structured logging for tests)
- [ ] T020 [US5] Verify existing `src/test/java/com/milki/mimi/MiMiApplicationTests.java` context-load test passes with Testcontainers (already imports `TestcontainersConfiguration` — confirm GREEN)
- [ ] T021 [US5] Run `TestcontainersIntegrationTest` — confirm tests pass (GREEN)

**Checkpoint**: All integration tests run with isolated Testcontainers PostgreSQL. No external database dependency.

---

## Phase 8: User Story 6 — Local Development Environment (Priority: P2)

**Goal**: Developer can start and stop the local PostgreSQL instance with Docker Compose and connect the application.

**Independent Test**: Run `docker compose up -d`, start the application, verify connectivity, then `docker compose down`.

### Implementation for User Story 6

- [ ] T022 [US6] Verify existing `docker-compose.yml` is correct — PostgreSQL 17 Alpine image, healthcheck, named volume `db_data`, environment variables from `.env` (already configured — confirm, do not duplicate)
- [ ] T023 [US6] Create `README.md` in project root with local setup instructions: prerequisites, `.env` setup, Docker Compose commands, application startup, verification steps, and stop/cleanup commands per quickstart.md

**Checkpoint**: Developer can follow README to set up and run the complete local environment.

---

## Phase 9: User Story 7 — Consistent Error Responses (Priority: P2)

**Goal**: All error conditions return structured RFC 9457 ProblemDetail responses with no internal details exposed.

**Independent Test**: Send requests to invalid endpoints and verify structured error responses per contracts/api-endpoints.md.

### Tests for User Story 7

- [ ] T024 [US7] Write integration test in `src/test/java/com/milki/mimi/platform/ErrorHandlingIntegrationTest.java` — verify: (a) `GET /api/v1/nonexistent` returns 404 with ProblemDetail JSON (`type`, `title`, `status`, `detail`), (b) `DELETE /actuator/health` returns 405 with ProblemDetail JSON, (c) no response body contains stack traces, SQL, or class names

### Implementation for User Story 7

- [ ] T025 [US7] Create `GlobalExceptionHandler` in `src/main/java/com/milki/mimi/platform/api/GlobalExceptionHandler.java` — implement `@ControllerAdvice` with handlers for `NoHandlerFoundException` (404), `HttpRequestMethodNotSupportedException` (405), and generic `Exception` (500), all returning `ProblemDetail` per research.md R3
- [ ] T026 [US7] Configure Spring to throw `NoHandlerFoundException` for unknown endpoints instead of default 404 page — add `spring.mvc.throw-exception-if-no-handler-found=true` and `spring.web.resources.add-mappings=false` to `src/main/resources/application.yaml` (verify compatibility with Swagger UI static resources)
- [ ] T027 [US7] Run `ErrorHandlingIntegrationTest` — confirm tests pass (GREEN)

**Checkpoint**: All error responses follow RFC 9457 ProblemDetail format. No internal details leak.

---

## Phase 10: User Story 8 — Basic Application Observability (Priority: P3)

**Goal**: Application produces structured log output with timestamps, severity, and source identifiers.

**Independent Test**: Start the application, examine log output format, verify no sensitive data in logs.

### Implementation for User Story 8

- [ ] T028 [US8] Review and configure logging in `src/main/resources/application.yaml` — verify default structured log format includes timestamp, level, thread, logger, and message. Suppress noisy framework loggers if needed
- [ ] T029 [US8] Verify log output from previous integration test runs does not contain passwords, tokens, or database credentials from configuration

**Checkpoint**: Structured logging operational. Sensitive data excluded from logs.

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Final validation across all user stories

- [ ] T030 [P] Run full test suite (`./mvnw test`) — verify all integration tests pass together
- [ ] T031 [P] Run quickstart.md validation checklist end-to-end (steps 1–9)
- [ ] T032 Verify Swagger UI displays all exposed endpoints (health, liveness, readiness) with correct response schemas
- [ ] T033 Verify `.env.example` documents all required and optional environment variables with descriptions
- [ ] T034 Final review: confirm no implementation details in error responses, no sensitive data in logs, no speculative business schema

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS all user stories
- **Phase 3 (US1)**: Depends on Phase 2 — MVP
- **Phase 4 (US2)**: Depends on Phase 2 — can run in parallel with US1
- **Phase 5 (US3)**: Depends on Phase 2 — can run in parallel with US1, US2
- **Phase 6 (US4)**: Depends on Phase 2 — can run in parallel with US1–US3
- **Phase 7 (US5)**: Depends on Phase 2 — can run in parallel with US1–US4
- **Phase 8 (US6)**: Depends on Phase 1 (`.env.example`) — can run in parallel with Phase 2+
- **Phase 9 (US7)**: Depends on Phase 2 (SecurityConfig) — can run in parallel with US1–US5
- **Phase 10 (US8)**: Depends on Phase 2 — can run in parallel with all stories
- **Phase 11 (Polish)**: Depends on all user stories complete

### User Story Dependencies

- **US1 (Startup)**: Phase 2 only — no other story dependencies
- **US2 (Flyway)**: Phase 2 only — shares migration file with US1 but no code dependency
- **US3 (Health)**: Phase 2 only — independent
- **US4 (Swagger)**: Phase 2 only — independent
- **US5 (Testcontainers)**: Phase 2 only — independent
- **US6 (Docker Compose)**: Phase 1 (`.env.example`) — independent of other stories
- **US7 (Error Handling)**: Phase 2 (SecurityConfig) — independent
- **US8 (Logging)**: Phase 2 only — independent

### Within Each User Story

- Tests MUST be written and FAIL before implementation (RED)
- Implementation makes tests pass (GREEN)
- Refactor if needed (REFACTOR)
- Story complete before moving to next priority

### Parallel Opportunities

```text
After Phase 2 completes, ALL user stories can proceed in parallel:

                    ┌── US1 (Startup)
                    ├── US2 (Flyway)
Phase 1 → Phase 2 ─┼── US3 (Health)
    │               ├── US4 (Swagger)
    │               ├── US5 (Testcontainers)
    │               ├── US7 (Error Handling)
    │               └── US8 (Logging)
    │
    └── US6 (Docker Compose / README)
```

---

## Parallel Example: User Story 3 (Health Endpoints)

```text
# Write test FIRST (RED):
Task: T012 — Write HealthEndpointIntegrationTest

# Implement (GREEN):
Task: T013 — Configure Actuator endpoints in application.yaml

# Verify (confirm GREEN):
Task: T014 — Run HealthEndpointIntegrationTest
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T003)
2. Complete Phase 2: Foundational (T004–T006)
3. Complete Phase 3: US1 — Application Startup (T007–T009)
4. **STOP and VALIDATE**: Application starts, connects to PostgreSQL, Flyway runs
5. Continue with remaining P1 stories (US2, US3, US5) then P2 (US4, US6, US7) then P3 (US8)

### Recommended Execution Order (Single Developer)

1. Phase 1 → Phase 2 → US1 → US2 → US3 → US5 (all P1 stories)
2. US7 → US4 → US6 (P2 stories — error handling before docs/setup)
3. US8 (P3 — logging)
4. Phase 11 — Polish

### Incremental Delivery

Each user story adds independently verifiable capability:
- After US1: Application starts ✓
- After US2: Migrations verified ✓
- After US3: Health monitoring works ✓
- After US5: Tests are self-contained ✓
- After US7: Error responses are structured ✓
- After US4: API documentation accessible ✓
- After US6: Local setup documented ✓
- After US8: Logging is structured ✓

---

## Notes

- All test tasks follow TDD per Constitution Principle VII
- No business-domain code or schema is created (Constitution Principle VI)
- Existing files (`MiMiApplication.java`, `TestcontainersConfiguration.java`, `application.yaml`, `docker-compose.yml`) are verified and extended, not replaced
- `platform` sub-package contains all M1 infrastructure code per plan.md structure decision
- data-model.md has no constrained business fields in M1 — no field citations required
