package com.timeoff.assignment.employee;

import com.timeoff.assignment.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    @Setter
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @Setter
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    @Setter
    private String email;

    @Column(name = "annual_leave_allowance", nullable = false)
    private int annualLeaveAllowance;

    @Column(name = "remaining_leave_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal remainingLeaveDays;

    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * Creates a new active employee whose remaining balance equals the annual allowance.
     */
    public Employee(String firstName, String lastName, String email, int annualLeaveAllowance) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.annualLeaveAllowance = annualLeaveAllowance;
        this.remainingLeaveDays = BigDecimal.valueOf(annualLeaveAllowance);
        this.active = true;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    /**
     * Deducts the given number of days from the remaining balance.
     *
     * @throws IllegalArgumentException if the deduction would make the balance negative;
     *                                  callers must validate sufficiency beforehand.
     */
    public void deductLeave(BigDecimal days) {
        BigDecimal updated = this.remainingLeaveDays.subtract(days);
        if (updated.signum() < 0) {
            throw new IllegalArgumentException("Deduction would result in a negative leave balance");
        }
        this.remainingLeaveDays = updated;
    }

    public boolean hasSufficientBalance(BigDecimal days) {
        return this.remainingLeaveDays.compareTo(days) >= 0;
    }
}
