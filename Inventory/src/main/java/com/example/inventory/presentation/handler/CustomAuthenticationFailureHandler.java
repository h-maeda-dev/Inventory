// src/main/java/com/example/inventory/presentation/handler/CustomAuthenticationFailureHandler.java
package com.example.inventory.presentation.handler;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.example.inventory.application.service.LoginAttemptService;

/**
 * パスワード間違いなどでログインに失敗した際に呼び出されるカスタムハンドラー。
 * 失敗回数のカウントアップと、エラー種類に応じたリダイレクトを行います。
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    public CustomAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * 認証失敗時の処理。リクエストから社員IDを取得して失敗を記録し、
     * 画面側へエラー状態を伝えるためのクエリパラメータを付与してリダイレクトします。
     *
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     * @param exception 発生した認証例外
     * @throws IOException 入出力例外
     * @throws ServletException サーブレットの例外
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, 
            HttpServletResponse response, 
            AuthenticationException exception) throws IOException, ServletException {
        
        // リクエストから直接ユーザー名（社員ID）を取得して失敗を記録
        String username = request.getParameter("username");
        if (username != null && !username.isBlank()) {
            loginAttemptService.loginFailed(username);
        }

        // 失敗の種類に応じたリダイレクト先URLのパラメータを確定
        String redirectUrl = "/login?error=true";
        if (exception instanceof LockedException) {
            redirectUrl = "/login?error=locked";
        }
        
        // HttpServletResponseを使ってリダイレクトを実行
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}