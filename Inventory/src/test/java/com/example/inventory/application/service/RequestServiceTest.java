// src/test/java/com/example/inventory/application/service/RequestServiceTest.java
package com.example.inventory.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.EquipmentStatus;
import com.example.inventory.domain.model.EquipmentUnit;
import com.example.inventory.domain.model.Item;
import com.example.inventory.domain.model.ItemType;
import com.example.inventory.domain.model.Request;
import com.example.inventory.domain.model.RequestReason;
import com.example.inventory.domain.model.RequestStatus;
import com.example.inventory.domain.model.RequestType;
import com.example.inventory.domain.repository.EquipmentUnitRepository;
import com.example.inventory.domain.repository.ItemRepository;
import com.example.inventory.domain.repository.RequestReasonRepository;
import com.example.inventory.domain.repository.RequestRepository;
import com.example.inventory.presentation.form.RequestForm;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock private RequestRepository requestRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private EquipmentUnitRepository equipmentUnitRepository;
    @Mock private RequestReasonRepository requestReasonRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private RequestService requestService;

    private Employee testEmployee;
    private RequestReason testReason;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("テスト社員");

        testReason = new RequestReason();
        testReason.setId(1L);
        testReason.setReasonText("業務使用");
    }

    @Test
    void 備品の貸出申請が正常に作成され個体が仮押さえされること() {
        // Arrange
        Item equipmentItem = new Item();
        equipmentItem.setId(10L);
        equipmentItem.setItemType(ItemType.EQUIPMENT);

        EquipmentUnit unit = new EquipmentUnit();
        unit.setId(100L);
        unit.setStatus(EquipmentStatus.AVAILABLE);
        unit.setItem(equipmentItem);

        RequestForm form = new RequestForm();
        form.setItemId(10L);
        form.setEquipmentUnitId(100L);
        form.setRequestReasonId(1L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(equipmentItem));
        when(requestReasonRepository.findById(1L)).thenReturn(Optional.of(testReason));
        when(equipmentUnitRepository.findById(100L)).thenReturn(Optional.of(unit));

        // Act
        requestService.createRequest(form, testEmployee);

        // Assert
        assertEquals(EquipmentStatus.RESERVED, unit.getStatus()); // 個体が仮押さえされたか
        verify(equipmentUnitRepository, times(1)).save(unit);
        verify(requestRepository, times(1)).save(any(Request.class));
        verify(notificationService, times(1)).sendRequestNotification(any(Request.class));
    }

    @Test
    void 消耗品の支給申請が正常に作成され在庫が減算されること() {
        // Arrange
        Item consumableItem = new Item();
        consumableItem.setId(20L);
        consumableItem.setItemType(ItemType.CONSUMABLE);
        consumableItem.setStockQuantity(10);
        consumableItem.setVersion(1L);

        RequestForm form = new RequestForm();
        form.setItemId(20L);
        form.setQuantity(3);
        form.setVersion(1L);
        form.setRequestReasonId(1L);

        when(itemRepository.findById(20L)).thenReturn(Optional.of(consumableItem));
        when(requestReasonRepository.findById(1L)).thenReturn(Optional.of(testReason));

        // Act
        requestService.createRequest(form, testEmployee);

        // Assert
        assertEquals(7, consumableItem.getStockQuantity()); // 在庫が減ったか (10 - 3 = 7)
        verify(itemRepository, times(1)).save(consumableItem);
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    void 消耗品申請時に在庫が不足している場合は例外が発生すること() {
        // Arrange
        Item consumableItem = new Item();
        consumableItem.setId(20L);
        consumableItem.setItemType(ItemType.CONSUMABLE);
        consumableItem.setStockQuantity(2); // 在庫2
        consumableItem.setVersion(1L);

        RequestForm form = new RequestForm();
        form.setItemId(20L);
        form.setQuantity(5); // 申請5
        form.setVersion(1L);
        form.setRequestReasonId(1L);

        when(itemRepository.findById(20L)).thenReturn(Optional.of(consumableItem));
        when(requestReasonRepository.findById(1L)).thenReturn(Optional.of(testReason));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            requestService.createRequest(form, testEmployee);
        });
        assertEquals("在庫が不足しています", exception.getMessage());
    }

    @Test
    void 消耗品申請時に他者が先に更新していた場合は排他制御エラーになること() {
        // Arrange
        Item consumableItem = new Item();
        consumableItem.setId(20L);
        consumableItem.setItemType(ItemType.CONSUMABLE);
        consumableItem.setStockQuantity(10);
        consumableItem.setVersion(2L); // DB側のバージョンは2に進んでいる

        RequestForm form = new RequestForm();
        form.setItemId(20L);
        form.setQuantity(1);
        form.setVersion(1L); // 画面が持っていたバージョンは1 (古い)
        form.setRequestReasonId(1L);

        when(itemRepository.findById(20L)).thenReturn(Optional.of(consumableItem));
        when(requestReasonRepository.findById(1L)).thenReturn(Optional.of(testReason));

        // Act & Assert
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            requestService.createRequest(form, testEmployee);
        });
    }

    @Test
    void 承認待ちでない申請をキャンセルしようとすると例外が発生すること() {
        // Arrange
        Request request = new Request();
        request.setId(300L);
        request.setEmployee(testEmployee);
        request.setStatus(RequestStatus.APPROVED); // 既に承認済み

        when(requestRepository.findById(300L)).thenReturn(Optional.of(request));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            requestService.cancelRequest(300L, testEmployee);
        });
        assertEquals("キャンセルできない申請です", exception.getMessage());
    }
    
    @Test
    void 他人の申請をキャンセルしようとすると例外が発生すること() {
        // Arrange
        Request request = new Request();
        request.setId(300L);
        Employee otherEmployee = new Employee();
        otherEmployee.setId(99L);
        request.setEmployee(otherEmployee); // 他人の申請
        request.setStatus(RequestStatus.PENDING);

        when(requestRepository.findById(300L)).thenReturn(Optional.of(request));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            requestService.cancelRequest(300L, testEmployee);
        });
        assertEquals("キャンセルできない申請です", exception.getMessage());
    }

    @Test
    void 貸出備品を返却するとステータスが更新され個体が利用可能に戻ること() {
        // Arrange
        EquipmentUnit unit = new EquipmentUnit();
        unit.setId(100L);
        unit.setStatus(EquipmentStatus.IN_USE); // 貸出中

        Request request = new Request();
        request.setId(300L);
        request.setEmployee(testEmployee);
        request.setRequestType(RequestType.BORROW);
        request.setStatus(RequestStatus.APPROVED);
        request.setEquipmentUnit(unit);

        when(requestRepository.findById(300L)).thenReturn(Optional.of(request));

        // Act
        requestService.returnEquipment(300L, testEmployee);

        // Assert
        assertEquals(RequestStatus.RETURNED, request.getStatus());
        assertNotNull(request.getReturnedAt());
        assertEquals(EquipmentStatus.AVAILABLE, unit.getStatus()); // 個体が利用可能に戻ったか
        verify(equipmentUnitRepository, times(1)).save(unit);
        verify(requestRepository, times(1)).save(request);
    }
}