// src/main/java/com/example/inventory/application/service/ItemService.java
package com.example.inventory.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.application.dto.ItemSaveDto;
import com.example.inventory.domain.model.EquipmentStatus;
import com.example.inventory.domain.model.EquipmentUnit;
import com.example.inventory.domain.model.Item;
import com.example.inventory.domain.repository.EquipmentUnitRepository;
import com.example.inventory.domain.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

/**
 * 品目および個体管理に関する業務ロジックを担うサービスクラス。
 * トランザクション境界として機能し、データベースの整合性を保証します。
 */
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final EquipmentUnitRepository equipmentUnitRepository;

    
    /**
     * 指定されたキーワードで品目を検索し、ページングされた結果を返します。
     *
     * @param keyword 検索キーワード（品名またはカテゴリ）。nullまたは空文字の場合は全件を返します。
     * @param pageable ページネーション情報
     * @return 条件に一致する品目のPageオブジェクト
     */
    @Transactional(readOnly = true)
    public Page<Item> searchItems(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return itemRepository.findAll(pageable);
        }
        return itemRepository.findByKeyword(keyword, pageable);
    }
    

    /**
     * IDを指定して品目を取得します。
     *
     * @param id 品目ID
     * @return 対象の品目
     * @throws IllegalArgumentException 対象の品目が存在しない場合
     */
    @Transactional(readOnly = true)
    public Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("対象の品目が見つかりません"));
    }

    /**
     * 指定された品目に紐づき、現在貸出可能な個体のリストを取得します。
     *
     * @param itemId 品目ID
     * @return 貸出可能な個体のリスト
     */
    @Transactional(readOnly = true)
    public List<EquipmentUnit> getAvailableUnits(Long itemId) {
        return getItem(itemId).getEquipmentUnits().stream()
                // ステータスがAVAILABLE（利用可能）な個体のみを抽出
                .filter(unit -> unit.getStatus() == EquipmentStatus.AVAILABLE)
                .toList();
    }

    /**
     * 新規品目を登録します。
     * Controller層から渡されたDTOを元にEntityを生成し、Repository経由で保存します。
     *
     * @param dto 登録する品目の情報を持ったDTO
     */
    @Transactional
    public void saveItem(ItemSaveDto dto) {
        Item item = new Item();
        item.setName(dto.name());
        item.setCategory(dto.category());
        item.setItemType(dto.itemType());
        item.setSafeStockCount(dto.safeStockCount());
        // 在庫数が未指定（null）の場合は0で初期化
        item.setStockQuantity(dto.stockQuantity() != null ? dto.stockQuantity() : 0);
        item.setRentalPeriodValue(dto.rentalPeriodValue());
        item.setRentalPeriodUnit(dto.rentalPeriodUnit());
        
        itemRepository.save(item);
    }

    /**
     * 指定された品目を削除します。
     *
     * @param id 削除対象の品目ID
     */
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * 消耗品の在庫数を調整（更新）します。
     * 楽観的排他制御により、他者の更新との競合を防ぎます。
     *
     * @param id 品目ID
     * @param newStockQuantity 変更後の新しい在庫数
     * @param version 画面表示時点のバージョン（排他制御用）
     * @throws ObjectOptimisticLockingFailureException バージョンが不一致の場合（他者が先に更新した場合）
     */
    @Transactional
    public void adjustStock(Long id, Integer newStockQuantity, Long version) {
        Item item = getItem(id);
        // バージョンの一致確認（排他制御）
        if (!item.getVersion().equals(version)) {
            throw new ObjectOptimisticLockingFailureException(Item.class, id);
        }
        item.setStockQuantity(newStockQuantity);
        itemRepository.save(item);
    }

    /**
     * 備品に対して新しい管理番号を持つ個体を追加します。
     *
     * @param itemId 対象の備品の品目ID
     * @param managementCode 新しく追加する個体の管理番号
     */
    @Transactional
    public void addEquipmentUnit(Long itemId, String managementCode) {
        Item item = getItem(itemId);
        EquipmentUnit unit = new EquipmentUnit();
        unit.setManagementCode(managementCode);
        // 新規追加時は「利用可能」ステータスとする
        unit.setStatus(EquipmentStatus.AVAILABLE);
        unit.setItem(item);
        
        equipmentUnitRepository.save(unit);
    }
}