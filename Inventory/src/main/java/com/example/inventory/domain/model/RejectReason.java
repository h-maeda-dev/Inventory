// src/main/java/com/example/inventory/domain/model/RejectReason.java
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
 * 管理者が申請を却下する際に選択する事由（例: 在庫不足、理由不十分など）を管理するマスタエンティティ。
 */
@Entity
@Table(name = "reject_reasons")
@Getter
@Setter
@NoArgsConstructor
public class RejectReason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reasonText;
}