package com.timeoff.assignment.leaverequest;

import com.timeoff.assignment.employee.Employee;
import com.timeoff.assignment.employee.EmployeeInactiveException;
import com.timeoff.assignment.employee.EmployeeService;
import com.timeoff.assignment.leaverequest.dto.ApproveLeaveRequest;
import com.timeoff.assignment.leaverequest.dto.CreateLeaveRequest;
import com.timeoff.assignment.leaverequest.dto.LeaveRequestResponse;
import com.timeoff.assignment.leaverequest.dto.RejectLeaveRequest;
import com.timeoff.assignment.leaverequest.exception.InsufficientLeaveBalanceException;
import com.timeoff.assignment.leaverequest.exception.InvalidDateRangeException;
import com.timeoff.assignment.leaverequest.exception.InvalidLeaveRequestStatusException;
import com.timeoff.assignment.leaverequest.exception.LeaveRequestInThePastException;
import com.timeoff.assignment.leaverequest.exception.NoWorkingDaysException;
import com.timeoff.assignment.leaverequest.exception.OverlappingLeaveRequestException;
import com.timeoff.assignment.leaverequest.mapper.LeaveRequestMapperImpl;
import com.timeoff.assignment.leavetype.LeaveType;
import com.timeoff.assignment.leavetype.LeaveTypeCode;
import com.timeoff.assignment.leavetype.LeaveTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    // Fixed "today" = Wednesday, 2026-07-01 (UTC).
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-01T09:00:00Z"), ZoneOffset.UTC);

    // Monday .. Friday -> 5 working days, safely in the future relative to CLOCK.
    private static final LocalDate START = LocalDate.of(2026, 9, 14);
    private static final LocalDate END = LocalDate.of(2026, 9, 18);

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeService employeeService;

    @Mock
    private LeaveTypeService leaveTypeService;

    private LeaveRequestService service;

    @BeforeEach
    void setUp() {
        service = new LeaveRequestService(
                leaveRequestRepository,
                employeeService,
                leaveTypeService,
                new WorkingDaysCalculator(),
                new LeaveRequestMapperImpl(),
                CLOCK);
    }

    private Employee activeEmployee(int allowance, double remaining) {
        Employee employee = new Employee("Jane", "Doe", "jane@example.com", allowance);
        ReflectionTestUtils.setField(employee, "id", 1L);
        ReflectionTestUtils.setField(employee, "remainingLeaveDays", BigDecimal.valueOf(remaining));
        return employee;
    }

    private LeaveType getLeaveType(boolean deductsFromAnnualBalance) {
        LeaveType leaveType = new LeaveType("Annual leave", LeaveTypeCode.ANNUAL_LEAVE, deductsFromAnnualBalance);
        ReflectionTestUtils.setField(leaveType, "id", 1L);

        return leaveType;
    }

    private CreateLeaveRequest createRequest(LocalDate start, LocalDate end) {
        return new CreateLeaveRequest(1L, start, end, "Vacation", 1L);
    }

    private LeaveRequest pendingRequest(Employee employee, double days) {
        return new LeaveRequest(employee, START, END, BigDecimal.valueOf(days), "Vacation", getLeaveType(true));
    }

    private LeaveRequest pendingRequest(Employee employee, double days, boolean leaveTypeDeductsFromAnnualBalance) {
        return new LeaveRequest(employee, START, END, BigDecimal.valueOf(days), "Vacation", getLeaveType(leaveTypeDeductsFromAnnualBalance));
    }

    // --- creation ---

    @Test
    void createsPendingRequestAndComputesWorkingDays() {
        Employee employee = spy(activeEmployee(25, 25));
        when(employeeService.getEntity(1L)).thenReturn(employee);
        when(leaveRequestRepository.existsOverlapping(any(), anyList(), any(), any())).thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(leaveTypeService.getEntity(1L)).thenReturn(getLeaveType(true));

        LeaveRequestResponse response = service.create(createRequest(START, END));

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.PENDING);
        assertThat(response.requestedDays()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(response.reviewedAt()).isNull();
        verify(employee, times(1)).hasSufficientBalance(BigDecimal.valueOf(5));
    }

    @Test
    void create_nonDeductingLeaveType_createLeaveRequestWithoutCheckingForSufficientBalance() {
        Employee employee = spy(activeEmployee(25, 25));
        LeaveType leaveType = getLeaveType(false);
        when(employeeService.getEntity(1L)).thenReturn(employee);
        when(leaveRequestRepository.existsOverlapping(any(), anyList(), any(), any())).thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(leaveTypeService.getEntity(1L)).thenReturn(leaveType);

        LeaveRequestResponse response = service.create(createRequest(START, END));

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.PENDING);
        assertThat(response.requestedDays()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(response.reviewedAt()).isNull();
        assertThat(response.requestedDays()).isEqualByComparingTo(BigDecimal.valueOf(5));

        assertThat(response.leaveTypeId()).isEqualTo(1L);
        verify(employee, never()).hasSufficientBalance(BigDecimal.valueOf(5));
    }


    @Test
    void rejectsCreationForInactiveEmployee() {
        Employee employee = activeEmployee(25, 25);
        employee.deactivate();
        when(employeeService.getEntity(1L)).thenReturn(employee);
        when(leaveTypeService.getEntity(1L)).thenReturn(getLeaveType(true));

        assertThatThrownBy(() -> service.create(createRequest(START, END)))
                .isInstanceOf(EmployeeInactiveException.class);
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void rejectsInvertedDateRange() {
        when(employeeService.getEntity(1L)).thenReturn(activeEmployee(25, 25));
        when(leaveTypeService.getEntity(1L)).thenReturn(getLeaveType(true));

        assertThatThrownBy(() -> service.create(createRequest(END, START)))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void rejectsDatesInThePast() {
        when(employeeService.getEntity(1L)).thenReturn(activeEmployee(25, 25));
        when(leaveTypeService.getEntity(1L)).thenReturn(getLeaveType(true));

        LocalDate pastStart = LocalDate.of(2026, 6, 1);
        LocalDate pastEnd = LocalDate.of(2026, 6, 5);
        assertThatThrownBy(() -> service.create(createRequest(pastStart, pastEnd)))
                .isInstanceOf(LeaveRequestInThePastException.class);
    }

    @Test
    void rejectsRangeWithoutWorkingDays() {
        when(employeeService.getEntity(1L)).thenReturn(activeEmployee(25, 25));
        when(leaveTypeService.getEntity(1L)).thenReturn(getLeaveType(true));

        // Saturday .. Sunday
        LocalDate saturday = LocalDate.of(2026, 9, 19);
        LocalDate sunday = LocalDate.of(2026, 9, 20);
        assertThatThrownBy(() -> service.create(createRequest(saturday, sunday)))
                .isInstanceOf(NoWorkingDaysException.class);
    }

    @Test
    void rejectsOverlappingRequest() {
        when(employeeService.getEntity(1L)).thenReturn(activeEmployee(25, 25));
        when(leaveTypeService.getEntity(1L)).thenReturn(getLeaveType(true));
        when(leaveRequestRepository.existsOverlapping(any(), anyList(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest(START, END)))
                .isInstanceOf(OverlappingLeaveRequestException.class);
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void rejectsCreationWhenBalanceInsufficient() {
        when(employeeService.getEntity(1L)).thenReturn(activeEmployee(25, 3));
        when(leaveTypeService.getEntity(1L)).thenReturn(getLeaveType(true));
        when(leaveRequestRepository.existsOverlapping(any(), anyList(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.create(createRequest(START, END)))
                .isInstanceOf(InsufficientLeaveBalanceException.class);
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void overlapCheckUsesPendingAndApprovedStatuses() {
        when(employeeService.getEntity(1L)).thenReturn(activeEmployee(25, 25));
        when(leaveTypeService.getEntity(1L)).thenReturn(getLeaveType(true));
        when(leaveRequestRepository.existsOverlapping(eq(1L),
                eq(List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED)),
                eq(START), eq(END))).thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(createRequest(START, END));

        verify(leaveRequestRepository).existsOverlapping(1L,
                List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED), START, END);
    }

    // --- approval ---

    @Test
    void approvesPendingRequestAndDeductsBalance() {
        Employee employee = activeEmployee(25, 10);
        LeaveRequest request = pendingRequest(employee, 5);
        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(request));

        LeaveRequestResponse response = service.approve(7L, new ApproveLeaveRequest("Approved"));

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.APPROVED);
        assertThat(response.reviewComment()).isEqualTo("Approved");
        assertThat(response.reviewedAt()).isEqualTo(Instant.now(CLOCK));
        assertThat(employee.getRemainingLeaveDays()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    void rejectsApprovalWhenBalanceInsufficientAtApprovalTime() {
        Employee employee = activeEmployee(25, 2);
        LeaveRequest request = pendingRequest(employee, 5);
        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approve(7L, new ApproveLeaveRequest(null)))
                .isInstanceOf(InsufficientLeaveBalanceException.class);
        assertThat(employee.getRemainingLeaveDays()).isEqualByComparingTo(BigDecimal.valueOf(2));
    }

    @Test
    void cannotApproveNonPendingRequest() {
        Employee employee = activeEmployee(25, 10);
        LeaveRequest request = pendingRequest(employee, 5);
        request.cancel();
        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approve(7L, new ApproveLeaveRequest(null)))
                .isInstanceOf(InvalidLeaveRequestStatusException.class);
    }

    @Test
    void approve_nonDeductingLeaveType_approveLeaveRequestWithoutCheckingForSufficientBalance() {
        Employee employee = spy(activeEmployee(25, 10));
        LeaveRequest request = pendingRequest(employee, 5, false);
        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(request));

        LeaveRequestResponse response = service.approve(7L, new ApproveLeaveRequest("Approved"));

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.APPROVED);
        assertThat(response.reviewComment()).isEqualTo("Approved");
        assertThat(response.reviewedAt()).isEqualTo(Instant.now(CLOCK));
        assertThat(employee.getRemainingLeaveDays()).isEqualByComparingTo(BigDecimal.valueOf(10));

        assertThat(response.leaveTypeId()).isEqualTo(1L);
        verify(employee, never()).hasSufficientBalance(BigDecimal.valueOf(5));
        verify(employee, never()).deductLeave(any());
    }

    // --- rejection ---

    @Test
    void rejectsPendingRequestWithoutTouchingBalance() {
        Employee employee = activeEmployee(25, 10);
        LeaveRequest request = pendingRequest(employee, 5);
        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(request));

        LeaveRequestResponse response = service.reject(7L, new RejectLeaveRequest("No coverage"));

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.REJECTED);
        assertThat(response.reviewComment()).isEqualTo("No coverage");
        assertThat(employee.getRemainingLeaveDays()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    void cannotRejectNonPendingRequest() {
        Employee employee = activeEmployee(25, 10);
        LeaveRequest request = pendingRequest(employee, 5);
        request.approve("done", Instant.now(CLOCK));
        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.reject(7L, new RejectLeaveRequest("late")))
                .isInstanceOf(InvalidLeaveRequestStatusException.class);
    }

    // --- cancellation ---

    @Test
    void cancelsPendingRequest() {
        Employee employee = activeEmployee(25, 10);
        LeaveRequest request = pendingRequest(employee, 5);
        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(request));

        LeaveRequestResponse response = service.cancel(7L);

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.CANCELLED);
        assertThat(employee.getRemainingLeaveDays()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    void cannotCancelApprovedRequest() {
        Employee employee = activeEmployee(25, 10);
        LeaveRequest request = pendingRequest(employee, 5);
        request.approve("ok", Instant.now(CLOCK));
        when(leaveRequestRepository.findById(7L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.cancel(7L))
                .isInstanceOf(InvalidLeaveRequestStatusException.class);
    }
}
