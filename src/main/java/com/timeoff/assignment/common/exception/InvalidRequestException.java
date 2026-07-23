package com.timeoff.assignment.common.exception;

/**
 * Thrown when a request is semantically invalid beyond simple bean validation
 * (e.g. an invalid date range that also depends on other fields). Maps to HTTP 400.
 */
public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
