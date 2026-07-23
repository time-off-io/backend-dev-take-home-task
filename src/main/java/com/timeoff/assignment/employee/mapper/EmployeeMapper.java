package com.timeoff.assignment.employee.mapper;

import com.timeoff.assignment.employee.Employee;
import com.timeoff.assignment.employee.dto.EmployeeResponse;
import org.mapstruct.Mapper;

@Mapper
public interface EmployeeMapper {

    EmployeeResponse toResponse(Employee employee);
}
