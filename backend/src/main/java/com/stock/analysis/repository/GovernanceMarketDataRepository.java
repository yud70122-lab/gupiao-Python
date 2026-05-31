package com.stock.analysis.repository;

import com.stock.analysis.entity.GovernanceMarketData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GovernanceMarketDataRepository extends JpaRepository<GovernanceMarketData, Long> {

    List<GovernanceMarketData> findByCodeOrderByTradeDateDesc(String code);

    List<GovernanceMarketData> findByCodeAndDataStatusOrderByTradeDateDesc(String code, String dataStatus);

    List<GovernanceMarketData> findByCodeAndQualityLevelOrderByTradeDateDesc(String code, String qualityLevel);

    List<GovernanceMarketData> findByCodeAndDataStatusAndQualityLevelOrderByTradeDateDesc(String code, String dataStatus, String qualityLevel);
}
