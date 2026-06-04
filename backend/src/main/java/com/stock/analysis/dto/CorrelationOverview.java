package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationOverview {

    private String stockCodeA;
    private String stockNameA;
    private String stockCodeB;
    private String stockNameB;
    private LocalDate statStartDate;
    private LocalDate statEndDate;
    private String dataType;
    private Double pearsonCoefficient;
    private Double spearmanCoefficient;
    private String correlationDescription;
}
