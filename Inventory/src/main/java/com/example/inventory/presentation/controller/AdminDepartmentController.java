// src/main/java/com/example/inventory/presentation/controller/AdminDepartmentController.java
package com.example.inventory.presentation.controller;

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

import com.example.inventory.application.service.DepartmentService;
import com.example.inventory.domain.model.Department;
import com.example.inventory.presentation.form.DepartmentForm;

import lombok.RequiredArgsConstructor;

/**
 * 管理者向けの部署マスタ管理を行うコントローラー。
 * 部署の一覧表示、新規登録、および削除リクエストを処理します。
 */
@Controller
@RequestMapping("/admin/departments")
@RequiredArgsConstructor
public class AdminDepartmentController {
    
    private final DepartmentService departmentService;

    /**
     * 登録済みの部署一覧および新規登録フォームを表示します。
     *
     * @param model ビューへ渡すデータモデル
     * @return 部署管理画面のテンプレート名
     */
    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("departmentForm", new DepartmentForm());
        return "admin_departments";
    }

    /**
     * 新規部署を登録します。入力エラー時は元の画面へ戻します。
     *
     * @param form 登録する部署の情報を持つフォーム
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時は一覧画面へのリダイレクト、エラー時は元の画面
     */
    @PostMapping
    public String addDepartment(
            @Validated @ModelAttribute DepartmentForm form, 
            BindingResult result, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin_departments";
        }
        
        // 保存時の重複エラー（一意制約違反）等はGlobalExceptionHandlerで捕捉される
        departmentService.saveDepartment(form.getName());
        
        redirectAttributes.addFlashAttribute("successMessage", "部署「" + form.getName() + "」を登録しました");
        return "redirect:/admin/departments";
    }

    /**
     * 指定された部署を削除します。
     * ※所属社員がいる場合などの削除エラー（外部キー制約違反）はGlobalExceptionHandlerにて捕捉されます。
     *
     * @param id 削除対象の部署ID
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 一覧画面へのリダイレクト
     */
    @PostMapping("/{id}/delete")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // メッセージ表示用に、削除を実行する前に部署名を取得しておく
        String deptName = departmentService.getAllDepartments().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst().map(Department::getName).orElse("不明な部署");
        
        // 削除実行（エラー発生時はGlobalExceptionHandlerへ処理を委譲）
        departmentService.deleteDepartment(id);
        
        redirectAttributes.addFlashAttribute("successMessage", "部署「" + deptName + "」を削除しました");
        return "redirect:/admin/departments";
    }
}