// src/main/java/com/example/inventory/domain/model/RequestReason.java
package com.example.inventory.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 申請時に選択する事由（例: 業務使用、故障代替など）を管理するマスタエンティティ。
 */
@Entity
@Table(name = "request_reasons")
@Getter
@Setter
@NoArgsConstructor
public class RequestReason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reasonText;
}