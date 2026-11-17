package com.example.inventory.application.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.domain.model.*;
import com.example.inventory.domain.repository.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class BatchServiceIntegrationTest {

    @Autowired private BatchService batchService;
    @Autowired private RequestRepository requestRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private EquipmentUnitRepository equipmentUnitRepository;

    @MockitoBean private NotificationService notificationService;

    @Test
    void データベースから取得した実際の申請状態に基づいてバッチ処理が正しく実行されること() {
        Employee emp = new Employee();
        emp.setName("テスト社員");
        emp.setEmail("test@example.com");
        emp.setPassword("password");
        emp.setRole(Role.USER);
        employeeRepository.save(emp);

        Item item = new Item();
        item.setName("テスト備品");
        item.setCategory("テスト");
        item.setItemType(ItemType.EQUIPMENT);
        item.setSafeStockCount(1);
        itemRepository.save(item);

        EquipmentUnit unit = new EquipmentUnit();
        unit.setManagementCode("UNIT-01");
        unit.setStatus(EquipmentStatus.IN_USE);
        unit.setItem(item);
        equipmentUnitRepository.save(unit);

        LocalDate today = LocalDate.now();

        Request overdueReq = new Request();
        overdueReq.setEmployee(emp);
        overdueReq.setItem(item);
        overdueReq.setEquipmentUnit(unit);
        overdueReq.setRequestType(RequestType.BORROW);
        overdueReq.setStatus(RequestStatus.APPROVED);
        overdueReq.setRequestedAt(LocalDateTime.now());
        overdueReq.setDueDate(today.minusDays(1).atStartOfDay());
        requestRepository.save(overdueReq);

        Request safeReq = new Request();
        safeReq.setEmployee(emp);
        safeReq.setItem(item);
        safeReq.setEquipmentUnit(unit);
        safeReq.setRequestType(RequestType.BORROW);
        safeReq.setStatus(RequestStatus.APPROVED);
        safeReq.setRequestedAt(LocalDateTime.now());
        safeReq.setDueDate(today.plusDays(10).atStartOfDay());
        requestRepository.save(safeReq);

        batchService.executeDailyNotifications();

        verify(notificationService, times(1)).sendOverdueNotification(argThat(r -> r.getId().equals(overdueReq.getId())));
        verify(notificationService, never()).sendOverdueNotification(argThat(r -> r.getId().equals(safeReq.getId())));
        verify(notificationService, never()).sendRemindNotification(argThat(r -> r.getId().equals(safeReq.getId())));
    }
}