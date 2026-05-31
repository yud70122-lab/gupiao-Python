package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "governance_market_data")
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceMarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate tradeDate;

    @Column(length = 20)
    private String code;

    @Column(length = 100)
    private String name;

    @Column
    private Double openPrice;

    @Column
    private Double highPrice;

    @Column
    private Double lowPrice;

    @Column
    private Double closePrice;

    @Column
    private Double changePercent;

    @Column
    private Long volume;

    @Column(length = 20)
    private String dataStatus;

    @Column(length = 5)
    private String qualityLevel;

    @Column(length = 50)
    private String abnormalType;
}
