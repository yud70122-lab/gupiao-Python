package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "quant_calculation_result")
@NoArgsConstructor
@AllArgsConstructor
public class QuantCalculationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calc_type", nullable = false, length = 50)
    private String calculationType;

    @Column(name = "stock_code_a", nullable = false, length = 20)
    private String stockCodeA;

    @Column(name = "stock_name_a", nullable = false, length = 100)
    private String stockNameA;

    @Column(name = "stock_code_b", nullable = false, length = 20)
    private String stockCodeB;

    @Column(name = "stock_name_b", nullable = false, length = 100)
    private String stockNameB;

    @Column(name = "stat_start")
    private LocalDate statStartDate;

    @Column(name = "stat_end")
    private LocalDate statEndDate;

    @Column(name = "data_type", length = 20)
    private String dataType;

    @Column(name = "coeff_type", length = 20)
    private String coefficientType;

    @Column(name = "pearson_coeff")
    private Double pearsonCoefficient;

    @Column(name = "spearman_coeff")
    private Double spearmanCoefficient;

    @Column(name = "roll_window")
    private Integer rollingWindow;

    @Column(name = "calc_date")
    private LocalDate calculationDate;

    @Column(name = "corr_desc", length = 100)
    private String correlationDescription;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
