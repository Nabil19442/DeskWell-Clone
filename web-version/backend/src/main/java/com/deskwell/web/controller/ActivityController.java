package com.deskwell.web.controller;

import com.deskwell.web.dto.ActivityRecordRequest;
import com.deskwell.web.model.UsageTracking;
import com.deskwell.web.service.ActivityTrackingService;
import com.deskwell.web.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityTrackingService activityTrackingService;
    private final SettingsService settingsService;

    public ActivityController(ActivityTrackingService activityTrackingService, SettingsService settingsService) {
        this.activityTrackingService = activityTrackingService;
        this.settingsService = settingsService;
    }

    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> recordActivity(
            @RequestHeader(value = "X-Agent-Key", required = false) String agentKey,
            @Valid @RequestBody ActivityRecordRequest request) {
        
        UsageTracking saved = activityTrackingService.recordActivity(request);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Activity logged successfully");
        resp.put("activeApp", activityTrackingService.getCurrentActiveApp());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, String>> getCurrentActivity() {
        Map<String, String> map = new HashMap<>();
        map.put("activeApp", activityTrackingService.getCurrentActiveApp());
        return ResponseEntity.ok(map);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTrackingStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("activeApp", activityTrackingService.getCurrentActiveApp());
        status.put("frequencySeconds", settingsService.getSetting("tracking_frequency_seconds", "3"));
        status.put("idleThresholdSeconds", settingsService.getSetting("idle_threshold_seconds", "60"));
        status.put("agentStatus", settingsService.getSetting("agent_status", "disconnected"));
        status.put("isBrowserOnly", true);
        status.put("browserNotice", "Web application does not have access to Win32 process APIs. Activity data is ingested via authenticated agent or manual records.");
        return ResponseEntity.ok(status);
    }
}
