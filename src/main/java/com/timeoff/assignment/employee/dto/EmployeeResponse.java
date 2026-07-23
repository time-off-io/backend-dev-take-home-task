package com.timeoff.assignment.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "EmployeeResponse", description = "Employee representation returned by the API")
public record EmployeeResponse(

        @Schema(example = "1") Long id,
        @Schema(example = "George") String firstName,
        @Schema(example = "Papadopoulos") String lastName,
        @Schema(example = "george@example.com") String email,
        @Schema(example = "25") int annualLeaveAllowance,
        @Schema(description = "Remaining leave balance in days", example = "25") BigDecimal remainingLeaveDays,
        @Schema(example = "true") boolean active,
        @Schema(example = "2026-07-23T10:30:00Z") Instant createdAt,
        @Schema(example = "2026-07-23T10:30:00Z") Instant updatedAt
) {
}
