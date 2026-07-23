package com.timeoff.assignment.leaverequest.exception;

import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.common.exception.ResourceNotFoundException;

public class LeaveRequestNotFoundException extends ResourceNotFoundException {

    public LeaveRequestNotFoundException(Long leaveRequestId) {
        super(ErrorCode.LEAVE_REQUEST_NOT_FOUND, "Leave request %d was not found".formatted(leaveRequestId));
    }
}
