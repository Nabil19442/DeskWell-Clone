package com.deskwell.agent.tracker;

import com.deskwell.agent.blocker.WindowsAppBlocker;
import com.deskwell.agent.config.AgentConfig;
import com.deskwell.agent.model.ActivitySlice;
import com.deskwell.agent.storage.LocalQueueStorage;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WindowsActivityTracker {
    private static final Logger logger = Logger.getLogger(WindowsActivityTracker.class.getName());

    private final AgentConfig config;
    private final LocalQueueStorage storage;
    private final WindowsAppBlocker appBlocker;
    private final ScheduledExecutorService scheduler;

    private volatile boolean isRunning = false;
    private volatile boolean isIdle = false;

    private String currentProcessName = null;
    private String currentWindowTitle = null;
    private String currentExecutablePath = null;
    private LocalDateTime currentAppStartTime = null;
    private long currentAppAccumulatedSeconds = 0;

    private long lastPollEpochMillis = 0;
    private LocalDate currentTrackingDate = LocalDate.now();

    private static final int SLEEP_GAP_THRESHOLD_SECONDS = 120;
    private static final long CHUNK_FLUSH_INTERVAL_SECONDS = 60; // Periodically enqueue active app chunks

    public WindowsActivityTracker(AgentConfig config, LocalQueueStorage storage, WindowsAppBlocker appBlocker) {
        this.config = config;
        this.storage = storage;
        this.appBlocker = appBlocker;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;
        currentTrackingDate = LocalDate.now();
        lastPollEpochMillis = System.currentTimeMillis();

        int pollInterval = Math.max(1, config.getPollingIntervalSeconds());
        scheduler.scheduleAtFixedRate(this::pollActiveWindow, 0, pollInterval, TimeUnit.SECONDS);
        logger.info("WindowsActivityTracker started (polling every " + pollInterval + "s, idle threshold: " + config.getIdleThresholdSeconds() + "s)");
    }

    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;
        flushCurrentApp(LocalDateTime.now());
        scheduler.shutdown();
        logger.info("WindowsActivityTracker stopped");
    }

    private synchronized void pollActiveWindow() {
        try {
            if (!isRunning) return;

            long nowMillis = System.currentTimeMillis();
            if (lastPollEpochMillis > 0) {
                long gapSeconds = (nowMillis - lastPollEpochMillis) / 1000;
                int sleepThreshold = Math.max(SLEEP_GAP_THRESHOLD_SECONDS, config.getPollingIntervalSeconds() * 10);
                if (gapSeconds > sleepThreshold) {
                    // System was asleep or hibernating - exclude suspended gap
                    handleWakeFromSleep();
                    lastPollEpochMillis = nowMillis;
                    return;
                }
            }
            lastPollEpochMillis = nowMillis;

            // Day rollover check
            checkDayRollover();

            // Detect active foreground window and process
            ProcessInfo procInfo = getActiveWindow();

            // Check if app is blocked by rules
            if (appBlocker != null && procInfo.processName != null && appBlocker.isBlocked(procInfo.processName)) {
                appBlocker.enforceBlock(procInfo.hwnd, procInfo.processName);
            }

            long idleTime = getIdleTimeSeconds();
            int idleThreshold = config.getIdleThresholdSeconds();

            if (idleTime > idleThreshold) {
                if (!isIdle) {
                    // Transition to Idle
                    isIdle = true;
                    flushCurrentApp(LocalDateTime.now());
                    currentProcessName = "Idle";
                    currentWindowTitle = "Away from Keyboard";
                    currentExecutablePath = "";
                    currentAppStartTime = LocalDateTime.now();
                    currentAppAccumulatedSeconds = 0;
                    logger.info("User became idle (> " + idleTime + "s)");
                } else {
                    currentAppAccumulatedSeconds += config.getPollingIntervalSeconds();
                }
            } else {
                if (isIdle) {
                    // Resumed from idle
                    isIdle = false;
                    flushCurrentApp(LocalDateTime.now());
                    logger.info("User resumed activity from idle");
                }

                // Check if application or window changed
                boolean appChanged = currentProcessName == null || 
                                     !currentProcessName.equalsIgnoreCase(procInfo.processName) ||
                                     !isEqual(currentWindowTitle, procInfo.windowTitle);

                if (appChanged) {
                    flushCurrentApp(LocalDateTime.now());
                    currentProcessName = procInfo.processName;
                    currentWindowTitle = procInfo.windowTitle;
                    currentExecutablePath = procInfo.executablePath;
                    currentAppStartTime = LocalDateTime.now();
                    currentAppAccumulatedSeconds = 0;
                } else {
                    currentAppAccumulatedSeconds += config.getPollingIntervalSeconds();
                    // Flush chunks periodically so long active sessions are committed regularly
                    if (currentAppAccumulatedSeconds >= CHUNK_FLUSH_INTERVAL_SECONDS) {
                        flushCurrentApp(LocalDateTime.now());
                        currentAppStartTime = LocalDateTime.now();
                        currentAppAccumulatedSeconds = 0;
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Error in pollActiveWindow: " + e.getMessage());
        }
    }

    private void flushCurrentApp(LocalDateTime endTime) {
        if (currentProcessName != null && currentAppAccumulatedSeconds > 0) {
            LocalDateTime start = currentAppStartTime != null ? currentAppStartTime : endTime.minusSeconds(currentAppAccumulatedSeconds);
            boolean idleState = "Idle".equalsIgnoreCase(currentProcessName) || isIdle;

            ActivitySlice slice = new ActivitySlice(
                currentProcessName,
                currentWindowTitle != null ? currentWindowTitle : currentProcessName,
                currentExecutablePath != null ? currentExecutablePath : "",
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                currentAppAccumulatedSeconds,
                idleState
            );

            storage.enqueue(slice);
            logger.fine("Enqueued activity slice: " + slice);
        }
    }

    private void handleWakeFromSleep() {
        logger.info("System resumed from sleep/hibernation - resetting current tracker accumulator");
        flushCurrentApp(LocalDateTime.now());
        isIdle = false;
        currentProcessName = null;
        currentAppAccumulatedSeconds = 0;
    }

    private void checkDayRollover() {
        LocalDate today = LocalDate.now();
        if (!today.equals(currentTrackingDate)) {
            logger.info("Day rollover: " + currentTrackingDate + " -> " + today);
            flushCurrentApp(LocalDateTime.now());
            currentTrackingDate = today;
            currentAppStartTime = LocalDateTime.now();
            currentAppAccumulatedSeconds = 0;
        }
    }

    public ProcessInfo getActiveWindow() {
        ProcessInfo info = new ProcessInfo();
        if (!Platform.isWindows()) {
            info.processName = "GenericDesktop";
            info.windowTitle = "Desktop";
            return info;
        }

        try {
            WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
            info.hwnd = hwnd;
            if (hwnd == null) {
                info.processName = "Desktop";
                info.windowTitle = "Desktop";
                return info;
            }

            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
            info.windowTitle = Native.toString(windowText).trim();

            IntByReference processId = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, processId);
            info.processId = processId.getValue();

            WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ,
                false,
                info.processId
            );

            if (processHandle != null) {
                try {
                    char[] exePath = new char[2048];
                    IntByReference size = new IntByReference(exePath.length);
                    if (Kernel32.INSTANCE.QueryFullProcessImageName(processHandle, 0, exePath, size)) {
                        String fullPath = Native.toString(exePath);
                        info.executablePath = fullPath;
                        info.processName = fullPath.substring(fullPath.lastIndexOf('\\') + 1);
                    }
                } finally {
                    Kernel32.INSTANCE.CloseHandle(processHandle);
                }
            }

            if (info.processName == null || info.processName.isBlank()) {
                info.processName = info.windowTitle.isEmpty() ? "Unknown" : info.windowTitle;
            }
        } catch (Exception e) {
            info.processName = "Unknown";
            info.windowTitle = "Unknown";
        }
        return info;
    }

    public long getIdleTimeSeconds() {
        if (!Platform.isWindows()) return 0;
        try {
            WinUser.LASTINPUTINFO lastInputInfo = new WinUser.LASTINPUTINFO();
            if (User32.INSTANCE.GetLastInputInfo(lastInputInfo)) {
                long tickCount = Kernel32.INSTANCE.GetTickCount();
                return (tickCount - lastInputInfo.dwTime) / 1000;
            }
        } catch (Exception e) {
            logger.warning("Error getting idle time: " + e.getMessage());
        }
        return 0;
    }

    private boolean isEqual(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }

    public static class ProcessInfo {
        public WinDef.HWND hwnd;
        public int processId;
        public String processName;
        public String windowTitle;
        public String executablePath;
    }
}
