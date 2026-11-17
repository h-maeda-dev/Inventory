// src/test/java/com/example/inventory/application/service/PasswordResetServiceTest.java
package com.example.inventory.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.PasswordResetRequest;
import com.example.inventory.domain.repository.EmployeeRepository;
import com.example.inventory.domain.repository.PasswordResetRequestRepository;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private PasswordResetRequestRepository resetRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void パスワードリセット申請が正常に作成されること() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmail("test@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        passwordResetService.requestReset(1L, "test@example.com");

        verify(resetRepository, times(1)).save(argThat(req -> 
            req.getEmployee().getId().equals(1L) && 
            req.getToken() != null && 
            !req.getIsApproved()
        ));
    }

    @Test
    void 申請時にメールアドレスが不一致の場合は例外が発生すること() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmail("test@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        assertThrows(IllegalArgumentException.class, () -> {
            passwordResetService.requestReset(1L, "wrong@example.com");
        });
    }

    @Test
    void 管理者が承認するとフラグが更新されメールが送信されること() {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setId(10L);
        req.setToken("dummy-token");
        Employee emp = new Employee();
        emp.setEmail("test@example.com");
        req.setEmployee(emp);
        req.setIsApproved(false);

        when(resetRepository.findById(10L)).thenReturn(Optional.of(req));

        passwordResetService.approveRequest(10L);

        assertTrue(req.getIsApproved());
        verify(resetRepository, times(1)).save(req);
        verify(notificationService, times(1)).sendPasswordResetUrl("test@example.com", "dummy-token");
    }

    @Test
    void トークンを使用したパスワード更新が正常に行われること() {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setToken("valid-token");
        req.setIsApproved(true);
        req.setIsUsed(false);
        req.setExpiresAt(LocalDateTime.now().plusHours(1));
        Employee emp = new Employee();
        req.setEmployee(emp);

        when(resetRepository.findByToken("valid-token")).thenReturn(Optional.of(req));
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded");

        passwordResetService.updatePasswordWithToken("valid-token", "newPassword");

        assertTrue(req.getIsUsed());
        assertEquals("encoded", emp.getPassword());
        verify(employeeRepository, times(1)).save(emp);
        verify(resetRepository, times(1)).save(req);
    }
}