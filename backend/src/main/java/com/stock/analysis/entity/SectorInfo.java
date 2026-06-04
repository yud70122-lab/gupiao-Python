package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "sector_info")
@NoArgsConstructor
@AllArgsConstructor
public class SectorInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String sectorCode;

    @Column(nullable = false, length = 100)
    private String sectorName;

    @Column(length = 50)
    private String sectorType;

    @Column(length = 500)
    private String stockCodes;

    @Column
    private Integer stockCount;
}
