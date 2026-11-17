// src/main/java/com/example/inventory/presentation/form/EmployeeEditForm.java
package com.example.inventory.presentation.form;

import com.example.inventory.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 既存の社員（ユーザー）情報を更新する際に入力されるフォームクラス。
 * パスワードは任意入力（変更する場合のみ入力）となります。
 */
@Data
public class EmployeeEditForm {
    
    /** 更新対象の社員ID */
    @NotNull
    private Long id;

    /** 社員の氏名 */
    @NotBlank(message = "社員名を入力してください")
    private String name;
    
    /** ログインや通知に使用するメールアドレス */
    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "正しいメールアドレスの形式で入力してください")
    private String email;

    /** 
     * 変更後のパスワード。
     * 任意入力のため空文字（^$）を許容し、入力された場合のみ複雑性をチェックします。
     */
    @Pattern(regexp = "^$|^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "8文字以上で、半角英大文字・小文字・数字をすべて含めてください")
    private String password;

    /** システム上の権限（ADMIN または USER） */
    @NotNull(message = "権限を選択してください")
    private Role role;

    /** 所属する部署のID */
    @NotNull(message = "所属部署を選択してください")
    private Long departmentId;
}