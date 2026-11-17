// src/main/java/com/example/inventory/application/service/RequestService.java
package com.example.inventory.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.domain.model.Employee;
import com.example.inventory.domain.model.EquipmentStatus;
import com.example.inventory.domain.model.EquipmentUnit;
import com.example.inventory.domain.model.Item;
import com.example.inventory.domain.model.ItemType;
import com.example.inventory.domain.model.PeriodUnit;
import com.example.inventory.domain.model.RejectReason;
import com.example.inventory.domain.model.Request;
import com.example.inventory.domain.model.RequestReason;
import com.example.inventory.domain.model.RequestStatus;
import com.example.inventory.domain.model.RequestType;
import com.example.inventory.domain.repository.EquipmentUnitRepository;
import com.example.inventory.domain.repository.ItemRepository;
import com.example.inventory.domain.repository.RejectReasonRepository;
import com.example.inventory.domain.repository.RequestReasonRepository;
import com.example.inventory.domain.repository.RequestRepository;
import com.example.inventory.presentation.form.RequestForm;

import lombok.RequiredArgsConstructor;

/**
 * 物品の貸出・支給申請、承認、却下、返却に関する業務ロジックを管理するサービスクラス。
 * トランザクション境界（@Transactional）として機能し、在庫や個体の状態と申請状態の整合性を保証します。
 */
@Service
@RequiredArgsConstructor
public class RequestService {
    
    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final EquipmentUnitRepository equipmentUnitRepository;
    private final RequestReasonRepository requestReasonRepository;
    private final RejectReasonRepository rejectReasonRepository;
    private final NotificationService notificationService;
   
    /**
     * 指定された社員のすべての申請履歴を取得し、申請日時の降順（新しい順）で返します。
     * 
     * @param employee 対象の社員
     * @return 申請履歴のリスト
     */
    @Transactional(readOnly = true)
    public List<Request> getMyRequests(Employee employee) {
        return requestRepository.findAll().stream()
                .filter(req -> req.getEmployee().getId().equals(employee.getId()))
                .sorted((a, b) -> b.getRequestedAt().compareTo(a.getRequestedAt())).toList();
    }

    /**
     * 指定された社員が現在「貸出中（承認済みの備品）」の申請一覧を取得します。
     * 
     * @param employee 対象の社員
     * @return 貸出中の申請リスト
     */
    @Transactional(readOnly = true)
    public List<Request> getMyBorrowingRequests(Employee employee) {
        return getMyRequests(employee).stream()
                .filter(req -> req.getStatus() == RequestStatus.APPROVED && req.getRequestType() == RequestType.BORROW).toList();
    }

    /**
     * 全社員からの「承認待ち（PENDING）」状態の申請一覧を取得します。
     * 
     * @return 承認待ち申請のリスト
     */
    @Transactional(readOnly = true)
    public List<Request> getPendingRequests() {
        return requestRepository.findAll().stream()
                .filter(req -> req.getStatus() == RequestStatus.PENDING).toList();
    }

    /**
     * すべての申請履歴（貸出・消費、すべてのステータス）をページネーション付きで取得します。
     * 
     * @param pageable ページネーション情報
     * @return 申請履歴のPageオブジェクト
     */
    @Transactional(readOnly = true)
    public Page<Request> getAllHistory(Pageable pageable) {
        return requestRepository.findAll(pageable);
    }
    
