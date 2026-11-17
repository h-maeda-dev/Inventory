// src/main/java/com/example/inventory/GlobalControllerAdvice.java
package com.example.inventory;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.inventory.domain.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

/**
 * 全てのControllerで共通して実行される処理を定義するクラス。
 * ヘッダーやメニューなどの共通レイアウト（layout.html）で必要なデータを、
 * 各Controllerのメソッドで毎回モデルに詰める手間を省くために使用します。
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final EmployeeRepository employeeRepository;

    /**
     * 現在のリクエストURLを取得し、全画面のModelに自動で追加します。
     * layout.htmlのナビゲーションメニューで、現在開いているページをハイライト表示するために使用されます。
     *
     * @param request 現在のHTTPリクエスト
     * @return 現在のURLパス（例: "/dashboard"）
     */
    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * ログイン中のユーザー情報を取得し、ヘッダーに表示する形式に整形して全画面のModelに追加します。
     * DBアクセスを伴うため、読み取り専用のトランザクションとして実行します。
     *
     * @param principal Spring Securityの認証情報（ログイン中のみ存在）
     * @return ヘッダー用のユーザー情報文字列
     */
    @ModelAttribute("loginUserInfo")
    @Transactional(readOnly = true)
    public String loginUserInfo(Principal principal) {
        // 未ログイン時（パスワードリセット画面など）
        if (principal == null) {
            return "ゲスト";
        }
        
        try {
            Long empId = Long.valueOf(principal.getName());
            return employeeRepository.findById(empId)
                    .map(e -> {
                        String deptName = (e.getDepartment() != null) ? e.getDepartment().getName() : "未所属";
                        // String.formatを用いて、IDを8桁の0埋め文字列として画面に渡す
                        return e.getName() + "（" + String.format("%08d", e.getId()) + "）: " + deptName;
                    })
                    .orElse("不明なユーザー");
        } catch (NumberFormatException e) {
            return principal.getName();
        }
    }
}