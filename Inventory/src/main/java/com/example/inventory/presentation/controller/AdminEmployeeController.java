// src/main/java/com/example/inventory/presentation/controller/AdminEmployeeController.java
package com.example.inventory.presentation.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.inventory.application.service.EmployeeService;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.presentation.form.EmployeeEditForm;
import com.example.inventory.presentation.form.EmployeeForm;

import lombok.RequiredArgsConstructor;

/**
 * 管理者向けの社員（ユーザー）管理を行うコントローラー。
 * ユーザーの一覧表示、新規登録、情報の編集、および削除リクエストを処理します。
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminEmployeeController {
    
    private final EmployeeService employeeService;

    /**
     * 登録済みの全社員一覧と、新規登録用のフォーム画面を表示します。
     *
     * @param model ビューへ渡すデータモデル
     * @return ユーザー管理画面のテンプレート名
     */
    @GetMapping
    public String listEmployees(Model model) {
        model.addAttribute("users", employeeService.getAllEmployees());
        model.addAttribute("departments", employeeService.getAllDepartments());
        model.addAttribute("employeeForm", new EmployeeForm());
        return "admin_users";
    }
    
    /**
     * 指定された社員の情報を編集するフォーム画面を表示します。
     *
     * @param id 編集対象の社員ID
     * @param model ビューへ渡すデータモデル
     * @return 社員情報編集画面のテンプレート名
     */
    @GetMapping("/{id}/edit")
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployee(id);
        
        EmployeeEditForm form = new EmployeeEditForm();
        form.setId(employee.getId());
        form.setName(employee.getName());
        form.setEmail(employee.getEmail());
        form.setRole(employee.getRole());
        
        if (employee.getDepartment() != null) {
            form.setDepartmentId(employee.getDepartment().getId());
        }
        
        model.addAttribute("employeeEditForm", form);
        model.addAttribute("departments", employeeService.getAllDepartments());
        return "admin_user_edit";
    }

    /**
     * 画面から送信された社員の更新情報をデータベースへ保存します。
     * メールアドレスの重複などが発生した場合は元の画面へ戻します。
     *
     * @param id パス変数として渡された対象社員ID
     * @param form 送信された更新情報
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時は一覧画面へリダイレクト、エラー時は編集画面へ戻る
     */
    @PostMapping("/{id}/edit")
    public String updateEmployee(
            @PathVariable Long id, 
            @Validated @ModelAttribute("employeeEditForm") EmployeeEditForm form, 
            BindingResult result, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        // パス変数とフォームのIDが一致しない場合は不正アクセスとして処理
        if (!id.equals(form.getId())) {
            throw new IllegalArgumentException("不正なリクエストです");
        }

        if (result.hasErrors()) {
            model.addAttribute("departments", employeeService.getAllDepartments());
            return "admin_user_edit";
        }
        
        try {
            employeeService.updateEmployee(form);
            redirectAttributes.addFlashAttribute("successMessage", "社員「" + form.getName() + "」の情報を更新しました");
        } catch (DataIntegrityViolationException e) {
            // 他の社員が既に使用しているメールアドレスへの変更を検知
            model.addAttribute("departments", employeeService.getAllDepartments());
            model.addAttribute("errorMessage", "メールアドレスが既に他の社員に登録されています");
            return "admin_user_edit";
        }
        
        return "redirect:/admin/users";
    }
    
    /**
     * 新規の社員アカウントを作成します。
     *
     * @param form 送信された新規社員情報
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時は一覧画面へリダイレクト、エラー時は一覧画面（登録フォーム付き）へ戻る
     */
    @PostMapping
    public String createEmployee(
            @Validated @ModelAttribute("employeeForm") EmployeeForm form, 
            BindingResult result, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("users", employeeService.getAllEmployees());
            model.addAttribute("departments", employeeService.getAllDepartments());
            return "admin_users";
        }
        
        // 保存時の重複エラー等はGlobalExceptionHandlerで捕捉される
        employeeService.saveEmployee(form);
        
        redirectAttributes.addFlashAttribute("successMessage", "社員「" + form.getName() + "」を登録しました");
        return "redirect:/admin/users";
    }

    /**
     * 指定された社員を削除します。
     *
     * @param id 削除対象の社員ID
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return ユーザー管理画面へのリダイレクト
     */
    @PostMapping("/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // メッセージ表示用に、削除実行前に社員名を取得しておく
        String empName = employeeService.getAllEmployees().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst().map(Employee::getName).orElse("不明な社員");
                
        // 削除に伴う外部キー制約違反等はGlobalExceptionHandlerで捕捉される
        employeeService.deleteEmployee(id);
        
        redirectAttributes.addFlashAttribute("successMessage", "社員「" + empName + "」を削除しました");
        return "redirect:/admin/users";
    }
}