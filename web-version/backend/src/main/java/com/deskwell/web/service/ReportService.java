package com.deskwell.web.service;

import com.deskwell.web.dto.DashboardStatsDto;
import com.deskwell.web.dto.ReportDto;
import com.deskwell.web.model.DailyStats;
import com.deskwell.web.model.UsageTracking;
import com.deskwell.web.repository.DailyStatsRepository;
import com.deskwell.web.repository.UsageTrackingRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class ReportService {

    private final DailyStatsRepository dailyStatsRepository;
    private final UsageTrackingRepository usageTrackingRepository;

    public ReportService(DailyStatsRepository dailyStatsRepository, UsageTrackingRepository usageTrackingRepository) {
        this.dailyStatsRepository = dailyStatsRepository;
        this.usageTrackingRepository = usageTrackingRepository;
    }

    public ReportDto getDailyReport(LocalDate date) {
        ReportDto report = new ReportDto();
        report.setPeriodType("daily");
        report.setTitle("Daily Activity Report");
        report.setPeriodLabel(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));

        DailyStats stats = dailyStatsRepository.findByDate(date).orElse(new DailyStats(date));
        report.setTotalScreenTimeSeconds(stats.getTotalScreenTime());
        report.setProductiveTimeSeconds(stats.getProductiveTime());
        report.setIdleTimeSeconds(stats.getIdleTime());
        report.setProductivityScore(stats.getProductivityScore());
        report.setSessionCount(stats.getSessionCount());

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        report.setTopApplications(fetchTopApps(start, end));
        report.setCategoryBreakdown(fetchCategoryBreakdown(start, end));
        report.setHourlyBreakdown(fetchHourlyBreakdown(start, end));

        return report;
    }

    public ReportDto getWeeklyReport(int weekOffset) {
        LocalDate today = LocalDate.now();
        LocalDate target = today.minusWeeks(weekOffset);
        LocalDate startOfWeek = target.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        ReportDto report = new ReportDto();
        report.setPeriodType("weekly");
        report.setTitle("Weekly Productivity Report");
        report.setPeriodLabel(startOfWeek.format(DateTimeFormatter.ofPattern("MMM d")) + " - " + endOfWeek.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));

        List<DailyStats> dailyList = dailyStatsRepository.findByDateBetweenOrderByDateAsc(startOfWeek, endOfWeek);
        Map<LocalDate, DailyStats> map = new HashMap<>();
        for (DailyStats ds : dailyList) map.put(ds.getDate(), ds);

        long totalScreen = 0;
        long totalProd = 0;
        long totalIdle = 0;
        int totalSessions = 0;
        List<DashboardStatsDto.DailyTrendDto> dailyBreakdowns = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            DailyStats ds = map.get(day);
            if (ds != null) {
                totalScreen += ds.getTotalScreenTime();
                totalProd += ds.getProductiveTime();
                totalIdle += ds.getIdleTime();
                totalSessions += ds.getSessionCount();
                dailyBreakdowns.add(new DashboardStatsDto.DailyTrendDto(
                    day.toString(), ds.getTotalScreenTime(), ds.getProductiveTime(), ds.getProductivityScore()
                ));
            } else {
                dailyBreakdowns.add(new DashboardStatsDto.DailyTrendDto(day.toString(), 0L, 0L, 0.0));
            }
        }

        report.setTotalScreenTimeSeconds(totalScreen);
        report.setProductiveTimeSeconds(totalProd);
        report.setIdleTimeSeconds(totalIdle);
        report.setSessionCount(totalSessions);
        report.setAverageDailyScreenTimeSeconds(totalScreen / 7);
        report.setProductivityScore(totalScreen > 0 ? Math.round(((double) totalProd / totalScreen * 100.0) * 10.0) / 10.0 : 0.0);
        report.setAverageProductivityScore(report.getProductivityScore());
        report.setDailyBreakdown(dailyBreakdowns);

        LocalDateTime startDt = startOfWeek.atStartOfDay();
        LocalDateTime endDt = endOfWeek.atTime(LocalTime.MAX);
        report.setTopApplications(fetchTopApps(startDt, endDt));
        report.setCategoryBreakdown(fetchCategoryBreakdown(startDt, endDt));

        return report;
    }

    public ReportDto getMonthlyReport(int monthOffset) {
        LocalDate today = LocalDate.now();
        LocalDate target = today.minusMonths(monthOffset);
        LocalDate startOfMonth = target.withDayOfMonth(1);
        LocalDate endOfMonth = target.with(TemporalAdjusters.lastDayOfMonth());

        ReportDto report = new ReportDto();
        report.setPeriodType("monthly");
        report.setTitle("Monthly Performance Report");
        report.setPeriodLabel(startOfMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        List<DailyStats> dailyList = dailyStatsRepository.findByDateBetweenOrderByDateAsc(startOfMonth, endOfMonth);
        long totalScreen = 0;
        long totalProd = 0;
        long totalIdle = 0;
        int totalSessions = 0;
        List<DashboardStatsDto.DailyTrendDto> dailyBreakdowns = new ArrayList<>();

        for (DailyStats ds : dailyList) {
            totalScreen += ds.getTotalScreenTime();
            totalProd += ds.getProductiveTime();
            totalIdle += ds.getIdleTime();
            totalSessions += ds.getSessionCount();
            dailyBreakdowns.add(new DashboardStatsDto.DailyTrendDto(
                ds.getDate().toString(), ds.getTotalScreenTime(), ds.getProductiveTime(), ds.getProductivityScore()
            ));
        }

        int daysCount = Math.max(1, startOfMonth.lengthOfMonth());
        report.setTotalScreenTimeSeconds(totalScreen);
        report.setProductiveTimeSeconds(totalProd);
        report.setIdleTimeSeconds(totalIdle);
        report.setSessionCount(totalSessions);
        report.setAverageDailyScreenTimeSeconds(totalScreen / daysCount);
        report.setProductivityScore(totalScreen > 0 ? Math.round(((double) totalProd / totalScreen * 100.0) * 10.0) / 10.0 : 0.0);
        report.setAverageProductivityScore(report.getProductivityScore());
        report.setDailyBreakdown(dailyBreakdowns);

        LocalDateTime startDt = startOfMonth.atStartOfDay();
        LocalDateTime endDt = endOfMonth.atTime(LocalTime.MAX);
        report.setTopApplications(fetchTopApps(startDt, endDt));
        report.setCategoryBreakdown(fetchCategoryBreakdown(startDt, endDt));

        return report;
    }

    private List<DashboardStatsDto.AppUsageDto> fetchTopApps(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rawApps = usageTrackingRepository.aggregateAppUsageBetween(start, end);
        List<DashboardStatsDto.AppUsageDto> list = new ArrayList<>();
        for (Object[] row : rawApps) {
            String appName = (String) row[0];
            String catName = row[1] != null ? (String) row[1] : "Other";
            String color = row[2] != null ? (String) row[2] : "#607D8B";
            Long totalSec = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            Long sessCount = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            list.add(new DashboardStatsDto.AppUsageDto(appName, catName, color, totalSec, sessCount));
        }
        return list;
    }

    private List<DashboardStatsDto.CategoryUsageDto> fetchCategoryBreakdown(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rawCats = usageTrackingRepository.aggregateCategoryUsageBetween(start, end);
        List<DashboardStatsDto.CategoryUsageDto> list = new ArrayList<>();
        long totalSum = 0;
        for (Object[] r : rawCats) totalSum += (r[2] != null ? ((Number) r[2]).longValue() : 0L);
        for (Object[] row : rawCats) {
            String catName = row[0] != null ? (String) row[0] : "Other";
            String color = row[1] != null ? (String) row[1] : "#607D8B";
            Long totalSec = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            double pct = totalSum > 0 ? Math.round(((double) totalSec / totalSum * 100.0) * 10.0) / 10.0 : 0.0;
            list.add(new DashboardStatsDto.CategoryUsageDto(catName, color, totalSec, pct));
        }
        return list;
    }

    private List<DashboardStatsDto.HourlyUsageDto> fetchHourlyBreakdown(LocalDateTime start, LocalDateTime end) {
        List<UsageTracking> usages = usageTrackingRepository.findTrackedUsageBetween(start, end);
        Map<Integer, Long> hourlyMap = new HashMap<>();
        for (int h = 0; h < 24; h++) hourlyMap.put(h, 0L);
        for (UsageTracking u : usages) {
            int h = u.getStartTime().getHour();
            hourlyMap.put(h, hourlyMap.get(h) + (u.getDurationSeconds() != null ? u.getDurationSeconds() : 0L));
        }
        List<DashboardStatsDto.HourlyUsageDto> list = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            list.add(new DashboardStatsDto.HourlyUsageDto(h, hourlyMap.get(h)));
        }
        return list;
    }
}
