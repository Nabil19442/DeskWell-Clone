package com.deskwell.web.service;

import com.deskwell.web.dto.AgentActivitySliceDto;
import com.deskwell.web.model.*;
import com.deskwell.web.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@Service
public class AgentService {
    private static final Logger logger = Logger.getLogger(AgentService.class.getName());

    private final AgentInstallationRepository agentInstallationRepository;
    private final UsageTrackingRepository usageTrackingRepository;
    private final ApplicationRepository applicationRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final BlockedAppRepository blockedAppRepository;
    private final CategoryService categoryService;
    private final SettingRepository settingRepository;

    public AgentService(
            AgentInstallationRepository agentInstallationRepository,
            UsageTrackingRepository usageTrackingRepository,
            ApplicationRepository applicationRepository,
            DailyStatsRepository dailyStatsRepository,
            BlockedAppRepository blockedAppRepository,
            CategoryService categoryService,
            SettingRepository settingRepository) {
        this.agentInstallationRepository = agentInstallationRepository;
        this.usageTrackingRepository = usageTrackingRepository;
        this.applicationRepository = applicationRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.blockedAppRepository = blockedAppRepository;
        this.categoryService = categoryService;
        this.settingRepository = settingRepository;
    }

    public boolean authenticate(String token) {
        if (token == null || token.isBlank()) return false;
        if (agentInstallationRepository.existsByAgentToken(token)) return true;
        
        // Also permit default configured agent token from settings for zero-config local runs
        String configuredToken = settingRepository.findById("agent_api_key")
                .map(Setting::getValue)
                .orElse("deskwell-agent-local-token");
        return configuredToken.equals(token);
    }

    @Transactional
    public Map<String, Object> registerAgent(String hostname, String osVersion) {
        String agentId = UUID.randomUUID().toString();
        String token = "dw_token_" + UUID.randomUUID().toString().replace("-", "");
        AgentInstallation installation = new AgentInstallation(agentId, token, hostname, osVersion);
        agentInstallationRepository.save(installation);

        Map<String, Object> resp = new HashMap<>();
        resp.put("agentId", agentId);
        resp.put("agentToken", token);
        resp.put("message", "Agent registered successfully. Store this token securely.");
        return resp;
    }

    @Transactional
    public void processHeartbeat(String agentId, String token, String hostname, String osVersion, int pendingCount) {
        if (!authenticate(token)) {
            throw new IllegalArgumentException("Invalid agent authorization token");
        }

        AgentInstallation installation = agentInstallationRepository.findByAgentId(agentId)
                .orElseGet(() -> new AgentInstallation(agentId, token, hostname, osVersion));

        installation.setHostname(hostname);
        installation.setOsVersion(osVersion);
        installation.setLastHeartbeatAt(LocalDateTime.now());
        installation.setIsActive(true);
        agentInstallationRepository.save(installation);

        settingRepository.save(new Setting("agent_status", "connected"));
    }

    @Transactional
    public Map<String, Object> syncBatch(String token, List<AgentActivitySliceDto> records) {
        if (!authenticate(token)) {
            throw new IllegalArgumentException("Invalid agent authorization token");
        }

        int processed = 0;
        int duplicates = 0;

        for (AgentActivitySliceDto slice : records) {
            if (slice.getSyncId() == null || slice.getSyncId().isBlank()) {
                continue;
            }

            // Idempotency: skip already recorded syncIds
            if (usageTrackingRepository.existsBySyncId(slice.getSyncId())) {
                duplicates++;
                continue;
            }

            String processName = slice.getProcessName();
            if (processName == null || processName.isBlank()) {
                processName = "Unknown Process";
            }

            LocalDateTime start = parseIsoDateTime(slice.getStartTime());
            LocalDateTime end = slice.getEndTime() != null ? parseIsoDateTime(slice.getEndTime()) : start.plusSeconds(slice.getDurationSeconds());
            LocalDate recordDate = start.toLocalDate();

            if (slice.isIdle() || "Idle".equalsIgnoreCase(processName)) {
                updateDailyIdleTime(recordDate, slice.getDurationSeconds());
            } else {
                final String finalProcName = processName;
                Application app = applicationRepository.findByNameIgnoreCase(finalProcName)
                        .orElseGet(() -> {
                            Application newApp = new Application(finalProcName, slice.getExecutablePath(), null);
                            categoryService.autoCategorize(newApp);
                            return applicationRepository.save(newApp);
                        });

                UsageTracking ut = new UsageTracking();
                ut.setSyncId(slice.getSyncId());
                ut.setApplication(app);
                ut.setWindowTitle(slice.getWindowTitle() != null ? slice.getWindowTitle() : finalProcName);
                ut.setStartTime(start);
                ut.setEndTime(end);
                ut.setDurationSeconds(slice.getDurationSeconds());
                usageTrackingRepository.save(ut);

                updateDailyActivityStats(recordDate, app, slice.getDurationSeconds());
            }

            processed++;
        }

        // Update last sync time on active installations
        agentInstallationRepository.findByAgentToken(token).ifPresent(inst -> {
            inst.setLastSyncAt(LocalDateTime.now());
            agentInstallationRepository.save(inst);
        });

        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("syncedCount", processed);
        res.put("processedCount", processed);
        res.put("duplicateSkippedCount", duplicates);
        return res;
    }

    public List<String> getActiveBlockingRules(String token) {
        if (!authenticate(token)) {
            throw new IllegalArgumentException("Invalid agent authorization token");
        }

        List<BlockedApp> list = blockedAppRepository.findAll();
        List<String> names = new ArrayList<>();
        for (BlockedApp ba : list) {
            if (ba.getApplication() != null && ba.getApplication().getName() != null) {
                names.add(ba.getApplication().getName());
            }
        }
        return names;
    }

    private void updateDailyIdleTime(LocalDate date, long seconds) {
        DailyStats stats = dailyStatsRepository.findByDate(date)
                .orElseGet(() -> dailyStatsRepository.save(new DailyStats(date)));
        stats.setIdleTime(stats.getIdleTime() + seconds);
        dailyStatsRepository.save(stats);
    }

    private void updateDailyActivityStats(LocalDate date, Application app, long seconds) {
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

    private LocalDateTime parseIsoDateTime(String str) {
        if (str == null || str.isBlank()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(str);
        } catch (Exception ignored) {}
        try {
            return java.time.OffsetDateTime.parse(str).toLocalDateTime();
        } catch (Exception ignored) {}
        try {
            return java.time.Instant.parse(str).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
