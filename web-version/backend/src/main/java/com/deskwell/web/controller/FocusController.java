package com.deskwell.web.controller;

import com.deskwell.web.dto.FocusActionRequest;
import com.deskwell.web.dto.FocusStateDto;
import com.deskwell.web.model.FocusSession;
import com.deskwell.web.service.FocusModeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/focus-sessions")
public class FocusController {

    private final FocusModeService focusModeService;

    public FocusController(FocusModeService focusModeService) {
        this.focusModeService = focusModeService;
    }

    @GetMapping("/active")
    public ResponseEntity<FocusStateDto> getActiveSession() {
        return ResponseEntity.ok(focusModeService.getActiveFocusState());
    }

    @PostMapping("/start")
    public ResponseEntity<FocusStateDto> startSession(@RequestBody(required = false) FocusActionRequest req) {
        return ResponseEntity.ok(focusModeService.startSession(req));
    }

    @PostMapping("/pause")
    public ResponseEntity<FocusStateDto> pauseSession() {
        return ResponseEntity.ok(focusModeService.pauseSession());
    }

    @PostMapping("/resume")
    public ResponseEntity<FocusStateDto> resumeSession() {
        return ResponseEntity.ok(focusModeService.resumeSession());
    }

    @PostMapping("/stop")
    public ResponseEntity<FocusStateDto> stopSession() {
        return ResponseEntity.ok(focusModeService.stopSession());
    }

    @PostMapping("/skip-break")
    public ResponseEntity<FocusStateDto> skipBreak() {
        return ResponseEntity.ok(focusModeService.skipBreak());
    }

    @GetMapping("/history")
    public ResponseEntity<List<FocusSession>> getHistory() {
        return ResponseEntity.ok(focusModeService.getRecentHistory());
    }
}
