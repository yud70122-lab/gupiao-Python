package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationDetailRow {

    private Integer serialNumber;
    private String stockCodeA;
    private String stockNameA;
    private String stockCodeB;
    private String stockNameB;
    private LocalDate statDate;
    private Integer rollingWindow;
    private Double pearsonCoefficient;
    private Double spearmanCoefficient;
    private String dataType;
    private String correlationLevel;
    private Integer sampleCount;
    private LocalDateTime calculationTime;
}
