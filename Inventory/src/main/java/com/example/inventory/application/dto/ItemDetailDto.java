// src/main/java/com/example/inventory/application/dto/ItemDetailDto.java
package com.example.inventory.application.dto;
import java.util.List;
public record ItemDetailDto(
    Long id, String name, String category, String itemType, 
    Integer stockQuantity, List<UnitDto> availableUnits
) {}