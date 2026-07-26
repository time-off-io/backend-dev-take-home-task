package com.timeoff.assignment.leavetype.mapper;


import com.timeoff.assignment.leavetype.LeaveType;
import com.timeoff.assignment.leavetype.dto.LeaveTypeResponse;
import org.mapstruct.Mapper;

@Mapper
public interface LeaveTypeMapper {

    LeaveTypeResponse toResponse(LeaveType leaveType);
}
