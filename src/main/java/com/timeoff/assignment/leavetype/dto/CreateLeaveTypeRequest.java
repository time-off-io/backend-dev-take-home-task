package com.timeoff.assignment.leavetype.dto;

import com.timeoff.assignment.leavetype.LeaveTypeCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateLeaveTypeRequest", description = "Payload for creating a new leave type")
public record CreateLeaveTypeRequest (
    @Schema(example = "Annual leave") String name,
    @Schema(example = "ANNUAL_LEAVE")
    LeaveTypeCode code,
    @Schema(example = "true") boolean deductsFromAnnualBalance
) {}
