package com.stock.analysis.repository;

import com.stock.analysis.entity.GovernanceBasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GovernanceBasicInfoRepository extends JpaRepository<GovernanceBasicInfo, Long> {

    List<GovernanceBasicInfo> findByCodeContainingOrNameContaining(String code, String name);

    List<GovernanceBasicInfo> findByDataStatus(String dataStatus);

    List<GovernanceBasicInfo> findByQualityLevel(String qualityLevel);

    List<GovernanceBasicInfo> findByCodeContainingAndDataStatusAndQualityLevel(String code, String dataStatus, String qualityLevel);
}
