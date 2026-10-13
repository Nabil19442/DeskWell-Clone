package com.deskwell.agent.model;

import java.util.UUID;

public class ActivitySlice {
    private String syncId;
    private String processName;
    private String windowTitle;
    private String executablePath;
    private String startTime;
    private String endTime;
    private long durationSeconds;
    private boolean isIdle;
    private long createdAt;

    public ActivitySlice() {
        this.syncId = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }

    public ActivitySlice(String processName, String windowTitle, String executablePath,
                         String startTime, String endTime, long durationSeconds, boolean isIdle) {
        this.syncId = UUID.randomUUID().toString();
        this.processName = processName;
        this.windowTitle = windowTitle;
        this.executablePath = executablePath;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
        this.isIdle = isIdle;
        this.createdAt = System.currentTimeMillis();
    }

    public String getSyncId() { return syncId; }
    public void setSyncId(String syncId) { this.syncId = syncId; }

    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }

    public String getWindowTitle() { return windowTitle; }
    public void setWindowTitle(String windowTitle) { this.windowTitle = windowTitle; }

    public String getExecutablePath() { return executablePath; }
    public void setExecutablePath(String executablePath) { this.executablePath = executablePath; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public boolean isIdle() { return isIdle; }
    public void setIdle(boolean idle) { isIdle = idle; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ActivitySlice{" +
                "syncId='" + syncId + '\'' +
                ", processName='" + processName + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", isIdle=" + isIdle +
                '}';
    }
}
