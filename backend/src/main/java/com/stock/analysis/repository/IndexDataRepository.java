package com.stock.analysis.repository;

import com.stock.analysis.entity.IndexData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

    List<IndexData> findByIndexCodeAndTradeDateBetweenOrderByTradeDateAsc(String indexCode, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT i.indexCode, i.indexName FROM IndexData i ORDER BY i.indexCode")
    List<Object[]> findAllDistinctIndices();

    List<IndexData> findByTradeDateBetweenOrderByTradeDateAsc(LocalDate startDate, LocalDate endDate);
}
