// src/main/java/com/example/inventory/presentation/controller/BatchTestController.java
package com.example.inventory.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.inventory.application.service.BatchService;

import lombok.RequiredArgsConstructor;

/**
 * 開発・テスト、および管理者による手動実行用のコントローラー。
 * 毎朝8時に自動実行されるバッチ処理を、任意のタイミングで画面から呼び出します。
 */
@Controller
@RequiredArgsConstructor
public class BatchTestController {
    private final BatchService batchService;

    /**
     * 通知バッチ（期日前・督促メール送信）を手動で実行します。
     * メール送信という状態変更を伴う操作のため、GETではなくPOSTメソッドを使用します。
     *
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return ダッシュボードへのリダイレクト
     */
    @PostMapping("/admin/test-batch")
    public String runBatch(RedirectAttributes redirectAttributes) {
        batchService.executeDailyNotifications();
        redirectAttributes.addFlashAttribute("successMessage", "バッチ処理（期日前・督促メール送信）を手動実行しました。MailPitを確認してください。");
        return "redirect:/dashboard";
    }
}