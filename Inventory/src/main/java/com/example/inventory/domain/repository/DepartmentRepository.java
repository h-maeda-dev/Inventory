// src/main/java/com/example/inventory/domain/repository/DepartmentRepository.java
package com.example.inventory.domain.repository;
import com.example.inventory.domain.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DepartmentRepository extends JpaRepository<Department, Long> {}