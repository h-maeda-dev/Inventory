// src/test/java/com/example/inventory/application/service/DepartmentServiceTest.java
package com.example.inventory.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.inventory.domain.model.Department;
import com.example.inventory.domain.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    // モック化（偽物化）する依存オブジェクト
    @Mock
    private DepartmentRepository departmentRepository;

    // モックを注入してテスト対象とするオブジェクト
    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void 全ての部署が取得できること() {
        // 準備 (Arrange): リポジトリが呼ばれた際のダミーの戻り値を設定
        Department d1 = new Department();
        d1.setId(1L);
        d1.setName("営業部");
        Department d2 = new Department();
        d2.setId(2L);
        d2.setName("システム管理部");
        
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(d1, d2));

        // 実行 (Act)
        List<Department> result = departmentService.getAllDepartments();

        // 検証 (Assert): 結果が期待通りか確認
        assertEquals(2, result.size());
        assertEquals("営業部", result.get(0).getName());
        assertEquals("システム管理部", result.get(1).getName());
        verify(departmentRepository, times(1)).findAll(); // findAllが1回呼ばれたことを検証
    }

    @Test
    void 部署の新規保存が正しくリポジトリに渡されること() {
        // 実行 (Act)
        departmentService.saveDepartment("新規部署");

        // 検証 (Assert): リポジトリのsaveメソッドが、任意のDepartmentオブジェクトを引数にして1回呼ばれたか確認
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void 部署の削除処理が正しくリポジトリに渡されること() {
        // 実行 (Act)
        departmentService.deleteDepartment(1L);

        // 検証 (Assert)
        verify(departmentRepository, times(1)).deleteById(1L);
    }
}