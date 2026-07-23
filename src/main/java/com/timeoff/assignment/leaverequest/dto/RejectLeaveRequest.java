package com.timeoff.assignment.leaverequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RejectLeaveRequest", description = "Payload for rejecting a leave request")
public record RejectLeaveRequest(

        @Schema(description = "Mandatory reviewer comment explaining the rejection",
                example = "Insufficient team coverage")
        @NotBlank @Size(max = 500)
        String reviewComment
) {
}
