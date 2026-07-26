package com.timeoff.assignment.leavetype.exception;

import com.timeoff.assignment.common.exception.ConflictException;
import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.leavetype.LeaveTypeCode;

public class LeaveTypeAlreadyExistsException extends ConflictException {

    public LeaveTypeAlreadyExistsException(LeaveTypeCode code) {
        super(ErrorCode.LEAVE_TYPE_ALREADY_EXISTS,
                "A leave type with code '%s' already exists".formatted(code));
    }
}
