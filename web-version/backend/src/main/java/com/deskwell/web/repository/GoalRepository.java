package com.deskwell.web.repository;

import com.deskwell.web.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByIsActiveTrueOrderByCreatedAtDesc();
}
