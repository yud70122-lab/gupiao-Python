package com.stock.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "favorite_stock", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userId", "code"})
})
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(length = 100)
    private String name;

    private Double price;

    private Double changePercent;

    @Column(nullable = false)
    private Long groupId;

    @Column(updatable = false)
    private LocalDateTime addTime;

    @PrePersist
    protected void onCreate() {
        addTime = LocalDateTime.now();
    }
}
