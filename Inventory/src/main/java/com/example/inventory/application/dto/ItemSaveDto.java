// src/main/java/com/example/inventory/application/dto/ItemSaveDto.java
package com.example.inventory.application.dto;

import com.example.inventory.domain.model.ItemType;
import com.example.inventory.domain.model.PeriodUnit;

/**
 * 品目マスタ登録用のDTO（Data Transfer Object）クラス。
 * ControllerからService層へ、画面入力データを安全に受け渡すために使用します。
 * Entityを直接操作するのを避けるためのカプセル化の役割を担います。
 */
public record ItemSaveDto(
    String name,
    String category,
    ItemType itemType,
    Integer safeStockCount,
    Integer stockQuantity,
    Integer rentalPeriodValue,
    PeriodUnit rentalPeriodUnit
) {}