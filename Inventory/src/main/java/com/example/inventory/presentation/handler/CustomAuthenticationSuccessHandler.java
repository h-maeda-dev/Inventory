// src/main/java/com/example/inventory/presentation/handler/CustomAuthenticationSuccessHandler.java
package com.example.inventory.presentation.handler;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.inventory.application.service.LoginAttemptService;

/**
 * ログイン認証が成功した直後に呼び出されるカスタムハンドラー。
 * 成功回数のリセット（アカウントロック機能）と、ダッシュボードへのリダイレクトを行います。
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;

    public CustomAuthenticationSuccessHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
        // ログイン成功時のデフォルト遷移先をダッシュボードに設定
        setDefaultTargetUrl("/dashboard");
        // 直前のURLを無視し、必ずダッシュボードへ遷移させる（今回の要件定義）
        setAlwaysUseDefaultTargetUrl(true); 
    }

    /**
     * 認証成功時の処理。ログイン失敗回数をリセットした上で、親クラスの遷移処理を呼び出します。
     *
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     * @param authentication 認証されたユーザー情報
     * @throws ServletException サーブレットの例外
     * @throws IOException 入出力例外
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, 
            HttpServletResponse response, 
            Authentication authentication) throws ServletException, IOException {
        
        String username = authentication.getName();
        loginAttemptService.loginSucceeded(username);
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
}