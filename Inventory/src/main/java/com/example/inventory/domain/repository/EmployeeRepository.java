// src/main/java/com/example/inventory/domain/repository/EmployeeRepository.java
package com.example.inventory.domain.repository;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    
    // 権限（Role）を指定して該当する社員一覧を取得
    List<Employee> findByRole(Role role);
}