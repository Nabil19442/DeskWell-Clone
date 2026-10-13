package com.deskwell.web.repository;

import com.deskwell.web.model.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
    Optional<DailyStats> findByDate(LocalDate date);
    List<DailyStats> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
}
