// src/test/java/com/example/inventory/application/service/ItemServiceTest.java
package com.example.inventory.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.inventory.domain.model.EquipmentStatus;
import com.example.inventory.domain.model.EquipmentUnit;
import com.example.inventory.domain.model.Item;
import com.example.inventory.domain.model.ItemType;
import com.example.inventory.domain.repository.EquipmentUnitRepository;
import com.example.inventory.domain.repository.ItemRepository;

/**
 * 品目および個体管理サービス（ItemService）の単体テストクラス。
 * Mockitoを使用してリポジトリ層をモック化し、ビジネスロジックの動作を検証します。
 */
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EquipmentUnitRepository equipmentUnitRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void キーワードがnullまたは空の場合に全件取得できること() {
        // Arrange (準備)
        Item item1 = new Item(); item1.setName("モニター");
        Item item2 = new Item(); item2.setName("ペン");
        
        // ページネーション用のモックオブジェクトを作成
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> mockPage = new PageImpl<>(Arrays.asList(item1, item2));
        
        // リポジトリが呼ばれた際の戻り値を設定
        when(itemRepository.findAll(pageable)).thenReturn(mockPage);

        // Act (実行)
        Page<Item> resultNull = itemService.searchItems(null, pageable);
        Page<Item> resultEmpty = itemService.searchItems("   ", pageable);

        // Assert (検証)
        assertEquals(2, resultNull.getContent().size());
        assertEquals(2, resultEmpty.getContent().size());
    }

    @Test
    void 指定したキーワードで品名とカテゴリを正しく絞り込めること() {
        // Arrange
        Item item1 = new Item(); item1.setName("27インチモニター"); item1.setCategory("PC周辺機器");
        Item item2 = new Item(); item2.setName("ボールペン黒"); item2.setCategory("文房具");
        Item item3 = new Item(); item3.setName("プロジェクター"); item3.setCategory("PC周辺機器");
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> mockNamePage = new PageImpl<>(Arrays.asList(item1));
        Page<Item> mockCategoryPage = new PageImpl<>(Arrays.asList(item1, item3));

        // 品名、またはカテゴリで検索された場合の戻り値をそれぞれ設定
        when(itemRepository.findByKeyword("モニター", pageable)).thenReturn(mockNamePage);
        when(itemRepository.findByKeyword("PC", pageable)).thenReturn(mockCategoryPage);

        // Act
        Page<Item> resultNameMatch = itemService.searchItems("モニター", pageable);
        Page<Item> resultCategoryMatch = itemService.searchItems("PC", pageable);

        // Assert
        assertEquals(1, resultNameMatch.getContent().size());
        assertEquals("27インチモニター", resultNameMatch.getContent().get(0).getName());
        assertEquals(2, resultCategoryMatch.getContent().size());
    }

    @Test
    void 存在しないIDを指定して取得しようとすると例外が発生すること() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            itemService.getItem(999L);
        });
        assertEquals("対象の品目が見つかりません", exception.getMessage());
    }

    @Test
    void 指定した品目の利用可能な個体のみを取得できること() {
        // Arrange
        Item item = new Item();
        item.setId(1L);

        EquipmentUnit unitAvailable = new EquipmentUnit();
        unitAvailable.setStatus(EquipmentStatus.AVAILABLE);

        EquipmentUnit unitInUse = new EquipmentUnit();
        unitInUse.setStatus(EquipmentStatus.IN_USE);

        item.setEquipmentUnits(Arrays.asList(unitAvailable, unitInUse));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        List<EquipmentUnit> result = itemService.getAvailableUnits(1L);

        // Assert: リストの件数が1件であり、そのステータスがAVAILABLEであることを確認
        assertEquals(1, result.size());
        assertEquals(EquipmentStatus.AVAILABLE, result.get(0).getStatus());
    }

    @Test
    void 在庫数の調整が正しく行われリポジトリに渡されること() {
        // Arrange
        Item item = new Item();
        item.setId(10L);
        item.setStockQuantity(5);
        item.setVersion(1L);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        // Act: バージョンが一致する状態で在庫を20に変更
        itemService.adjustStock(10L, 20, 1L);

        // Assert
        assertEquals(20, item.getStockQuantity());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void 在庫調整時に他者が更新してバージョンが進んでいた場合は例外が発生すること() {
        // Arrange
        Item item = new Item();
        item.setId(10L);
        item.setStockQuantity(5);
        item.setVersion(2L); // データベース側のバージョンが進んでいる状態
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        // Act & Assert: 画面側のバージョン(1L)が古いため、楽観的排他制御エラーが発生することを検証
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            itemService.adjustStock(10L, 20, 1L); 
        });
        
        // 排他エラーが起きた場合、saveメソッドが一度も呼ばれないことを確認
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void 備品に新しい管理番号の個体を追加できること() {
        // Arrange
        Item item = new Item();
        item.setId(5L);
        item.setItemType(ItemType.EQUIPMENT);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        // Act
        itemService.addEquipmentUnit(5L, "TEST-UNIT-01");

        // Assert: 期待するプロパティを持ったオブジェクトがsaveに渡されたかを検証
        verify(equipmentUnitRepository, times(1)).save(argThat(unit -> 
            unit.getManagementCode().equals("TEST-UNIT-01") &&
            unit.getStatus() == EquipmentStatus.AVAILABLE &&
            unit.getItem().getId().equals(5L)
        ));
    }
}