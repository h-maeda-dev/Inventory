// src/main/java/com/example/inventory/domain/model/Item.java
package com.example.inventory.domain.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
 // unique = true を追加し、重複登録を防ぐ
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer safeStockCount;

    @Version
    private Long version;
    
 // 追加: レンタル期限
    private Integer rentalPeriodValue;

    @Enumerated(EnumType.STRING)
    private PeriodUnit rentalPeriodUnit;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<EquipmentUnit> equipmentUnits;
}