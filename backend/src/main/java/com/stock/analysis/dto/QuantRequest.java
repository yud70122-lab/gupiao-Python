package com.stock.analysis.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class QuantRequest {

    private List<String> stockCodes;

    private String compareBenchmark;

    private LocalDate startDate;

    private LocalDate endDate;

    private String dataType;

    private String coefficientType;

    private Integer rollingWindow;

    private String calculationMode;

    private String sectorCode;
}
