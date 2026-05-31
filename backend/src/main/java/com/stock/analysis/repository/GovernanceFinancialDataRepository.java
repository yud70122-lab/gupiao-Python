package com.stock.analysis.repository;

import com.stock.analysis.entity.GovernanceFinancialData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GovernanceFinancialDataRepository extends JpaRepository<GovernanceFinancialData, Long> {

    List<GovernanceFinancialData> findByCodeAndReportTypeOrderByPeriodDesc(String code, String reportType);

    List<GovernanceFinancialData> findByCodeAndReportTypeAndDataStatusOrderByPeriodDesc(String code, String reportType, String dataStatus);
}
