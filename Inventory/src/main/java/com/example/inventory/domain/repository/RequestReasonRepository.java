// src/main/java/com/example/inventory/domain/repository/RequestReasonRepository.java
package com.example.inventory.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.inventory.domain.model.RequestReason;

public interface RequestReasonRepository extends JpaRepository<RequestReason, Long> {}