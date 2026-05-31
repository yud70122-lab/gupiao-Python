package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "governance_market_overview")
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceMarketOverview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String dataType;

    @Column
    private LocalDate tradeDate;

    @Column(length = 20)
    private String indexCode;

    @Column(length = 50)
    private String indexName;

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
    private Double volume;

    @Column(length = 20)
    private String dataStatus;

    @Column
    private Double shNetBuy;

    @Column
    private Double szNetBuy;

    @Column
    private Double totalNetBuy;

    @Column
    private Double shBuy;

    @Column
    private Double shSell;

    @Column
    private Double szBuy;

    @Column
    private Double szSell;
}
