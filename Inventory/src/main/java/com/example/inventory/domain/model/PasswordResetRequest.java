// src/main/java/com/example/inventory/domain/model/PasswordResetRequest.java
package com.example.inventory.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * パスワードリセットの申請情報および発行されたトークンを管理するエンティティ。
 * トークンの有効期限や、使用済み・承認済みのフラグを保持します。
 */
@Entity
@Table(name = "password_reset_requests")
@Getter 
@Setter 
@NoArgsConstructor
public class PasswordResetRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 申請を行った対象の社員（多対1のリレーション） */
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** URLに付与される一意のワンタイムトークン */
    @Column(nullable = false, unique = true)
    private String token;

    /** リセット申請が行われた日時 */
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    /** トークンの有効期限（例：申請から24時間後など） */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** 管理者によって承認され、メールが送信されたかどうかのフラグ */
    @Column(nullable = false)
    private Boolean isApproved = false;

    /** このトークンが既にパスワード再設定に使用されたかどうかのフラグ */
    @Column(nullable = false)
    private Boolean isUsed = false;
}