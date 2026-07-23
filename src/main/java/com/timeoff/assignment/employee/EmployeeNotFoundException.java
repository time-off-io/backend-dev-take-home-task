package com.timeoff.assignment.employee;

import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.common.exception.ResourceNotFoundException;

public class EmployeeNotFoundException extends ResourceNotFoundException {

    public EmployeeNotFoundException(Long employeeId) {
        super(ErrorCode.EMPLOYEE_NOT_FOUND, "Employee %d was not found".formatted(employeeId));
    }
}
