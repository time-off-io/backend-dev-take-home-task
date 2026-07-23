package com.timeoff.assignment.employee;

import com.timeoff.assignment.common.exception.ConflictException;
import com.timeoff.assignment.common.exception.ErrorCode;

public class EmployeeInactiveException extends ConflictException {

    public EmployeeInactiveException(Long employeeId) {
        super(ErrorCode.EMPLOYEE_INACTIVE,
                "Employee %d is inactive and cannot perform this action".formatted(employeeId));
    }
}
