package com.deskwell.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public class AgentActivitySliceDto {
    @NotBlank(message = "syncId is required")
    private String syncId;

    @JsonAlias({"appName", "applicationName"})
    @NotBlank(message = "processName is required")
    private String processName;

    private String windowTitle;
    private String executablePath;

    @NotBlank(message = "startTime is required")
    private String startTime;

    private String endTime;

    @PositiveOrZero(message = "durationSeconds must be >= 0")
    private long durationSeconds;

    private boolean isIdle;

    public AgentActivitySliceDto() {}

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
}
