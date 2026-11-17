// src/main/java/com/example/inventory/application/service/BatchService.java
package com.example.inventory.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.domain.model.Request;
import com.example.inventory.domain.model.RequestStatus;
import com.example.inventory.domain.model.RequestType;
import com.example.inventory.domain.repository.RequestRepository;

import lombok.RequiredArgsConstructor;

/**
 * 定期的に実行されるバッチ処理を管理するサービスクラス。
 * 返却期限前のリマインドや、期限超過の督促メール送信を自動で行います。
 */
@Service
@RequiredArgsConstructor
public class BatchService {
    private final RequestRepository requestRepository;
    private final HolidayService holidayService;
    private final NotificationService notificationService;

    /**
     * 毎日朝8時に自動実行される通知処理。
     * 貸出中の備品をすべて確認し、期限超過または3営業日前のアラートを送信します。
     * データの更新は行わないため readOnly = true としています。
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void executeDailyNotifications() {
        LocalDate today = LocalDate.now();
        List<Request> allRequests = requestRepository.findAll();

        for (Request req : allRequests) {
            // 承認済み（貸出中）かつ備品貸出かつ返却期限が設定されているものを対象とする
            if (req.getStatus() == RequestStatus.APPROVED && req.getRequestType() == RequestType.BORROW && req.getDueDate() != null) {
                LocalDate dueDate = req.getDueDate().toLocalDate();

                // 督促ロジック: 現在日が返却予定日を過ぎている
                if (today.isAfter(dueDate)) {
                    notificationService.sendOverdueNotification(req);
                } 
                // リマインドロジック: 現在日が返却予定日の3営業日前（土日祝日を除外）と一致する
                else if (today.equals(holidayService.calculateBusinessDaysBefore(dueDate, 3))) {
                    notificationService.sendRemindNotification(req);
                }
            }
        }
    }
}