// src/main/java/com/example/inventory/presentation/handler/AuthenticationEventListener.java
package com.example.inventory.presentation.handler;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.example.inventory.application.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;

/**
 * Spring Securityの内部で発生する認証イベントを監視するリスナークラス。
 * 主にログイン失敗回数の記録とアカウントロック機能のために使用します。
 */
@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;

    /**
     * ログインが成功したイベントを検知し、失敗回数をリセットします。
     *
     * @param event 認証成功イベント
     */
    @EventListener
    public void authenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        loginAttemptService.loginSucceeded(username);
    }

    /**
     * 資格情報（パスワード等）の間違いによる認証失敗イベントを検知し、
     * 失敗回数をカウントアップします。
     *
     * @param event 認証失敗イベント
     */
    @EventListener
    public void authenticationFailed(AuthenticationFailureBadCredentialsEvent event) {
        // イベントオブジェクトから入力されたユーザー名（社員ID）を取得
        String username = (String) event.getAuthentication().getPrincipal();
        loginAttemptService.loginFailed(username);
    }
}