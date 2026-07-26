package com.timeoff.assignment.leavetype.dto;

import com.timeoff.assignment.leavetype.LeaveTypeCode;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name = "LeaveTypeResponse", description = "Leave type representation returned by the API")
public record LeaveTypeResponse (
    @Schema(example = "1") Long id,
    @Schema(example = "Annual leave") String name,
    @Schema(example = "ANNUAL_LEAVE") LeaveTypeCode code,
    @Schema(example = "true") boolean deductsFromAnnualBalance,
    @Schema(example = "true") boolean active
) {}
