// src/main/java/com/example/inventory/application/service/NotificationService.java
package com.example.inventory.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.Request;
import com.example.inventory.domain.model.Role;
import com.example.inventory.domain.repository.EmployeeRepository;

/**
 * システム内からのメール送信を担当するサービスクラス。
 * 申請通知、承認結果、在庫アラート、期日前リマインド、督促、パスワードリセットなどの
 * 各種メールを非同期（@Async）で送信します。
 */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;
    private final EmployeeRepository employeeRepository;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public NotificationService(JavaMailSender mailSender, EmployeeRepository employeeRepository) {
        this.mailSender = mailSender;
        this.employeeRepository = employeeRepository;
    }

    /**
     * 共通のメール送信処理。
     * メール送信に失敗した場合でも、システム全体の業務処理（データベース更新など）を
     * 止めないよう、例外をキャッチしてログ出力のみ行います。
     *
     * @param to 送信先のメールアドレス
     * @param subject メールの件名
     * @param text メールの本文
     */
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@example.com");
            message.setTo(to);
            message.setSubject("[備品管理システム] " + subject);
            message.setText(text + "\n\n--\n備品管理システム\n" + baseUrl);
            mailSender.send(message);
        } catch (Exception e) {
            // メール送信失敗時は業務処理（DB更新等）を止めないようログ出力のみとする
            logger.error("メール送信失敗: " + subject, e);
        }
    }

    /**
     * 管理者権限（ADMIN）を持つ全社員のメールアドレス一覧を取得する内部メソッド。
     *
     * @return 管理者のメールアドレスのリスト
     */
    private List<String> getAdminEmails() {
        return employeeRepository.findByRole(Role.ADMIN).stream()
                .map(Employee::getEmail)
                .toList();
    }

    /**
     * 新規の申請があったことを全管理者へ通知します。
     *
     * @param request 提出された申請情報
     */
    @Async
    public void sendRequestNotification(Request request) {
        List<String> adminEmails = getAdminEmails();
        for (String email : adminEmails) {
            sendEmail(email, "新規申請がありました", request.getEmployee().getDisplayName() + "さんから新規申請が提出されました。");
        }
    }

    /**
     * 申請が承認または却下された結果を申請者へ通知します。
     *
     * @param request 処理された申請情報
     */
    @Async
    public void sendApprovalResultNotification(Request request) {
        String result = request.getStatus().name().equals("APPROVED") ? "承認" : "却下";
        sendEmail(request.getEmployee().getEmail(), "申請結果のお知らせ", "あなたの申請（ID: " + request.getId() + "）が" + result + "されました。");
    }

    /**
     * 消耗品の在庫が安全基準を下回った場合に全管理者へアラートを通知します。
     *
     * @param itemName 在庫低下した品名
     * @param currentStock 現在の在庫数
     */
    @Async
    public void sendLowStockAlert(String itemName, int currentStock) {
        List<String> adminEmails = getAdminEmails();
        for (String email : adminEmails) {
            sendEmail(email, "在庫低下アラート", itemName + " の利用可能在庫（個体数）が " + currentStock + " になりました。補充をご検討ください。");
        }
    }

    /**
     * 貸出中備品の返却期限が近づいている（3営業日前）ことを申請者へリマインドします。
     *
     * @param request 対象の貸出申請情報
     */
    @Async
    public void sendRemindNotification(Request request) {
        sendEmail(request.getEmployee().getEmail(), "返却期限が近づいています", "貸出中の備品（申請ID: " + request.getId() + "）の返却期限が3営業日後に迫っています。");
    }

    /**
     * 貸出中備品の返却期限を過ぎていることを申請者へ督促します。
     *
     * @param request 対象の貸出申請情報
     */
    @Async
    public void sendOverdueNotification(Request request) {
        sendEmail(request.getEmployee().getEmail(), "【督促】返却期限を超過しています", "貸出中の備品（申請ID: " + request.getId() + "）の返却期限を過ぎています。至急ご返却ください。");
    }

    /**
     * パスワードリセット用のワンタイムURLを申請者へ送信します。
     *
     * @param email 送信先のメールアドレス
     * @param token 発行されたワンタイムトークン
     */
    @Async
    public void sendPasswordResetUrl(String email, String token) {
        String resetUrl = baseUrl + "/password-reset/form?token=" + token;
        sendEmail(email, "パスワード再設定URLのお知らせ", "以下のURLにアクセスして、新しいパスワードを設定してください（24時間有効）。\n" + resetUrl);
    }
}