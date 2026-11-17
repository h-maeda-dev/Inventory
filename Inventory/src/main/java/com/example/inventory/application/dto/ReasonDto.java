// src/main/java/com/example/inventory/application/dto/ReasonDto.java
package com.example.inventory.application.dto;

/**
 * 画面へ事由マスタの情報を渡すためのDTO。
 */
public record ReasonDto(Long id, String reasonText) {}