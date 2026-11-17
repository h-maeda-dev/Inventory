// src/main/java/com/example/inventory/domain/repository/HolidayRepository.java
package com.example.inventory.domain.repository;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.inventory.domain.model.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {}