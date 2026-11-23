<!--
SYNC IMPACT REPORT (remove before committing)

Version change: N/A (initial) → 1.0.0
Added sections:
  - Core Principles:
    - I. Architecture and Maintainability
    - II. Financial Data Integrity
    - III. Concurrency and Transaction Safety
    - IV. Idempotency
    - V. Security
    - VI. API and Database Standards
    - VII. Testing and Quality
    - VIII. Observability and Operations
    - IX. Learning-First Development
  - Technology Baseline
  - Development Workflow
  - Governance
Removed sections: none (initial creation)
Modified principles: none (initial creation)
Follow-up TODOs: none
-->

# MiMi Constitution

## Core Principles

### I. Architecture and Maintainability

MiMi MUST use a Modular Monolith architecture deployed as a single
Spring Boot application backed by a single PostgreSQL database.

- Domain module boundaries MUST be explicit. Each module owns its
  domain logic, persistence model, and internal entities.
- Cross-module interaction MUST use public application-level
  interfaces or published contracts. Direct access to another
  module's repositories or internal entities is prohibited.
- Circular dependencies between modules are prohibited.
- Code MUST separate API, application, domain, and infrastructure
  responsibilities within each module.
- Microservices, message brokers, and distributed transactions MUST
  NOT be introduced without an approved architecture specification.
- Premature abstractions and speculative generalization MUST be
  avoided. Introduce complexity only when a concrete, approved
  requirement demands it.
- SOLID principles MUST be applied where they measurably improve
  maintainability, not as ceremony.

### II. Financial Data Integrity

PostgreSQL is the transactional source of truth for all financial
state. Every financial operation — even simulated — MUST satisfy
these invariants.

- VND amounts MUST be represented as integer values: `long` in Java,
  `BIGINT` in PostgreSQL. Floating-point types (`float`, `double`,
  `DECIMAL` with scale for fractional units) MUST NOT be used for
  money.
- The ledger MUST use immutable double-entry bookkeeping. Every
  completed financial transaction MUST have balanced debit and
  credit entries (total debits equal total credits).
- Posted ledger entries are append-only. To correct history, a
  compensating or reversal transaction with its own postings MUST be
  created. Direct modification or deletion of posted ledger records
  is prohibited.
- The ledger is authoritative. Any wallet balance field is a
  projection that MUST be updated atomically with its corresponding
  ledger postings within the same database transaction.
- Financial state changes MUST be atomic and auditable. Database
  constraints MUST enforce critical invariants; application-only
  checks are insufficient.

### III. Concurrency and Transaction Safety

All financial operations MUST be protected against race conditions,
double spending, and overdrafts under concurrent access.

- Each financial use case MUST define explicit transaction
  boundaries, isolation level, and locking strategy.
- When locking multiple wallet or account rows, locks MUST be
  acquired in deterministic order to prevent deadlocks.
- Database failures MUST NOT leave partial financial state.
  Transactions MUST roll back atomically on any failure within the
  financial operation.
- `@Transactional` alone does not guarantee concurrency safety.
  Locking, isolation, and ordering MUST be explicitly designed and
  tested for each financial use case.
- Slow network calls, notification delivery, or non-transactional
  side effects MUST NOT execute while holding financial database
  locks.

### IV. Idempotency

All state-changing financial commands MUST support safe retries
without creating duplicate financial effects.

- Idempotency MUST be enforced through durable uniqueness
  constraints at the PostgreSQL level (unique indexes, constraint
  checks on idempotency keys).
- Redis MAY be used to accelerate idempotency-key lookup, but MUST
  NOT be the sole mechanism preventing duplicate financial effects.
  PostgreSQL constraints are the correctness boundary.
- Defined behavior is REQUIRED for: same key with same payload, same
  key with different payload, concurrent duplicate requests, and
  retry after timeout or partial failure.

### V. Security

All external inputs are untrusted. The backend MUST enforce security
independently of any client.

- Passwords MUST be hashed using a secure password-hashing algorithm
  (e.g., bcrypt via Spring Security). Plaintext password storage is
  prohibited.
- Authentication and authorization MUST be enforced at the server
  boundary. The acting user MUST be derived from the authenticated
  principal, never from a caller-controlled field.
- All inputs MUST be validated at the API boundary. Ownership and
  access checks MUST be applied to every user- or merchant-scoped
  resource.
- Credentials, tokens, passwords, PINs, authentication codes, and
  sensitive financial data MUST NOT appear in logs, error responses,
  or repository files.
- Least-privilege principles MUST be applied to roles, permissions,
  and database access.
- All financial operations are simulated. Real funds, real bank
  integrations, and production payment rails are prohibited.

### VI. API and Database Standards

Backend capabilities MUST be exposed through well-documented REST
APIs and managed through versioned database migrations.

- REST APIs MUST use the `/api/v1` prefix, resource-oriented
  endpoints, and appropriate HTTP methods and status codes.
- APIs MUST be documented with OpenAPI/Swagger. Swagger UI MUST be
  available in development and test environments. Documentation MUST
  match actual API behavior.
- Error responses MUST use a consistent structured format (Spring
  `ProblemDetail` / RFC 9457 preferred). Stack traces, SQL details,
  and internal implementation details MUST NOT be exposed in API
  error responses.
- API request/response DTOs MUST be separate from persistence
  entities and domain models.
- Potentially unbounded collection endpoints MUST support pagination
  and stable sorting.
- PostgreSQL schema changes MUST be managed through Flyway versioned
  migrations. Applied migrations are immutable; schema amendments
  require a new migration file.
- Hibernate `ddl-auto=update` or `ddl-auto=create` MUST NOT be used
  in shared or production-like environments. Schema validation is
  the permitted mode outside isolated test contexts.
