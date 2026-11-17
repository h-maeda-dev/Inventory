// src/main/java/com/example/inventory/application/service/LoginAttemptService.java
package com.example.inventory.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    
    public static final int MAX_FAILED_ATTEMPTS = 5; // 最大失敗許容回数
    private static final long LOCK_TIME_DURATION_MINUTES = 15; // ロック期間（分）

    private final EmployeeRepository employeeRepository;

    @Transactional
    public void loginSucceeded(String username) {
        try {
            Long empId = Long.valueOf(username);
            Optional<Employee> opt = employeeRepository.findById(empId);
            if (opt.isPresent()) {
                Employee employee = opt.get();
                if (employee.getFailedAttempt() > 0) {
                    employee.setFailedAttempt(0);
                    employeeRepository.save(employee);
                }
            }
        } catch (NumberFormatException e) {
            // 数値変換エラー時は無視（後続で認証失敗となるため）
        }
    }

    @Transactional
    public void loginFailed(String username) {
        try {
            Long empId = Long.valueOf(username);
            Optional<Employee> opt = employeeRepository.findById(empId);
            if (opt.isPresent()) {
                Employee employee = opt.get();
                if (employee.isAccountNonLocked()) {
                    int attempts = employee.getFailedAttempt() + 1;
                    employee.setFailedAttempt(attempts);
                    
                    if (attempts >= MAX_FAILED_ATTEMPTS) {
                        employee.setAccountNonLocked(false);
                        employee.setLockTime(LocalDateTime.now());
                    }
                    employeeRepository.save(employee);
                }
            }
        } catch (NumberFormatException e) {
            // 無視
        }
    }

    @Transactional
    public boolean unlockWhenTimeExpired(Employee employee) {
        if (employee.getLockTime() == null) return false;
        
        LocalDateTime lockExpiredTime = employee.getLockTime().plusMinutes(LOCK_TIME_DURATION_MINUTES);
        if (lockExpiredTime.isBefore(LocalDateTime.now())) {
            employee.setAccountNonLocked(true);
            employee.setLockTime(null);
            employee.setFailedAttempt(0);
            employeeRepository.save(employee);
            return true; // ロック解除成功
        }
        return false; // まだロック期間中
    }
}