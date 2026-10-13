package com.deskwell.web.repository;

import com.deskwell.web.model.AgentInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentInstallationRepository extends JpaRepository<AgentInstallation, Long> {
    Optional<AgentInstallation> findByAgentId(String agentId);
    Optional<AgentInstallation> findByAgentToken(String agentToken);
    boolean existsByAgentToken(String agentToken);
}
