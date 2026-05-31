package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "governance_basic_info")
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceBasicInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(length = 10)
    private String exchange;

    @Column(length = 50)
    private String industry;

    @Column
    private Double totalShares;

    @Column(length = 20)
    private String dataStatus;

    @Column(length = 5)
    private String qualityLevel;

    @Column
    private Integer completeness;

    @Column
    private Integer accuracy;

    @Column
    private LocalDateTime updateTime;
}
