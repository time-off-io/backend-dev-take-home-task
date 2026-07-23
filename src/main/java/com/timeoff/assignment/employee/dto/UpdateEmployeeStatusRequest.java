package com.timeoff.assignment.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateEmployeeStatusRequest", description = "Payload for activating or deactivating an employee")
public record UpdateEmployeeStatusRequest(

        @Schema(description = "Whether the employee should be active", example = "false")
        @NotNull
        Boolean active
) {
}
