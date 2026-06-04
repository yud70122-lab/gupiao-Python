package com.stock.analysis.repository;

import com.stock.analysis.entity.StockGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockGroupRepository extends JpaRepository<StockGroup, Long> {

    List<StockGroup> findByUserIdOrderByCreateTimeAsc(Long userId);

    Optional<StockGroup> findByUserIdAndName(Long userId, String name);

    Optional<StockGroup> findByUserIdAndIsDefaultTrue(Long userId);

    boolean existsByUserIdAndName(Long userId, String name);
}
