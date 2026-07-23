package com.timeoff.assignment.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateEmployeeRequest", description = "Payload for creating a new employee")
public record CreateEmployeeRequest(

        @Schema(description = "Employee first name", example = "George")
        @NotBlank @Size(max = 100)
        String firstName,

        @Schema(description = "Employee last name", example = "Papadopoulos")
        @NotBlank @Size(max = 100)
        String lastName,

        @Schema(description = "Unique employee email address", example = "george@example.com")
        @NotBlank @Email @Size(max = 255)
        String email,

        @Schema(description = "Annual leave allowance in whole days", example = "25")
        @NotNull @Positive
        Integer annualLeaveAllowance
) {
}
