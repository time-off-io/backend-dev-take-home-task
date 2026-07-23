package com.timeoff.assignment.leaverequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "ApproveLeaveRequest", description = "Payload for approving a leave request")
public record ApproveLeaveRequest(

        @Schema(description = "Optional reviewer comment", example = "Approved")
        @Size(max = 500)
        String reviewComment
) {
}
