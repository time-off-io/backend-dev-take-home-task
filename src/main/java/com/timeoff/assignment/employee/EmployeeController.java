package com.timeoff.assignment.employee;

import com.timeoff.assignment.common.api.ApiError;
import com.timeoff.assignment.common.api.PageResponse;
import com.timeoff.assignment.employee.dto.CreateEmployeeRequest;
import com.timeoff.assignment.employee.dto.EmployeeResponse;
import com.timeoff.assignment.employee.dto.UpdateEmployeeStatusRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees", description = "Manage employees and their leave allowance")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @Operation(summary = "Create employee",
            description = "Creates a new active employee. The remaining leave balance is initialised "
                    + "to the annual allowance. The email must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        EmployeeResponse created = employeeService.create(request);
        URI location = uriBuilder.path("/api/v1/employees/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "Get employee", description = "Returns a single employee by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EmployeeResponse get(@PathVariable Long employeeId) {
        return employeeService.get(employeeId);
    }

    @GetMapping
    @Operation(summary = "List employees",
            description = "Returns a paged list of employees. Supports the standard "
                    + "`page`, `size` and `sort` query parameters (e.g. `sort=lastName,asc`).")
    @ApiResponse(responseCode = "200", description = "Page of employees")
    public PageResponse<EmployeeResponse> list(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(employeeService.list(pageable));
    }

    @PatchMapping("/{employeeId}/status")
    @Operation(summary = "Update employee status",
            description = "Activates or deactivates an employee. Inactive employees cannot submit new leave requests.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EmployeeResponse updateStatus(@PathVariable Long employeeId,
                                         @Valid @RequestBody UpdateEmployeeStatusRequest request) {
        return employeeService.updateStatus(employeeId, request);
    }
}
