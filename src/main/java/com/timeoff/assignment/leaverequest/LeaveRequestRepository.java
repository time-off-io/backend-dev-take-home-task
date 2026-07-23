package com.timeoff.assignment.leaverequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    /**
     * Returns whether the employee already has a request in one of the given statuses whose
     * date range overlaps [{@code startDate}, {@code endDate}] (inclusive on both ends).
     * Two ranges overlap when {@code existing.start <= new.end AND existing.end >= new.start}.
     */
    @Query("""
            select count(lr) > 0 from LeaveRequest lr
            where lr.employee.id = :employeeId
              and lr.status in :statuses
              and lr.startDate <= :endDate
              and lr.endDate >= :startDate
            """)
    boolean existsOverlapping(@Param("employeeId") Long employeeId,
                              @Param("statuses") List<LeaveRequestStatus> statuses,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);
}
