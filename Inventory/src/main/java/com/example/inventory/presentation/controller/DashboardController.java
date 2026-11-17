// src/main/java/com/example/inventory/presentation/controller/DashboardController.java
package com.example.inventory.presentation.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.inventory.application.dto.RequestViewDto;
import com.example.inventory.application.service.RequestService;
import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.RequestType;
import com.example.inventory.domain.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

/**
 * ログイン直後に表示されるダッシュボード（ホーム画面）を制御するコントローラー。
 * 自身の貸出中の備品一覧や、最近の申請履歴を取得して画面へ渡します。
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final RequestService requestService;
    private final EmployeeRepository employeeRepository;

    /**
     * ダッシュボード画面を表示します。
     *
     * @param model ビューへ渡すデータモデル
     * @param principal 現在ログインしているユーザーの認証情報
     * @return ダッシュボード画面のテンプレート名
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            // 現在ログイン中の社員IDを取得し、データベースから情報を取得する
            Long empId = Long.valueOf(principal.getName());
            Employee employee = employeeRepository.findById(empId).orElse(null);
            
            if (employee != null) {
                // 1. 貸出中の備品一覧を取得し、画面表示用のDTOへ詰め替える
                List<RequestViewDto> borrowing = requestService.getMyBorrowingRequests(employee).stream()
                    .map(r -> new RequestViewDto(
                        r.getId(), 
                        r.getRequestType().name(),
                        r.getEquipmentUnit().getItem().getName(),
                        r.getEquipmentUnit().getManagementCode(),
                        r.getQuantity(), 
                        r.getStatus().name(), 
                        r.getRequestedAt(), 
                        r.getDueDate(), 
                        r.getReturnedAt(),
                        employee.getDisplayName()
                    )).toList();
                
                // 2. 自身のすべての申請履歴を取得し、画面表示用のDTOへ詰め替える
                // ※備品（BORROW）と消耗品（CONSUME）で取得元が異なる項目（品名や管理番号）を条件分岐でセットする
                List<RequestViewDto> myRequests = requestService.getMyRequests(employee).stream()
                    .map(r -> new RequestViewDto(
                        r.getId(), 
                        r.getRequestType().name(),
                        r.getRequestType() == RequestType.BORROW ? r.getEquipmentUnit().getItem().getName() : r.getItem().getName(),
                        r.getRequestType() == RequestType.BORROW ? r.getEquipmentUnit().getManagementCode() : null,
                        r.getQuantity(), 
                        r.getStatus().name(), 
                        r.getRequestedAt(), 
                        r.getDueDate(), 
                        r.getReturnedAt(),
                        employee.getDisplayName()
                    )).toList();

                // 詰め替えたDTOのリストをModelへ登録して画面へ渡す
                model.addAttribute("borrowingRequests", borrowing);
                model.addAttribute("myRequests", myRequests);
            }
        }
        return "dashboard";
    }

    /**
     * ルートURL（/）へのアクセスをダッシュボード画面へリダイレクトします。
     *
     * @return ダッシュボードへのリダイレクト指示
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}