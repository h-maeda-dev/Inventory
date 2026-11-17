// src/main/java/com/example/inventory/application/dto/RequestViewDto.java
package com.example.inventory.application.dto;

import java.time.LocalDateTime;

/**
 * 申請履歴および貸出中の備品をダッシュボードや履歴画面に表示するためのDTO（Data Transfer Object）。
 * データベースのEntity（Request等）を直接画面へ渡さず、必要な項目だけを抽出・平坦化することで、
 * 画面テンプレートの依存関係を減らし、安全にデータを受け渡します。
 */
public record RequestViewDto(
    Long id, 
    String requestType, 
    String itemName, 
    String managementCode,
    Integer quantity, 
    String status, 
    LocalDateTime requestedAt, 
    LocalDateTime dueDate, 
    LocalDateTime returnedAt, 
    String employeeDisplayName
) {}