// src/main/java/com/example/inventory/application/service/DepartmentService.java
package com.example.inventory.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.domain.model.Department;
import com.example.inventory.domain.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 部署マスタに関する業務ロジックを管理するサービスクラス。
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;

    /**
     * 登録済みの全部署を取得します。
     *
     * @return 部署のリスト
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * 新しい部署を保存します。
     *
     * @param name 登録する部署名
     */
    @Transactional
    public void saveDepartment(String name) {
        Department dept = new Department();
        dept.setName(name);
        departmentRepository.save(dept);
    }

    /**
     * 指定されたIDの部署を削除します。
     *
     * @param id 削除対象の部署ID
     */
    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}