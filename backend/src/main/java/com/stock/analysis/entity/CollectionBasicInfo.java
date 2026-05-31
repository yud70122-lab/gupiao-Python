package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collection_basic_info")
@NoArgsConstructor
@AllArgsConstructor
public class CollectionBasicInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String exchange;

    @Column(length = 50)
    private String industry;

    @Column(length = 500)
    private String concept;

    @Column
    private Double totalShares;

    @Column
    private Double floatShares;

    @Column(length = 20)
    private String listDate;

    @Column
    private LocalDateTime updateTime;
}
