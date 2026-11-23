# Quickstart Validation Guide: M1 — Backend Foundation

**Date**: 2026-11-24 | **Feature**: [spec.md](spec.md)

## Prerequisites

- Java 21 installed (`java --version`)
- Container runtime installed and running (Docker Desktop, Podman,
  or compatible — `docker info` should succeed)
- Repository cloned and working directory is the project root

## 1. Start Local Development Environment

### 1a. Create environment file

Copy the example environment file and adjust if needed:

```bash
cp .env.example .env
```

Default values work for local development without modification.

### 1b. Start PostgreSQL

```bash
docker compose up -d
```

**Expected outcome**: PostgreSQL container `mimi-db` starts and
reports healthy within 30 seconds.

**Verify**:

```bash
docker compose ps
```

Should show `mimi-db` with status `Up (healthy)`.

## 2. Start the Application

```bash
./mvnw spring-boot:run
```

**Expected outcome**: Application starts without errors, connects
to PostgreSQL, runs Flyway migrations, and logs a startup message
including the port number (default: 8080).

**Verify** (from a second terminal):

```bash
curl -s http://localhost:8080/actuator/health | python -m json.tool
```

Should return `"status": "UP"` with `db` component showing `"UP"`.

## 3. Validate Health and Readiness Endpoints

### Health (aggregate)

```bash
curl -s http://localhost:8080/actuator/health
```

Expected: `200 OK` with `"status": "UP"` and database component
details. See [api-endpoints.md](contracts/api-endpoints.md) for
response format.

### Liveness

```bash
curl -s http://localhost:8080/actuator/health/liveness
```

Expected: `200 OK` with `"status": "UP"`.

### Readiness

```bash
curl -s http://localhost:8080/actuator/health/readiness
```

Expected: `200 OK` with `"status": "UP"`.

## 4. Validate API Documentation

### Swagger UI

Open in a browser: `http://localhost:8080/swagger-ui.html`

**Expected outcome**: Interactive API documentation page loads
within 5 seconds showing available endpoints.

### OpenAPI spec (machine-readable)

```bash
curl -s http://localhost:8080/v3/api-docs | python -m json.tool
```

Expected: Valid JSON OpenAPI 3.x specification document.

## 5. Validate Flyway Migrations

Confirm Flyway created its tracking table and recorded the baseline
migration:

```bash
docker exec mimi-db psql -U "${POSTGRES_USER:-mimi_user}" \
  -d "${POSTGRES_DB:-mimi_db}" \
  -c "SELECT version, description, success FROM flyway_schema_history;"
```

Expected: One row showing version `1`, description matching the
baseline migration, and `success = true`.

## 6. Validate Error Handling

### Unknown endpoint (404)

```bash
curl -s -w "\n%{http_code}" http://localhost:8080/api/v1/nonexistent
```

Expected: `404` status code with RFC 9457 ProblemDetail JSON body.
No stack traces or internal details.

### Unsupported method (405)

```bash
curl -s -w "\n%{http_code}" -X DELETE http://localhost:8080/actuator/health
```

Expected: `405` status code with structured error response.

## 7. Run Integration Tests

Stop the local PostgreSQL first (tests use their own container):

```bash
./mvnw test
```

**Expected outcome**: All tests pass. Testcontainers starts an
isolated PostgreSQL 17 container, runs migrations, verifies
connectivity, and cleans up. No external database required.

## 8. Validate Structured Logging

Review the application console output from step 2.

**Expected**: Each log line includes:
- Timestamp
- Severity level (INFO, WARN, ERROR, etc.)
- Thread name
- Logger name (source)
- Message

No passwords, tokens, or database credentials should appear in
log output.

## 9. Stop Local Environment

```bash
docker compose down
```

**Expected**: PostgreSQL container stops. Data is preserved in the
named volume `db_data` for the next session.

To also remove data:

```bash
docker compose down -v
```

## Validation Checklist

| # | Validation | SC | Pass? |
|---|------------|----|-------|
| 1 | PostgreSQL starts via Docker Compose | SC-005 | ☐ |
| 2 | Application starts within 30 seconds | SC-001 | ☐ |
| 3 | Health endpoint returns correct status | SC-002 | ☐ |
| 4 | Readiness endpoint returns correct status | SC-002 | ☐ |
| 5 | Swagger UI loads within 5 seconds | SC-006 | ☐ |
| 6 | OpenAPI JSON is valid | SC-006 | ☐ |
| 7 | Flyway migration recorded successfully | SC-007 | ☐ |
| 8 | 404 error returns structured ProblemDetail | SC-003 | ☐ |
| 9 | 405 error returns structured ProblemDetail | SC-003 | ☐ |
| 10 | Error responses contain no internals | SC-003 | ☐ |
| 11 | Integration tests pass with Testcontainers | SC-004 | ☐ |
| 12 | Log output is structured | — | ☐ |
| 13 | No sensitive data in logs | — | ☐ |
