// src/main/java/com/example/inventory/presentation/form/PasswordChangeForm.java
package com.example.inventory.presentation.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ログイン中の社員が自身のパスワードを変更する際に入力するフォームクラス。
 * 画面とコントローラー間で安全にデータを受け渡す役割（DTO）を担います。
 */
@Data
public class PasswordChangeForm {
    
    /** 
     * 現在のパスワード。
     * 本人確認のため、未入力（空文字）を許可しません。
     */
    @NotBlank(message = "現在のパスワードを入力してください")
    private String currentPassword;

    /** 
     * 新しく設定するパスワード。
     * セキュリティ要件として、8文字以上であることを必須とします。
     */
    @NotBlank(message = "新しいパスワードを入力してください")
    @Size(min = 8, message = "8文字以上で入力してください")
    private String newPassword;
}