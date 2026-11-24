# MiMi — Project Context

## 1. Project Overview

**MiMi** is a backend-only learning project that builds a simulated digital-wallet platform. The purpose is to master backend engineering, database design, transaction processing, security, testing, and distributed-systems concepts through a realistic but non-production financial domain.

The project is educational. It must never move real money, connect to real bank rails, or be presented as a production-ready financial service.

The repository has previously used the application/artifact name `MilkiPay` and the Java base package `com.milki.milkipay`. Preserve the existing identifiers unless the developer explicitly approves a rename.

## 2. Product Scope

### In scope

- REST APIs only; no web, Flutter, or mobile UI.
- Swagger UI / OpenAPI for manual API exploration and testing.
- Customer registration, authentication, and profile management.
- A wallet provisioned for each customer in the initial product scope.
- Simulated deposits and internal person-to-person transfers.
- Immutable financial ledger and transaction history.
- Merchant profiles, merchant payment requests, QR payloads, and simulated payments.
- Automated tests, local infrastructure, observability, and deployment automation.

### Explicitly out of scope

- Real deposits, withdrawals, bank transfers, or payment-network integrations.
- Real customer funds, real merchant settlement, or production financial operations.
- Frontend/mobile application development.
- Lending, credit, investments, insurance, bill-pay, vouchers, loyalty, or other super-app utilities unless separately approved.
- Production regulatory, licensing, or compliance claims.

All balances and payment flows are simulated test data.

## 3. Technology Baseline

Use the versions already committed in the repository and verify compatibility before changing them. Do not upgrade or downgrade framework versions as an incidental part of a feature.

