// src/main/java/com/example/inventory/presentation/controller/ItemController.java
package com.example.inventory.presentation.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.inventory.application.dto.ItemDetailDto;
import com.example.inventory.application.dto.UnitDto;
import com.example.inventory.application.service.ItemService;
import com.example.inventory.application.service.ReasonService;
import com.example.inventory.domain.model.Item;
import com.example.inventory.domain.model.ItemType;
import com.example.inventory.presentation.form.RequestForm;

import lombok.RequiredArgsConstructor;

/**
 * 物品（備品・消耗品）の検索および詳細画面の表示を制御するコントローラー。
 * 一般社員が貸出や支給の申請を行う際の入り口となります。
 */
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    private final ReasonService reasonService;

    /**
     * 物品の検索画面（一覧）を表示します。
     *
     * @param keyword 検索キーワード（品名またはカテゴリの一部）
     * @param pageable ページネーション情報（デフォルトは1ページ10件、ID昇順）
     * @param model ビューへ渡すデータモデル
     * @return 物品検索画面のテンプレート名
     */
    @GetMapping
    public String listItems(
            @RequestParam(required = false) String keyword, 
            @PageableDefault(size = 10, sort = "id") Pageable pageable, 
            Model model) {
        
        Page<Item> itemPage = itemService.searchItems(keyword, pageable);
        model.addAttribute("keyword", keyword);
        model.addAttribute("itemPage", itemPage);
        
        return "items";
    }

    /**
     * 指定された物品の詳細情報と、申請用の入力フォームを表示します。
     * 画面テンプレートがEntityの構造に直接依存しないよう、DTOへ詰め替えて渡します。
     *
     * @param id 詳細を表示する品目ID
     * @param model ビューへ渡すデータモデル
     * @return 物品詳細・申請画面のテンプレート名
     */
    @GetMapping("/{id}")
    public String itemDetail(@PathVariable Long id, Model model) {
        Item item = itemService.getItem(id);
        
        // 【アーキテクチャ規約】画面への直接的なEntity公開を防ぐため、貸出可能な個体をDTOリストへ変換
        List<UnitDto> unitDtos = itemService.getAvailableUnits(id).stream()
                .map(u -> new UnitDto(u.getId(), u.getManagementCode())).toList();
                
        // 品目全体の情報をDTOへ詰め替え
        ItemDetailDto dto = new ItemDetailDto(
            item.getId(), 
            item.getName(), 
            item.getCategory(), 
            item.getItemType().name(), 
            item.getStockQuantity(), 
            unitDtos
        );
        
        model.addAttribute("itemDto", dto);
        // 申請時に選択させる「事由マスタ」の一覧を取得して渡す
        model.addAttribute("requestReasons", reasonService.getAllRequestReasons());

        // 申請用の空フォームを準備し、初期値をセット
        RequestForm form = new RequestForm();
        form.setItemId(id);
        // 消耗品の場合は、他者の同時申請による在庫数の不整合を防ぐためバージョン（楽観的ロック用）を持たせる
        if (item.getItemType() == ItemType.CONSUMABLE) {
            form.setVersion(item.getVersion());
        }
        model.addAttribute("requestForm", form);
        
        return "item_detail";
    }
}