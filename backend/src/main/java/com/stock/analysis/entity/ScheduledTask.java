package com.stock.analysis.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_scheduled_task")
public class ScheduledTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 100)
    private String name;
    
    @Column(length = 50)
    private String taskGroup;
    
    @Column(length = 100)
    private String cron;
    
    @Column(length = 200)
    private String targetMethod;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 20)
    private String status;
    
    private LocalDateTime lastExecuteTime;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTaskGroup() { return taskGroup; }
    public void setTaskGroup(String taskGroup) { this.taskGroup = taskGroup; }
    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }
    public String getTargetMethod() { return targetMethod; }
    public void setTargetMethod(String targetMethod) { this.targetMethod = targetMethod; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastExecuteTime() { return lastExecuteTime; }
    public void setLastExecuteTime(LocalDateTime lastExecuteTime) { this.lastExecuteTime = lastExecuteTime; }
}
