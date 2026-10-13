package com.deskwell.web.controller;

import com.deskwell.web.dto.DashboardStatsDto;
import com.deskwell.web.service.ActivityTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ActivityTrackingService activityTrackingService;

    public DashboardController(ActivityTrackingService activityTrackingService) {
        this.activityTrackingService = activityTrackingService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(activityTrackingService.getDashboardStats());
    }
}
