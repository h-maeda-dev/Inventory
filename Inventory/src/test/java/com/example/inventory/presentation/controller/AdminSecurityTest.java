// src/test/java/com/example/inventory/presentation/controller/AdminSecurityTest.java
package com.example.inventory.presentation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.inventory.SecurityConfig;
import com.example.inventory.application.service.CustomUserDetailsService;
import com.example.inventory.application.service.ItemService;
import com.example.inventory.domain.repository.EmployeeRepository;

@WebMvcTest(AdminItemController.class)
@Import(SecurityConfig.class)
public class AdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // WebMvcTestでControllerを起動するために必要なモック（実際のDB通信を行わないため）
    @MockitoBean private ItemService itemService;
    @MockitoBean private EmployeeRepository employeeRepository;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void 管理者権限を持つユーザーは管理者画面にアクセスできること() throws Exception {
        // ADMIN権限を持つユーザーとしてリクエストを送信
        mockMvc.perform(get("/admin/items").with(user("99").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void 一般権限のユーザーが管理者画面にアクセスすると403Forbiddenエラーになること() throws Exception {
        // USER権限しか持たないユーザーとして管理者URLへアクセス
        mockMvc.perform(get("/admin/items").with(user("1").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void 未認証ユーザーが管理者画面にアクセスするとログイン画面へリダイレクトされること() throws Exception {
        // ログインしていない状態でアクセス
        mockMvc.perform(get("/admin/items"))
                .andExpect(status().is3xxRedirection());
    }
}