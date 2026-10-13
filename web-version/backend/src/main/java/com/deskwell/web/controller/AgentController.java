package com.deskwell.web.controller;

import com.deskwell.web.dto.AgentActivitySliceDto;
import com.deskwell.web.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    private String extractToken(String xToken, String authHeader) {
        if (xToken != null && !xToken.isBlank()) {
            return xToken.trim();
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerAgent(@RequestBody Map<String, String> body) {
        String hostname = body.getOrDefault("hostname", body.getOrDefault("machineName", "unknown-host"));
        String osVersion = body.getOrDefault("osVersion", "Windows");
        return ResponseEntity.ok(agentService.registerAgent(hostname, osVersion));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(
            @RequestHeader(value = "X-Agent-Id", required = false) String headerAgentId,
            @RequestHeader(value = "X-DeskWell-Agent-Token", required = false) String xToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> body) {

        String token = extractToken(xToken, authHeader);
        if (token == null || !agentService.authenticate(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized agent token"));
        }

        Map<String, Object> reqBody = body != null ? body : Map.of();
        String agentId = (String) reqBody.getOrDefault("agentId", headerAgentId != null ? headerAgentId : "unknown-agent");
        String hostname = (String) reqBody.getOrDefault("hostname", reqBody.getOrDefault("machineName", "unknown"));
        String osVersion = (String) reqBody.getOrDefault("osVersion", "Windows");
        int pendingCount = reqBody.get("pendingCount") != null ? ((Number) reqBody.get("pendingCount")).intValue() : 0;

        agentService.processHeartbeat(agentId, token, hostname, osVersion, pendingCount);

        return ResponseEntity.ok(Map.of("status", "ACK", "message", "Heartbeat received"));
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncBatch(
            @RequestHeader(value = "X-Agent-Id", required = false) String agentId,
            @RequestHeader(value = "X-DeskWell-Agent-Token", required = false) String xToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody List<AgentActivitySliceDto> records) {

        String token = extractToken(xToken, authHeader);
        if (token == null || !agentService.authenticate(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or missing agent authorization token"));
        }

        Map<String, Object> result = agentService.syncBatch(token, records);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rules")
    public ResponseEntity<List<String>> getBlockingRules(
            @RequestHeader(value = "X-DeskWell-Agent-Token", required = false) String xToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractToken(xToken, authHeader);
        if (token == null || !agentService.authenticate(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> rules = agentService.getActiveBlockingRules(token);
        return ResponseEntity.ok(rules);
    }
}

