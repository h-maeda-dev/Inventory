// src/main/java/com/example/inventory/domain/repository/PasswordResetRequestRepository.java
package com.example.inventory.domain.repository;
import com.example.inventory.domain.model.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
    List<PasswordResetRequest> findByIsApprovedFalse();
    Optional<PasswordResetRequest> findByToken(String token);
}