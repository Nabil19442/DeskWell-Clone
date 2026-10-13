package com.deskwell.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public class ActivityRecordRequest {
    @NotBlank(message = "Process name is required")
    private String processName;

    private String windowTitle;
    private String executablePath;

    @PositiveOrZero(message = "Duration must be zero or positive")
    private long durationSeconds;

    private boolean isIdle;

    public ActivityRecordRequest() {}

    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }

    public String getWindowTitle() { return windowTitle; }
    public void setWindowTitle(String windowTitle) { this.windowTitle = windowTitle; }

    public String getExecutablePath() { return executablePath; }
    public void setExecutablePath(String executablePath) { this.executablePath = executablePath; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public boolean isIdle() { return isIdle; }
    public void setIdle(boolean idle) { isIdle = idle; }
}
