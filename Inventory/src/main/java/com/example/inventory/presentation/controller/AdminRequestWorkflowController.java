// src/main/java/com/example/inventory/presentation/controller/AdminRequestWorkflowController.java
package com.example.inventory.presentation.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import com.example.inventory.application.dto.PendingRequestDto;
import com.example.inventory.application.dto.RequestViewDto;
import com.example.inventory.application.service.ReasonService;
import com.example.inventory.application.service.RequestService;
import com.example.inventory.domain.model.Request;
import com.example.inventory.domain.model.RequestType;
import com.example.inventory.presentation.form.RejectForm;

import lombok.RequiredArgsConstructor;

/**
 * 管理者向けの申請承認ワークフローおよび履歴照会を担うコントローラー。
 * 未承認申請の表示、承認・却下処理、および過去の全履歴の表示を行います。
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminRequestWorkflowController {
    
    private final RequestService requestService;
    private final ReasonService reasonService;

    /**
     * 未承認（承認待ち）の申請一覧画面を表示します。
     * データベースから取得したエンティティをそのまま画面に渡さず、
     * 表示に必要な項目だけを抽出したDTOへ詰め替えて渡します。
     *
     * @param model ビューへ渡すデータモデル
     * @return ワークフロー画面のテンプレート名
     */
    @GetMapping
    public String workflow(Model model) {
        List<PendingRequestDto> dtos = requestService.getPendingRequests().stream().map(r -> {
            // 備品と消耗品で、参照先のエンティティが異なる項目を条件分岐で取得
            String itemName = r.getRequestType() == RequestType.BORROW ? r.getEquipmentUnit().getItem().getName() : r.getItem().getName();
            String mgmtCode = r.getRequestType() == RequestType.BORROW ? r.getEquipmentUnit().getManagementCode() : null;
            
            return new PendingRequestDto(
                r.getId(), 
                r.getRequestedAt(), 
                r.getEmployee().getDisplayName(),
                r.getRequestType().name(), 
                itemName, 
                mgmtCode, 
                r.getDueDate(), 
                r.getQuantity()
            );
        }).toList();
        
        model.addAttribute("pendingRequests", dtos);
        return "admin_workflow";
    }

    /**
     * 一覧画面から直接、指定された申請を承認します。
     *
     * @param id 承認対象の申請ID
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return ワークフロー画面へのリダイレクト
     */
    @PostMapping("/requests/{id}/approve")
    public String approveRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        requestService.approveRequest(id);
        redirectAttributes.addFlashAttribute("successMessage", "承認しました");
        
        return "redirect:/admin";
    }

    /**
     * 指定された申請の却下確認画面を表示します。
     * 却下対象の申請情報と、却下事由の選択フォーム（プルダウン用データ）を画面へ渡します。
     *
     * @param id 却下対象の申請ID
     * @param model ビューへ渡すデータモデル
     * @return 却下確認画面のテンプレート名
     */
    @GetMapping("/requests/{id}/reject-confirm")
    public String rejectConfirmForm(@PathVariable Long id, Model model) {
        Request request = requestService.getRequest(id);
        
        // 備品と消耗品で参照先が異なる項目を解決
        String itemName = request.getRequestType() == RequestType.BORROW ? request.getEquipmentUnit().getItem().getName() : request.getItem().getName();
        String mgmtCode = request.getRequestType() == RequestType.BORROW ? request.getEquipmentUnit().getManagementCode() : null;
        
        // 確認画面に表示するため、DTOを生成して渡す
        PendingRequestDto dto = new PendingRequestDto(
            request.getId(), 
            request.getRequestedAt(), 
            request.getEmployee().getDisplayName(),
            request.getRequestType().name(), 
            itemName, 
            mgmtCode, 
            request.getDueDate(), 
            request.getQuantity()
        );
        model.addAttribute("requestDto", dto);
        
        // 事由マスタから、却下時に選択可能な事由のリストを取得して渡す
        model.addAttribute("rejectReasons", reasonService.getAllRejectReasons());
        
        // 入力用の空フォームを渡す
        model.addAttribute("rejectForm", new RejectForm());
        
        return "admin_reject_confirm";
    }

    /**
     * 確認画面から送信された却下情報を受け取り、却下処理を実行します。
     * 入力内容（却下事由の未選択など）にエラーがある場合は、確認画面へ戻します。
     *
     * @param id 却下対象の申請ID
     * @param form 選択された却下事由と補足テキスト
     * @param result バリデーション結果
     * @param model ビューへ渡すデータモデル（エラー時に再表示するため）
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 成功時はワークフロー画面へリダイレクト、エラー時は元の確認画面を再表示
     */
    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(
            @PathVariable Long id, 
            @Validated @ModelAttribute RejectForm form, 
            BindingResult result, 
            Model model, 
            RedirectAttributes redirectAttributes) {
        
        // バリデーションエラー時は、必要な情報を再度モデルに詰めて確認画面を表示する
        if (result.hasErrors()) {
            return rejectConfirmForm(id, model);
        }
        
        requestService.rejectRequest(id, form.getRejectReasonId(), form.getRejectNote());
        redirectAttributes.addFlashAttribute("successMessage", "却下しました");
        
        return "redirect:/admin";
    }

    /**
     * 過去のすべての申請・貸出・消費の履歴を照会する画面を表示します。
     * ページネーション（1ページ15件、申請日時の新しい順）に対応しています。
     *
     * @param pageable ページネーションおよびソートの条件
     * @param model ビューへ渡すデータモデル
     * @return 履歴照会画面のテンプレート名
     */
    @GetMapping("/history")
    public String history(
            @PageableDefault(size = 15, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable, 
            Model model) {
        
        Page<Request> requestPage = requestService.getAllHistory(pageable);
        
        // 取得したEntityのPage（ページ情報）を保持したまま、中身だけをDTOへ変換する
        Page<RequestViewDto> dtoPage = requestPage.map(r -> {
            String itemName = r.getRequestType() == RequestType.BORROW ? r.getEquipmentUnit().getItem().getName() : r.getItem().getName();
            String mgmtCode = r.getRequestType() == RequestType.BORROW ? r.getEquipmentUnit().getManagementCode() : null;
            
            return new RequestViewDto(
                r.getId(), 
                r.getRequestType().name(), 
                itemName, 
                mgmtCode,
                r.getQuantity(), 
                r.getStatus().name(), 
                r.getRequestedAt(), 
                r.getDueDate(), 
                r.getReturnedAt(),
                r.getEmployee().getDisplayName()
            );
        });
        
        model.addAttribute("historyPage", dtoPage);
        return "admin_history";
    }
}