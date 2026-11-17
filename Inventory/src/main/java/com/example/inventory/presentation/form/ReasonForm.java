// src/main/java/com/example/inventory/presentation/form/ReasonForm.java
package com.example.inventory.presentation.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 事由マスタの新規登録時に入力されるフォームクラス。
 */
@Data
public class ReasonForm {
    /** 登録する事由のテキスト */
    @NotBlank(message = "事由を入力してください")
    private String reasonText;
}