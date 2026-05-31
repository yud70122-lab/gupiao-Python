package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "collection_market_data")
@NoArgsConstructor
@AllArgsConstructor
public class CollectionMarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate tradeDate;

    @Column
    private String period;

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

    @Column
    private Double amount;

    @Column
    private Double turnover;
}
