// src/main/java/com/example/inventory/presentation/controller/RequestController.java
package com.example.inventory.presentation.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.inventory.application.service.RequestService;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.repository.EmployeeRepository;
import com.example.inventory.presentation.form.RequestForm;

import lombok.RequiredArgsConstructor;

/**
 * 物品の貸出・支給の「申請」、申請の「キャンセル」、および貸出中備品の「返却」を受け付けるコントローラー。
 * 状態を変更する操作であるため、すべてPOSTメソッドで実装されています。
 */
@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {
    
    private final RequestService requestService;
    private final EmployeeRepository employeeRepository;

    /**
     * 現在ログインしている社員のエンティティを取得する内部メソッド。
     *
     * @param principal 認証情報
     * @return ログイン中のEmployeeオブジェクト
     */
    private Employee getCurrentEmployee(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("認証されていません");
        }
        Long empId = Long.valueOf(principal.getName());
        return employeeRepository.findById(empId)
                .orElseThrow(() -> new IllegalArgumentException("社員情報が見つかりません"));
    }

    /**
     * 物品詳細画面から送信された申請情報を処理します。
     * 申請の受領と同時に、在庫の減算や個体の仮押さえが行われます。
     *
     * @param form 画面から送信された申請内容
     * @param result バリデーション結果
     * @param principal 認証情報
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時はダッシュボード、入力エラー時は元の物品詳細画面へリダイレクト
     */
    @PostMapping
    public String createRequest(
            @Validated @ModelAttribute RequestForm form, 
            BindingResult result, 
            Principal principal, 
            RedirectAttributes redirectAttributes) {
        
        // 入力値にエラーがある場合は元の品目詳細画面へ差し戻す
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "入力内容にエラーがあります");
            return "redirect:/items/" + form.getItemId();
        }
        
        requestService.createRequest(form, getCurrentEmployee(principal));
        redirectAttributes.addFlashAttribute("successMessage", "申請が完了しました");
        
        return "redirect:/dashboard";
    }

    /**
     * 承認待ちの申請をキャンセル（取り下げ）します。
     * 申請時に仮押さえされた在庫や個体の状態を元に戻します。
     *
     * @param id キャンセル対象の申請ID
     * @param principal 認証情報
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return ダッシュボードへのリダイレクト
     */
    @PostMapping("/{id}/cancel")
    public String cancelRequest(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        requestService.cancelRequest(id, getCurrentEmployee(principal));
        redirectAttributes.addFlashAttribute("successMessage", "申請をキャンセルしました");
        
        return "redirect:/dashboard";
    }

    /**
     * 貸出中（承認済み）の備品を返却します。
     *
     * @param id 返却対象の申請ID
     * @param principal 認証情報
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return ダッシュボードへのリダイレクト
     */
    @PostMapping("/{id}/return")
    public String returnRequest(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        requestService.returnEquipment(id, getCurrentEmployee(principal));
        redirectAttributes.addFlashAttribute("successMessage", "返却処理が完了しました");
        
        return "redirect:/dashboard";
    }
}