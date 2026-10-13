package com.deskwell.web.controller;

import com.deskwell.web.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllSettings() {
        return ResponseEntity.ok(settingsService.getAllSettings());
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody Map<String, String> newSettings) {
        return ResponseEntity.ok(settingsService.updateBulkSettings(newSettings));
    }

    @PutMapping("/{key}")
    public ResponseEntity<Map<String, String>> updateSingleSetting(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        String value = body.get("value");
        settingsService.setSetting(key, value);
        return ResponseEntity.ok(settingsService.getAllSettings());
    }
}
