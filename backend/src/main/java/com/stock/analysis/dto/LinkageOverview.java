package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkageOverview {

    private String indicatorName;
    private String sectorNameA;
    private String sectorNameB;
    private Double linkageCoefficient;
    private String linkageDescription;
}
