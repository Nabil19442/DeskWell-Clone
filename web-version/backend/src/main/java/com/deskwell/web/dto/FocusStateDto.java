package com.deskwell.web.dto;

public class FocusStateDto {
    private Long sessionId;
    private String state; // IDLE, FOCUS, BREAK, PAUSED, COMPLETED
    private int remainingSeconds;
    private int totalDurationSeconds;
    private int focusDurationMinutes;
    private int breakDurationMinutes;
    private int longBreakDurationMinutes;
    private int sessionsBeforeLongBreak;
    private int completedSessionsToday;
    private long totalFocusMinutesToday;
    private int interruptions;
    private Long serverTimestamp;
    private Long targetEndEpochMillis;

    public FocusStateDto() {}

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(int remainingSeconds) { this.remainingSeconds = remainingSeconds; }

    public int getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(int totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }

    public int getFocusDurationMinutes() { return focusDurationMinutes; }
    public void setFocusDurationMinutes(int focusDurationMinutes) { this.focusDurationMinutes = focusDurationMinutes; }

    public int getBreakDurationMinutes() { return breakDurationMinutes; }
    public void setBreakDurationMinutes(int breakDurationMinutes) { this.breakDurationMinutes = breakDurationMinutes; }

    public int getLongBreakDurationMinutes() { return longBreakDurationMinutes; }
    public void setLongBreakDurationMinutes(int longBreakDurationMinutes) { this.longBreakDurationMinutes = longBreakDurationMinutes; }

    public int getSessionsBeforeLongBreak() { return sessionsBeforeLongBreak; }
    public void setSessionsBeforeLongBreak(int sessionsBeforeLongBreak) { this.sessionsBeforeLongBreak = sessionsBeforeLongBreak; }

    public int getCompletedSessionsToday() { return completedSessionsToday; }
    public void setCompletedSessionsToday(int completedSessionsToday) { this.completedSessionsToday = completedSessionsToday; }

    public long getTotalFocusMinutesToday() { return totalFocusMinutesToday; }
    public void setTotalFocusMinutesToday(long totalFocusMinutesToday) { this.totalFocusMinutesToday = totalFocusMinutesToday; }

    public int getInterruptions() { return interruptions; }
    public void setInterruptions(int interruptions) { this.interruptions = interruptions; }

    public Long getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(Long serverTimestamp) { this.serverTimestamp = serverTimestamp; }

    public Long getTargetEndEpochMillis() { return targetEndEpochMillis; }
    public void setTargetEndEpochMillis(Long targetEndEpochMillis) { this.targetEndEpochMillis = targetEndEpochMillis; }
}
