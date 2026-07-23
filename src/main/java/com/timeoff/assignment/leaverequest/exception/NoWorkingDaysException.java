package com.timeoff.assignment.leaverequest.exception;

import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.common.exception.InvalidRequestException;

public class NoWorkingDaysException extends InvalidRequestException {

    public NoWorkingDaysException() {
        super(ErrorCode.LEAVE_REQUEST_NO_WORKING_DAYS,
                "The requested period does not contain any working day");
    }
}
