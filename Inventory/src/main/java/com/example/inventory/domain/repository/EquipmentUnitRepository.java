// src/main/java/com/example/inventory/domain/repository/EquipmentUnitRepository.java
package com.example.inventory.domain.repository;
import com.example.inventory.domain.model.EquipmentUnit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentUnitRepository extends JpaRepository<EquipmentUnit, Long> {}