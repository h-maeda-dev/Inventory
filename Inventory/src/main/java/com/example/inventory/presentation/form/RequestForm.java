// src/main/java/com/example/inventory/presentation/form/RequestForm.java
package com.example.inventory.presentation.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 物品の貸出・支給を申請する際に入力されるフォームクラス。
 * 備品と消耗品のどちらの申請にも共通して使用されます。
 */
@Data
public class RequestForm {
    
    /** 対象となる品目のID */
    @NotNull 
    private Long itemId;
    
    /** 貸出希望の個体ID（備品申請時のみセットされる） */
    private Long equipmentUnitId;
    
    /** 申請数（消耗品申請時のみセットされる） */
    @Min(1) 
    private Integer quantity;
    
    /** 同時更新を防ぐためのバージョン番号（消耗品申請時のみセットされる） */
    private Long version;
    
    /** 選択された申請事由マスタのID */
    @NotNull(message = "申請事由を選択してください")
    private Long requestReasonId;
    
    /** 申請理由の補足（任意入力） */
    @Size(max = 1000)
    private String requestNote;
}