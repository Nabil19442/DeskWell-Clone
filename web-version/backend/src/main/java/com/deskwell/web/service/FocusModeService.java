package com.deskwell.web.service;

import com.deskwell.web.dto.FocusActionRequest;
import com.deskwell.web.dto.FocusStateDto;
import com.deskwell.web.model.FocusSession;
import com.deskwell.web.repository.FocusSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class FocusModeService {

    private final FocusSessionRepository focusSessionRepository;

    private int defaultFocusMinutes = 25;
    private int defaultBreakMinutes = 5;
    private int defaultLongBreakMinutes = 15;
    private int defaultSessionsBeforeLongBreak = 4;

    public FocusModeService(FocusSessionRepository focusSessionRepository) {
        this.focusSessionRepository = focusSessionRepository;
    }

    @Transactional
    public FocusStateDto getActiveFocusState() {
        Optional<FocusSession> activeOpt = focusSessionRepository.findFirstByStateInOrderByStartTimeDesc(
            List.of("FOCUS", "BREAK", "PAUSED")
        );

        long now = System.currentTimeMillis();

        if (activeOpt.isEmpty()) {
            return buildIdleState();
        }

        FocusSession session = activeOpt.get();

        if ("PAUSED".equals(session.getState())) {
            return toDto(session, session.getRemainingSeconds() != null ? session.getRemainingSeconds() : 0);
        }

        // Active FOCUS or BREAK: Compute server-authoritative remaining time
        if (session.getTargetEndEpochMillis() != null) {
            long remainingMillis = session.getTargetEndEpochMillis() - now;
            if (remainingMillis <= 0) {
                // Timer completed on server!
                return handleSessionCompletion(session);
            } else {
                int remainingSeconds = (int) (remainingMillis / 1000);
                session.setRemainingSeconds(remainingSeconds);
                focusSessionRepository.save(session);
                return toDto(session, remainingSeconds);
            }
        }

        return buildIdleState();
    }

    @Transactional
    public FocusStateDto startSession(FocusActionRequest req) {
        // Stop any existing active session to avoid duplicates
        focusSessionRepository.findFirstByStateInOrderByStartTimeDesc(List.of("FOCUS", "BREAK", "PAUSED"))
            .ifPresent(s -> {
                s.setState("IDLE");
                s.setEndTime(LocalDateTime.now());
                focusSessionRepository.save(s);
            });

        int duration = (req != null && req.getFocusDurationMinutes() != null && req.getFocusDurationMinutes() > 0)
            ? req.getFocusDurationMinutes()
            : defaultFocusMinutes;

        int breakMin = (req != null && req.getBreakDurationMinutes() != null && req.getBreakDurationMinutes() > 0)
            ? req.getBreakDurationMinutes()
            : defaultBreakMinutes;

        FocusSession session = new FocusSession();
        session.setStartTime(LocalDateTime.now());
        session.setDurationMinutes(duration);
        session.setBreakMinutes(breakMin);
        session.setState("FOCUS");
        session.setCompleted(false);
        session.setInterruptions(0);

        long targetEnd = System.currentTimeMillis() + ((long) duration * 60 * 1000);
        session.setTargetEndEpochMillis(targetEnd);
        session.setRemainingSeconds(duration * 60);

        FocusSession saved = focusSessionRepository.save(session);
        return toDto(saved, duration * 60);
    }

    @Transactional
    public FocusStateDto pauseSession() {
        Optional<FocusSession> activeOpt = focusSessionRepository.findFirstByStateInOrderByStartTimeDesc(
            List.of("FOCUS", "BREAK")
        );
        if (activeOpt.isEmpty()) return getActiveFocusState();

        FocusSession session = activeOpt.get();
        long now = System.currentTimeMillis();
        long remainingMillis = session.getTargetEndEpochMillis() != null ? (session.getTargetEndEpochMillis() - now) : 0;
        int remainingSeconds = Math.max(0, (int) (remainingMillis / 1000));

        session.setState("PAUSED");
        session.setRemainingSeconds(remainingSeconds);
        session.setInterruptions(session.getInterruptions() + 1);
        focusSessionRepository.save(session);

        return toDto(session, remainingSeconds);
    }

    @Transactional
    public FocusStateDto resumeSession() {
        Optional<FocusSession> activeOpt = focusSessionRepository.findFirstByStateInOrderByStartTimeDesc(
            List.of("PAUSED")
        );
        if (activeOpt.isEmpty()) return getActiveFocusState();

        FocusSession session = activeOpt.get();
        int remainingSeconds = session.getRemainingSeconds() != null ? session.getRemainingSeconds() : 0;
        long targetEnd = System.currentTimeMillis() + ((long) remainingSeconds * 1000);

        // Resume back to FOCUS or BREAK
        session.setState(session.getBreakMinutes() != null && session.getCompleted() ? "BREAK" : "FOCUS");
        session.setTargetEndEpochMillis(targetEnd);
        focusSessionRepository.save(session);

        return toDto(session, remainingSeconds);
    }

    @Transactional
    public FocusStateDto stopSession() {
        focusSessionRepository.findFirstByStateInOrderByStartTimeDesc(List.of("FOCUS", "BREAK", "PAUSED"))
            .ifPresent(s -> {
                s.setState("IDLE");
                s.setEndTime(LocalDateTime.now());
                s.setRemainingSeconds(0);
                focusSessionRepository.save(s);
            });
        return buildIdleState();
    }

    @Transactional
    public FocusStateDto skipBreak() {
        focusSessionRepository.findFirstByStateInOrderByStartTimeDesc(List.of("BREAK", "PAUSED"))
            .ifPresent(s -> {
                s.setState("IDLE");
                s.setEndTime(LocalDateTime.now());
                s.setRemainingSeconds(0);
                focusSessionRepository.save(s);
            });
        return buildIdleState();
    }

    private FocusStateDto handleSessionCompletion(FocusSession session) {
        if ("FOCUS".equals(session.getState())) {
            session.setCompleted(true);
            session.setEndTime(LocalDateTime.now());

            // Start break automatically
            session.setState("BREAK");
            int breakMin = session.getBreakMinutes() != null ? session.getBreakMinutes() : defaultBreakMinutes;
            long targetEnd = System.currentTimeMillis() + ((long) breakMin * 60 * 1000);
            session.setTargetEndEpochMillis(targetEnd);
            session.setRemainingSeconds(breakMin * 60);
            focusSessionRepository.save(session);
            return toDto(session, breakMin * 60);
        } else {
            // Break completed
            session.setState("COMPLETED");
            session.setRemainingSeconds(0);
            focusSessionRepository.save(session);
            return buildIdleState();
        }
    }

    private FocusStateDto buildIdleState() {
        FocusStateDto dto = new FocusStateDto();
        dto.setState("IDLE");
        dto.setRemainingSeconds(0);
        dto.setTotalDurationSeconds(defaultFocusMinutes * 60);
        dto.setFocusDurationMinutes(defaultFocusMinutes);
        dto.setBreakDurationMinutes(defaultBreakMinutes);
        dto.setLongBreakDurationMinutes(defaultLongBreakMinutes);
        dto.setSessionsBeforeLongBreak(defaultSessionsBeforeLongBreak);
        dto.setServerTimestamp(System.currentTimeMillis());
        populateTodayStats(dto);
        return dto;
    }

    private FocusStateDto toDto(FocusSession session, int remainingSeconds) {
        FocusStateDto dto = new FocusStateDto();
        dto.setSessionId(session.getId());
        dto.setState(session.getState());
        dto.setRemainingSeconds(remainingSeconds);
        dto.setTotalDurationSeconds(session.getDurationMinutes() * 60);
        dto.setFocusDurationMinutes(session.getDurationMinutes());
        dto.setBreakDurationMinutes(session.getBreakMinutes() != null ? session.getBreakMinutes() : defaultBreakMinutes);
        dto.setLongBreakDurationMinutes(defaultLongBreakMinutes);
        dto.setSessionsBeforeLongBreak(defaultSessionsBeforeLongBreak);
        dto.setInterruptions(session.getInterruptions());
        dto.setServerTimestamp(System.currentTimeMillis());
        dto.setTargetEndEpochMillis(session.getTargetEndEpochMillis());
        populateTodayStats(dto);
        return dto;
    }

    private void populateTodayStats(FocusStateDto dto) {
        LocalDate today = LocalDate.now();
        List<FocusSession> todaySessions = focusSessionRepository.findByStartTimeBetweenOrderByStartTimeDesc(
            today.atStartOfDay(), today.atTime(LocalTime.MAX)
        );

        int completed = 0;
        long totalMins = 0;
        for (FocusSession s : todaySessions) {
            if (Boolean.TRUE.equals(s.getCompleted())) {
                completed++;
                totalMins += (s.getDurationMinutes() != null ? s.getDurationMinutes() : 0);
            }
        }
        dto.setCompletedSessionsToday(completed);
        dto.setTotalFocusMinutesToday(totalMins);
    }

    public List<FocusSession> getRecentHistory() {
        return focusSessionRepository.findTop20ByOrderByStartTimeDesc();
    }
}