- Language: Java 21.
- Framework: Spring Boot (the stable version selected in `pom.xml` / Spring Initializr).
- Build: Maven Wrapper; use the wrapper rather than relying on a machine-global Maven installation.
- Database: PostgreSQL (the local Docker Compose image is the repository's declared version).
- Persistence: Spring Data JPA where appropriate; use explicit SQL when it is clearer or required for locking/query behavior.
- Schema migrations: Flyway. Hibernate schema generation must not mutate the schema automatically; use validation outside isolated tests.
- API: Spring MVC REST, OpenAPI, and Swagger UI.
- Testing: JUnit 5, Mockito where useful, and Testcontainers with PostgreSQL for database integration tests.
- Local infrastructure: Docker Compose.
- Development workflow: Git, GitHub, GitHub Actions (when the deployment milestone is reached).
- AI engineering tools: Spec Kit for specification artifacts and Superpowers for engineering workflow, operated through the configured coding agent.

Use dependency versions managed by the Spring Boot dependency-management mechanism or a compatible BOM where available. Select Springdoc and Testcontainers versions compatible with the actual Spring Boot version in the repository. Do not copy version numbers from old examples without checking compatibility.

## 4. Architectural Direction

Use a **Modular Monolith**. Keep one deployable Spring Boot application and one PostgreSQL database initially, while enforcing clear domain/module boundaries in code. Do not introduce microservices merely for architectural appearance.

The intended business modules are:

| Module | Responsibility |
|---|---|
| `identity` | Customer identity and profile data |
| `access` | Authentication, credentials, sessions, roles, and authorization |
| `wallet` | Wallet lifecycle, wallet ownership, and balance query/projection |
| `ledger` | Financial accounts, immutable postings, and balance/reconciliation rules |
| `funding` | Simulated deposit/funding operations |
| `transfer` | Internal person-to-person transfer use cases |
| `merchant` | Merchant profile and merchant account lifecycle |
| `payment` | Merchant payment requests, QR payloads, payment processing, and payment status |
| `notification` | Non-critical user/merchant notifications |
| `platform` | Cross-cutting technical capabilities such as configuration, error handling, observability, and API infrastructure; it must not become a dumping ground for business logic |

A module owns its domain logic and persistence model. Other modules must use explicit application-level interfaces or public contracts instead of reaching into another module's repositories or internal entities. Avoid circular dependencies. Keep shared code small and technical; do not put domain rules in `shared` or `platform` simply to make them globally accessible.

A practical package shape for a module, when it has enough complexity, is:

```text
<module>/
  api/             # REST controllers and API DTOs
  application/     # use cases and orchestration
  domain/          # entities, value objects, policies, domain rules
  infrastructure/  # persistence and external adapters
```

Do not create empty packages for every planned module at bootstrap. Introduce a module when its first approved feature is implemented. Preserve the current root package unless a rename is explicitly approved.

## 5. Financial and Data-Integrity Invariants

These rules are mandatory for all financial features, including simulated ones.

### 5.1 Money representation

- VND amounts are non-negative integer VND values represented as Java `long` and PostgreSQL `BIGINT`.
- Never use `float` or `double` for money.
- Validate amount range and business constraints at the API and domain boundaries.
- Currency must be explicit in persisted financial records, even if the first release only supports VND.

### 5.2 Ledger

- The ledger is the authoritative financial record; a wallet balance field is a query-friendly projection, not an independent source of truth.
- Use double-entry bookkeeping: each posted financial movement must be balanced, with total debits equal to total credits for the transaction in the relevant currency/accounting scope.
- Posted ledger entries are append-only. Do not edit or delete posted entries to correct history; use a compensating/reversal transaction with its own postings.
- The exact chart of accounts, ledger schema, and posting rules must be defined in the relevant feature specification and reviewed before implementation. Do not invent accounting semantics in code.
- If a balance projection is maintained, update it atomically with ledger postings in the same database transaction and provide a reconciliation strategy.

### 5.3 Transaction integrity and concurrency

- Financial state changes must be atomic within an explicitly designed database transaction.
- Define and test transaction boundaries, rollback behavior, and isolation/locking requirements for each financial use case.
- Prevent overspending and double spending under concurrent requests. Choose and document an appropriate locking or concurrency-control strategy.
- When locking multiple wallet/account rows, use deterministic lock ordering to reduce deadlock risk.
- Do not assume `@Transactional` alone solves concurrency or idempotency.
- Do not perform slow network calls or non-transactional external side effects while holding financial database locks.

### 5.4 Idempotency

- State-changing financial commands must support safe retries using a client-supplied idempotency key or an explicitly justified equivalent.
- PostgreSQL constraints and durable records are the final correctness mechanism for uniqueness and duplicate prevention.
- Redis may later accelerate lookup, but must never be the only protection against duplicate financial effects.
- Define behavior for same key/same request, same key/different request, concurrent duplicate requests, in-progress requests, and retention/expiry.

### 5.5 Events and asynchronous work

- Core ledger posting and the corresponding transaction state must commit atomically.
- When reliable asynchronous publication is introduced, use a transactional outbox or another explicitly justified consistency pattern.
- Notifications and analytics are secondary effects; their failure must not silently reverse or duplicate a completed financial posting.

## 6. Security Baseline

- Hash passwords with an established password-hashing algorithm through Spring Security; never store plaintext passwords.
- Enforce authentication and authorization at the server boundary. Derive the acting user from the authenticated principal, not from a caller-controlled user ID in the request body.
- Validate all input and enforce ownership/access checks on every user- or merchant-scoped resource.
- Never log passwords, access/refresh tokens, secrets, authentication codes, PINs, or full sensitive payment credentials.
- Do not add a transaction PIN, biometric flow, refresh-token mechanism, or other security feature unless its threat model and requirements are specified and approved.
- Keep secrets outside source control. Use local environment variables for development and an appropriate secret store for deployed environments.
- Use HTTPS in deployed environments. Do not expose database, broker, or management ports publicly without an explicit security design.
- Treat QR payloads and client-provided payment details as untrusted input. The backend must load and validate the authoritative payment request before processing it.

## 7. API and Error-Handling Conventions

- Prefix APIs with `/api/v1`.
- Use resource-oriented REST endpoints and appropriate HTTP methods/status codes.
- Separate API request/response DTOs from persistence entities and domain models.
- Publish the API contract through OpenAPI and keep Swagger UI available in development/test environments.
- Use a consistent error response format (prefer Spring `ProblemDetail` / RFC 9457 where appropriate).
- Validate requests at the boundary and return stable, documented error codes/messages without leaking internals.
- Add pagination and stable sorting to potentially unbounded collection endpoints.
- Do not expose internal database IDs or sensitive attributes unless the API contract requires them.

## 8. Database and Migration Conventions

- PostgreSQL is the transactional source of truth.
- Flyway is the schema-change mechanism. Versioned migrations are immutable after they have been applied/shared; add a new migration to amend the schema.
- Use explicit database constraints for invariants that must survive concurrent application requests (unique keys, foreign keys, check constraints where appropriate).
- Use indexes based on real query and constraint requirements; document why an index is needed.
- Avoid `ddl-auto=update` in shared environments. Prefer schema validation.
- Test migrations against PostgreSQL, not only an in-memory database.
- Do not create speculative business tables during the Foundation milestone. Add schema only when an approved feature requires it.

## 9. Testing and Quality Gates

Use the testing level that matches the behavior:

- Unit tests: domain rules, validation, calculations, and application behavior with dependencies isolated where useful.
- Integration tests: Spring wiring, repositories, migrations, transactions, constraints, and locking against PostgreSQL Testcontainers.
- API tests: authentication, authorization, request validation, error contracts, and end-to-end user journeys.
- Concurrency tests: competing transfers, overspending prevention, deterministic locking, and duplicate requests where relevant.

For financial features, test at least:

- Successful posting and balanced ledger entries.
- Insufficient balance and inactive/frozen account behavior.
- Atomic rollback when any posting or update fails.
- Concurrent requests against the same wallet/account.
- Same idempotency key repeated sequentially and concurrently.
- Reconciliation between ledger-derived balance and any stored balance projection.

A feature is not complete merely because it compiles. Its acceptance criteria and automated tests must pass, and verification evidence must be reported accurately.

## 10. Milestones

Milestones are delivered sequentially. A later module may be discussed or documented, but must not be implemented early without explicit developer approval.

### M1 — Foundation

- Spring Boot application and Maven Wrapper build.
- Configuration profiles and safe local configuration.
- PostgreSQL via Docker Compose.
- Flyway integration and a verified migration path (no speculative business schema).
- Health/readiness endpoints and baseline error handling.
- OpenAPI/Swagger UI.
- JUnit and PostgreSQL Testcontainers baseline.
- Local setup instructions and repository hygiene.

**Exit criteria:** clean build; application starts; PostgreSQL connectivity and Flyway are verified; health endpoints and Swagger work; baseline tests pass.

### M2 — Identity, Access, and User

- Customer registration and profile.
- Password hashing and login.
- Authentication/authorization (JWT only after its design is specified).
- User status and access rules.
- Provision one wallet for a customer if wallet creation is included in the approved user-onboarding specification; no financial balance operations yet.

### M3 — Wallet Core

- Wallet details and balance query.
- Ledger model and posting invariants.
- Simulated deposit/funding.
- Internal P2P transfer.
- Transaction history and details.
- Durable idempotency and concurrency protection for financial commands.
- Balance reconciliation.

### M4 — Merchant Payment

- Merchant profile and account lifecycle.
- Merchant payment request.
- QR payload generation/decoding contract (no camera/UI requirement).
- Payment details, confirmation, cancellation/expiry rules, and simulated merchant payment.
- Merchant and customer transaction views.
- Refunds only if separately specified and approved.

### M5 — Advanced Backend

- Redis for suitable non-authoritative caching/rate limiting.
- Message broker and event-driven secondary workflows.
- Transactional outbox and reliable event consumption where required.
- Notification processing.
- Enhanced idempotency operations, observability, metrics, and tracing.

Do not defer basic financial idempotency or concurrency protection from M3 to M5; M5 hardens and extends the platform capabilities.

### M6 — Deployment

- Production-oriented container build and deployment configuration.
- CI/CD pipeline and automated test gates.
- Secrets/configuration management.
- HTTPS, health monitoring, logs, metrics, database migration strategy, and backup/restore considerations.
- Deploy the backend and database using an explicitly selected environment.

## 11. Spec-Driven Development Workflow

Use **Spec Kit** as the source of truth for what is being built:

1. Constitution defines enduring project principles.
2. Specify defines the feature's user value, scope, requirements, and acceptance criteria.
3. Clarify resolves material ambiguity.
4. Plan defines technical design and module/data/API impacts.
5. Tasks decomposes the approved design into dependency-ordered work.
6. Analyze checks consistency across specification, plan, and tasks.
7. Converge compares implementation with approved artifacts and identifies remaining gaps.

Use **Superpowers** for how approved work is engineered:

- Brainstorming when requirements/design are genuinely open.
- Writing-plans only to expand approved Spec Kit tasks into small executable steps; do not create a competing requirements/task system.
- Test-driven development for implementation.
- Systematic debugging for failures.
- Code review and verification before completion.
- Git worktrees/branch-finishing where useful.

Do not run Spec Kit's implementation executor and Superpowers implementation workflow against the same tasks at the same time. In this project, Superpowers is the implementation workflow; Spec Kit owns requirements, design, and task scope.

## 12. Definition of Done

A feature is complete only when:

- Its specification and acceptance criteria are approved.
- Its implementation stays within the approved scope and module boundaries.
- Relevant unit, integration, and API tests pass.
- Database migrations and constraints are tested where applicable.
- Security, data integrity, concurrency, and idempotency concerns are addressed where relevant.
- OpenAPI documentation matches the implemented API.
- No secrets or sensitive values are exposed in logs or repository files.
- Code review findings are resolved or explicitly accepted by the developer.
- The final report lists changed files, tests/commands run, results, and known limitations.
