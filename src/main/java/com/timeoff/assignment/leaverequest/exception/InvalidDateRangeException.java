package com.timeoff.assignment.leaverequest.exception;

import com.timeoff.assignment.common.exception.ErrorCode;
import com.timeoff.assignment.common.exception.InvalidRequestException;

public class InvalidDateRangeException extends InvalidRequestException {

    public InvalidDateRangeException() {
        super(ErrorCode.LEAVE_REQUEST_INVALID_DATE_RANGE, "Start date must not be after end date");
    }
}
