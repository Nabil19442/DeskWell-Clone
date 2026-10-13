package com.deskwell.web.controller;

import com.deskwell.web.dto.ReportDto;
import com.deskwell.web.service.ExportService;
import com.deskwell.web.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/exports")
public class ExportController {

    private final ReportService reportService;
    private final ExportService exportService;

    public ExportController(ReportService reportService, ExportService exportService) {
        this.reportService = reportService;
        this.exportService = exportService;
    }

    @GetMapping("/csv/daily")
    public ResponseEntity<byte[]> exportDailyCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        ReportDto report = reportService.getDailyReport(targetDate);
        byte[] csv = exportService.generateCsvReport(report);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"deskwell_daily_" + targetDate + ".csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/csv/weekly")
    public ResponseEntity<byte[]> exportWeeklyCsv(@RequestParam(defaultValue = "0") int weekOffset) {
        ReportDto report = reportService.getWeeklyReport(weekOffset);
        byte[] csv = exportService.generateCsvReport(report);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"deskwell_weekly_offset_" + weekOffset + ".csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/pdf/daily")
    public ResponseEntity<byte[]> exportDailyPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        ReportDto report = reportService.getDailyReport(targetDate);
        byte[] pdf = exportService.generatePdfReport(report);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"deskwell_daily_" + targetDate + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/pdf/weekly")
    public ResponseEntity<byte[]> exportWeeklyPdf(@RequestParam(defaultValue = "0") int weekOffset) throws IOException {
        ReportDto report = reportService.getWeeklyReport(weekOffset);
        byte[] pdf = exportService.generatePdfReport(report);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"deskwell_weekly_offset_" + weekOffset + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
