// src/main/java/com/example/inventory/presentation/controller/PasswordResetController.java
package com.example.inventory.presentation.controller;

import com.example.inventory.application.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 未ログインユーザー向けのパスワードリセット機能を提供するコントローラー。
 * リセット申請画面の表示と申請処理、およびメールのURLから遷移する再設定画面の処理を行います。
 */
@Controller
@RequestMapping("/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;

    /**
     * パスワードリセットの申請画面を表示します。
     *
     * @return リセット申請画面のテンプレート名
     */
    @GetMapping("/request")
    public String requestForm() {
        return "password_reset_request";
    }

    /**
     * パスワードリセットの申請処理を実行します。
     *
     * @param employeeId 画面で入力された社員ID
     * @param email 画面で入力されたメールアドレス
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時はログイン画面へ、エラー時は再度申請画面へリダイレクト
     */
    @PostMapping("/request")
    public String submitRequest(@RequestParam Long employeeId, @RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.requestReset(employeeId, email);
            redirectAttributes.addFlashAttribute("successMessage", "パスワードリセット申請を送信しました。管理者の承認をお待ちください。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/password-reset/request";
        }
        return "redirect:/login";
    }

    /**
     * メールのURLから遷移する、新しいパスワードの設定画面を表示します。
     * 画面側でトークンを保持し、POST時に送信させるためモデルにトークンを渡します。
     *
     * @param token メールに記載されていたワンタイムトークン
     * @param model ビューへ渡すデータモデル
     * @return パスワード再設定画面のテンプレート名
     */
    @GetMapping("/form")
    public String resetForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "password_reset_form";
    }

    /**
     * ワンタイムトークンを用いて、新しいパスワードへの更新処理を実行します。
     *
     * @param token 画面（hidden）から送信されたワンタイムトークン
     * @param newPassword 新しく設定するパスワード
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時はログイン画面へ、エラー時はトークン付きの再設定画面へリダイレクト
     */
    @PostMapping("/update")
    public String updatePassword(@RequestParam String token, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.updatePasswordWithToken(token, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "パスワードを再設定しました。新しいパスワードでログインしてください。");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/password-reset/form?token=" + token;
        }
        return "redirect:/login";
    }
}