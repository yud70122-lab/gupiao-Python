package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationAnalysisResponse {

    private List<CorrelationOverview> overviews;
    private List<CorrelationMatrixCell> correlationMatrix;
    private List<String> matrixStockCodes;
    private List<String> matrixStockNames;
    private List<RollingCorrelationPoint> rollingCorrelation;
    private List<CorrelationDetailRow> detailRows;
}
