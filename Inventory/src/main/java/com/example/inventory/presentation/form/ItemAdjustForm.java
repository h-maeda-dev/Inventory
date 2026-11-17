// src/main/java/com/example/inventory/presentation/form/ItemAdjustForm.java
package com.example.inventory.presentation.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理者が消耗品の実在庫数を調整（棚卸し・補充）する際に入力するフォームクラス。
 * 排他制御（他者による同時更新の防止）のためのバージョン情報を含みます。
 */
@Data
public class ItemAdjustForm {
    
    /** 
     * 変更後の新しい在庫数。
     * 必須入力であり、マイナス値は許可しません。
     */
    @NotNull(message = "変更後の在庫数を入力してください")
    @Min(value = 0, message = "在庫数は0以上で入力してください")
    private Integer stockQuantity;
    
    /** 
     * 画面を開いた時点のデータバージョン。
     * 更新時にデータベース側のバージョンと比較し、一致しない場合は排他エラーとします。
     */
    private Long version;
}