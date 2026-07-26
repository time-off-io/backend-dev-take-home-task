package com.timeoff.assignment.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Stable, machine-readable error codes exposed in every error response.
 * The associated {@link HttpStatus} keeps the HTTP contract consistent across the API.
 */
public enum ErrorCode {

    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND),
    EMPLOYEE_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT),
    EMPLOYEE_INACTIVE(HttpStatus.CONFLICT),

    LEAVE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND),
    LEAVE_REQUEST_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_IN_THE_PAST(HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_NO_WORKING_DAYS(HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_OVERLAP(HttpStatus.CONFLICT),
    INSUFFICIENT_LEAVE_BALANCE(HttpStatus.CONFLICT),
    INVALID_LEAVE_REQUEST_STATUS(HttpStatus.CONFLICT),

    LEAVE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND),
    INACTIVE_LEAVE_TYPE(HttpStatus.BAD_REQUEST),
    LEAVE_TYPE_ALREADY_EXISTS(HttpStatus.CONFLICT),

    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
