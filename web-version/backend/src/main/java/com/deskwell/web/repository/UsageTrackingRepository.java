package com.deskwell.web.repository;

import com.deskwell.web.model.UsageTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageTrackingRepository extends JpaRepository<UsageTracking, Long> {

    boolean existsBySyncId(String syncId);
    java.util.Optional<UsageTracking> findBySyncId(String syncId);

    List<UsageTracking> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT u FROM UsageTracking u WHERE u.startTime >= :start AND u.startTime < :end ORDER BY u.startTime ASC")
    List<UsageTracking> findTrackedUsageBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u.application.name, u.application.category.name, u.application.category.color, SUM(u.durationSeconds), COUNT(u.id) " +
           "FROM UsageTracking u " +
           "WHERE u.startTime >= :start AND u.startTime < :end " +
           "GROUP BY u.application.name, u.application.category.name, u.application.category.color " +
           "ORDER BY SUM(u.durationSeconds) DESC")
    List<Object[]> aggregateAppUsageBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u.application.category.name, u.application.category.color, SUM(u.durationSeconds) " +
           "FROM UsageTracking u " +
           "WHERE u.startTime >= :start AND u.startTime < :end " +
           "GROUP BY u.application.category.name, u.application.category.color " +
           "ORDER BY SUM(u.durationSeconds) DESC")
    List<Object[]> aggregateCategoryUsageBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
