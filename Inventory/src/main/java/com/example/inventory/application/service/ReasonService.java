// src/main/java/com/example/inventory/application/service/ReasonService.java
package com.example.inventory.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.application.dto.ReasonDto;
import com.example.inventory.domain.model.RejectReason;
import com.example.inventory.domain.model.RequestReason;
import com.example.inventory.domain.repository.RejectReasonRepository;
import com.example.inventory.domain.repository.RequestReasonRepository;

import lombok.RequiredArgsConstructor;

/**
 * 申請事由および却下事由マスタに関する業務ロジックを管理するサービスクラス。
 */
@Service
@RequiredArgsConstructor
public class ReasonService {
    private final RequestReasonRepository requestReasonRepo;
    private final RejectReasonRepository rejectReasonRepo;

    /**
     * 登録済みの全申請事由を取得し、DTOに変換して返します。
     *
     * @return 申請事由のDTOリスト
     */
    @Transactional(readOnly = true)
    public List<ReasonDto> getAllRequestReasons() {
        return requestReasonRepo.findAll().stream()
                .map(r -> new ReasonDto(r.getId(), r.getReasonText())).toList();
    }

    /**
     * 登録済みの全却下事由を取得し、DTOに変換して返します。
     *
     * @return 却下事由のDTOリスト
     */
    @Transactional(readOnly = true)
    public List<ReasonDto> getAllRejectReasons() {
        return rejectReasonRepo.findAll().stream()
                .map(r -> new ReasonDto(r.getId(), r.getReasonText())).toList();
    }

    /**
     * 新しい申請事由を登録します。
     *
     * @param text 登録する申請事由のテキスト
     */
    @Transactional
    public void saveRequestReason(String text) {
        RequestReason r = new RequestReason();
        r.setReasonText(text);
        requestReasonRepo.save(r);
    }

    /**
     * 新しい却下事由を登録します。
     *
     * @param text 登録する却下事由のテキスト
     */
    @Transactional
    public void saveRejectReason(String text) {
        RejectReason r = new RejectReason();
        r.setReasonText(text);
        rejectReasonRepo.save(r);
    }

    /**
     * 指定されたIDの申請事由を削除します。
     *
     * @param id 削除対象の申請事由ID
     */
    @Transactional
    public void deleteRequestReason(Long id) {
        requestReasonRepo.deleteById(id);
    }

    /**
     * 指定されたIDの却下事由を削除します。
     *
     * @param id 削除対象の却下事由ID
     */
    @Transactional
    public void deleteRejectReason(Long id) {
        rejectReasonRepo.deleteById(id);
    }
}