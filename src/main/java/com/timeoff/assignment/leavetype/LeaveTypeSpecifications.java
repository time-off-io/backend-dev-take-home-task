package com.timeoff.assignment.leavetype;


import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable {@link Specification} fragments for filtering leave types.
 * Each returns {@code null} when its filter value is absent, so they compose cleanly.
 */
final class LeaveTypeSpecifications {

    private LeaveTypeSpecifications() {
    }

    static Specification<LeaveType> isActive(Boolean active) {
        if (active == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }
}
