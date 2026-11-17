package com.example.inventory.domain.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles; // ▼ 追記（自動importで入らない場合）

import com.example.inventory.domain.model.Department;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.Role;

@DataJpaTest
@ActiveProfiles("test") // ▼ 追記
public class EmployeeRepositoryTest {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DepartmentRepository departmentRepository;

    @Test
    void メールアドレスで社員を正しく取得できること() {
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

        Optional<Employee> result = employeeRepository.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("テスト社員", result.get().getName());
    }

    @Test
    void 権限を指定して該当する社員一覧を取得できること() {
        Department dept = new Department();
        dept.setName("テスト部");
        departmentRepository.save(dept);

        Employee emp1 = new Employee();
        emp1.setName("一般社員");
        emp1.setEmail("user@example.com");
        emp1.setPassword("pass");
        emp1.setRole(Role.USER);
        emp1.setDepartment(dept);
        emp1.setAccountNonLocked(true);
        emp1.setFailedAttempt(0);
        
        Employee emp2 = new Employee();
        emp2.setName("管理者");
        emp2.setEmail("admin@example.com");
        emp2.setPassword("pass");
        emp2.setRole(Role.ADMIN);
        emp2.setDepartment(dept);
        emp2.setAccountNonLocked(true);
        emp2.setFailedAttempt(0);
        
        employeeRepository.saveAll(List.of(emp1, emp2));

        List<Employee> admins = employeeRepository.findByRole(Role.ADMIN);

        assertEquals(1, admins.size());
        assertEquals("管理者", admins.get(0).getName());
    }
}