package com.deskwell.web.service;

import com.deskwell.web.dto.AgentActivitySliceDto;
import com.deskwell.web.model.Application;
import com.deskwell.web.model.BlockedApp;
import com.deskwell.web.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:test_agent_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class AgentServiceTest {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentInstallationRepository agentInstallationRepository;

    @Autowired
    private UsageTrackingRepository usageTrackingRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private BlockedAppRepository blockedAppRepository;

    private String validToken;
    private String agentId;

    @BeforeEach
    public void setup() {
        Map<String, Object> reg = agentService.registerAgent("TEST-WORKSTATION", "Windows 11 Enterprise");
        agentId = (String) reg.get("agentId");
        validToken = (String) reg.get("agentToken");
    }

    @Test
    public void testRegistrationAndAuthentication() {
        assertNotNull(agentId);
        assertNotNull(validToken);
        assertTrue(agentService.authenticate(validToken));
        assertFalse(agentService.authenticate("bad-fake-token"));
        assertFalse(agentService.authenticate(""));
        assertFalse(agentService.authenticate(null));
    }

    @Test
    public void testHeartbeatUpdatesStatus() {
        assertDoesNotThrow(() -> {
            agentService.processHeartbeat(agentId, validToken, "TEST-WORKSTATION", "Windows 11 Enterprise", 0);
        });

        assertTrue(agentInstallationRepository.findByAgentId(agentId).isPresent());
        assertTrue(agentInstallationRepository.findByAgentId(agentId).get().getIsActive());
    }

    @Test
    public void testSyncBatchAndIdempotency() {
        String syncId1 = UUID.randomUUID().toString();
        String syncId2 = UUID.randomUUID().toString();

        AgentActivitySliceDto slice1 = new AgentActivitySliceDto();
        slice1.setSyncId(syncId1);
        slice1.setProcessName("Code.exe");
        slice1.setWindowTitle("DeskWell - VS Code");
        slice1.setStartTime(LocalDateTime.now().minusMinutes(10).toString());
        slice1.setEndTime(LocalDateTime.now().minusMinutes(5).toString());
        slice1.setDurationSeconds(300);
        slice1.setIdle(false);

        AgentActivitySliceDto slice2 = new AgentActivitySliceDto();
        slice2.setSyncId(syncId2);
        slice2.setProcessName("chrome.exe");
        slice2.setWindowTitle("DeskWell Dashboard");
        slice2.setStartTime(LocalDateTime.now().minusMinutes(5).toString());
        slice2.setEndTime(LocalDateTime.now().toString());
        slice2.setDurationSeconds(300);
        slice2.setIdle(false);

        // First synchronization: both must be ingested
        Map<String, Object> res1 = agentService.syncBatch(validToken, List.of(slice1, slice2));
        assertEquals("SUCCESS", res1.get("status"));
        assertEquals(2, ((Number) res1.get("syncedCount")).intValue());
        assertEquals(0, ((Number) res1.get("duplicateSkippedCount")).intValue());

        assertTrue(usageTrackingRepository.existsBySyncId(syncId1));
        assertTrue(usageTrackingRepository.existsBySyncId(syncId2));

        // Resend identical batch: idempotency must trigger and duplicates skipped
        Map<String, Object> res2 = agentService.syncBatch(validToken, List.of(slice1, slice2));
        assertEquals("SUCCESS", res2.get("status"));
        assertEquals(0, ((Number) res2.get("syncedCount")).intValue());
        assertEquals(2, ((Number) res2.get("duplicateSkippedCount")).intValue());
    }

    @Test
    public void testGetActiveBlockingRules() {
        Application steamApp = applicationRepository.findByNameIgnoreCase("Steam.exe")
                .orElseGet(() -> applicationRepository.save(new Application("Steam.exe", "C:\\Steam.exe", null)));

        blockedAppRepository.save(new BlockedApp(steamApp, true));

        List<String> rules = agentService.getActiveBlockingRules(validToken);
        assertNotNull(rules);
        assertTrue(rules.contains("Steam.exe"));
    }
}
