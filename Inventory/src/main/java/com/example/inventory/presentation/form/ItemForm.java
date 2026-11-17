// src/main/java/com/example/inventory/presentation/form/ItemForm.java
package com.example.inventory.presentation.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.example.inventory.domain.model.ItemType;
import com.example.inventory.domain.model.PeriodUnit;

/**
 * 管理者が新しい物品（備品または消耗品）をマスタに登録する際に入力するフォームクラス。
 * 画面とコントローラー間で安全にデータを受け渡す役割（DTO）を担います。
 */
@Data
public class ItemForm {
    
    /** 品名（例：ボールペン黒、27インチモニターなど） */
    @NotBlank(message = "品名を入力してください") 
    private String name;
    
    /** カテゴリ（例：文房具、PC周辺機器など） */
    @NotBlank(message = "カテゴリを入力してください") 
    private String category;
    
    /** 物品の種類（備品か消耗品か） */
    @NotNull(message = "品目種別を選択してください") 
    private ItemType itemType;
    
    /** 
     * 在庫アラートを出す基準となる数量。
     * マイナス値の入力を許可しません。
     */
    @NotNull(message = "安全在庫数を入力してください") 
    @Min(value = 0, message = "0以上を入力してください") 
    private Integer safeStockCount;
    
    /** 
     * 消耗品の場合のみ入力される初期在庫数。
     * 備品の場合は画面側で非活性化されるためnullを許容しますが、マイナス値は許可しません。
     */
    @Min(value = 0, message = "0以上を入力してください") 
    private Integer stockQuantity;
    
    /** 
     * 備品の場合のみ入力される、レンタル期間の数値。
     */
    @Min(value = 1, message = "1以上を入力してください") 
    private Integer rentalPeriodValue;
    
    /** 
     * 備品の場合のみ入力される、レンタル期間の単位（週、月、年）。
     */
    private PeriodUnit rentalPeriodUnit;
}