    /**
     * IDを指定して申請情報を取得します。
     * 
     * @param id 申請ID
     * @return 該当する申請エンティティ
     * @throws IllegalArgumentException 申請が見つからない場合
     */
    @Transactional(readOnly = true)
    public Request getRequest(Long id) {
        return requestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("申請が見つかりません"));
    }

    /**
     * 新しい申請（備品の貸出、または消耗品の支給）を作成します。
     * 申請と同時に、対象の備品個体の仮押さえ、または消耗品の在庫減算（仮引き当て）を行います。
     * 
     * @param form 画面から送信された申請フォームデータ
     * @param employee 申請を行う社員
     */
    @Transactional
    public void createRequest(RequestForm form, Employee employee) {
        Item item = itemRepository.findById(form.getItemId()).orElseThrow(() -> new IllegalArgumentException("品目が見つかりません"));
        
        Request request = new Request();
        request.setEmployee(employee);
        request.setRequestedAt(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING); // 初期ステータスは承認待ち
        request.setRequestNote(form.getRequestNote());
        
        RequestReason reason = requestReasonRepository.findById(form.getRequestReasonId())
                .orElseThrow(() -> new IllegalArgumentException("申請事由が見つかりません"));
        request.setRequestReason(reason);

        // 消耗品の場合の処理
        if (item.getItemType() == ItemType.CONSUMABLE) {
            // 他のユーザーによる在庫の同時更新を防ぐための排他制御（楽観的ロック）
            if (!item.getVersion().equals(form.getVersion())) {
                throw new ObjectOptimisticLockingFailureException(Item.class, item.getId());
            }
            if (item.getStockQuantity() < form.getQuantity()) {
                throw new IllegalArgumentException("在庫が不足しています");
            }
            // 在庫を減算して保存（本引当前に一旦減らす）
            item.setStockQuantity(item.getStockQuantity() - form.getQuantity());
            itemRepository.save(item);
            
            request.setRequestType(RequestType.CONSUME);
            request.setItem(item);
            request.setQuantity(form.getQuantity());
        } 
        // 備品の場合の処理
        else {
            EquipmentUnit unit = equipmentUnitRepository.findById(form.getEquipmentUnitId())
                    .orElseThrow(() -> new IllegalArgumentException("個体が見つかりません"));
            
            if (unit.getStatus() != EquipmentStatus.AVAILABLE) {
                throw new IllegalArgumentException("選択された個体は現在貸出不可です");
            }
            // 個体を「仮押さえ中」に変更
            unit.setStatus(EquipmentStatus.RESERVED);
            equipmentUnitRepository.save(unit);

            request.setRequestType(RequestType.BORROW);
            request.setEquipmentUnit(unit);
            
            // 品目に設定されたレンタル期限（週・月・年）から返却予定日を自動計算
            LocalDateTime dueDate = LocalDateTime.now();
            if (item.getRentalPeriodValue() != null && item.getRentalPeriodUnit() != null) {
                if (item.getRentalPeriodUnit() == PeriodUnit.WEEK) {
                    dueDate = dueDate.plusWeeks(item.getRentalPeriodValue());
                } else if (item.getRentalPeriodUnit() == PeriodUnit.MONTH) {
                    dueDate = dueDate.plusMonths(item.getRentalPeriodValue()); 
                } else if (item.getRentalPeriodUnit() == PeriodUnit.YEAR) {
                    dueDate = dueDate.plusYears(item.getRentalPeriodValue());
                }
            } else {
                dueDate = dueDate.plusWeeks(1); // 設定漏れの場合のフォールバック
            }
            request.setDueDate(dueDate);
        }
        
        requestRepository.save(request);
        // 管理者へ新規申請があった旨をメール通知
        notificationService.sendRequestNotification(request);
    }

    /**
     * 社員自身が「承認待ち」の申請をキャンセル（取り下げ）します。
     * キャンセルに伴い、仮押さえしていた個体や在庫を元に戻します。
     * 
     * @param requestId キャンセル対象の申請ID
     * @param employee キャンセルを実行する社員（本人確認用）
     */
    @Transactional
    public void cancelRequest(Long requestId, Employee employee) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        
        // 本人の申請であること、かつ承認待ちであることを確認
        if (!request.getEmployee().getId().equals(employee.getId()) || request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("キャンセルできない申請です");
        }
        
        // 仮押さえ状態の解除
        revertAllocation(request);
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
    }

    /**
     * 管理者が申請を承認します。
     * 備品の場合は貸出中にステータスを変更し、在庫（利用可能数）が安全基準を下回った場合はアラートを送信します。
     * 
     * @param requestId 承認対象の申請ID
     */
    @Transactional
    public void approveRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("未承認の申請ではありません");
        }
        
        request.setStatus(RequestStatus.APPROVED);
        
        if (request.getRequestType() == RequestType.BORROW) {
            EquipmentUnit unit = request.getEquipmentUnit();
            unit.setStatus(EquipmentStatus.IN_USE); // 貸出中に変更
            equipmentUnitRepository.save(unit);
            
            // 備品の利用可能個体数を算出し、安全在庫基準を下回ったら通知
            Item item = unit.getItem();
            long availableCount = item.getEquipmentUnits().stream()
                    .filter(u -> u.getStatus() == EquipmentStatus.AVAILABLE)
                    .count();
            if (availableCount < item.getSafeStockCount()) {
                notificationService.sendLowStockAlert(item.getName(), (int) availableCount);
            }
        } else {
            // 消耗品の在庫数をチェックし、安全在庫基準を下回ったら通知
            Item item = request.getItem();
            if (item.getStockQuantity() < item.getSafeStockCount()) {
                notificationService.sendLowStockAlert(item.getName(), item.getStockQuantity());
            }
        }
        
        requestRepository.save(request);
        // 申請者へ承認結果を通知
        notificationService.sendApprovalResultNotification(request);
    }

    /**
     * 管理者が申請を却下します。
     * 却下に伴い、仮押さえしていた個体や在庫を元に戻します。
     * 
     * @param requestId 却下対象の申請ID
     * @param rejectReasonId 選択された却下事由のID
     * @param rejectNote 却下に関する補足理由
     */
    @Transactional
    public void rejectRequest(Long requestId, Long rejectReasonId, String rejectNote) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("未承認の申請ではありません");
        }
        
        RejectReason reason = rejectReasonRepository.findById(rejectReasonId)
                .orElseThrow(() -> new IllegalArgumentException("却下事由が見つかりません"));
        
        request.setRejectReason(reason);
        request.setRejectNote(rejectNote);
        
        // 仮押さえ状態の解除
        revertAllocation(request);
        request.setStatus(RequestStatus.REJECTED);
        
        requestRepository.save(request);
        // 申請者へ却下結果を通知
        notificationService.sendApprovalResultNotification(request);
    }

    /**
     * 貸出中の備品を返却します。
     * 申請ステータスを返却済みにし、対象の個体を再び利用可能（AVAILABLE）に戻します。
     * 
     * @param requestId 返却対象の申請ID
     * @param employee 返却操作を行う社員（本人確認用）
     */
    @Transactional
    public void returnEquipment(Long requestId, Employee employee) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        
        // 本人の申請であり、かつ承認済みの備品貸出であることを確認
        if (!request.getEmployee().getId().equals(employee.getId()) || 
            request.getStatus() != RequestStatus.APPROVED || 
            request.getRequestType() != RequestType.BORROW) {
            throw new IllegalArgumentException("返却できない申請です");
        }
        
        // 個体を利用可能状態に戻す
        EquipmentUnit unit = request.getEquipmentUnit();
        unit.setStatus(EquipmentStatus.AVAILABLE);
        equipmentUnitRepository.save(unit);
        
        // 申請情報のステータスと返却日時を記録
        request.setStatus(RequestStatus.RETURNED);
        request.setReturnedAt(LocalDateTime.now());
        requestRepository.save(request);
    }

    /**
     * 申請のキャンセルまたは却下時に、仮引当・仮押さえしていた在庫や個体を元の状態に戻す内部メソッド。
     * 
     * @param request 処理を元に戻す対象の申請
     */
    private void revertAllocation(Request request) {
        if (request.getRequestType() == RequestType.CONSUME) {
            Item item = request.getItem();
            // 減算していた在庫数を足し戻す
            item.setStockQuantity(item.getStockQuantity() + request.getQuantity());
            itemRepository.save(item);
        } else if (request.getRequestType() == RequestType.BORROW) {
            EquipmentUnit unit = request.getEquipmentUnit();
            // 仮押さえ（RESERVED）から利用可能（AVAILABLE）に戻す
            unit.setStatus(EquipmentStatus.AVAILABLE);
            equipmentUnitRepository.save(unit);
        }
    }
}