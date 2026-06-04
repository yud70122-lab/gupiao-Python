package com.stock.analysis.repository;

import com.stock.analysis.entity.QuantCalculationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuantCalculationResultRepository extends JpaRepository<QuantCalculationResult, Long> {

    List<QuantCalculationResult> findByCalculationType(String calculationType);

    List<QuantCalculationResult> findByStockCodeAAndStockCodeBAndCalculationType(
            String stockCodeA, String stockCodeB, String calculationType);

    List<QuantCalculationResult> findByCalculationDateBetween(LocalDate startDate, LocalDate endDate);

    List<QuantCalculationResult> findByCalculationTypeAndStatStartDateAndStatEndDate(
            String calculationType, LocalDate startDate, LocalDate endDate);
}
