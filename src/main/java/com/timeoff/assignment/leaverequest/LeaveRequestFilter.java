package com.timeoff.assignment.leaverequest;

import java.time.LocalDate;

/**
 * Optional filter criteria for listing leave requests. Any {@code null} field is ignored.
 */
public record LeaveRequestFilter(
        Long employeeId,
        LeaveRequestStatus status,
        LocalDate startDateFrom,
        LocalDate startDateTo
) {
}
