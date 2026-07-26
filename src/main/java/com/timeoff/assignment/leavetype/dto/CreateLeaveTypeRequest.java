package com.timeoff.assignment.leavetype.dto;

import com.timeoff.assignment.leavetype.LeaveTypeCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateLeaveTypeRequest", description = "Payload for creating a new leave type")
public record CreateLeaveTypeRequest (

    @Schema(example = "Annual leave")
    @Size(max = 255)
    @NotNull
    String name,

    @Schema(example = "ANNUAL_LEAVE")
    @NotNull
    LeaveTypeCode code,

    @Schema(example = "true")
    boolean deductsFromAnnualBalance
) {
}
