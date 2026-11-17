// src/main/java/com/example/inventory/presentation/controller/AdminEquipmentUnitController.java
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

import com.example.inventory.application.service.ItemService;
import com.example.inventory.presentation.form.UnitForm;

import lombok.RequiredArgsConstructor;

/**
 * 管理者向けの備品個体（シリアルナンバー等）管理を担うコントローラー。
 * 特定の備品（品目）に紐づく個体の一覧表示と、新規個体の追加処理を行います。
 */
@Controller
@RequestMapping("/admin/items/{id}/units")
@RequiredArgsConstructor
public class AdminEquipmentUnitController {
    
    private final ItemService itemService;

    /**
     * 指定された備品（品目ID）に紐づく個体の一覧と、新規追加用のフォーム画面を表示します。
     *
     * @param id 対象の品目ID（URLのパスから取得）
     * @param model ビューへ渡すデータモデル
     * @return 個体管理画面のテンプレート名
     */
    @GetMapping
    public String units(@PathVariable Long id, Model model) {
        // 親となる品目（Item）の情報を取得し、画面へ渡す
        model.addAttribute("item", itemService.getItem(id));
        // 個体追加用の空フォームをセットする
        model.addAttribute("unitForm", new UnitForm());
        
        return "admin_item_units";
    }

    /**
     * 画面から送信された管理番号を用いて、指定された備品に新しい個体を追加します。
     * 入力エラー時や、管理番号の重複エラー（一意制約違反）時は元の画面へ戻します。
     *
     * @param id 対象の品目ID
     * @param form 画面から送信された新規個体の管理番号
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時は個体管理画面へリダイレクト、エラー時は元の画面へ戻る
     */
    @PostMapping
    public String addUnit(
            @PathVariable Long id, 
            @Validated @ModelAttribute UnitForm form, 
            BindingResult result, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        // 入力値にエラー（未入力など）がある場合は、必要な情報を再セットして元の画面を返す
        if (result.hasErrors()) {
            model.addAttribute("item", itemService.getItem(id));
            return "admin_item_units";
        }
        
        // サービス層のメソッドを呼び出し、新しい個体をデータベースへ登録
        // ※管理番号の重複登録（一意制約違反）が発生した場合は、GlobalExceptionHandlerで一括捕捉されます
        itemService.addEquipmentUnit(id, form.getManagementCode());
        
        // 追加した管理番号を含めて完了メッセージをセット
        redirectAttributes.addFlashAttribute("successMessage", "個体（管理番号: " + form.getManagementCode() + "）を追加しました");
        
        // 処理後は、現在の品目IDを持つ個体管理画面へリダイレクトして再表示
        return "redirect:/admin/items/" + id + "/units";
    }
}