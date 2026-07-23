package com.timeoff.assignment.employee;

import com.timeoff.assignment.common.exception.ConflictException;
import com.timeoff.assignment.common.exception.ErrorCode;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String email) {
        super(ErrorCode.EMPLOYEE_EMAIL_ALREADY_EXISTS,
                "An employee with email '%s' already exists".formatted(email));
    }
}
