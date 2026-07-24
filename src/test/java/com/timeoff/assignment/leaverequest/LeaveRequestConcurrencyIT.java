package com.timeoff.assignment.leaverequest;

import com.timeoff.assignment.employee.Employee;
import com.timeoff.assignment.employee.EmployeeRepository;
import com.timeoff.assignment.leaverequest.dto.ApproveLeaveRequest;
import com.timeoff.assignment.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that two concurrent approvals for the same employee cannot drive the leave balance
 * negative. The employee has enough balance for only one of the two pending requests; optimistic
 * locking must ensure exactly one approval succeeds.
 */
@SpringBootTest
class LeaveRequestConcurrencyIT extends AbstractIntegrationTest {

    @Autowired
    private LeaveRequestService leaveRequestService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private Long employeeId;
    private Long firstRequestId;
    private Long secondRequestId;

    @BeforeEach
    void setUp() {
        leaveRequestRepository.deleteAll();
        employeeRepository.deleteAll();

        // Allowance (and therefore balance) is 5 days: enough for exactly one 5-day request.
        Employee employee = employeeRepository.save(
                new Employee("Race", "Condition", "race@example.com", 5));
        employeeId = employee.getId();

        LocalDate firstWeek = LocalDate.of(2027, 3, 1);   // Monday
        LocalDate secondWeek = LocalDate.of(2027, 3, 8);  // Monday
        firstRequestId = leaveRequestRepository.save(
                new LeaveRequest(employee, firstWeek, firstWeek.plusDays(4), BigDecimal.valueOf(5), "A")).getId();
        secondRequestId = leaveRequestRepository.save(
                new LeaveRequest(employee, secondWeek, secondWeek.plusDays(4), BigDecimal.valueOf(5), "B")).getId();
    }

    @Test
    void concurrentApprovalsDoNotProduceNegativeBalance() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(approvalTask(firstRequestId, barrier));
            Future<Boolean> second = executor.submit(approvalTask(secondRequestId, barrier));

            int successes = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);

            assertThat(successes)
                    .as("exactly one of the two concurrent approvals should succeed")
                    .isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }

        Employee reloaded = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(reloaded.getRemainingLeaveDays())
                .as("balance must never go negative")
                .isEqualByComparingTo(BigDecimal.ZERO);

        long approvedCount = leaveRequestRepository.findAll().stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED)
                .count();
        assertThat(approvedCount).isEqualTo(1);
    }

    private Callable<Boolean> approvalTask(Long requestId, CyclicBarrier barrier) {
        return () -> {
            barrier.await();
            try {
                leaveRequestService.approve(requestId, new ApproveLeaveRequest("concurrent"));
                return true;
            } catch (RuntimeException expectedOnLoser) {
                // The losing thread fails with either an optimistic-locking failure or an
                // insufficient-balance error, depending on interleaving. Both are acceptable.
                return false;
            }
        };
    }
}
