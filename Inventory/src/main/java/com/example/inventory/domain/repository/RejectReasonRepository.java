// src/main/java/com/example/inventory/domain/repository/RejectReasonRepository.java
package com.example.inventory.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.inventory.domain.model.RejectReason;

public interface RejectReasonRepository extends JpaRepository<RejectReason, Long> {}