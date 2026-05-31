package com.stock.analysis.repository;

import com.stock.analysis.entity.GovernanceMarketOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GovernanceMarketOverviewRepository extends JpaRepository<GovernanceMarketOverview, Long> {

    List<GovernanceMarketOverview> findByDataTypeOrderByTradeDateDesc(String dataType);

    List<GovernanceMarketOverview> findByDataTypeAndIndexCodeOrderByTradeDateDesc(String dataType, String indexCode);

    List<GovernanceMarketOverview> findByDataTypeAndIndexCodeAndDataStatusOrderByTradeDateDesc(String dataType, String indexCode, String dataStatus);

    List<GovernanceMarketOverview> findByDataTypeAndTradeDateBetweenOrderByTradeDateAsc(String dataType, LocalDate startDate, LocalDate endDate);
}
