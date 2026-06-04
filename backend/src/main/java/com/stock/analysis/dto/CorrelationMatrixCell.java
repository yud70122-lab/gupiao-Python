package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationMatrixCell {

    private String stockCodeX;
    private String stockNameX;
    private String stockCodeY;
    private String stockNameY;
    private Double pearsonCoefficient;
    private Double spearmanCoefficient;
}
