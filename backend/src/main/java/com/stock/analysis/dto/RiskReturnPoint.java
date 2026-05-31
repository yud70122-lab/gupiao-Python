package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskReturnPoint {
    private String code;
    private String name;
    private Double risk;
    private Double returnRate;
}
