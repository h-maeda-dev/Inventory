// src/main/java/com/example/inventory/application/service/PasswordResetService.java
package com.example.inventory.application.service;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.PasswordResetRequest;
import com.example.inventory.domain.repository.EmployeeRepository;
import com.example.inventory.domain.repository.PasswordResetRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * パスワードリセットに関する業務ロジックを管理するサービスクラス。
 * トークンの生成・検証や、データベースの更新、メール送信の呼び出しを行います。
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    private final EmployeeRepository employeeRepository;
    private final PasswordResetRequestRepository resetRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    /**
     * 未ログインのユーザーからパスワードリセットの申請を受け付けます。
     * 入力された社員IDとメールアドレスが一致するか検証し、新しいトークンを発行します。
     *
     * @param employeeId 申請画面で入力された社員ID
     * @param email 申請画面で入力されたメールアドレス
     * @throws IllegalArgumentException 社員が存在しない、または情報が一致しない場合
     */
    @Transactional
    public void requestReset(Long employeeId, String email) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("社員が存在しません"));
                
        // セキュリティ対策：登録メールアドレスと一致しているか確認
        if (!emp.getEmail().equals(email)) {
            throw new IllegalArgumentException("登録情報と一致しません");
        }
        
        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmployee(emp);
        // ランダムで推測困難な一意の文字列（UUID）をトークンとして生成
        req.setToken(UUID.randomUUID().toString());
        req.setRequestedAt(LocalDateTime.now());
        // 有効期限を申請から24時間後に設定
        req.setExpiresAt(LocalDateTime.now().plusHours(24));
        resetRepository.save(req);
    }

    /**
     * 管理者が承認待ちのパスワードリセット申請一覧を取得します。
     *
     * @return 未承認のパスワードリセット申請リスト
     */
    @Transactional(readOnly = true)
    public List<PasswordResetRequest> getPendingRequests() {
        return resetRepository.findByIsApprovedFalse();
    }

    /**
     * 管理者がパスワードリセット申請を承認し、対象者へURL付きメールを送信します。
     *
     * @param id 承認対象のリセット申請ID
     */
    @Transactional
    public void approveRequest(Long id) {
        PasswordResetRequest req = resetRepository.findById(id).orElseThrow();
        req.setIsApproved(true);
        resetRepository.save(req);
        // 非同期でメール送信サービスを呼び出す
        notificationService.sendPasswordResetUrl(req.getEmployee().getEmail(), req.getToken());
    }

    /**
     * トークンを使用して新しいパスワードへ更新します。
     * トークンの有効性（使用済みでないか、承認済みか、期限切れでないか）を厳密にチェックします。
     *
     * @param token メールに記載されていたワンタイムトークン
     * @param newPassword 設定する新しいパスワード（平文）
     * @throws IllegalArgumentException トークンが無効、または期限切れ等の場合
     */
    @Transactional
    public void updatePasswordWithToken(String token, String newPassword) {
        PasswordResetRequest req = resetRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効なトークンです"));
                
        // トークンの状態と有効期限をチェック
        if (req.getIsUsed() || !req.getIsApproved() || req.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("このトークンは期限切れ、または既に使用されています");
        }
        
        Employee emp = req.getEmployee();
        // 新しいパスワードを暗号化して保存
        emp.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(emp);
        
        // 再利用を防ぐため、トークンを使用済みに更新
        req.setIsUsed(true);
        resetRepository.save(req);
    }
    
    /**
     * ログイン中のユーザーが自身のパスワードを変更する際に使用します。
     *
     * @param employeeId 対象の社員ID
     * @param newPassword 設定する新しいパスワード（平文）
     */
    @Transactional
    public void updatePassword(Long employeeId, String newPassword) {
        Employee emp = employeeRepository.findById(employeeId).orElseThrow();
        emp.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(emp);
    }
}