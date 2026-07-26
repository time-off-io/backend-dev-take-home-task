package com.timeoff.assignment.leavetype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateLeaveTypeStatusRequest", description = "Payload for activating or deactivating a leave type")
public record UpdateLeaveTypeStatusRequest(

        @Schema(description = "Whether the leave type should be active", example = "false")
        @NotNull
        Boolean active
) {
}
