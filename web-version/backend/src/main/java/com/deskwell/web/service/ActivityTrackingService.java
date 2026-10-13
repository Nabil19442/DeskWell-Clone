package com.deskwell.web.service;

import com.deskwell.web.dto.ActivityRecordRequest;
import com.deskwell.web.dto.DashboardStatsDto;
import com.deskwell.web.model.*;
import com.deskwell.web.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ActivityTrackingService {

    private final UsageTrackingRepository usageTrackingRepository;
    private final ApplicationRepository applicationRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final SessionRepository sessionRepository;
    private final SettingRepository settingRepository;

    private volatile String currentActiveApp = "DeskWell Web";

    public ActivityTrackingService(
            UsageTrackingRepository usageTrackingRepository,
            ApplicationRepository applicationRepository,
            DailyStatsRepository dailyStatsRepository,
            CategoryRepository categoryRepository,
            CategoryService categoryService,
            SessionRepository sessionRepository,
            SettingRepository settingRepository) {
        this.usageTrackingRepository = usageTrackingRepository;
        this.applicationRepository = applicationRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.sessionRepository = sessionRepository;
        this.settingRepository = settingRepository;
    }

    public String getCurrentActiveApp() {
        return currentActiveApp;
    }

    @Transactional
    public UsageTracking recordActivity(ActivityRecordRequest req) {
        if (req.isIdle()) {
            recordIdleTime(req.getDurationSeconds());
            currentActiveApp = "Idle";
            return null;
        }

        currentActiveApp = req.getProcessName();

        Application app = applicationRepository.findByNameIgnoreCase(req.getProcessName())
            .orElseGet(() -> {
                Application newApp = new Application(req.getProcessName(), req.getExecutablePath(), null);
                categoryService.autoCategorize(newApp);
                return applicationRepository.save(newApp);
            });

        UsageTracking tracking = new UsageTracking();
        tracking.setApplication(app);
        tracking.setWindowTitle(req.getWindowTitle() != null ? req.getWindowTitle() : req.getProcessName());
        LocalDateTime now = LocalDateTime.now();
        tracking.setEndTime(now);
        tracking.setStartTime(now.minusSeconds(req.getDurationSeconds()));
        tracking.setDurationSeconds(req.getDurationSeconds());

        UsageTracking saved = usageTrackingRepository.save(tracking);
        updateDailyStatsAfterActivity(LocalDate.now(), app, req.getDurationSeconds());
        return saved;
    }

    @Transactional
    public void recordIdleTime(long seconds) {
        LocalDate today = LocalDate.now();
        DailyStats stats = dailyStatsRepository.findByDate(today)
            .orElseGet(() -> dailyStatsRepository.save(new DailyStats(today)));
        stats.setIdleTime(stats.getIdleTime() + seconds);
        dailyStatsRepository.save(stats);
    }

    private void updateDailyStatsAfterActivity(LocalDate date, Application app, long seconds) {
        DailyStats stats = dailyStatsRepository.findByDate(date)
            .orElseGet(() -> dailyStatsRepository.save(new DailyStats(date)));

        stats.setTotalScreenTime(stats.getTotalScreenTime() + seconds);

        double weight = (app.getCategory() != null && app.getCategory().getProductivityWeight() != null)
            ? app.getCategory().getProductivityWeight()
            : 0.0;

        long addedProd = Math.round(seconds * weight);
        stats.setProductiveTime(stats.getProductiveTime() + addedProd);

        if (stats.getTotalScreenTime() > 0) {
            double score = ((double) stats.getProductiveTime() / stats.getTotalScreenTime()) * 100.0;
            stats.setProductivityScore(Math.round(score * 100.0) / 100.0);
        }

        dailyStatsRepository.save(stats);
    }

    public DashboardStatsDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        DailyStats stats = dailyStatsRepository.findByDate(today)
            .orElseGet(() -> new DailyStats(today));

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTotalScreenTimeToday(stats.getTotalScreenTime());
        dto.setProductiveTimeToday(stats.getProductiveTime());
        dto.setIdleTimeToday(stats.getIdleTime());
        dto.setProductivityPercentage(stats.getProductivityScore());
        dto.setSessionCount(stats.getSessionCount());
        dto.setActiveApplicationName(currentActiveApp);

        String agentSetting = settingRepository.findById("agent_status").map(Setting::getValue).orElse("web_standalone");
        dto.setCurrentTrackingStatus(agentSetting.equalsIgnoreCase("connected") ? "Agent Connected" : "Web Standalone");

        // Top applications
        List<Object[]> rawApps = usageTrackingRepository.aggregateAppUsageBetween(startOfDay, endOfDay);
        List<DashboardStatsDto.AppUsageDto> topApps = new ArrayList<>();
        for (Object[] row : rawApps) {
            String appName = (String) row[0];
            String catName = row[1] != null ? (String) row[1] : "Other";
            String color = row[2] != null ? (String) row[2] : "#607D8B";
            Long totalSec = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            Long sessCount = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            topApps.add(new DashboardStatsDto.AppUsageDto(appName, catName, color, totalSec, sessCount));
        }
        dto.setTopApplications(topApps);

        // Category breakdown
        List<Object[]> rawCats = usageTrackingRepository.aggregateCategoryUsageBetween(startOfDay, endOfDay);
        List<DashboardStatsDto.CategoryUsageDto> catList = new ArrayList<>();
        long totalSecsSum = topApps.stream().mapToLong(DashboardStatsDto.AppUsageDto::getTotalSeconds).sum();
        for (Object[] row : rawCats) {
            String catName = row[0] != null ? (String) row[0] : "Other";
            String color = row[1] != null ? (String) row[1] : "#607D8B";
            Long totalSec = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            double pct = totalSecsSum > 0 ? Math.round(((double) totalSec / totalSecsSum * 100.0) * 10.0) / 10.0 : 0.0;
            catList.add(new DashboardStatsDto.CategoryUsageDto(catName, color, totalSec, pct));
        }
        dto.setCategoryBreakdown(catList);

        // Hourly activity
        List<UsageTracking> todayUsages = usageTrackingRepository.findTrackedUsageBetween(startOfDay, endOfDay);
        Map<Integer, Long> hourlyMap = new HashMap<>();
        for (int h = 0; h < 24; h++) hourlyMap.put(h, 0L);
        for (UsageTracking u : todayUsages) {
            int h = u.getStartTime().getHour();
            hourlyMap.put(h, hourlyMap.get(h) + (u.getDurationSeconds() != null ? u.getDurationSeconds() : 0L));
        }
        List<DashboardStatsDto.HourlyUsageDto> hourlyList = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            hourlyList.add(new DashboardStatsDto.HourlyUsageDto(h, hourlyMap.get(h)));
        }
        dto.setHourlyActivity(hourlyList);

        // Weekly trend (past 7 days)
        LocalDate weekStart = today.minusDays(6);
        List<DailyStats> weekStats = dailyStatsRepository.findByDateBetweenOrderByDateAsc(weekStart, today);
        Map<LocalDate, DailyStats> statMap = new HashMap<>();
        for (DailyStats ds : weekStats) statMap.put(ds.getDate(), ds);

        List<DashboardStatsDto.DailyTrendDto> trends = new ArrayList<>();
        for (int d = 6; d >= 0; d--) {
            LocalDate dt = today.minusDays(d);
            DailyStats ds = statMap.get(dt);
            if (ds != null) {
                trends.add(new DashboardStatsDto.DailyTrendDto(
                    dt.toString(),
                    ds.getTotalScreenTime(),
                    ds.getProductiveTime(),
                    ds.getProductivityScore()
                ));
            } else {
                trends.add(new DashboardStatsDto.DailyTrendDto(dt.toString(), 0L, 0L, 0.0));
            }
        }
        dto.setWeeklyTrend(trends);

        return dto;
    }
}
