// src/main/java/com/example/inventory/presentation/controller/AdminReasonController.java
package com.example.inventory.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.inventory.application.service.ReasonService;
import com.example.inventory.presentation.form.ReasonForm;

import lombok.RequiredArgsConstructor;

/**
 * 管理者向けの申請事由・却下事由マスタ管理を行うコントローラー。
 * 申請時や却下時に選択させる事由の一覧表示、新規登録、削除リクエストを処理します。
 */
@Controller
@RequestMapping("/admin/reasons")
@RequiredArgsConstructor
public class AdminReasonController {
    private final ReasonService reasonService;

    /**
     * 申請事由・却下事由の一覧と、それぞれの登録フォームを表示します。
     *
     * @param model ビューへ渡すデータモデル
     * @return 事由管理画面のテンプレート名
     */
    @GetMapping
    public String listReasons(Model model) {
        model.addAttribute("requestReasons", reasonService.getAllRequestReasons());
        model.addAttribute("rejectReasons", reasonService.getAllRejectReasons());
        model.addAttribute("reasonForm", new ReasonForm());
        return "admin_reasons";
    }

    /**
     * 新しい申請事由を登録します。入力エラーがある場合は元の画面に戻します。
     *
     * @param form 登録する申請事由
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 一覧画面へのリダイレクト
     */
    @PostMapping("/request")
    public String addRequestReason(@Validated @ModelAttribute ReasonForm form, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return listReasons(model);
        }
        reasonService.saveRequestReason(form.getReasonText());
        redirectAttributes.addFlashAttribute("successMessage", "申請事由「" + form.getReasonText() + "」を登録しました");
        return "redirect:/admin/reasons";
    }

    /**
     * 指定された申請事由を削除します。
     * ※既存の申請履歴に紐づいている場合のエラーはGlobalExceptionHandlerで捕捉されます。
     *
     * @param id 削除対象の事由ID
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 一覧画面へのリダイレクト
     */
    @PostMapping("/request/{id}/delete")
    public String deleteRequestReason(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // メッセージ表示用に事由テキストを先に取得
        String reasonText = reasonService.getAllRequestReasons().stream()
                .filter(r -> r.id().equals(id))
                .findFirst().map(r -> r.reasonText()).orElse("不明な事由");
        
        reasonService.deleteRequestReason(id);
        redirectAttributes.addFlashAttribute("successMessage", "申請事由「" + reasonText + "」を削除しました");
        
        return "redirect:/admin/reasons";
    }

    /**
     * 新しい却下事由を登録します。
     *
     * @param form 登録する却下事由
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 一覧画面へのリダイレクト
     */
    @PostMapping("/reject")
    public String addRejectReason(@Validated @ModelAttribute ReasonForm form, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return listReasons(model);
        }
        reasonService.saveRejectReason(form.getReasonText());
        redirectAttributes.addFlashAttribute("successMessage", "却下事由「" + form.getReasonText() + "」を登録しました");
        return "redirect:/admin/reasons";
    }

    /**
     * 指定された却下事由を削除します。
     *
     * @param id 削除対象の事由ID
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 一覧画面へのリダイレクト
     */
    @PostMapping("/reject/{id}/delete")
    public String deleteRejectReason(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // メッセージ表示用に事由テキストを先に取得
        String reasonText = reasonService.getAllRejectReasons().stream()
                .filter(r -> r.id().equals(id))
                .findFirst().map(r -> r.reasonText()).orElse("不明な事由");
        
        reasonService.deleteRejectReason(id);
        redirectAttributes.addFlashAttribute("successMessage", "却下事由「" + reasonText + "」を削除しました");
        
        return "redirect:/admin/reasons";
    }
}