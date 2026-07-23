package com.timeoff.assignment.leaverequest;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Reusable {@link Specification} fragments for filtering leave requests.
 * Each returns {@code null} when its filter value is absent, so they compose cleanly.
 */
final class LeaveRequestSpecifications {

    private LeaveRequestSpecifications() {
    }

    static Specification<LeaveRequest> hasEmployeeId(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("employee").get("id"), employeeId);
    }

    static Specification<LeaveRequest> hasStatus(LeaveRequestStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    static Specification<LeaveRequest> startDateFrom(LocalDate from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), from);
    }

    static Specification<LeaveRequest> startDateTo(LocalDate to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), to);
    }
}
