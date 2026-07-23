package com.timeoff.assignment.leaverequest.exception;

import com.timeoff.assignment.common.exception.ConflictException;
import com.timeoff.assignment.common.exception.ErrorCode;

public class InsufficientLeaveBalanceException extends ConflictException {

    public InsufficientLeaveBalanceException() {
        super(ErrorCode.INSUFFICIENT_LEAVE_BALANCE,
                "The employee does not have enough remaining leave balance");
    }
}
