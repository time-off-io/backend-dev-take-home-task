package com.timeoff.assignment.leaverequest.exception;

import com.timeoff.assignment.common.exception.ConflictException;
import com.timeoff.assignment.common.exception.ErrorCode;

public class OverlappingLeaveRequestException extends ConflictException {

    public OverlappingLeaveRequestException() {
        super(ErrorCode.LEAVE_REQUEST_OVERLAP,
                "The employee already has an overlapping leave request");
    }
}
