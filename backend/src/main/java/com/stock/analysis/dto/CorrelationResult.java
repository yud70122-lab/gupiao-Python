package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationResult {
    private String stockCode1;
    private String stockName1;
    private String stockCode2;
    private String stockName2;
    private Double correlation;
}
