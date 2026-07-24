package com.timeoff.assignment.leaverequest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkingDaysCalculatorTest {

    private final WorkingDaysCalculator calculator = new WorkingDaysCalculator();

    @Test
    void countsSingleWorkingDay() {
        // Monday
        LocalDate day = LocalDate.of(2026, 9, 14);
        assertThat(calculator.countWorkingDays(day, day)).isEqualByComparingTo(BigDecimal.valueOf(1));
    }

    @Test
    void excludesWeekendsWithinRange() {
        // Monday 2026-09-14 .. Friday 2026-09-18 -> 5 working days
        assertThat(calculator.countWorkingDays(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 18)))
                .isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    void spanningTwoWeeksExcludesTheWeekend() {
        // Monday 2026-09-14 .. Monday 2026-09-21 -> 6 working days (weekend excluded)
        assertThat(calculator.countWorkingDays(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 21)))
                .isEqualByComparingTo(BigDecimal.valueOf(6));
    }

    @Test
    void weekendOnlyRangeHasNoWorkingDays() {
        // Saturday 2026-09-19 .. Sunday 2026-09-20
        assertThat(calculator.countWorkingDays(LocalDate.of(2026, 9, 19), LocalDate.of(2026, 9, 20)))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void singleSaturdayHasNoWorkingDays() {
        LocalDate saturday = LocalDate.of(2026, 9, 19);
        assertThat(calculator.countWorkingDays(saturday, saturday))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void rejectsInvertedRange() {
        assertThatThrownBy(() ->
                calculator.countWorkingDays(LocalDate.of(2026, 9, 18), LocalDate.of(2026, 9, 14)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullDates() {
        assertThatThrownBy(() -> calculator.countWorkingDays(null, LocalDate.of(2026, 9, 14)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
