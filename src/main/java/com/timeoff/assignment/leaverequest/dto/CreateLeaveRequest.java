package com.timeoff.assignment.leaverequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "CreateLeaveRequest", description = "Payload for creating a leave request")
public record CreateLeaveRequest(

        @Schema(description = "Id of the employee requesting leave", example = "1")
        @NotNull
        Long employeeId,

        @Schema(description = "First day of leave (inclusive)", example = "2026-09-14")
        @NotNull
        LocalDate startDate,

        @Schema(description = "Last day of leave (inclusive)", example = "2026-09-18")
        @NotNull
        LocalDate endDate,

        @Schema(description = "Optional reason for the leave", example = "Vacation")
        @Size(max = 500)
        String reason,

        @Schema(
                description = "Leave type id",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        Long leaveTypeId
) {
}
