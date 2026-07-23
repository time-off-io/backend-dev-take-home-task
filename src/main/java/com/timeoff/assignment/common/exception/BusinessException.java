package com.timeoff.assignment.common.exception;

/**
 * Base type for expected business errors. Each instance carries an {@link ErrorCode}
 * that maps to a consistent API error response and HTTP status.
 *
 * <p>Business flows must throw subclasses of this exception instead of generic
 * {@link RuntimeException} so the failure mode is explicit and translatable.
 */
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
