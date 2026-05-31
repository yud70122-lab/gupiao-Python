package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "governance_financial_data")
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceFinancialData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String period;

    @Column(length = 20)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(length = 20)
    private String reportType;

    @Column
    private Long item1;

    @Column
    private Long item2;

    @Column
    private Long item3;

    @Column
    private Long item4;

    @Column
    private Long item5;

    @Column(length = 20)
    private String dataStatus;

    @Column(length = 20)
    private String verifyStatus;

    @Column
    private LocalDateTime updateTime;
}
