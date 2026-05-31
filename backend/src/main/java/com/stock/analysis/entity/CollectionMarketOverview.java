package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collection_market_overview")
@NoArgsConstructor
@AllArgsConstructor
public class CollectionMarketOverview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String dataType;

    @Column(length = 20)
    private String indexCode;

    @Column(length = 50)
    private String indexName;

    @Column
    private LocalDate tradeDate;

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

    @Column
    private Double amount;

    @Column
    private Integer upCount;

    @Column
    private Integer downCount;

    @Column
    private Integer flatCount;

    @Column
    private Double upPercent;

    @Column
    private Double downPercent;

    @Column
    private Double flatPercent;

    @Column
    private Double northFlow;

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

    @Column(length = 20)
    private String stockCode;

    @Column(length = 50)
    private String stockName;

    @Column
    private Double stockPrice;

    @Column
    private Double stockChange;

    @Column
    private Double stockTurnover;

    @Column
    private Double volumeRatio;

    @Column
    private Long stockAmount;

    @Column
    private LocalDateTime updateTime;
}
