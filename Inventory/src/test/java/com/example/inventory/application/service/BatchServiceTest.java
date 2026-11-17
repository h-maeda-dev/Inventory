// src/test/java/com/example/inventory/application/service/BatchServiceTest.java
package com.example.inventory.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.inventory.domain.model.Request;
import com.example.inventory.domain.model.RequestStatus;
import com.example.inventory.domain.model.RequestType;
import com.example.inventory.domain.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
public class BatchServiceTest {

    @Mock private RequestRepository requestRepository;
    @Mock private HolidayService holidayService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private BatchService batchService;

    @Test
    void 期限超過と期限接近のアラートメールが正しく送信されること() {
        LocalDate today = LocalDate.now();

        // 1. 期限を超過している申請（督促対象）
        Request overdueReq = new Request();
        overdueReq.setStatus(RequestStatus.APPROVED);
        overdueReq.setRequestType(RequestType.BORROW);
        overdueReq.setDueDate(today.minusDays(1).atStartOfDay());

        // 2. ちょうど3営業日後が期限の申請（リマインド対象）
        Request remindReq = new Request();
        remindReq.setStatus(RequestStatus.APPROVED);
        remindReq.setRequestType(RequestType.BORROW);
        remindReq.setDueDate(today.plusDays(3).atStartOfDay()); // 仮に3日後とする

        // 3. 期限に余裕がある申請（対象外）
        Request safeReq = new Request();
        safeReq.setStatus(RequestStatus.APPROVED);
        safeReq.setRequestType(RequestType.BORROW);
        safeReq.setDueDate(today.plusDays(10).atStartOfDay());

        when(requestRepository.findAll()).thenReturn(Arrays.asList(overdueReq, remindReq, safeReq));
        // 3営業日前を計算するモック設定
        when(holidayService.calculateBusinessDaysBefore(remindReq.getDueDate().toLocalDate(), 3)).thenReturn(today);
        when(holidayService.calculateBusinessDaysBefore(safeReq.getDueDate().toLocalDate(), 3)).thenReturn(today.plusDays(7));

        // Act
        batchService.executeDailyNotifications();

        // Assert
        verify(notificationService, times(1)).sendOverdueNotification(overdueReq);
        verify(notificationService, times(1)).sendRemindNotification(remindReq);
        verify(notificationService, never()).sendOverdueNotification(safeReq);
        verify(notificationService, never()).sendRemindNotification(safeReq);
    }
}