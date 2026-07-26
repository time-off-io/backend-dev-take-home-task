package com.timeoff.assignment.leavetype.exception;

import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.common.exception.ResourceNotFoundException;
import com.timeoff.assignment.leavetype.LeaveTypeCode;

public class LeaveTypeNotFoundException  extends ResourceNotFoundException {
    public LeaveTypeNotFoundException(LeaveTypeCode leaveTypeCode) {
        super(ErrorCode.LEAVE_TYPE_NOT_FOUND, "Leave type with code %s was not found".formatted(leaveTypeCode));
    }
}
