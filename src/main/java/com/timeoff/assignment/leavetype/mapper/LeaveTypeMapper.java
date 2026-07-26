package com.timeoff.assignment.leavetype.mapper;

import com.timeoff.assignment.leaverequest.LeaveRequest;
import com.timeoff.assignment.leaverequest.dto.LeaveRequestResponse;
import com.timeoff.assignment.leavetype.LeaveType;
import com.timeoff.assignment.leavetype.dto.LeaveTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface LeaveTypeMapper {

    LeaveTypeResponse toResponse(LeaveType leaveType);
}
