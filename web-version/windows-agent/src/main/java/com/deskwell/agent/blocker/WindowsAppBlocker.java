package com.deskwell.agent.blocker;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class WindowsAppBlocker {
    private static final Logger logger = Logger.getLogger(WindowsAppBlocker.class.getName());

    private final Set<String> blockedApps = ConcurrentHashMap.newKeySet();
    private volatile boolean isEnforcementEnabled = true;

    private static final int SW_MINIMIZE = 6;

    public WindowsAppBlocker() {}

    public void updateRules(Set<String> newBlockedApps) {
        blockedApps.clear();
        if (newBlockedApps != null) {
            blockedApps.addAll(newBlockedApps);
        }
        logger.info("Updated active blocking rules: " + blockedApps);
    }

    public boolean isBlocked(String processName) {
        if (!isEnforcementEnabled || processName == null || blockedApps.isEmpty()) return false;
        String lower = processName.toLowerCase();
        for (String blocked : blockedApps) {
            if (lower.contains(blocked.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void enforceBlock(WinDef.HWND hwnd, String processName) {
        if (!Platform.isWindows() || hwnd == null) return;
        try {
            logger.warning("[ENFORCEMENT] Blocked application detected in foreground: " + processName + " -> Minimizing window");
            User32.INSTANCE.ShowWindow(hwnd, SW_MINIMIZE);
        } catch (Exception e) {
            logger.warning("Error enforcing window block: " + e.getMessage());
        }
    }

    public Set<String> getBlockedApps() {
        return Collections.unmodifiableSet(blockedApps);
    }

    public boolean isEnforcementEnabled() {
        return isEnforcementEnabled;
    }

    public void setEnforcementEnabled(boolean enforcementEnabled) {
        isEnforcementEnabled = enforcementEnabled;
    }
}
