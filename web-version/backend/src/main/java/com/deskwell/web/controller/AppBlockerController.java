package com.deskwell.web.controller;

import com.deskwell.web.model.BlockedApp;
import com.deskwell.web.service.AppBlockerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app-blocker")
public class AppBlockerController {

    private final AppBlockerService appBlockerService;

    public AppBlockerController(AppBlockerService appBlockerService) {
        this.appBlockerService = appBlockerService;
    }

    @GetMapping("/rules")
    public ResponseEntity<List<BlockedApp>> getRules() {
        return ResponseEntity.ok(appBlockerService.getAllBlockedApps());
    }

    @PostMapping("/rules")
    public ResponseEntity<BlockedApp> addRule(@RequestBody Map<String, Object> payload) {
        String appName = (String) payload.get("appName");
        boolean isPermanent = Boolean.TRUE.equals(payload.get("isPermanent"));
        String notes = (String) payload.get("notes");
        return ResponseEntity.ok(appBlockerService.addBlockedApp(appName, isPermanent, notes));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> removeRule(@PathVariable Long id) {
        appBlockerService.removeBlockedApp(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(appBlockerService.getEnforcementStatus());
    }
}
