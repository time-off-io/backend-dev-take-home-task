package com.timeoff.assignment.leaverequest.exception;

import com.timeoff.assignment.common.exception.ConflictException;
import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.leaverequest.LeaveRequestStatus;

public class InvalidLeaveRequestStatusException extends ConflictException {

    public InvalidLeaveRequestStatusException(String action, LeaveRequestStatus current) {
        super(ErrorCode.INVALID_LEAVE_REQUEST_STATUS,
                "Cannot %s a leave request in status %s".formatted(action, current));
    }
}
