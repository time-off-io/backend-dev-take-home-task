package com.timeoff.assignment.leaverequest.exception;

import com.timeoff.assignment.common.exception.ConflictException;
import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.leaverequest.LeaveRequestStatus;
import com.timeoff.assignment.leavetype.LeaveTypeCode;

public class InactiveLeaveTypeException extends ConflictException {

    public InactiveLeaveTypeException(String action, LeaveTypeCode code) {
        super(ErrorCode.INACTIVE_LEAVE_TYPE,
                "The requested leave type during %s with code %s is inactive".formatted(action, code));
    }
}

