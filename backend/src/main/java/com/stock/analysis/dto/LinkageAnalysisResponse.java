package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkageAnalysisResponse {

    private List<LinkageOverview> overviews;
    private List<SectorLinkageRank> sectorRankings;
    private List<CorrelationMatrixCell> sectorLinkageMatrix;
    private List<String> matrixSectorCodes;
    private List<String> matrixSectorNames;
    private List<RollingCorrelationPoint> rollingLinkage;
}
