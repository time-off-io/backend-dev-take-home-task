package com.timeoff.assignment.leaverequest;

import com.timeoff.assignment.common.api.ApiError;
import com.timeoff.assignment.common.api.PageResponse;
import com.timeoff.assignment.leaverequest.dto.ApproveLeaveRequest;
import com.timeoff.assignment.leaverequest.dto.CreateLeaveRequest;
import com.timeoff.assignment.leaverequest.dto.LeaveRequestResponse;
import com.timeoff.assignment.leaverequest.dto.RejectLeaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/leave-requests")
@Tag(name = "Leave Requests", description = "Create and review employee leave requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    @Operation(summary = "Create leave request",
            description = "Creates a PENDING leave request. The number of working days is computed by "
                    + "the backend (weekends excluded) and must not be supplied by the client. The employee "
                    + "must exist and be active, the range must be valid and in the future, contain at least "
                    + "one working day, not overlap an existing PENDING/APPROVED request, and fit the balance.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Leave request created"),
            @ApiResponse(responseCode = "400", description = "Validation error / invalid or past date range",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Inactive employee / overlap / insufficient balance",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<LeaveRequestResponse> create(@Valid @RequestBody CreateLeaveRequest request,
                                                       UriComponentsBuilder uriBuilder) {
        LeaveRequestResponse created = leaveRequestService.create(request);
        URI location = uriBuilder.path("/api/v1/leave-requests/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{leaveRequestId}")
    @Operation(summary = "Get leave request", description = "Returns a single leave request by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request found"),
            @ApiResponse(responseCode = "404", description = "Leave request not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public LeaveRequestResponse get(@PathVariable Long leaveRequestId) {
        return leaveRequestService.get(leaveRequestId);
    }

    @GetMapping
    @Operation(summary = "List leave requests",
            description = "Returns a paged list of leave requests. Optional filters: `employeeId`, `status`, "
                    + "`startDateFrom`, `startDateTo`. Supports `page`, `size` and `sort` parameters.")
    @ApiResponse(responseCode = "200", description = "Page of leave requests")
    public PageResponse<LeaveRequestResponse> list(
            @Parameter(description = "Filter by employee id") @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) LeaveRequestStatus status,
            @Parameter(description = "Only requests starting on or after this date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @Parameter(description = "Only requests starting on or before this date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @ParameterObject @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        LeaveRequestFilter filter = new LeaveRequestFilter(employeeId, status, startDateFrom, startDateTo);
        return PageResponse.from(leaveRequestService.list(filter, pageable));
    }

    @PostMapping("/{leaveRequestId}/approve")
    @Operation(summary = "Approve leave request",
            description = "Approves a PENDING request, deducting the requested days from the employee balance. "
                    + "Protected against concurrent approvals via optimistic locking.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request approved"),
            @ApiResponse(responseCode = "404", description = "Leave request not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Invalid status / insufficient balance / "
                    + "concurrent modification",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public LeaveRequestResponse approve(@PathVariable Long leaveRequestId,
                                        @Valid @RequestBody(required = false) ApproveLeaveRequest request) {
        ApproveLeaveRequest payload = request != null ? request : new ApproveLeaveRequest(null);
        return leaveRequestService.approve(leaveRequestId, payload);
    }

    @PostMapping("/{leaveRequestId}/reject")
    @Operation(summary = "Reject leave request",
            description = "Rejects a PENDING request. A review comment is mandatory. The balance is unchanged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request rejected"),
            @ApiResponse(responseCode = "400", description = "Validation error (missing review comment)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Leave request not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Invalid status",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public LeaveRequestResponse reject(@PathVariable Long leaveRequestId,
                                       @Valid @RequestBody RejectLeaveRequest request) {
        return leaveRequestService.reject(leaveRequestId, request);
    }

    @PostMapping("/{leaveRequestId}/cancel")
    @Operation(summary = "Cancel leave request",
            description = "Cancels a PENDING request. Approved requests cannot be cancelled. The balance is unchanged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request cancelled"),
            @ApiResponse(responseCode = "404", description = "Leave request not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Invalid status",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public LeaveRequestResponse cancel(@PathVariable Long leaveRequestId) {
        return leaveRequestService.cancel(leaveRequestId);
    }
}
