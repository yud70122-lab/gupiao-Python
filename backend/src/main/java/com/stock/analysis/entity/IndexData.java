package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "index_data")
@NoArgsConstructor
@AllArgsConstructor
public class IndexData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String indexCode;

    @Column(nullable = false, length = 100)
    private String indexName;

    @Column(nullable = false)
    private LocalDate tradeDate;

    @Column(nullable = false)
    private Double openPrice;

    @Column(nullable = false)
    private Double highPrice;

    @Column(nullable = false)
    private Double lowPrice;

    @Column(nullable = false)
    private Double closePrice;

    @Column
    private Double changePercent;

    @Column
    private Long volume;

    @Column
    private Double turnover;

    @Column
    private Double dailyReturn;
}
