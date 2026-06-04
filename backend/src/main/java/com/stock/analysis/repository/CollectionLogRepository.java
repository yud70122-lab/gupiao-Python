package com.stock.analysis.repository;

import com.stock.analysis.entity.CollectionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionLogRepository extends JpaRepository<CollectionLog, Long> {
    Page<CollectionLog> findByStatus(String status, Pageable pageable);
    Page<CollectionLog> findByDataType(String dataType, Pageable pageable);
    Page<CollectionLog> findByStatusAndDataType(String status, String dataType, Pageable pageable);
    List<CollectionLog> findByStatus(String status);
    long countByStatus(String status);
}
