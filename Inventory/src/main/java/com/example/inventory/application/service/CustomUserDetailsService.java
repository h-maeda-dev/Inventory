// src/main/java/com/example/inventory/application/service/CustomUserDetailsService.java
package com.example.inventory.application.service;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Securityの認証処理に利用されるカスタムユーザー詳細サービス。
 * 入力された社員IDを基にデータベースからユーザー情報を取得し、
 * アカウントのロック状態などのチェックを行います。
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final LoginAttemptService loginAttemptService;

    /**
     * 指定されたユーザー名（社員ID）からユーザー情報をロードします。
     *
     * @param username ログイン画面で入力された社員ID（文字列）
     * @return 認証に使用されるUserDetailsオブジェクト
     * @throws UsernameNotFoundException 社員IDが数値でない、または存在しない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long id;
        try {
            // 社員IDは数値として扱うため、文字列からの変換を試みる
            id = Long.valueOf(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("社員IDは数値で入力してください");
        }
        
        // データベースから社員情報を検索
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));

        // ロック中であれば、ロック期間が過ぎていないかチェックして解除を試みる
        if (!employee.isAccountNonLocked()) {
            if (loginAttemptService.unlockWhenTimeExpired(employee)) {
                // ロック解除成功（そのまま正常処理へ移行）
            } else {
                // ロック継続中のため、Spring Securityにロック状態であることを伝える
                return org.springframework.security.core.userdetails.User.builder()
                        .username(String.valueOf(employee.getId()))
                        .password(employee.getPassword())
                        .roles(employee.getRole().name())
                        .accountLocked(true) // 確実にロックフラグをセット
                        .build();
            }
        }

        // 正常なユーザー（ロックされていない）の情報をSpring Securityへ返す
        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(employee.getId()))
                .password(employee.getPassword())
                .roles(employee.getRole().name())
                .accountLocked(false)
                .build();
    }
}