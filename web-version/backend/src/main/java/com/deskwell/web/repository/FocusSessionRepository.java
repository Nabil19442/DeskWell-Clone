package com.deskwell.web.repository;

import com.deskwell.web.model.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    Optional<FocusSession> findFirstByStateInOrderByStartTimeDesc(List<String> states);
    List<FocusSession> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);
    List<FocusSession> findTop20ByOrderByStartTimeDesc();
}
