// src/test/java/com/example/inventory/presentation/controller/RequestControllerTest.java
package com.example.inventory.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
import com.example.inventory.application.service.RequestService;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.repository.EmployeeRepository;
import com.example.inventory.presentation.form.RequestForm;

@WebMvcTest(RequestController.class)
@Import(SecurityConfig.class)
public class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private RequestService requestService;
    @MockitoBean private EmployeeRepository employeeRepository;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void 入力値にエラーがある場合は申請処理が実行されず元の画面へリダイレクトすること() throws Exception {
        // Act: バリデーション違反となるデータ（数量0、事由未選択など）を送信
        mockMvc.perform(post("/requests")
                .with(user("1").roles("USER"))
                .with(csrf()) // POSTリクエストにはCSRFトークンが必要
                .param("itemId", "10")
                .param("quantity", "0"))
                // Assert: 申請完了画面（ダッシュボード）ではなく、元の品目詳細画面へ差し戻されることを確認
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/10"))
                .andExpect(flash().attributeExists("errorMessage"));

        // Assert: Serviceの申請処理が1度も呼び出されていない（不正なデータで登録処理が走っていない）ことを検証
        verify(requestService, never()).createRequest(any(RequestForm.class), any(Employee.class));
    }
}