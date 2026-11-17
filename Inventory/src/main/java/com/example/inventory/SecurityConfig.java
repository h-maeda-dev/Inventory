// src/main/java/com/example/inventory/SecurityConfig.java
package com.example.inventory;

import com.example.inventory.presentation.handler.CustomAuthenticationFailureHandler;
import com.example.inventory.presentation.handler.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Securityの基本設定を行う構成クラス。
 * パスワードの暗号化方式や、各URLへのアクセス権限、ログイン・ログアウトの処理方法を定義します。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * パスワードを安全に保存・照合するためのエンコーダーをBeanとして登録します。
     * 本システムではBCrypt暗号化方式を採用します。
     *
     * @return BCryptPasswordEncoderのインスタンス
     */
    @Bean 
    public PasswordEncoder passwordEncoder() { 
        return new BCryptPasswordEncoder(); 
    }

    /**
     * セキュリティフィルターチェーンを定義し、HTTPリクエストに対するアクセス制御を行います。
     *
     * @param http Spring SecurityのHttpSecurityオブジェクト
     * @param successHandler ログイン成功時の独自処理を行うハンドラー
     * @param failureHandler ログイン失敗時（アカウントロック等）の独自処理を行うハンドラー
     * @return 構築されたSecurityFilterChain
     * @throws Exception 設定時の例外
     */
    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http,
            CustomAuthenticationSuccessHandler successHandler,
            CustomAuthenticationFailureHandler failureHandler) throws Exception {
        
        http
            .authorizeHttpRequests(authz -> authz
                // CSS, JS, パスワードリセット機能は未ログインでもアクセス可能に設定
                .requestMatchers("/css/**", "/js/**", "/password-reset/**").permitAll()
                // ログイン画面へのアクセスも許可
                .requestMatchers("/login").permitAll()
                // 管理者機能はADMIN権限を持つユーザーのみアクセス可能に制限
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // その他のすべてのリクエストは認証（ログイン）を必要とする
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // カスタムログイン画面のURLを指定
                .loginPage("/login")
                // ログイン成功・失敗時のカスタムハンドラーを設定
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                // ログアウト成功時のリダイレクト先を指定
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
            
        return http.build();
    }
}