package com.deskwell.web.repository;

import com.deskwell.web.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findFirstByIsActiveTrueOrderByStartTimeDesc();
    List<Session> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);
}
