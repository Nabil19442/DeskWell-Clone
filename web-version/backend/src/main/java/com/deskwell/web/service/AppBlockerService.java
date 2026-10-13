package com.deskwell.web.service;

import com.deskwell.web.model.Application;
import com.deskwell.web.model.BlockedApp;
import com.deskwell.web.repository.ApplicationRepository;
import com.deskwell.web.repository.BlockedAppRepository;
import com.deskwell.web.repository.SettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AppBlockerService {

    private final BlockedAppRepository blockedAppRepository;
    private final ApplicationRepository applicationRepository;
    private final SettingRepository settingRepository;
    private final CategoryService categoryService;

    public AppBlockerService(
            BlockedAppRepository blockedAppRepository,
            ApplicationRepository applicationRepository,
            SettingRepository settingRepository,
            CategoryService categoryService) {
        this.blockedAppRepository = blockedAppRepository;
        this.applicationRepository = applicationRepository;
        this.settingRepository = settingRepository;
        this.categoryService = categoryService;
    }

    public List<BlockedApp> getAllBlockedApps() {
        return blockedAppRepository.findAll();
    }

    @Transactional
    public BlockedApp addBlockedApp(String appName, boolean isPermanent, String notes) {
        Application app = applicationRepository.findByNameIgnoreCase(appName)
            .orElseGet(() -> {
                Application newApp = new Application(appName, null, null);
                categoryService.autoCategorize(newApp);
                return applicationRepository.save(newApp);
            });

        BlockedApp blocked = new BlockedApp(app, isPermanent);
        blocked.setNotes(notes);
        return blockedAppRepository.save(blocked);
    }

    @Transactional
    public void removeBlockedApp(Long id) {
        blockedAppRepository.deleteById(id);
    }

    public Map<String, Object> getEnforcementStatus() {
        Map<String, Object> status = new HashMap<>();
        String agentStatus = settingRepository.findById("agent_status")
            .map(s -> s.getValue())
            .orElse("disconnected");

        boolean isAgentConnected = "connected".equalsIgnoreCase(agentStatus);
        status.put("agentConnected", isAgentConnected);
        status.put("enforcementActive", isAgentConnected);
        status.put("configuredRulesCount", blockedAppRepository.count());
        status.put("statusMessage", isAgentConnected
            ? "Rules are actively enforced via Windows Agent"
            : "Rules configured in cloud/server. Active desktop process blocking requires running the DeskWell Windows Agent companion.");
        return status;
    }
}
