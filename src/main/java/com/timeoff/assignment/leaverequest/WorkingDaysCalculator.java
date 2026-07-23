package com.timeoff.assignment.leaverequest;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/**
 * Calculates the number of working days in an inclusive date range.
 *
 * <p>Weekends (Saturday and Sunday) are not counted. Public holidays are intentionally
 * not considered in this version. The result is a {@link BigDecimal} so the model can later
 * support fractional days without changing the calculator's contract.
 */
@Component
public class WorkingDaysCalculator {

    private static final Set<DayOfWeek> WEEKEND = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    /**
     * Counts the working days between {@code startDate} and {@code endDate}, both inclusive.
     *
     * @throws IllegalArgumentException if either date is null or the range is inverted
     */
    public BigDecimal countWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }

        long workingDays = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (!WEEKEND.contains(date.getDayOfWeek())) {
                workingDays++;
            }
        }
        return BigDecimal.valueOf(workingDays);
    }
}
