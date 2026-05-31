package com.stock.analysis.repository;

import com.stock.analysis.entity.CollectionBasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionBasicInfoRepository extends JpaRepository<CollectionBasicInfo, Long> {

    List<CollectionBasicInfo> findByCodeContainingOrNameContaining(String code, String name);

    List<CollectionBasicInfo> findByExchange(String exchange);

    List<CollectionBasicInfo> findByIndustry(String industry);

    @Query("SELECT DISTINCT c.industry FROM CollectionBasicInfo c WHERE c.industry IS NOT NULL ORDER BY c.industry")
    List<String> findAllDistinctIndustries();
}
