package com.stock.analysis.repository;

import com.stock.analysis.entity.CollectionFinancialData;
import com.stock.analysis.dto.StockBasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionFinancialDataRepository extends JpaRepository<CollectionFinancialData, Long> {

    List<CollectionFinancialData> findByCodeAndReportTypeOrderByReportPeriodDesc(String code, String reportType);

    List<CollectionFinancialData> findByCodeAndReportTypeAndReportPeriodContainingOrderByReportPeriodDesc(String code, String reportType, String periodType);

    @Query("SELECT DISTINCT new com.stock.analysis.dto.StockBasicInfo(c.code, c.name) FROM CollectionFinancialData c ORDER BY c.code")
    List<StockBasicInfo> findAllDistinctStocks();
}
