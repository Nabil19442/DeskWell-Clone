package com.deskwell.web.service;

import com.deskwell.web.dto.GoalDto;
import com.deskwell.web.model.DailyStats;
import com.deskwell.web.model.FocusSession;
import com.deskwell.web.model.Goal;
import com.deskwell.web.model.UsageTracking;
import com.deskwell.web.repository.DailyStatsRepository;
import com.deskwell.web.repository.FocusSessionRepository;
import com.deskwell.web.repository.GoalRepository;
import com.deskwell.web.repository.UsageTrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final UsageTrackingRepository usageTrackingRepository;

    public GoalService(
            GoalRepository goalRepository,
            DailyStatsRepository dailyStatsRepository,
            FocusSessionRepository focusSessionRepository,
            UsageTrackingRepository usageTrackingRepository) {
        this.goalRepository = goalRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.focusSessionRepository = focusSessionRepository;
        this.usageTrackingRepository = usageTrackingRepository;
    }

    public List<GoalDto> getActiveGoalsWithProgress() {
        List<Goal> goals = goalRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<GoalDto> result = new ArrayList<>();

        LocalDate today = LocalDate.now();
        DailyStats todayStats = dailyStatsRepository.findByDate(today).orElse(null);

        for (Goal g : goals) {
            int currentMinutes = calculateTodayMinutesForGoal(g.getType(), today, todayStats);
            g.setCurrentValue(currentMinutes);
            goalRepository.save(g);

            GoalDto dto = new GoalDto();
            dto.setId(g.getId());
            dto.setType(g.getType());
            dto.setTargetValue(g.getTargetValue());
            dto.setCurrentValue(currentMinutes);
            dto.setStartDate(g.getStartDate());
            dto.setEndDate(g.getEndDate());
            dto.setIsActive(g.getIsActive());

            double pct = g.getTargetValue() > 0 ? ((double) currentMinutes / g.getTargetValue() * 100.0) : 0.0;
            dto.setProgressPercentage(Math.min(100.0, Math.round(pct * 10.0) / 10.0));
            dto.setCompleted(currentMinutes >= g.getTargetValue());

            result.add(dto);
        }
        return result;
    }

    private int calculateTodayMinutesForGoal(String type, LocalDate today, DailyStats stats) {
        if (type == null) return 0;
        switch (type.toLowerCase()) {
            case "screen_time":
                return stats != null ? (int) (stats.getTotalScreenTime() / 60) : 0;
            case "productive_time":
                return stats != null ? (int) (stats.getProductiveTime() / 60) : 0;
            case "focus_time":
                List<FocusSession> sessions = focusSessionRepository.findByStartTimeBetweenOrderByStartTimeDesc(
                    today.atStartOfDay(), today.atTime(LocalTime.MAX)
                );
                int focusMins = 0;
                for (FocusSession s : sessions) {
                    if (Boolean.TRUE.equals(s.getCompleted()) && s.getDurationMinutes() != null) {
                        focusMins += s.getDurationMinutes();
                    }
                }
                return focusMins;
            case "social_media":
            case "communication":
                List<UsageTracking> list = usageTrackingRepository.findTrackedUsageBetween(
                    today.atStartOfDay(), today.atTime(LocalTime.MAX)
                );
                long commSeconds = 0;
                for (UsageTracking u : list) {
                    if (u.getApplication() != null && u.getApplication().getCategory() != null) {
                        if ("Communication".equalsIgnoreCase(u.getApplication().getCategory().getName())) {
                            commSeconds += (u.getDurationSeconds() != null ? u.getDurationSeconds() : 0);
                        }
                    }
                }
                return (int) (commSeconds / 60);
            default:
                return 0;
        }
    }

    @Transactional
    public GoalDto createGoal(GoalDto dto) {
        Goal goal = new Goal();
        goal.setType(dto.getType());
        goal.setTargetValue(dto.getTargetValue());
        goal.setCurrentValue(0);
        goal.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now());
        goal.setEndDate(dto.getEndDate());
        goal.setIsActive(true);

        Goal saved = goalRepository.save(goal);
        dto.setId(saved.getId());
        dto.setCurrentValue(0);
        dto.setProgressPercentage(0.0);
        dto.setCompleted(false);
        return dto;
    }

    @Transactional
    public GoalDto updateGoal(Long id, GoalDto dto) {
        Goal goal = goalRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + id));

        if (dto.getType() != null) goal.setType(dto.getType());
        if (dto.getTargetValue() != null) goal.setTargetValue(dto.getTargetValue());
        if (dto.getStartDate() != null) goal.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) goal.setEndDate(dto.getEndDate());
        if (dto.getIsActive() != null) goal.setIsActive(dto.getIsActive());

        Goal saved = goalRepository.save(goal);
        dto.setId(saved.getId());
        return dto;
    }

    @Transactional
    public void deleteGoal(Long id) {
        goalRepository.deleteById(id);
    }
}
