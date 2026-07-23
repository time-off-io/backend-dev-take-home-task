package com.timeoff.assignment.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error payload returned by every failing endpoint.
 */
@Schema(name = "ApiError", description = "Standard error response")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(

        @Schema(description = "Time the error occurred (UTC)", example = "2026-07-23T10:30:00Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "409")
        int status,

        @Schema(description = "HTTP status reason phrase", example = "Conflict")
        String error,

        @Schema(description = "Stable machine-readable error code", example = "LEAVE_REQUEST_OVERLAP")
        String code,

        @Schema(description = "Human-readable error message",
                example = "The employee already has an overlapping leave request")
        String message,

        @Schema(description = "Request path that produced the error", example = "/api/v1/leave-requests")
        String path,

        @Schema(description = "Field-level validation errors, present only for validation failures")
        List<FieldViolation> violations
) {

    @Schema(name = "FieldViolation", description = "A single field validation error")
    public record FieldViolation(
            @Schema(description = "Rejected field name", example = "email") String field,
            @Schema(description = "Validation message", example = "must be a well-formed email address") String message
    ) {
    }
}
