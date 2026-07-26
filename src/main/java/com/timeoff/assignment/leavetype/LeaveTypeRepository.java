package com.timeoff.assignment.leavetype;

import com.timeoff.assignment.leaverequest.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long>,  JpaSpecificationExecutor<LeaveType> {

}
