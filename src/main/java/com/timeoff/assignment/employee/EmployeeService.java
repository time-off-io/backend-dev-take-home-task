package com.timeoff.assignment.employee;

import com.timeoff.assignment.employee.dto.CreateEmployeeRequest;
import com.timeoff.assignment.employee.dto.EmployeeResponse;
import com.timeoff.assignment.employee.dto.UpdateEmployeeStatusRequest;
import com.timeoff.assignment.employee.mapper.EmployeeMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        Employee employee = new Employee(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.annualLeaveAllowance());
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse get(Long employeeId) {
        return employeeMapper.toResponse(getEntity(employeeId));
    }

    public Page<EmployeeResponse> list(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toResponse);
    }

    @Transactional
    public EmployeeResponse updateStatus(Long employeeId, UpdateEmployeeStatusRequest request) {
        Employee employee = getEntity(employeeId);
        if (request.active()) {
            employee.activate();
        } else {
            employee.deactivate();
        }
        return employeeMapper.toResponse(employee);
    }

    /**
     * Loads an employee entity or throws. Intended for use by other services in the same
     * transactional context (e.g. leave request creation and approval).
     */
    public Employee getEntity(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }
}
