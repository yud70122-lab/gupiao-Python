package com.stock.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnAnalysis {
    private LocalDate date;
    private String code;
    private String name;
    private Double dailyReturn;
    private Double cumulativeReturn;
}
