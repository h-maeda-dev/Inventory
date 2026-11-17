// src/main/java/com/example/inventory/presentation/form/DepartmentForm.java
package com.example.inventory.presentation.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部署マスタの新規登録時に入力されるフォームクラス。
 */
@Data
public class DepartmentForm {
    
    /** 
     * 登録する部署名。
     * 未入力（空文字）を許可しません。
     */
    @NotBlank(message = "部署名を入力してください")
    private String name;
}