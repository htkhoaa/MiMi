# Research: M1 — Backend Foundation

**Date**: 2026-11-24 | **Feature**: [spec.md](spec.md)

No NEEDS CLARIFICATION items exist in the Technical Context. All
technology choices, versions, and constraints are established by
the existing repository (`pom.xml`, `application.yaml`,
`docker-compose.yml`) and the Constitution's Technology Baseline.

The following research documents decisions derived from the existing
repository state and project conventions.

---

## R1: Spring Security Configuration for M1

**Decision**: Configure Spring Security to permit all requests
without authentication in M1.

**Rationale**: Spring Security is on the classpath
(`spring-boot-starter-security`) and auto-configures default
form-login with a generated password. M1 explicitly excludes
authentication (spec Out of Scope). A `SecurityFilterChain` bean
that permits all requests is needed to prevent default security
from blocking health, Swagger, and actuator endpoints.

**Alternatives considered**:
- Remove `spring-boot-starter-security` from pom.xml in M1 → Rejected:
  it is already committed and will be needed in M2. Removing and
  re-adding creates unnecessary churn.
- Use `spring.security.user` properties to set a known password →
  Rejected: still requires authentication, contradicts M1 scope.

---

## R2: Flyway Baseline Migration Strategy

**Decision**: Create a single `V1__init_flyway.sql` migration that
contains only a SQL comment (no business tables). This establishes
Flyway's migration tracking infrastructure without creating
speculative schema.

**Rationale**: The Constitution (Principle VI) and PROJECT_CONTEXT.md
(Section 8) prohibit speculative tables before the relevant feature
specification is approved. M1's purpose is to verify the migration
pipeline works, not to create business schema. Flyway needs at
least one migration file to initialize its `flyway_schema_history`
tracking table.

**Alternatives considered**:
- No migration file at all (empty `db/migration/` directory) →
  Rejected: Flyway reports a warning about no migrations found.
  While it still creates its history table, the warning can confuse
  verification.
- Create placeholder business tables → Rejected: violates
  Constitution Principle VI ("Speculative tables and migrations
  MUST NOT be created before the relevant feature specification is
  approved").

---

## R3: Structured Error Response Format

**Decision**: Use Spring's `ProblemDetail` (RFC 9457) as the
standard error response format. Implement a global exception
handler that catches common exceptions and returns `ProblemDetail`
responses.

**Rationale**: The Constitution (Principle VI) specifies "Spring
`ProblemDetail` / RFC 9457 preferred." PROJECT_CONTEXT.md Section 7
confirms "prefer Spring `ProblemDetail` / RFC 9457 where
appropriate." Spring Boot 4.2.0 has native support for RFC 9457
via `ProblemDetail` and `ErrorResponse`.

**Alternatives considered**:
- Custom error DTO → Rejected: Constitution explicitly prefers
  ProblemDetail. Custom format would need justification.
- `@ControllerAdvice` returning `Map<String, Object>` → Rejected:
  not structured, not RFC-compliant, not type-safe.

---

## R4: Actuator Health and Readiness Endpoints

**Decision**: Use Spring Boot Actuator's built-in health endpoint
(`/actuator/health`) with health groups for liveness and readiness.
Expose health details to show database connectivity status.

**Rationale**: Spring Boot Actuator is already on the classpath
(`spring-boot-starter-actuator`). It provides built-in health
indicators for database connectivity, Flyway migration status,
and disk space. Health groups (`/actuator/health/liveness` and
`/actuator/health/readiness`) map to Kubernetes-style probes.

**Alternatives considered**:
- Custom health endpoint under `/api/v1/health` → Rejected:
  Actuator health is the established convention; duplicating it
  under the API prefix adds unnecessary complexity.
- Expose all actuator endpoints → Rejected: least-privilege
  principle; expose only health-related endpoints in M1.

---

## R5: OpenAPI/Swagger Configuration

**Decision**: Use Springdoc OpenAPI 3.2.1 with default
configuration, customizing only the API info metadata (title,
version, description). Swagger UI will be accessible at the
default `/swagger-ui.html` path.

**Rationale**: Springdoc is already in `pom.xml` at version 3.2.1.
It auto-scans controllers and generates OpenAPI 3.x specifications.
Minimal configuration is needed for M1 since only actuator
endpoints and potentially the error handler are present.

**Alternatives considered**:
- Manually write OpenAPI YAML → Rejected: Springdoc generates it
  automatically. Manual maintenance is error-prone.
- Restrict Swagger UI to specific profiles → Deferred to M5/M6
  when production deployment is considered.

---

## R6: Testcontainers Integration Approach

**Decision**: Use the existing `TestcontainersConfiguration` class
with `@ServiceConnection` annotation. Pin the PostgreSQL
Testcontainers image to `postgres:17-alpine` to match the Docker
Compose development image.

**Rationale**: The repository already has a `TestcontainersConfiguration`
class using `@ServiceConnection`. Currently it uses `postgres:latest`
which may drift from the development image. Pinning to
`postgres:17-alpine` ensures test and development environments use
the same PostgreSQL version, as recommended by PROJECT_CONTEXT.md.

**Alternatives considered**:
- Keep `postgres:latest` → Rejected: version drift between test
  and development could mask PostgreSQL-version-specific behavior
  differences.
- Use `@DynamicPropertySource` instead of `@ServiceConnection` →
  Rejected: `@ServiceConnection` is simpler and is the Spring Boot
  recommended approach (available since Spring Boot 3.1).

---

## R7: Environment-Based Configuration Strategy

**Decision**: Use Spring profiles (`local`, `test`) with
environment-variable-backed properties. Provide a `.env.example`
file documenting required variables. The existing `application.yaml`
already uses `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` with
defaults.

**Rationale**: The existing `application.yaml` already implements
environment-variable configuration with defaults. The `.env` file
(gitignored) holds actual credentials. A `.env.example` template
documents required variables without exposing secrets.

**Alternatives considered**:
- Spring Cloud Config → Rejected: unnecessary complexity for local
  development. Deferred to M5/M6.
- Hardcoded credentials in `application.yaml` → Rejected: violates
  Constitution Principle V (secrets outside source control).

---

## R8: Structured Logging Approach

**Decision**: Use the default Spring Boot logging configuration
(Logback) with its default structured format. Spring Boot 4.x
supports structured logging out of the box. No additional logging
library is needed for M1.

**Rationale**: Spring Boot 4.2.0 includes built-in structured
logging support. The default console output includes timestamp,
log level, thread name, logger name, and message. Additional
structured output formats (JSON) can be enabled via configuration
when needed in later milestones.

**Alternatives considered**:
- Add Logstash Logback encoder for JSON logging → Deferred: not
  needed for M1 local development. Can be introduced in M5 when
  observability is expanded.
- Add Log4j2 → Rejected: Spring Boot defaults to Logback; switching
  adds unnecessary complexity and risk.
