// src/main/java/com/example/inventory/presentation/controller/PasswordChangeController.java
package com.example.inventory.presentation.controller;

import java.security.Principal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.inventory.application.service.PasswordResetService;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.repository.EmployeeRepository;
import com.example.inventory.presentation.form.PasswordChangeForm;

import lombok.RequiredArgsConstructor;

/**
 * ログイン中のユーザーが自身のパスワードを変更する機能を提供するコントローラー。
 * パスワード変更画面の表示と、入力された新旧パスワードの検証・更新処理を行います。
 */
@Controller
@RequestMapping("/password-change")
@RequiredArgsConstructor
public class PasswordChangeController {
    
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;

    /**
     * パスワード変更画面を表示します。
     *
     * @param model ビューへ渡すデータモデル（空のフォームをセット）
     * @return パスワード変更画面のテンプレート名
     */
    @GetMapping
    public String changeForm(Model model) {
        model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        return "password_change";
    }

    /**
     * 入力された情報に基づいてパスワードの更新処理を実行します。
     *
     * @param form 画面から送信された新旧パスワードの情報
     * @param result バリデーション（入力チェック）の結果
     * @param principal 現在ログインしているユーザーの認証情報
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @param model バリデーションエラー時等にビューへ渡すデータモデル
     * @return 成功時はダッシュボードへリダイレクト、エラー時はパスワード変更画面へ戻る
     */
    @PostMapping
    public String changePassword(
            @Validated @ModelAttribute PasswordChangeForm form, 
            BindingResult result, 
            Principal principal, 
            RedirectAttributes redirectAttributes, 
            Model model) {
        
        // 入力値にエラー（未入力、文字数不足など）がある場合は元の画面へ戻す
        if (result.hasErrors()) {
            return "password_change";
        }
        
        // 現在のログインユーザー情報を取得
        Long empId = Long.valueOf(principal.getName());
        Employee employee = employeeRepository.findById(empId).orElseThrow();
        
        // セキュリティ対策：入力された「現在のパスワード」とデータベースに保存された暗号化パスワードを照合
        if (!passwordEncoder.matches(form.getCurrentPassword(), employee.getPassword())) {
            model.addAttribute("errorMessage", "現在のパスワードが正しくありません");
            return "password_change";
        }
        
        // 照合に成功した場合、サービスを呼び出して新しいパスワードへ更新
        passwordResetService.updatePassword(empId, form.getNewPassword());
        
        // 完了メッセージをセットしてダッシュボードへリダイレクト
        redirectAttributes.addFlashAttribute("successMessage", "パスワードを変更しました");
        return "redirect:/dashboard";
    }
}