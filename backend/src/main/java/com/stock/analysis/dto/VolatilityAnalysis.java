package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolatilityAnalysis {
    private String code;
    private String name;
    private Double dailyVolatility;
    private Double annualizedVolatility;
    private Double maxDrawdown;
    private Double sharpeRatio;
    private Double averageReturn;
}
