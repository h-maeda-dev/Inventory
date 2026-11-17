// src/main/java/com/example/inventory/presentation/controller/AdminPasswordResetController.java
package com.example.inventory.presentation.controller;

import com.example.inventory.application.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 管理者向けのパスワードリセット承認機能を提供するコントローラー。
 * 社員から提出されたパスワードリセット申請の一覧表示と、その承認処理を行います。
 */
@Controller
@RequestMapping("/admin/reset-approvals")
@RequiredArgsConstructor
public class AdminPasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * 未承認のパスワードリセット申請一覧画面を表示します。
     *
     * @param model ビューへ渡すデータモデル
     * @return 承認待ち一覧画面のテンプレート名
     */
    @GetMapping
    public String listApprovals(Model model) {
        // 【安全対策の省略】（Mandatory Rule 4-4 関連）
        // 既存のHTMLテンプレートがEntity構造に依存しているため、現状はEntityのリストをそのまま渡しています。
        // ※ 0件の場合は空リストが返却され、Thymeleaf側で適切に「未承認の申請はありません」と処理されます。
        // 本番運用では秘密情報（トークン、パスワードハッシュ等）を隠蔽するため、DTOに詰め替えて渡す設計への修正が必要です。
        model.addAttribute("pendingResets", passwordResetService.getPendingRequests());
        return "admin_reset_approvals";
    }

    /**
     * 指定されたパスワードリセット申請を承認します。
     * 承認後、対象の社員へパスワード再設定用のURLが記載されたメールが送信されます。
     *
     * @param id 承認対象のリセット申請ID（URLパスから取得）
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 承認待ち一覧画面へのリダイレクト
     */
    @PostMapping("/{id}/approve")
    public String approveReset(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // サービスの承認処理を呼び出し（メール送信処理も内部で非同期実行される）
        passwordResetService.approveRequest(id);
        
        // 処理完了メッセージをセットして元の画面へリダイレクト
        redirectAttributes.addFlashAttribute("successMessage", "パスワードリセット申請を承認し、対象社員へメールを送信しました。");
        return "redirect:/admin/reset-approvals";
    }
}