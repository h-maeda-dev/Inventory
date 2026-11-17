// src/main/java/com/example/inventory/presentation/form/RejectForm.java
package com.example.inventory.presentation.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理者が申請を却下する際に入力するフォームクラス。
 * 選択必須の却下事由と、任意の補足テキストを受け取ります。
 */
@Data
public class RejectForm {
    
    /** 
     * 選択された却下事由マスタのID。
     * 未選択（null）での送信を許可しません。
     */
    @NotNull(message = "却下事由を選択してください")
    private Long rejectReasonId;
    
    /** 
     * 却下に関する補足理由（任意入力）。
     * データベースの保存許容量に合わせて、最大1000文字までに制限します。
     */
    @Size(max = 1000)
    private String rejectNote;
}