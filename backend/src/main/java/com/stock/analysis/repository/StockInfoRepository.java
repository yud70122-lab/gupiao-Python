package com.stock.analysis.repository;

import com.stock.analysis.entity.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockInfoRepository extends JpaRepository<StockInfo, Long> {

    List<StockInfo> findByCodeOrderByTradeDateAsc(String code);

    List<StockInfo> findByCodeAndTradeDateBetweenOrderByTradeDateAsc(
            String code, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT s.code FROM StockInfo s WHERE s.code LIKE %:keyword% OR s.name LIKE %:keyword%")
    List<String> findDistinctCodesByKeyword(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT s.code, s.name FROM StockInfo s")
    List<Object[]> findAllDistinctStocks();

    @Query("SELECT DISTINCT s.name FROM StockInfo s WHERE s.code = :code")
    Optional<String> findNameByCode(@Param("code") String code);

    @Query("SELECT DISTINCT s.code, s.name FROM StockInfo s WHERE s.code LIKE %:keyword% OR s.name LIKE %:keyword%")
    List<Object[]> searchStocks(@Param("keyword") String keyword);
}
