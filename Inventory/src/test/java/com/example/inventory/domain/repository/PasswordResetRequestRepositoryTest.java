package com.example.inventory.domain.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles; // ▼ 追記

import com.example.inventory.domain.model.Department;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.PasswordResetRequest;
import com.example.inventory.domain.model.Role;

@ActiveProfiles("test") // ▼ 追記
@DataJpaTest
public class PasswordResetRequestRepositoryTest {

    @Autowired private PasswordResetRequestRepository resetRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;

    @Test
    void 未承認の申請のみを取得できること() {
        Department dept = new Department();
        dept.setName("テスト部");
        departmentRepository.save(dept);

        Employee emp = new Employee();
        emp.setName("テスト社員");
        emp.setEmail("test@example.com");
        emp.setPassword("password");
        emp.setRole(Role.USER);
        emp.setDepartment(dept);
        emp.setAccountNonLocked(true);
        emp.setFailedAttempt(0);
        employeeRepository.save(emp);

        PasswordResetRequest req1 = new PasswordResetRequest();
        req1.setEmployee(emp);
        req1.setToken("token1");
        req1.setRequestedAt(LocalDateTime.now());
        req1.setExpiresAt(LocalDateTime.now().plusHours(24));
        req1.setIsApproved(false);

        PasswordResetRequest req2 = new PasswordResetRequest();
        req2.setEmployee(emp);
        req2.setToken("token2");
        req2.setRequestedAt(LocalDateTime.now());
        req2.setExpiresAt(LocalDateTime.now().plusHours(24));
        req2.setIsApproved(true);

        resetRepository.saveAll(List.of(req1, req2));

        List<PasswordResetRequest> pending = resetRepository.findByIsApprovedFalse();

        assertEquals(1, pending.size());
        assertEquals("token1", pending.get(0).getToken());
    }

    @Test
    void トークン文字列から申請を正しく取得できること() {
        Department dept = new Department();
        dept.setName("テスト部");
        departmentRepository.save(dept);

        Employee emp = new Employee();
        emp.setName("テスト社員");
        emp.setEmail("test2@example.com");
        emp.setPassword("password");
        emp.setRole(Role.USER);
        emp.setDepartment(dept);
        emp.setAccountNonLocked(true);
        emp.setFailedAttempt(0);
        employeeRepository.save(emp);

        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmployee(emp);
        req.setToken("target-token");
        req.setRequestedAt(LocalDateTime.now());
        req.setExpiresAt(LocalDateTime.now().plusHours(24));
        req.setIsApproved(false);
        req.setIsUsed(false);
        resetRepository.save(req);

        Optional<PasswordResetRequest> result = resetRepository.findByToken("target-token");

        assertTrue(result.isPresent());
        assertEquals(emp.getId(), result.get().getEmployee().getId());
    }
}