package com.timeoff.assignment.leaverequest;

import com.timeoff.assignment.employee.Employee;
import com.timeoff.assignment.employee.EmployeeInactiveException;
import com.timeoff.assignment.employee.EmployeeService;
import com.timeoff.assignment.leaverequest.dto.ApproveLeaveRequest;
import com.timeoff.assignment.leaverequest.dto.CreateLeaveRequest;
import com.timeoff.assignment.leaverequest.dto.LeaveRequestResponse;
import com.timeoff.assignment.leaverequest.dto.RejectLeaveRequest;
import com.timeoff.assignment.leaverequest.exception.*;
import com.timeoff.assignment.leaverequest.mapper.LeaveRequestMapper;
import com.timeoff.assignment.leavetype.LeaveType;
import com.timeoff.assignment.leavetype.LeaveTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LeaveRequestService {

    /** Statuses that consume a period and therefore block overlapping requests. */
    private static final List<LeaveRequestStatus> BLOCKING_STATUSES =
            List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED);

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeService employeeService;
    private final LeaveTypeService leaveTypeService;
    private final WorkingDaysCalculator workingDaysCalculator;
    private final LeaveRequestMapper leaveRequestMapper;
    private final Clock clock;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository,
                               EmployeeService employeeService,
                               LeaveTypeService leaveTypeService,
                               WorkingDaysCalculator workingDaysCalculator,
                               LeaveRequestMapper leaveRequestMapper,
                               Clock clock) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeService = employeeService;
        this.leaveTypeService = leaveTypeService;
        this.workingDaysCalculator = workingDaysCalculator;
        this.leaveRequestMapper = leaveRequestMapper;
        this.clock = clock;
    }

    @Transactional
    public LeaveRequestResponse create(CreateLeaveRequest request) {

        LeaveType leaveType = leaveTypeService.getEntity(request.leaveTypeCode());
        requireLeaveTypeActive(leaveType, "creation");

        Employee employee = employeeService.getEntity(request.employeeId());
        if (!employee.isActive()) {
            throw new EmployeeInactiveException(employee.getId());
        }

        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException();
        }
        if (startDate.isBefore(LocalDate.now(clock))) {
            throw new LeaveRequestInThePastException();
        }

        BigDecimal requestedDays = workingDaysCalculator.countWorkingDays(startDate, endDate);
        if (requestedDays.signum() <= 0) {
            throw new NoWorkingDaysException();
        }

        if (leaveRequestRepository.existsOverlapping(
                employee.getId(), BLOCKING_STATUSES, startDate, endDate)) {
            throw new OverlappingLeaveRequestException();
        }

        if (!employee.hasSufficientBalance(requestedDays)) {
            throw new InsufficientLeaveBalanceException();
        }

        LeaveRequest leaveRequest =
                new LeaveRequest(employee, startDate, endDate, requestedDays, request.reason());
        return leaveRequestMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }

    public LeaveRequestResponse get(Long leaveRequestId) {
        return leaveRequestMapper.toResponse(getEntity(leaveRequestId));
    }

    public Page<LeaveRequestResponse> list(LeaveRequestFilter filter, Pageable pageable) {
        Specification<LeaveRequest> spec = Specification
                .allOf(LeaveRequestSpecifications.hasEmployeeId(filter.employeeId()),
                        LeaveRequestSpecifications.hasStatus(filter.status()),
                        LeaveRequestSpecifications.startDateFrom(filter.startDateFrom()),
                        LeaveRequestSpecifications.startDateTo(filter.startDateTo()));
        return leaveRequestRepository.findAll(spec, pageable).map(leaveRequestMapper::toResponse);
    }

    /**
     * Approves a pending request and deducts the requested days from the employee balance.
     *
     * <p>Concurrency: the employee carries a JPA {@code @Version}. Two concurrent approvals
     * of the same employee both read the same version; only the first commit succeeds, while
     * the second fails with an optimistic-locking exception that the global handler maps to a
     * {@code CONCURRENT_MODIFICATION} response. This guarantees the balance can never go negative.
     * A database check constraint on {@code remaining_leave_days} acts as a final safety net.
     */
    @Transactional
    public LeaveRequestResponse approve(Long leaveRequestId, ApproveLeaveRequest request) {
        LeaveRequest leaveRequest = getEntity(leaveRequestId);
        requirePending(leaveRequest, "approve");
        requireLeaveTypeActive(leaveRequest.getLeaveType(), "approve");

        Employee employee = leaveRequest.getEmployee();
        BigDecimal requestedDays = leaveRequest.getRequestedDays();
        if (!employee.hasSufficientBalance(requestedDays)) {
            throw new InsufficientLeaveBalanceException();
        }

        employee.deductLeave(requestedDays);
        leaveRequest.approve(request.reviewComment(), Instant.now(clock));
        return leaveRequestMapper.toResponse(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse reject(Long leaveRequestId, RejectLeaveRequest request) {
        LeaveRequest leaveRequest = getEntity(leaveRequestId);
        requirePending(leaveRequest, "reject");

        leaveRequest.reject(request.reviewComment(), Instant.now(clock));
        return leaveRequestMapper.toResponse(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse cancel(Long leaveRequestId) {
        LeaveRequest leaveRequest = getEntity(leaveRequestId);
        requirePending(leaveRequest, "cancel");

        leaveRequest.cancel();
        return leaveRequestMapper.toResponse(leaveRequest);
    }

    private LeaveRequest getEntity(Long leaveRequestId) {
        return leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new LeaveRequestNotFoundException(leaveRequestId));
    }

    private void requirePending(LeaveRequest leaveRequest, String action) {
        if (!leaveRequest.isPending()) {
            throw new InvalidLeaveRequestStatusException(action, leaveRequest.getStatus());
        }
    }

    private void requireLeaveTypeActive(LeaveType leaveType, String action) {
        if (!leaveType.isActive()) {
            throw new InactiveLeaveTypeException(action, leaveType.getCode());
        }
    }
}
