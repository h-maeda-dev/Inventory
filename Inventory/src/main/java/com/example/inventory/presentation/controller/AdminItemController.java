// src/main/java/com/example/inventory/presentation/controller/AdminItemController.java
package com.example.inventory.presentation.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.example.inventory.application.dto.ItemSaveDto;
import com.example.inventory.application.service.ItemService;
import com.example.inventory.domain.model.Item;
import com.example.inventory.presentation.form.ItemAdjustForm;
import com.example.inventory.presentation.form.ItemForm;

import lombok.RequiredArgsConstructor;

/**
 * 管理者向けの品目（備品・消耗品）マスタ管理を担うコントローラー。
 * 物品の一覧表示、新規登録、削除、および消耗品の在庫数調整を行います。
 */
@Controller
@RequestMapping("/admin/items")
@RequiredArgsConstructor
public class AdminItemController {
    
    private final ItemService itemService;

    /**
     * 登録済みの物品（品目）一覧画面を表示します。
     *
     * @param pageable ページネーション情報（デフォルト1ページ15件、ID昇順）
     * @param model ビューへ渡すデータモデル
     * @return 品目マスタ一覧画面のテンプレート名
     */
    @GetMapping
    public String items(@PageableDefault(size = 15, sort = "id") Pageable pageable, Model model) {
        // 管理者画面では全件表示するため、検索キーワードにはnullを渡す
        Page<Item> itemPage = itemService.searchItems(null, pageable);
        model.addAttribute("itemPage", itemPage);
        
        return "admin_items";
    }

    /**
     * 新規の物品を登録するための入力フォーム画面を表示します。
     *
     * @param model ビューへ渡すデータモデル（空のフォームをセット）
     * @return 新規品目登録画面のテンプレート名
     */
    @GetMapping("/new")
    public String newItemForm(Model model) {
        model.addAttribute("itemForm", new ItemForm());
        return "admin_item_form";
    }

    /**
     * 画面から送信された新規物品の情報をデータベースへ登録します。
     * 品名の重複エラー（一意制約違反）が発生した場合は、元の登録画面へエラーメッセージ付きで戻します。
     *
     * @param form 画面から送信された物品情報
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時は一覧画面へリダイレクト、エラー時は登録画面へ戻る
     */
    @PostMapping("/new")
    public String createItem(
            @Validated @ModelAttribute ItemForm form, 
            BindingResult result, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin_item_form";
        }
        
        // Entityを直接操作せず、安全にデータを受け渡すためのDTOへ詰め替える
        ItemSaveDto dto = new ItemSaveDto(
            form.getName(), 
            form.getCategory(), 
            form.getItemType(),
            form.getSafeStockCount(), 
            form.getStockQuantity(),
            form.getRentalPeriodValue(), 
            form.getRentalPeriodUnit()
        );
        
        try {
            itemService.saveItem(dto);
        } catch (DataIntegrityViolationException e) {
            // 品名の重複登録（一意制約違反）を検知し、ユーザーフレンドリーなエラーメッセージを表示
            String errorMsg = e.getMostSpecificCause().getMessage();
            if (errorMsg != null && errorMsg.contains("items_name_key")) {
                model.addAttribute("errorMessage", "品名「" + form.getName() + "」は既に登録されています。別の名前を入力してください。");
            } else {
                model.addAttribute("errorMessage", "保存に失敗しました。データベースの制約違反の可能性があります。");
            }
            return "admin_item_form";
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "品目「" + form.getName() + "」を登録しました");
        return "redirect:/admin/items";
    }

    /**
     * 指定された物品をマスタから削除します。
     * ※すでに貸出履歴等が存在して削除できない場合（外部キー制約違反）は、GlobalExceptionHandlerにて一括処理されます。
     *
     * @param id 削除対象の品目ID
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 品目マスタ一覧画面へのリダイレクト
     */
    @PostMapping("/{id}/delete")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // メッセージ表示用のため、削除実行前に品名を取得しておく
        String itemName = itemService.getItem(id).getName();
        itemService.deleteItem(id);
        
        redirectAttributes.addFlashAttribute("successMessage", "品目「" + itemName + "」を削除しました");
        return "redirect:/admin/items";
    }

    /**
     * 消耗品の在庫数を調整（棚卸し・補充）するための入力フォーム画面を表示します。
     *
     * @param id 対象の品目ID
     * @param model ビューへ渡すデータモデル
     * @return 在庫数調整画面のテンプレート名
     */
    @GetMapping("/{id}/adjust")
    public String adjustStockForm(@PathVariable Long id, Model model) {
        Item item = itemService.getItem(id);
        model.addAttribute("item", item);
        
        // フォームに現在の在庫数とデータバージョン（排他制御用）をセットして画面へ渡す
        ItemAdjustForm form = new ItemAdjustForm();
        form.setStockQuantity(item.getStockQuantity());
        form.setVersion(item.getVersion());
        model.addAttribute("adjustForm", form);
        
        return "admin_item_adjust";
    }

    /**
     * 画面から送信された新しい在庫数で、対象の消耗品を更新します。
     *
     * @param id 対象の品目ID
     * @param form 送信された新しい在庫数とバージョン情報
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時は一覧画面へリダイレクト、エラー時は調整画面へ戻る
     */
    @PostMapping("/{id}/adjust")
    public String adjustStock(
            @PathVariable Long id, 
            @Validated @ModelAttribute ItemAdjustForm form, 
            BindingResult result, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("item", itemService.getItem(id));
            return "admin_item_adjust";
        }
        
        // メッセージ表示用のため、更新実行前に品名を取得しておく
        String itemName = itemService.getItem(id).getName();
        // サービス層のメソッドを呼び出し。他者が先に更新していた場合はここで楽観的ロック例外が発生する
        itemService.adjustStock(id, form.getStockQuantity(), form.getVersion());
        
        redirectAttributes.addFlashAttribute("successMessage", "品目「" + itemName + "」の在庫数を更新しました");
        return "redirect:/admin/items";
    }
}