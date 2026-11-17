// src/main/java/com/example/inventory/domain/model/Holiday.java
package com.example.inventory.domain.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 休業日（祝日および会社独自の休日）を表すマスタエンティティ。
 * 営業日計算（期日前リマインド等）の除外日として利用されます。
 */
@Entity
@Table(name = "holidays")
@Getter
@Setter
@NoArgsConstructor
public class Holiday {
    /** 休業日の日付（主キー） */
    @Id
    private LocalDate holidayDate;

    /** 休業日の名称（例：元日、創立記念日など） */
    @Column(nullable = false)
    private String holidayName;
}