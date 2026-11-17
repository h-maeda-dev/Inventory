// src/main/java/com/example/inventory/domain/repository/ItemRepository.java
package com.example.inventory.domain.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.inventory.domain.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
	// ページネーション対応の検索クエリ
    @Query("SELECT i FROM Item i WHERE :keyword IS NULL OR i.name LIKE %:keyword% OR i.category LIKE %:keyword%")
    Page<Item> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}