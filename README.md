# TimeOff Assignment API

A small, production-style Spring Boot backend for managing employee leave requests.

## Overview

Timeoff is a REST API that lets an organisation manage employees and their annual leave
requests. Employees have an annual leave allowance and a remaining balance. Leave requests go
through a simple review workflow (pending → approved / rejected / cancelled), with the business
rules and balance accounting enforced on the server side.

There is no authentication and no frontend — the project is intentionally focused on a clean,
well-tested backend.

## Business Scenario

* Create and view employees, each with an annual leave allowance and a remaining balance.
* Activate / deactivate employees (inactive employees cannot submit new requests).
* Submit leave requests for a date range; the number of **working days** (weekends excluded) is
  computed by the backend.
* Approve, reject or cancel pending requests.
* Approval deducts the requested days from the employee's remaining balance and is protected
  against concurrent approvals that could otherwise drive the balance negative.
* List and filter leave requests per employee, status and start date.

## Architecture

The code follows a **package-by-feature** layout:

```text
com.timeoff.assignment
├── common
│   ├── api          # ApiError, PageResponse (stable API contracts)
│   ├── config       # Clock, OpenAPI metadata
│   ├── exception    # ErrorCode, BusinessException hierarchy, @RestControllerAdvice
│   └── persistence  # AuditableEntity (UTC timestamps + @Version)
├── employee         # entity, repository, service, controller, dto, mapper
└── leaverequest     # entity, repository, service, controller, dto, mapper,
                     # WorkingDaysCalculator, status enum, filtering
```

Key conventions:

* Controllers contain **no business logic**; all rules live in services and domain components.
* JPA entities are never exposed by the API — requests and responses use dedicated DTOs, mapped
  with **MapStruct**.
* Constructor injection everywhere (no field injection).
* All timestamps are stored and returned in **UTC** (`Instant`).
* Expected business failures throw typed exceptions (never a generic `RuntimeException`) that map
  to a consistent error response with a stable `code`.

## Technology Stack

* Java 21, Spring Boot 3.3
* Maven (with wrapper)
* PostgreSQL + Flyway migrations
* Spring Data JPA, Bean Validation
* MapStruct, Lombok
* springdoc-openapi (Swagger UI)
* JUnit 5, Mockito, Testcontainers
* Docker & Docker Compose

## Prerequisites

* JDK 21
* Docker & Docker Compose (for PostgreSQL and for running integration tests)

## Getting Started

### Option A — Everything with Docker Compose

```bash
docker compose up --build
```

This starts PostgreSQL (with a health check) and the application. The app waits until the
database is healthy before starting and applies the Flyway migrations automatically.

* API base path: `http://localhost:8080/api/v1`
* Swagger UI: `http://localhost:8080/swagger-ui.html`

### Option B — App locally, PostgreSQL in Docker

Start only the database:

```bash
docker compose up postgres
```

Then run the application with Maven:

```bash
./mvnw spring-boot:run
```

To load development seed data (two employees, one pending and one approved request), activate the
`dev` profile:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

## Configuration

Database configuration is provided through environment variables (see `.env.example`):

| Variable      | Default    | Description          |
|---------------|------------|----------------------|
| `DB_HOST`     | `localhost`| PostgreSQL host      |
| `DB_PORT`     | `5432`     | PostgreSQL port      |
| `DB_NAME`     | `timeoff`  | Database name        |
| `DB_USERNAME` | `timeoff`  | Database username    |
| `DB_PASSWORD` | `timeoff`  | Database password    |

Copy `.env.example` to `.env` for local use. The real `.env` is git-ignored; no secrets are
committed to the repository. The defaults above are safe local development values only.

## Tests

```bash
# Unit tests only
./mvnw test

# Unit + integration tests (integration tests use Testcontainers/PostgreSQL)
./mvnw verify
```

Integration tests run the real Flyway migrations against a disposable PostgreSQL container, so a
running Docker daemon is required. H2 is not used.

## API Documentation

Auto-generated from the controllers, DTOs and Bean Validation annotations:

* Swagger UI: `http://localhost:8080/swagger-ui.html`
  (or `http://localhost:8080/swagger-ui/index.html`)
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`
* OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

## Example API Calls

Create an employee:

```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"George","lastName":"Papadopoulos","email":"george@example.com","annualLeaveAllowance":25}'
```

Create a leave request (working days are computed by the backend):

```bash
curl -X POST http://localhost:8080/api/v1/leave-requests \
  -H 'Content-Type: application/json' \
  -d '{"employeeId":1,"startDate":"2026-09-14","endDate":"2026-09-18","reason":"Vacation"}'
```

Approve, reject or cancel:

```bash
curl -X POST http://localhost:8080/api/v1/leave-requests/1/approve \
  -H 'Content-Type: application/json' -d '{"reviewComment":"Approved"}'

curl -X POST http://localhost:8080/api/v1/leave-requests/1/reject \
  -H 'Content-Type: application/json' -d '{"reviewComment":"Insufficient team coverage"}'

curl -X POST http://localhost:8080/api/v1/leave-requests/1/cancel
```

List and filter leave requests:

```bash
curl 'http://localhost:8080/api/v1/leave-requests?status=PENDING&employeeId=1&page=0&size=20&sort=startDate,asc'
```

### Error responses

All errors share the same shape and carry a stable `code`:

```json
{
  "timestamp": "2026-07-23T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "LEAVE_REQUEST_OVERLAP",
  "message": "The employee already has an overlapping leave request",
  "path": "/api/v1/leave-requests"
}
```

## Continuous Integration

GitHub Actions (`.github/workflows/ci.yml`) runs on every push and pull request. It uses an Ubuntu
runner with JDK 21 and a Maven cache to: compile, run unit and integration tests (Testcontainers),
build the application JAR, upload test reports on failure and upload the JAR as an artifact.
Dependabot proposes weekly updates for Maven and GitHub Actions dependencies.

## Key Technical Decisions

* **Working-day calculation** lives in a dedicated, side-effect-free `WorkingDaysCalculator`
  domain component. `requestedDays` is always computed server-side and never trusted from the
  client. It is modelled as `BigDecimal` so fractional days can be supported later without a
  contract change.
* **Concurrency on approval** uses optimistic locking (`@Version` on `Employee`). Two concurrent
  approvals of the same employee both read the same version; only the first commit succeeds and
  the second is translated into a `CONCURRENT_MODIFICATION` response. A database check constraint
  on `remaining_leave_days` acts as a final safety net so the balance can never go negative.
* **Consistent error handling** via a single `@RestControllerAdvice` and a closed set of
  `ErrorCode`s, each mapped to an HTTP status.
* **Database integrity** is enforced both in the application and in the schema (unique email,
  positive allowance, non-negative balance, valid date range, status check).
* **Stable pagination contract** (`PageResponse`) instead of serializing Spring's `Page` directly.

## Known Limitations

* No authentication or authorization (out of scope).
* Public holidays and half-days are not considered; only whole working days are counted.
* A single annual leave balance per employee; no leave types, carry-over or accruals.
* Approved requests cannot be cancelled.
* No multi-tenancy, notifications, or audit log.
