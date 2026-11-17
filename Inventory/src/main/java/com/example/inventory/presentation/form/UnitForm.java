// src/main/java/com/example/inventory/presentation/form/UnitForm.java
package com.example.inventory.presentation.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理者が備品に対して新しい個体（シリアルナンバー等）を追加する際に入力するフォームクラス。
 */
@Data
public class UnitForm {
    
    /** 
     * 追加する個体を一意に識別するための管理番号。
     * 未入力（空文字）を許可しません。
     */
    @NotBlank(message = "管理番号を入力してください")
    private String managementCode;
}