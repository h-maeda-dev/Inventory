package com.example.inventory.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
public class LoginAttemptServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private LoginAttemptService loginAttemptService;

    @Test
    void ロック期間が過ぎている場合にロックが自動解除され失敗回数がリセットされること() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setAccountNonLocked(false);
        employee.setFailedAttempt(5);
        employee.setLockTime(LocalDateTime.now().minusMinutes(16));

        boolean result = loginAttemptService.unlockWhenTimeExpired(employee);

        assertTrue(result);
        assertTrue(employee.isAccountNonLocked());
        assertNull(employee.getLockTime());
        assertEquals(0, employee.getFailedAttempt());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void ロック期間がまだ過ぎていない場合はロックが解除されないこと() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setAccountNonLocked(false);
        employee.setFailedAttempt(5);
        employee.setLockTime(LocalDateTime.now().minusMinutes(5));

        boolean result = loginAttemptService.unlockWhenTimeExpired(employee);

        assertFalse(result);
        assertFalse(employee.isAccountNonLocked());
        assertNotNull(employee.getLockTime());
        assertEquals(5, employee.getFailedAttempt());
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}