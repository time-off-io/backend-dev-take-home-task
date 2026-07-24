# Candidate Task — Add Leave Types

## Goal

Extend the existing system so that it supports different **types of leave**.

Today every leave request implicitly represents annual leave that is deducted from the
employee's annual balance. Your task is to introduce configurable leave types and make the
balance behaviour depend on the selected type.

## New Domain Model

Add a new entity `LeaveType` with the following fields:

```text
id
name
code
deductsFromAnnualBalance
active
createdAt
updatedAt
```

Examples of leave type codes:

```text
ANNUAL_LEAVE
SICK_LEAVE
UNPAID_LEAVE
```

Each `LeaveRequest` must be associated with exactly one `LeaveType`.

## Business Rules

* Every new leave request must include a `leaveTypeId`.
* The leave type must exist.
* The leave type must be active.
* If `deductsFromAnnualBalance = true`, all the existing balance checks apply.
* If `deductsFromAnnualBalance = false`, the request may be approved without deducting from
  the annual leave balance.
* A leave type that is already in use must not be deletable.
* It must be possible to activate and deactivate a leave type.
* The `code` must be unique.
* Existing leave requests must be migrated safely with a database migration.
* The API responses must be updated accordingly.
* The auto-generated OpenAPI/Swagger documentation must be updated.
* Unit and integration tests must be added.

## Expected API

### Create leave type

```http
POST /api/v1/leave-types
```

Request:

```json
{
  "name": "Annual Leave",
  "code": "ANNUAL_LEAVE",
  "deductsFromAnnualBalance": true
}
```

### List leave types

```http
GET /api/v1/leave-types
```

Support an optional filter:

```text
active
```

### Get leave type

```http
GET /api/v1/leave-types/{leaveTypeId}
```

### Update leave type status

```http
PATCH /api/v1/leave-types/{leaveTypeId}/status
```

Request:

```json
{
  "active": false
}
```

## Updated Leave Request Payload

The create leave request endpoint must accept a `leaveTypeId`:

```json
{
  "employeeId": 1,
  "leaveTypeId": 1,
  "startDate": "2026-09-14",
  "endDate": "2026-09-18",
  "reason": "Vacation"
}
```

## Requirements

You should:

* keep the existing functionality working,
* not remove existing tests,
* add database migrations,
* add unit tests,
* add integration tests,
* update the API,
* update the OpenAPI/Swagger documentation,
* update the README where needed,
* make commits with clear commit messages,
* briefly explain your main technical decisions in a separate file or in the pull request description.

## Time Expectation

The exercise is designed to be completed in roughly:

```text
2–4 hours
```

A production-perfect solution is not required. You may document any parts you did not have time
to finish.
