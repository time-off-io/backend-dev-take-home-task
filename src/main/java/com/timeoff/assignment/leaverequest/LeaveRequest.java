package com.timeoff.assignment.leaverequest;

import com.timeoff.assignment.common.persistence.AuditableEntity;
import com.timeoff.assignment.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "leave_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "requested_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal requestedDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveRequestStatus status;

    @Column(name = "reason")
    private String reason;

    @Column(name = "review_comment")
    private String reviewComment;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    /**
     * Creates a new leave request in {@link LeaveRequestStatus#PENDING} status.
     * {@code requestedDays} is computed by the backend, never supplied by the client.
     */
    public LeaveRequest(Employee employee, LocalDate startDate, LocalDate endDate,
                        BigDecimal requestedDays, String reason) {
        this.employee = employee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestedDays = requestedDays;
        this.reason = reason;
        this.status = LeaveRequestStatus.PENDING;
    }

    public boolean isPending() {
        return status == LeaveRequestStatus.PENDING;
    }

    public void approve(String reviewComment, Instant reviewedAt) {
        this.status = LeaveRequestStatus.APPROVED;
        this.reviewComment = reviewComment;
        this.reviewedAt = reviewedAt;
    }

    public void reject(String reviewComment, Instant reviewedAt) {
        this.status = LeaveRequestStatus.REJECTED;
        this.reviewComment = reviewComment;
        this.reviewedAt = reviewedAt;
    }

    public void cancel() {
        this.status = LeaveRequestStatus.CANCELLED;
    }
}
