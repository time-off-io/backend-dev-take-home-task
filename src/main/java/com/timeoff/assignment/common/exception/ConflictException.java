package com.timeoff.assignment.common.exception;

/**
 * Thrown when a request conflicts with the current state of a resource
 * (duplicate email, overlapping request, invalid status transition, ...). Maps to HTTP 409.
 */
public class ConflictException extends BusinessException {

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
