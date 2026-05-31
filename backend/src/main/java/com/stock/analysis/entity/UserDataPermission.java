package com.stock.analysis.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user_data_permission")
public class UserDataPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 500)
    private String allowedStockCodes;

    @Column(length = 500)
    private String allowedSectors;

    @Column(length = 20)
    private String scope;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAllowedStockCodes() { return allowedStockCodes; }
    public void setAllowedStockCodes(String allowedStockCodes) { this.allowedStockCodes = allowedStockCodes; }
    public String getAllowedSectors() { return allowedSectors; }
    public void setAllowedSectors(String allowedSectors) { this.allowedSectors = allowedSectors; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}