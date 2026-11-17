// src/main/java/com/example/inventory/application/service/EmployeeService.java
package com.example.inventory.application.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.domain.model.Department;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.repository.DepartmentRepository;
import com.example.inventory.domain.repository.EmployeeRepository;
import com.example.inventory.presentation.form.EmployeeEditForm;
import com.example.inventory.presentation.form.EmployeeForm;

import lombok.RequiredArgsConstructor;

/**
 * 社員（ユーザー）に関する業務ロジックを管理するサービスクラス。
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 登録済みの全社員を取得します。
     *
     * @return 社員のリスト
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * 登録済みの全部署を取得します（社員登録時のプルダウン用）。
     *
     * @return 部署のリスト
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
    
    /**
     * IDを指定して社員情報を取得します。
     *
     * @param id 取得対象の社員ID
     * @return 社員エンティティ
     * @throws IllegalArgumentException 社員が存在しない場合
     */
    @Transactional(readOnly = true)
    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定された社員が見つかりません"));
    }

    /**
     * 既存社員の情報を更新します。
     *
     * @param form 画面から送信された更新情報
     * @throws IllegalArgumentException 指定された部署が存在しない場合
     */
    @Transactional
    public void updateEmployee(EmployeeEditForm form) {
        Employee employee = getEmployee(form.getId());
        Department dept = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("指定された部署が存在しません"));

        employee.setName(form.getName());
        employee.setEmail(form.getEmail());
        employee.setRole(form.getRole());
        employee.setDepartment(dept);

        // パスワードが入力されている（空文字でない）場合のみ、暗号化して更新する
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        employeeRepository.save(employee);
    }
    
    /**
     * 新しい社員を登録します。パスワードは暗号化して保存されます。
     *
     * @param form 画面から送信された新規社員情報
     * @throws IllegalArgumentException 指定された部署が存在しない場合
     */
    @Transactional
    public void saveEmployee(EmployeeForm form) {
        Department dept = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("指定された部署が存在しません"));
                
        Employee employee = new Employee();
        employee.setName(form.getName());
        employee.setEmail(form.getEmail());
        
        // セキュリティのため、平文のまま保存せず暗号化する
        employee.setPassword(passwordEncoder.encode(form.getPassword()));
        employee.setRole(form.getRole());
        employee.setDepartment(dept);
        
        employeeRepository.save(employee);
    }

    /**
     * 指定されたIDの社員を削除します。
     *
     * @param id 削除対象の社員ID
     */
    @Transactional
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}