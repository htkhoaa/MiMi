# API Contracts: M1 — Backend Foundation

**Date**: 2026-11-24 | **Feature**: [spec.md](../spec.md)

## Overview

M1 exposes infrastructure endpoints only. No business-domain API
endpoints are created. All endpoints are unauthenticated in M1
(Spring Security configured to permit all requests).

---

## Health Endpoint

**Path**: `GET /actuator/health`

Returns the aggregate health status of the application and its
dependencies.

**Response** (healthy):

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

**Response** (database unavailable):

```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN"
    }
  }
}
```

| Status Code | Condition |
|-------------|-----------|
| 200 | All components healthy |
| 503 | One or more components unhealthy |

---

## Liveness Endpoint

**Path**: `GET /actuator/health/liveness`

Returns whether the application process is alive.

**Response**:

```json
{
  "status": "UP"
}
```

| Status Code | Condition |
|-------------|-----------|
| 200 | Application process is alive |
| 503 | Application process is not functioning |

---

## Readiness Endpoint

**Path**: `GET /actuator/health/readiness`

Returns whether the application is ready to accept traffic.

**Response**:

```json
{
  "status": "UP"
}
```

| Status Code | Condition |
|-------------|-----------|
| 200 | Application is ready to accept traffic |
| 503 | Application is not ready (dependencies unavailable) |

---

## OpenAPI Specification

**Path**: `GET /v3/api-docs`

Returns the machine-readable OpenAPI 3.x specification document.

| Status Code | Content-Type |
|-------------|-------------|
| 200 | `application/json` |

---

## Swagger UI

**Path**: `GET /swagger-ui.html` (redirects to `/swagger-ui/index.html`)

Serves the interactive API documentation interface.

| Status Code | Content-Type |
|-------------|-------------|
| 200 | `text/html` |

---

## Error Response Contract

All error responses MUST use the RFC 9457 Problem Detail format.

**Standard error response structure**:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "No endpoint GET /api/v1/nonexistent.",
  "instance": "/api/v1/nonexistent"
}
```

| Field | Type | Description |
|-------|------|-------------|
| type | string | URI reference identifying the problem type (default: `about:blank`) |
| title | string | Short human-readable summary of the problem |
| status | integer | HTTP status code |
| detail | string | Human-readable explanation specific to this occurrence |
| instance | string | URI reference identifying the specific occurrence |

**Error scenarios covered in M1**:

| Scenario | Status Code | Title |
|----------|-------------|-------|
| Unknown endpoint | 404 | Not Found |
| Unsupported HTTP method | 405 | Method Not Allowed |
| Unhandled server error | 500 | Internal Server Error |

**Security constraint**: Error responses MUST NOT contain stack
traces, SQL statements, internal class names, or any implementation
details.

---

## Endpoints NOT Created in M1

The following endpoint patterns are reserved for future milestones
and MUST NOT be implemented in M1:

- `/api/v1/users/**` (M2)
- `/api/v1/auth/**` (M2)
- `/api/v1/wallets/**` (M3)
- `/api/v1/transfers/**` (M3)
- `/api/v1/merchants/**` (M4)
- `/api/v1/payments/**` (M4)
