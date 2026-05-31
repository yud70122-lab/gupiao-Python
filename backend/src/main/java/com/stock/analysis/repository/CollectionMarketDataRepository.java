package com.stock.analysis.repository;

import com.stock.analysis.entity.CollectionMarketData;
import com.stock.analysis.dto.StockBasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CollectionMarketDataRepository extends JpaRepository<CollectionMarketData, Long> {

    List<CollectionMarketData> findByCodeAndTradeDateBetweenOrderByTradeDateAsc(String code, LocalDate startDate, LocalDate endDate);

    List<CollectionMarketData> findByCodeAndPeriodAndTradeDateBetweenOrderByTradeDateAsc(String code, String period, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT new com.stock.analysis.dto.StockBasicInfo(c.code, c.name) FROM CollectionMarketData c ORDER BY c.code")
    List<StockBasicInfo> findAllDistinctStocks();
}
