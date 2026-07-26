package com.timeoff.assignment.leavetype;

import com.timeoff.assignment.common.persistence.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leave_type",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "leave_type_unique_code",
                columnNames = "code"
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 25)
    private LeaveTypeCode code;

    @Column(name = "deducts_from_annual_balance", nullable = false)
    private boolean deductsFromAnnualBalance;

    @Column(name = "active", nullable = false)
    private boolean active;


    /**
     * Creates a new active leave type.
     * {@code requestedDays} is computed by the backend, never supplied by the client.
     */
    public LeaveType(String name, LeaveTypeCode code, boolean deductsFromAnnualBalance) {
        this.name = name;
        this.code = code;
        this.deductsFromAnnualBalance = deductsFromAnnualBalance;
        this.active = true;
    }


    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
