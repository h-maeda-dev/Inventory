// src/main/java/com/example/inventory/application/dto/PendingRequestDto.java
package com.example.inventory.application.dto;
import java.time.LocalDateTime;
public record PendingRequestDto(
    Long id, LocalDateTime requestedAt, String username, String requestType,
    String itemName, String managementCode, LocalDateTime dueDate, Integer quantity
) {}