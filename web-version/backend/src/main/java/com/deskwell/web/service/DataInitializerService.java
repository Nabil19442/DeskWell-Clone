package com.deskwell.web.service;

import com.deskwell.web.model.*;
import com.deskwell.web.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DataInitializerService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationRepository applicationRepository;
    private final SettingRepository settingRepository;
    private final UsageTrackingRepository usageTrackingRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final GoalRepository goalRepository;
    private final BlockedAppRepository blockedAppRepository;

    public DataInitializerService(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ApplicationRepository applicationRepository,
            SettingRepository settingRepository,
            UsageTrackingRepository usageTrackingRepository,
            DailyStatsRepository dailyStatsRepository,
            GoalRepository goalRepository,
            BlockedAppRepository blockedAppRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.applicationRepository = applicationRepository;
        this.settingRepository = settingRepository;
        this.usageTrackingRepository = usageTrackingRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.goalRepository = goalRepository;
        this.blockedAppRepository = blockedAppRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initDefaultUser();
        initDefaultCategories();
        initDefaultSettings();
        seedInitialDemoActivityIfEmpty();
    }

    private void initDefaultUser() {
        if (userRepository.count() == 0) {
            User user = new User("default");
            userRepository.save(user);
        }
    }

    private void initDefaultCategories() {
        String[][] defaults = {
            {"Productivity", "#4CAF50", "0.0"},
            {"Development", "#2196F3", "1.0"},
            {"Browser", "#FF9800", "0.0"},
            {"Entertainment", "#F44336", "0.0"},
            {"Communication", "#9C27B0", "0.0"},
            {"Gaming", "#795548", "0.0"},
            {"Other", "#607D8B", "0.0"}
        };

        for (String[] cat : defaults) {
            Optional<Category> existing = categoryRepository.findByNameIgnoreCase(cat[0]);
            if (existing.isEmpty()) {
                Category c = new Category(cat[0], cat[1], true, Double.parseDouble(cat[2]));
                categoryRepository.save(c);
            }
        }
    }

    private void initDefaultSettings() {
        String[][] defaultSettings = {
            {"theme", "light"},
            {"tracking_frequency_seconds", "3"},
            {"idle_threshold_seconds", "60"},
            {"break_reminder_enabled", "true"},
            {"break_interval_minutes", "60"},
            {"screen_time_warning_enabled", "true"},
            {"screen_time_warning_threshold", "360"},
            {"notifications_enabled", "true"},
            {"agent_status", "disconnected"},
            {"agent_api_key", "deskwell-agent-local-token"}
        };

        for (String[] s : defaultSettings) {
            if (!settingRepository.existsById(s[0])) {
                settingRepository.save(new Setting(s[0], s[1]));
            }
        }
    }

    private void seedInitialDemoActivityIfEmpty() {
        if (usageTrackingRepository.count() > 0) return;

        Category dev = categoryRepository.findByNameIgnoreCase("Development").orElse(null);
        Category prod = categoryRepository.findByNameIgnoreCase("Productivity").orElse(null);
        Category browser = categoryRepository.findByNameIgnoreCase("Browser").orElse(null);
        Category comm = categoryRepository.findByNameIgnoreCase("Communication").orElse(null);

        Application vscode = applicationRepository.save(new Application("Code.exe", "C:\\Users\\dev\\AppData\\Local\\Programs\\VS Code\\Code.exe", dev));
        Application intellij = applicationRepository.save(new Application("idea64.exe", "C:\\Program Files\\JetBrains\\IntelliJ IDEA\\bin\\idea64.exe", dev));
        Application chrome = applicationRepository.save(new Application("chrome.exe", "C:\\Program Files\\Google\\Chrome\\chrome.exe", browser));
        Application slack = applicationRepository.save(new Application("slack.exe", "C:\\Users\\dev\\AppData\\Local\\slack\\slack.exe", comm));
        Application notion = applicationRepository.save(new Application("Notion.exe", "C:\\Users\\dev\\AppData\\Local\\Programs\\Notion\\Notion.exe", prod));

        LocalDate today = LocalDate.now();
        LocalDateTime baseTime = today.atTime(9, 0);

        // VS Code session (Development)
        UsageTracking ut1 = new UsageTracking();
        ut1.setApplication(vscode);
        ut1.setWindowTitle("DeskWellApp.java - DeskWell");
        ut1.setStartTime(baseTime);
        ut1.setEndTime(baseTime.plusMinutes(90));
        ut1.setDurationSeconds(5400L);
        usageTrackingRepository.save(ut1);

        // Chrome session (Browser)
        UsageTracking ut2 = new UsageTracking();
        ut2.setApplication(chrome);
        ut2.setWindowTitle("Spring Boot Documentation - Google Chrome");
        ut2.setStartTime(baseTime.plusMinutes(95));
        ut2.setEndTime(baseTime.plusMinutes(135));
        ut2.setDurationSeconds(2400L);
        usageTrackingRepository.save(ut2);

        // IntelliJ session (Development)
        UsageTracking ut3 = new UsageTracking();
        ut3.setApplication(intellij);
        ut3.setWindowTitle("DeskWell - Spring Data JPA Migration");
        ut3.setStartTime(baseTime.plusMinutes(140));
        ut3.setEndTime(baseTime.plusMinutes(240));
        ut3.setDurationSeconds(6000L);
        usageTrackingRepository.save(ut3);

        // Slack session (Communication)
        UsageTracking ut4 = new UsageTracking();
        ut4.setApplication(slack);
        ut4.setWindowTitle("Team Sync #engineering - Slack");
        ut4.setStartTime(baseTime.plusMinutes(245));
        ut4.setEndTime(baseTime.plusMinutes(275));
        ut4.setDurationSeconds(1800L);
        usageTrackingRepository.save(ut4);

        // Notion session (Productivity)
        UsageTracking ut5 = new UsageTracking();
        ut5.setApplication(notion);
        ut5.setWindowTitle("DeskWell Architecture Notes");
        ut5.setStartTime(baseTime.plusMinutes(280));
        ut5.setEndTime(baseTime.plusMinutes(320));
        ut5.setDurationSeconds(2400L);
        usageTrackingRepository.save(ut5);

        // Calculate and save today's stats
        DailyStats todayStats = new DailyStats(today);
        todayStats.setTotalScreenTime(18000L); // 5 hours
        todayStats.setProductiveTime(11400L);  // VS Code + IntelliJ
        todayStats.setIdleTime(1200L);
        todayStats.setSessionCount(5);
        todayStats.setProductivityScore(63.33);
        dailyStatsRepository.save(todayStats);

        // Also add past 6 days history for weekly trends
        for (int i = 1; i <= 6; i++) {
            LocalDate pastDate = today.minusDays(i);
            DailyStats stats = new DailyStats(pastDate);
            long screenTime = 14400L + (long)(Math.random() * 7200L);
            long prodTime = (long)(screenTime * (0.55 + (Math.random() * 0.25)));
            stats.setTotalScreenTime(screenTime);
            stats.setProductiveTime(prodTime);
            stats.setIdleTime((long)(900L + Math.random() * 900L));
            stats.setSessionCount(4 + (int)(Math.random() * 4));
            stats.setProductivityScore(Math.round(((double)prodTime / screenTime * 100.0) * 10.0) / 10.0);
            dailyStatsRepository.save(stats);
        }

        // Default Goals
        Goal g1 = new Goal();
        g1.setType("productive_time");
        g1.setTargetValue(240); // 4 hours
        g1.setCurrentValue(190);
        g1.setStartDate(today);
        goalRepository.save(g1);

        Goal g2 = new Goal();
        g2.setType("screen_time");
        g2.setTargetValue(420); // 7 hours max
        g2.setCurrentValue(300);
        g2.setStartDate(today);
        goalRepository.save(g2);

        Goal g3 = new Goal();
        g3.setType("focus_time");
        g3.setTargetValue(100); // 100 mins focus
        g3.setCurrentValue(50);
        g3.setStartDate(today);
        goalRepository.save(g3);

        // Sample blocked app rule
        BlockedApp b1 = new BlockedApp(chrome, false);
        b1.setNotes("Block distractions during focus sessions");
        blockedAppRepository.save(b1);
    }
}
