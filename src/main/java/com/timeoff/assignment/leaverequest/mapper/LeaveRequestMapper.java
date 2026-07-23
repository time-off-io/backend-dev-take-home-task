package com.timeoff.assignment.leaverequest.mapper;

import com.timeoff.assignment.leaverequest.LeaveRequest;
import com.timeoff.assignment.leaverequest.dto.LeaveRequestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface LeaveRequestMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    LeaveRequestResponse toResponse(LeaveRequest leaveRequest);
}
