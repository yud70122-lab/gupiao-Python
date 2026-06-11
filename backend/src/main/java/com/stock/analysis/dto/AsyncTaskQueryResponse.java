package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskQueryResponse {

    private String taskId;
    private String status;
    private Integer progress;
    private String message;
    private CorrelationAnalysisResponse result;
}
