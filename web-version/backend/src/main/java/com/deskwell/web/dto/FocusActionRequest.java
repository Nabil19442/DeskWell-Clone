package com.deskwell.web.dto;

public class FocusActionRequest {
    private Integer focusDurationMinutes;
    private Integer breakDurationMinutes;
    private Integer longBreakDurationMinutes;
    private Integer sessionsBeforeLongBreak;

    public FocusActionRequest() {}

    public Integer getFocusDurationMinutes() { return focusDurationMinutes; }
    public void setFocusDurationMinutes(Integer focusDurationMinutes) { this.focusDurationMinutes = focusDurationMinutes; }

    public Integer getBreakDurationMinutes() { return breakDurationMinutes; }
    public void setBreakDurationMinutes(Integer breakDurationMinutes) { this.breakDurationMinutes = breakDurationMinutes; }

    public Integer getLongBreakDurationMinutes() { return longBreakDurationMinutes; }
    public void setLongBreakDurationMinutes(Integer longBreakDurationMinutes) { this.longBreakDurationMinutes = longBreakDurationMinutes; }

    public Integer getSessionsBeforeLongBreak() { return sessionsBeforeLongBreak; }
    public void setSessionsBeforeLongBreak(Integer sessionsBeforeLongBreak) { this.sessionsBeforeLongBreak = sessionsBeforeLongBreak; }
}
