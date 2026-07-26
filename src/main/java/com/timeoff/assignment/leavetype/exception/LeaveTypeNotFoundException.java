package com.timeoff.assignment.leavetype.exception;

import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.common.exception.ResourceNotFoundException;
import com.timeoff.assignment.leavetype.LeaveTypeCode;

public class LeaveTypeNotFoundException  extends ResourceNotFoundException {
    public LeaveTypeNotFoundException(Long id) {
        super(ErrorCode.LEAVE_TYPE_NOT_FOUND, "Leave type with id %d was not found".formatted(id));
    }
}
