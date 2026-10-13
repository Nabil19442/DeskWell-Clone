package com.deskwell.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "focus_sessions")
public class FocusSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 25;

    @Column(name = "break_minutes")
    private Integer breakMinutes = 5;

    @Column(name = "pomodoro_mode")
    private Boolean pomodoroMode = true;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private Integer interruptions = 0;

    @Column(nullable = false)
    private String state = "IDLE"; // IDLE, FOCUS, BREAK, PAUSED, COMPLETED

    @Column(name = "target_end_epoch_millis")
    private Long targetEndEpochMillis;

    @Column(name = "remaining_seconds")
    private Integer remainingSeconds = 0;

    public FocusSession() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }

    public Boolean getPomodoroMode() { return pomodoroMode; }
    public void setPomodoroMode(Boolean pomodoroMode) { this.pomodoroMode = pomodoroMode; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Integer getInterruptions() { return interruptions; }
    public void setInterruptions(Integer interruptions) { this.interruptions = interruptions; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Long getTargetEndEpochMillis() { return targetEndEpochMillis; }
    public void setTargetEndEpochMillis(Long targetEndEpochMillis) { this.targetEndEpochMillis = targetEndEpochMillis; }

    public Integer getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(Integer remainingSeconds) { this.remainingSeconds = remainingSeconds; }
}
