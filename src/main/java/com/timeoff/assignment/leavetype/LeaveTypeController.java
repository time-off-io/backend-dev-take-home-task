package com.timeoff.assignment.leavetype;

import com.timeoff.assignment.common.api.ApiError;
import com.timeoff.assignment.common.api.PageResponse;
import com.timeoff.assignment.leavetype.dto.CreateLeaveTypeRequest;
import com.timeoff.assignment.leavetype.dto.LeaveTypeResponse;
import com.timeoff.assignment.leavetype.dto.UpdateLeaveTypeStatusRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/leave-types")
@Tag(name = "Leave Types", description = "Manage leave types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @PostMapping
    @Operation(summary = "Create leave type",
            description = "Creates an active leave type.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Leave types created"),
            @ApiResponse(responseCode = "400", description = "Validation error / invalid",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Leave type already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<LeaveTypeResponse> create(@Valid @RequestBody CreateLeaveTypeRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        LeaveTypeResponse created = leaveTypeService.create(request);
        URI location = uriBuilder.path("/api/v1/leave-types/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List leave types",
            description = "Returns a paged list of leave types. Optional filters: `active`. Supports `page`, `size` and `sort` parameters.")
    @ApiResponse(responseCode = "200", description = "Page of leave types")
    public PageResponse<LeaveTypeResponse> list(
            @Parameter(description = "Filter by whether it's active") @RequestParam(required = false) Boolean active,
            @ParameterObject @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        LeaveTypeFilter filter = new LeaveTypeFilter(active);
        return PageResponse.from(leaveTypeService.list(filter, pageable));
    }


    @GetMapping("/{leaveTypeId}")
    @Operation(summary = "Get leave type", description = "Returns a single type request by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave type found"),
            @ApiResponse(responseCode = "404", description = "Leave type not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public LeaveTypeResponse get(@PathVariable Long leaveTypeId) {
        return leaveTypeService.get(leaveTypeId);
    }

    @PatchMapping("/{leaveTypeId}/status")
    @Operation(summary = "Update a leave type status",
            description = "Activates or deactivates an leave type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Leave type not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public LeaveTypeResponse updateStatus(@PathVariable Long leaveTypeId,
                                         @Valid @RequestBody UpdateLeaveTypeStatusRequest request) {
        return leaveTypeService.updateStatus(leaveTypeId, request);
    }

}
