package com.deskwell.web.dto;

import java.util.List;

public class DashboardStatsDto {
    private long totalScreenTimeToday;
    private long productiveTimeToday;
    private long idleTimeToday;
    private double productivityPercentage;
    private int sessionCount;
    private String currentTrackingStatus;
    private String activeApplicationName;
    private List<AppUsageDto> topApplications;
    private List<CategoryUsageDto> categoryBreakdown;
    private List<HourlyUsageDto> hourlyActivity;
    private List<DailyTrendDto> weeklyTrend;

    public DashboardStatsDto() {}

    public long getTotalScreenTimeToday() { return totalScreenTimeToday; }
    public void setTotalScreenTimeToday(long totalScreenTimeToday) { this.totalScreenTimeToday = totalScreenTimeToday; }

    public long getProductiveTimeToday() { return productiveTimeToday; }
    public void setProductiveTimeToday(long productiveTimeToday) { this.productiveTimeToday = productiveTimeToday; }

    public long getIdleTimeToday() { return idleTimeToday; }
    public void setIdleTimeToday(long idleTimeToday) { this.idleTimeToday = idleTimeToday; }

    public double getProductivityPercentage() { return productivityPercentage; }
    public void setProductivityPercentage(double productivityPercentage) { this.productivityPercentage = productivityPercentage; }

    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }

    public String getCurrentTrackingStatus() { return currentTrackingStatus; }
    public void setCurrentTrackingStatus(String currentTrackingStatus) { this.currentTrackingStatus = currentTrackingStatus; }

    public String getActiveApplicationName() { return activeApplicationName; }
    public void setActiveApplicationName(String activeApplicationName) { this.activeApplicationName = activeApplicationName; }

    public List<AppUsageDto> getTopApplications() { return topApplications; }
    public void setTopApplications(List<AppUsageDto> topApplications) { this.topApplications = topApplications; }

    public List<CategoryUsageDto> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<CategoryUsageDto> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public List<HourlyUsageDto> getHourlyActivity() { return hourlyActivity; }
    public void setHourlyActivity(List<HourlyUsageDto> hourlyActivity) { this.hourlyActivity = hourlyActivity; }

    public List<DailyTrendDto> getWeeklyTrend() { return weeklyTrend; }
    public void setWeeklyTrend(List<DailyTrendDto> weeklyTrend) { this.weeklyTrend = weeklyTrend; }

    public static class AppUsageDto {
        private String appName;
        private String category;
        private String color;
        private long totalSeconds;
        private long sessionCount;

        public AppUsageDto() {}
        public AppUsageDto(String appName, String category, String color, long totalSeconds, long sessionCount) {
            this.appName = appName;
            this.category = category;
            this.color = color;
            this.totalSeconds = totalSeconds;
            this.sessionCount = sessionCount;
        }

        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public long getTotalSeconds() { return totalSeconds; }
        public void setTotalSeconds(long totalSeconds) { this.totalSeconds = totalSeconds; }
        public long getSessionCount() { return sessionCount; }
        public void setSessionCount(long sessionCount) { this.sessionCount = sessionCount; }
    }

    public static class CategoryUsageDto {
        private String categoryName;
        private String color;
        private long totalSeconds;
        private double percentage;

        public CategoryUsageDto() {}
        public CategoryUsageDto(String categoryName, String color, long totalSeconds, double percentage) {
            this.categoryName = categoryName;
            this.color = color;
            this.totalSeconds = totalSeconds;
            this.percentage = percentage;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public long getTotalSeconds() { return totalSeconds; }
        public void setTotalSeconds(long totalSeconds) { this.totalSeconds = totalSeconds; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class HourlyUsageDto {
        private int hour;
        private long totalSeconds;

        public HourlyUsageDto() {}
        public HourlyUsageDto(int hour, long totalSeconds) {
            this.hour = hour;
            this.totalSeconds = totalSeconds;
        }

        public int getHour() { return hour; }
        public void setHour(int hour) { this.hour = hour; }
        public long getTotalSeconds() { return totalSeconds; }
        public void setTotalSeconds(long totalSeconds) { this.totalSeconds = totalSeconds; }
    }

    public static class DailyTrendDto {
        private String date;
        private long screenTimeSeconds;
        private long productiveTimeSeconds;
        private double productivityScore;

        public DailyTrendDto() {}
        public DailyTrendDto(String date, long screenTimeSeconds, long productiveTimeSeconds, double productivityScore) {
            this.date = date;
            this.screenTimeSeconds = screenTimeSeconds;
            this.productiveTimeSeconds = productiveTimeSeconds;
            this.productivityScore = productivityScore;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public long getScreenTimeSeconds() { return screenTimeSeconds; }
        public void setScreenTimeSeconds(long screenTimeSeconds) { this.screenTimeSeconds = screenTimeSeconds; }
        public long getProductiveTimeSeconds() { return productiveTimeSeconds; }
        public void setProductiveTimeSeconds(long productiveTimeSeconds) { this.productiveTimeSeconds = productiveTimeSeconds; }
        public double getProductivityScore() { return productivityScore; }
        public void setProductivityScore(double productivityScore) { this.productivityScore = productivityScore; }
    }
}
