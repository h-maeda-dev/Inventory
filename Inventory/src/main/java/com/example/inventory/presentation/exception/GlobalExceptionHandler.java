// src/main/java/com/example/inventory/presentation/exception/GlobalExceptionHandler.java
package com.example.inventory.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * アプリケーション全体で発生する例外を捕捉し、エラー画面（HTTP 500）を表示する代わりに、
 * 元の画面へ適切なエラーメッセージと共にリダイレクトさせる共通ハンドラー。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 楽観的排他制御エラー（他者が先に更新した）を捕捉するメソッド。
     * 消耗品の申請や在庫数調整などで競合が発生した場合に呼び出されます。
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex, 
            HttpServletRequest request, 
            RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addFlashAttribute("errorMessage", "他のユーザーによってデータが更新されました。もう一度やり直してください。");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    /**
     * 不正な引数や業務ロジックのチェックエラーを捕捉するメソッド。
     * Service層で投げられた IllegalArgumentException をキャッチします。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(
            IllegalArgumentException ex, 
            HttpServletRequest request, 
            RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    /**
     * データベースの制約違反（重複エラーや削除不可エラー）を包括的に捕捉するメソッド。
     * DataIntegrityViolationExceptionの内部メッセージを解析し、ユーザーに分かりやすい文言へ変換します。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(
            DataIntegrityViolationException ex, 
            HttpServletRequest request, 
            RedirectAttributes redirectAttributes) {
        
        String msg = ex.getMostSpecificCause().getMessage();
        
        if (msg != null) {
            // 一意制約違反（重複）の共通処理
            if (msg.contains("equipment_units_management_code_key")) {
                redirectAttributes.addFlashAttribute("errorMessage", "入力された管理番号は既に登録されています。別の番号を指定してください。");
            } else if (msg.contains("employees_email_key")) {
                redirectAttributes.addFlashAttribute("errorMessage", "入力されたメールアドレスは既に他の社員に登録されています。");
            } else if (msg.contains("departments_name_key")) {
                redirectAttributes.addFlashAttribute("errorMessage", "入力された部署名は既に登録されています。");
            } else if (msg.contains("items_name_key")) {
                redirectAttributes.addFlashAttribute("errorMessage", "入力された品名は既に登録されています。");
            } 
            // 外部キー制約違反（使用中のため削除不可）の共通処理
            else if (msg.toLowerCase().contains("violates foreign key constraint")) {
                redirectAttributes.addFlashAttribute("errorMessage", "このデータは他のシステム情報（申請履歴や所属社員など）で使用されているため、削除できません。");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "データの重複、または保存・削除できない内容が含まれています。");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "データベースの制約違反が発生しました。");
        }
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }
}