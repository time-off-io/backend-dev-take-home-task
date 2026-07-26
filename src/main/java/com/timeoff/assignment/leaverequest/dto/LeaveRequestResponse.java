package com.timeoff.assignment.leaverequest.dto;

import com.timeoff.assignment.leaverequest.LeaveRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(name = "LeaveRequestResponse", description = "Leave request representation returned by the API")
public record LeaveRequestResponse(

        @Schema(example = "1") Long id,
        @Schema(description = "Id of the requesting employee", example = "1") Long employeeId,
        @Schema(description = "Id of the leave type of the request", example = "1") Long leaveTypeId,
        @Schema(example = "2026-09-14") LocalDate startDate,
        @Schema(example = "2026-09-18") LocalDate endDate,
        @Schema(description = "Working days computed by the backend", example = "5") BigDecimal requestedDays,
        @Schema(example = "PENDING") LeaveRequestStatus status,
        @Schema(example = "Vacation") String reason,
        @Schema(example = "Approved") String reviewComment,
        @Schema(example = "2026-07-23T10:30:00Z") Instant createdAt,
        @Schema(example = "2026-07-23T10:30:00Z") Instant updatedAt,
        @Schema(example = "2026-07-23T11:00:00Z") Instant reviewedAt
) {
}
