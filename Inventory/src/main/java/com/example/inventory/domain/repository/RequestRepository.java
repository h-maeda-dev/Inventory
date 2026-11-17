// src/main/java/com/example/inventory/domain/repository/RequestRepository.java
package com.example.inventory.domain.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventory.domain.model.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
	// ページネーション対応の全件取得
    Page<Request> findAll(Pageable pageable);
}