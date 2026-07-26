package com.timeoff.assignment.leavetype;

import com.timeoff.assignment.employee.Employee;
import com.timeoff.assignment.employee.EmployeeNotFoundException;
import com.timeoff.assignment.employee.EmployeeService;
import com.timeoff.assignment.leaverequest.LeaveRequestRepository;
import com.timeoff.assignment.leaverequest.WorkingDaysCalculator;
import com.timeoff.assignment.leaverequest.mapper.LeaveRequestMapper;
import com.timeoff.assignment.leavetype.exception.LeaveTypeNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@Transactional(readOnly = true)
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    /**
     * Loads a leave type entity or throws a not found error. 
     */
    public LeaveType getEntity(LeaveTypeCode code) {
        return leaveTypeRepository.findByCode(code)
                .orElseThrow(() -> new LeaveTypeNotFoundException(code));
    }

}
