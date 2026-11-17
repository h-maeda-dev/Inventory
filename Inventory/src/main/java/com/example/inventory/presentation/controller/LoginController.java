// src/main/java/com/example/inventory/presentation/controller/LoginController.java
package com.example.inventory.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ログイン画面の表示を制御するコントローラー。
 */
@Controller
public class LoginController {

    /**
     * ログイン画面を表示します。
     * エラーメッセージやログアウトメッセージの表示制御は、
     * Spring Securityからのリダイレクトパラメータ（?error, ?logout）を用いてHTML側で行われます。
     *
     * @return ログイン画面のテンプレート名
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}