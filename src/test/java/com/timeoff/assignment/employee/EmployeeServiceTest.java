package com.timeoff.assignment.employee;

import com.timeoff.assignment.employee.dto.CreateEmployeeRequest;
import com.timeoff.assignment.employee.dto.EmployeeResponse;
import com.timeoff.assignment.employee.dto.UpdateEmployeeStatusRequest;
import com.timeoff.assignment.employee.mapper.EmployeeMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(employeeRepository, new EmployeeMapperImpl());
    }

    @Test
    void createsActiveEmployeeWithBalanceEqualToAllowance() {
        when(employeeRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        EmployeeResponse response = service.create(
                new CreateEmployeeRequest("Jane", "Doe", "jane@example.com", 25));

        assertThat(response.active()).isTrue();
        assertThat(response.annualLeaveAllowance()).isEqualTo(25);
        assertThat(response.remainingLeaveDays()).isEqualByComparingTo(BigDecimal.valueOf(25));
    }

    @Test
    void rejectsDuplicateEmail() {
        when(employeeRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new CreateEmployeeRequest("Jane", "Doe", "jane@example.com", 25)))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void throwsWhenEmployeeNotFound() {
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L)).isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void deactivatesEmployee() {
        Employee employee = new Employee("Jane", "Doe", "jane@example.com", 25);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponse response = service.updateStatus(1L, new UpdateEmployeeStatusRequest(false));

        assertThat(response.active()).isFalse();
    }
}
