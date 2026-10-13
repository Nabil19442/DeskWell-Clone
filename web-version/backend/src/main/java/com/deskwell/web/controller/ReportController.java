package com.deskwell.web.controller;

import com.deskwell.web.dto.ReportDto;
import com.deskwell.web.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    public ResponseEntity<ReportDto> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(reportService.getDailyReport(targetDate));
    }

    @GetMapping("/weekly")
    public ResponseEntity<ReportDto> getWeeklyReport(
            @RequestParam(defaultValue = "0") int weekOffset) {
        return ResponseEntity.ok(reportService.getWeeklyReport(weekOffset));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ReportDto> getMonthlyReport(
            @RequestParam(defaultValue = "0") int monthOffset) {
        return ResponseEntity.ok(reportService.getMonthlyReport(monthOffset));
    }
}
