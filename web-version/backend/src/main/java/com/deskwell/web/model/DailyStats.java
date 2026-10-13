package com.deskwell.web.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_stats")
public class DailyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "total_screen_time")
    private Long totalScreenTime = 0L;

    @Column(name = "productive_time")
    private Long productiveTime = 0L;

    @Column(name = "idle_time")
    private Long idleTime = 0L;

    @Column(name = "session_count")
    private Integer sessionCount = 0;

    @Column(name = "productivity_score")
    private Double productivityScore = 0.0;

    public DailyStats() {}

    public DailyStats(LocalDate date) {
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getTotalScreenTime() { return totalScreenTime; }
    public void setTotalScreenTime(Long totalScreenTime) { this.totalScreenTime = totalScreenTime; }

    public Long getProductiveTime() { return productiveTime; }
    public void setProductiveTime(Long productiveTime) { this.productiveTime = productiveTime; }

    public Long getIdleTime() { return idleTime; }
    public void setIdleTime(Long idleTime) { this.idleTime = idleTime; }

    public Integer getSessionCount() { return sessionCount; }
    public void setSessionCount(Integer sessionCount) { this.sessionCount = sessionCount; }

    public Double getProductivityScore() { return productivityScore; }
    public void setProductivityScore(Double productivityScore) { this.productivityScore = productivityScore; }
}
