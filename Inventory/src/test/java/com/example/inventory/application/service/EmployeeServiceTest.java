// src/test/java/com/example/inventory/application/service/EmployeeServiceTest.java
package com.example.inventory.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.inventory.domain.model.Department;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.Role;
import com.example.inventory.domain.repository.DepartmentRepository;
import com.example.inventory.domain.repository.EmployeeRepository;
import com.example.inventory.presentation.form.EmployeeForm;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void 全ての社員が取得できること() {
        Employee e1 = new Employee(); e1.setName("テスト社員A");
        Employee e2 = new Employee(); e2.setName("テスト社員B");
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void 新規社員の登録が正しく行われること() {
        // Arrange
        EmployeeForm form = new EmployeeForm();
        form.setName("新規社員");
        form.setEmail("test@example.com");
        form.setPassword("rawpassword");
        form.setRole(Role.USER);
        form.setDepartmentId(1L);

        Department dept = new Department();
        dept.setId(1L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(passwordEncoder.encode("rawpassword")).thenReturn("encodedpassword");

        // Act
        employeeService.saveEmployee(form);

        // Assert
        verify(employeeRepository, times(1)).save(argThat(emp -> 
            emp.getName().equals("新規社員") &&
            emp.getEmail().equals("test@example.com") &&
            emp.getPassword().equals("encodedpassword") &&
            emp.getDepartment().getId().equals(1L)
        ));
    }
    
    @Test
    void 存在しない部署を指定して社員登録すると例外が発生すること() {
        EmployeeForm form = new EmployeeForm();
        form.setDepartmentId(99L);
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.saveEmployee(form);
        });
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}