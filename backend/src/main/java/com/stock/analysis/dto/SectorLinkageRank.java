package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectorLinkageRank {

    private Integer rank;
    private String sectorCode;
    private String sectorName;
    private Double avgLinkageCoefficient;
    private Double volumeLinkage;
    private Double volatilityLinkage;
    private Integer stockCount;
}
