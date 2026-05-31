package com.stock.analysis.repository;

import com.stock.analysis.entity.CollectionMarketOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CollectionMarketOverviewRepository extends JpaRepository<CollectionMarketOverview, Long> {

    List<CollectionMarketOverview> findByDataType(String dataType);

    List<CollectionMarketOverview> findByDataTypeAndTradeDateBetweenOrderByTradeDateAsc(String dataType, LocalDate startDate, LocalDate endDate);

    List<CollectionMarketOverview> findByDataTypeAndIndexCodeOrderByTradeDateDesc(String dataType, String indexCode);

    @Query("SELECT c FROM CollectionMarketOverview c WHERE c.dataType = :dataType AND c.tradeDate = (SELECT MAX(c2.tradeDate) FROM CollectionMarketOverview c2 WHERE c2.dataType = :dataType)")
    CollectionMarketOverview findLatestByDataType(@Param("dataType") String dataType);
}