- Speculative tables and migrations MUST NOT be created before the
  relevant feature specification is approved.

### VII. Testing and Quality

Test-Driven Development (Red → Green → Refactor) is the required
development methodology.

- Unit tests are REQUIRED for domain rules, value-object behavior,
  and application-layer business logic.
- Integration tests against PostgreSQL (via Testcontainers) are
  REQUIRED for persistence, migrations, database constraints,
  transactions, and locking behavior.
- API tests are REQUIRED for authentication, authorization, input
  validation, and error-response contracts.
- Financial features MUST additionally test: balanced ledger
  postings, insufficient-balance rejection, atomic rollback on
  failure, concurrent requests against the same wallet/account,
  sequential and concurrent duplicate idempotency keys, and balance
  reconciliation.
- H2 or other in-memory databases MUST NOT substitute for
  PostgreSQL when correctness depends on PostgreSQL-specific
  semantics (locking, isolation, constraint behavior).
- Completion MUST NOT be claimed unless relevant tests have been
  executed and their results observed and reported.

### VIII. Observability and Operations

The application MUST provide sufficient operational visibility
without exposing sensitive information.

- Logging MUST use structured formats suitable for automated
  parsing and analysis.
- Health and readiness endpoints MUST be provided via Spring Boot
  Actuator.
- Application errors and performance characteristics MUST be
  monitorable through logs, metrics, or health indicators as the
  project matures.
- Logs and telemetry MUST NOT contain passwords, tokens, secrets,
  PINs, or sensitive financial data.

### IX. Learning-First Development

MiMi is an educational project. The developer's learning and
decision-making authority take precedence over speed of delivery.

- The developer is the final decision-maker on all design,
  architecture, and implementation choices.
- AI assistants MUST act as technical mentors: explain engineering
  trade-offs, teach relevant concepts, and guide the developer
  toward informed decisions.
- Application code, tests, migrations, and feature specifications
  MUST NOT be created or modified without the developer's explicit
  approval.
- Approved specifications, plans, and task scopes MUST NOT be
  bypassed or silently redefined during implementation.
- Implementation MUST proceed in small, reviewable, test-driven
  increments — not as complete features delivered in a single pass.

## Technology Baseline

The following technology versions are established in the repository
and MUST be used as-is. Version changes require explicit
justification and developer approval.

| Component          | Technology / Version                            |
|--------------------|-------------------------------------------------|
| Language           | Java 21                                         |
| Framework          | Spring Boot 4.2.0                               |
| Build              | Maven Wrapper                                   |
| Database           | PostgreSQL (Docker Compose)                     |
| Persistence        | Spring Data JPA                                 |
| Schema Migrations  | Flyway (`flyway-core`, `flyway-database-postgresql`) |
| API Documentation  | Springdoc OpenAPI 3.2.1 (`springdoc-openapi-starter-webmvc-ui`) |
| Security           | Spring Security                                 |
| Validation         | Spring Boot Starter Validation                  |
| Monitoring         | Spring Boot Actuator                            |
| Testing            | JUnit 5, Spring Boot Test, Testcontainers (PostgreSQL) |
| Code Generation    | Lombok                                          |
| Local Infra        | Docker Compose                                  |

Dependency versions managed by the Spring Boot BOM MUST NOT be
overridden without an explicit compatibility justification.

## Development Workflow

MiMi uses a spec-driven development process with two complementary
tool systems.

**Spec Kit** owns WHAT and WHY:
- Constitution defines enduring project principles.
- Specifications define feature scope, requirements, and acceptance
  criteria.
- Plans define technical design and module/data/API impacts.
- Tasks decompose approved designs into dependency-ordered work.
- Analysis and convergence verify artifact consistency and
  implementation completeness.

**Superpowers** owns HOW approved work is engineered:
- Test-driven development for implementation.
- Systematic debugging for failures.
- Code review and verification before completion.

Spec Kit artifacts are the source of truth for feature scope.
Superpowers MUST NOT create competing requirement or task
definitions. Superpowers MAY expand approved Spec Kit tasks into
concrete implementation steps but MUST NOT redefine product
requirements or add unapproved scope.

Spec Kit implementation executor and Superpowers implementation
workflow MUST NOT run concurrently on the same tasks. This project
uses Superpowers for implementation execution.

## Governance

This Constitution is the highest-level engineering standard for the
MiMi project. All feature specifications, architecture plans, task
definitions, and implementation work MUST comply with its
principles.

- **Precedence**: When a proposed change conflicts with a
  Constitution principle, the conflict MUST be reported to the
  developer with the specific principle cited. The developer decides
  whether to proceed, amend the approach, or amend the Constitution.
- **Amendment procedure**: Principles MAY be amended only when there
  is a justified architectural or educational reason. Amendments
  MUST be documented with a version increment, rationale, and the
  date of change.
- **Versioning**: The Constitution follows semantic versioning:
  - MAJOR: Backward-incompatible principle removals or
    redefinitions.
  - MINOR: New principles added or existing guidance materially
    expanded.
  - PATCH: Clarifications, wording improvements, or non-semantic
    refinements.
- **Compliance**: All specification, plan, and implementation
  reviews MUST verify compliance with applicable Constitution
  principles. Non-compliance MUST be flagged, not silently accepted.
- **Conciseness**: Principles MUST remain declarative, measurable,
  and enforceable. Vague guidance MUST be refined or removed.
- **Guidance file**: `AGENTS.md` provides runtime development rules
  that complement this Constitution. The Constitution takes
  precedence when there is a conflict.

**Version**: 1.0.0 | **Ratified**: 2026-11-24 | **Last Amended**: 2026-11-24
