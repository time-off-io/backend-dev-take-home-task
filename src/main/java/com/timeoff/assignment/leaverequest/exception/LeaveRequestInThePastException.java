package com.timeoff.assignment.leaverequest.exception;

import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.common.exception.InvalidRequestException;

public class LeaveRequestInThePastException extends InvalidRequestException {

    public LeaveRequestInThePastException() {
        super(ErrorCode.LEAVE_REQUEST_IN_THE_PAST, "Leave request dates must not be in the past");
    }
}
