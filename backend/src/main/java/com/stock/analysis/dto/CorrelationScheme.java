package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationScheme {

    private Long id;
    private String schemeName;
    private List<String> stockCodes;
    private LocalDate startDate;
    private LocalDate endDate;
    private String adjustmentType;
    private String calculationType;
    private String coefficientType;
    private String calculationMode;
    private Integer rollingWindow;
    private Integer rollingStep;
    private Boolean outlierFilter;
    private Boolean resultCache;
    private String createTime;
}
