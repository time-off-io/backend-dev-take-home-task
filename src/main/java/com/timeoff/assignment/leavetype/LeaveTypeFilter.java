package com.timeoff.assignment.leavetype;

/**
 * Optional filter criteria for listing leave types. Any {@code null} field is ignored.
 */
public record LeaveTypeFilter (Boolean active){}
