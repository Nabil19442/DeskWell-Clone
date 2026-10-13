package com.deskwell.web.dto;

import java.util.List;

public class ReportDto {
    private String periodType; // daily, weekly, monthly
    private String title;
    private String periodLabel;
    private long totalScreenTimeSeconds;
    private long productiveTimeSeconds;
    private long idleTimeSeconds;
    private double productivityScore;
    private int sessionCount;
    private long averageDailyScreenTimeSeconds;
    private double averageProductivityScore;
    private List<DashboardStatsDto.AppUsageDto> topApplications;
    private List<DashboardStatsDto.CategoryUsageDto> categoryBreakdown;
    private List<DashboardStatsDto.HourlyUsageDto> hourlyBreakdown;
    private List<DashboardStatsDto.DailyTrendDto> dailyBreakdown;

    public ReportDto() {}

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPeriodLabel() { return periodLabel; }
    public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }

    public long getTotalScreenTimeSeconds() { return totalScreenTimeSeconds; }
    public void setTotalScreenTimeSeconds(long totalScreenTimeSeconds) { this.totalScreenTimeSeconds = totalScreenTimeSeconds; }

    public long getProductiveTimeSeconds() { return productiveTimeSeconds; }
    public void setProductiveTimeSeconds(long productiveTimeSeconds) { this.productiveTimeSeconds = productiveTimeSeconds; }

    public long getIdleTimeSeconds() { return idleTimeSeconds; }
    public void setIdleTimeSeconds(long idleTimeSeconds) { this.idleTimeSeconds = idleTimeSeconds; }

    public double getProductivityScore() { return productivityScore; }
    public void setProductivityScore(double productivityScore) { this.productivityScore = productivityScore; }

    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }

    public long getAverageDailyScreenTimeSeconds() { return averageDailyScreenTimeSeconds; }
    public void setAverageDailyScreenTimeSeconds(long averageDailyScreenTimeSeconds) { this.averageDailyScreenTimeSeconds = averageDailyScreenTimeSeconds; }

    public double getAverageProductivityScore() { return averageProductivityScore; }
    public void setAverageProductivityScore(double averageProductivityScore) { this.averageProductivityScore = averageProductivityScore; }

    public List<DashboardStatsDto.AppUsageDto> getTopApplications() { return topApplications; }
    public void setTopApplications(List<DashboardStatsDto.AppUsageDto> topApplications) { this.topApplications = topApplications; }

    public List<DashboardStatsDto.CategoryUsageDto> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<DashboardStatsDto.CategoryUsageDto> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public List<DashboardStatsDto.HourlyUsageDto> getHourlyBreakdown() { return hourlyBreakdown; }
    public void setHourlyBreakdown(List<DashboardStatsDto.HourlyUsageDto> hourlyBreakdown) { this.hourlyBreakdown = hourlyBreakdown; }

    public List<DashboardStatsDto.DailyTrendDto> getDailyBreakdown() { return dailyBreakdown; }
    public void setDailyBreakdown(List<DashboardStatsDto.DailyTrendDto> dailyBreakdown) { this.dailyBreakdown = dailyBreakdown; }
}
