package com.stock.analysis.repository;

import com.stock.analysis.entity.SectorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectorInfoRepository extends JpaRepository<SectorInfo, Long> {

    Optional<SectorInfo> findBySectorCode(String sectorCode);

    List<SectorInfo> findBySectorType(String sectorType);

    List<SectorInfo> findBySectorNameContaining(String keyword);
}
