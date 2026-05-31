package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collection_financial_data")
@NoArgsConstructor
@AllArgsConstructor
public class CollectionFinancialData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(length = 20)
    private String reportPeriod;

    @Column(length = 20)
    private String reportType;

    @Column
    private Long totalRevenue;

    @Column
    private Long operatingCost;

    @Column
    private Long operatingProfit;

    @Column
    private Long totalProfit;

    @Column
    private Long netProfit;

    @Column
    private Long netProfitParent;

    @Column
    private Double eps;

    @Column
    private Long totalAssets;

    @Column
    private Long totalLiabilities;

    @Column
    private Long totalEquity;

    @Column
    private Long currentAssets;

    @Column
    private Long currentLiabilities;

    @Column
    private Double debtRatio;

    @Column
    private Long operatingCashFlow;

    @Column
    private Long investingCashFlow;

    @Column
    private Long financingCashFlow;

    @Column
    private Long netCashFlow;

    @Column
    private Double pe;

    @Column
    private Double pb;

    @Column
    private Double roe;

    @Column
    private Double grossMargin;

    @Column
    private LocalDateTime updateTime;
}
