package com.deskwell.web.service;

import com.deskwell.web.dto.DashboardStatsDto;
import com.deskwell.web.dto.ReportDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ExportService {

    public byte[] generateCsvReport(ReportDto report) {
        StringBuilder sb = new StringBuilder();
        sb.append("DeskWell ").append(report.getTitle()).append("\n");
        sb.append("Period: ").append(report.getPeriodLabel()).append("\n\n");

        sb.append("SUMMARY\n");
        sb.append("Total Screen Time (hours),").append(String.format("%.2f", report.getTotalScreenTimeSeconds() / 3600.0)).append("\n");
        sb.append("Productive Time (hours),").append(String.format("%.2f", report.getProductiveTimeSeconds() / 3600.0)).append("\n");
        sb.append("Idle Time (hours),").append(String.format("%.2f", report.getIdleTimeSeconds() / 3600.0)).append("\n");
        sb.append("Sessions Count,").append(report.getSessionCount()).append("\n");
        sb.append("Productivity Score,").append(String.format("%.1f%%", report.getProductivityScore())).append("\n\n");

        sb.append("APPLICATION USAGE\n");
        sb.append("Application,Category,Sessions,Time (hours)\n");
        if (report.getTopApplications() != null) {
            for (DashboardStatsDto.AppUsageDto app : report.getTopApplications()) {
                sb.append("\"").append(app.getAppName().replace("\"", "\"\"")).append("\",")
                  .append("\"").append(app.getCategory()).append("\",")
                  .append(app.getSessionCount()).append(",")
                  .append(String.format("%.2f", app.getTotalSeconds() / 3600.0)).append("\n");
            }
        }
        sb.append("\n");

        sb.append("CATEGORY BREAKDOWN\n");
        sb.append("Category,Time (hours),Percentage\n");
        if (report.getCategoryBreakdown() != null) {
            for (DashboardStatsDto.CategoryUsageDto cat : report.getCategoryBreakdown()) {
                sb.append("\"").append(cat.getCategoryName()).append("\",")
                  .append(String.format("%.2f", cat.getTotalSeconds() / 3600.0)).append(",")
                  .append(String.format("%.1f%%", cat.getPercentage())).append("\n");
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generatePdfReport(ReportDto report) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - 50;
                float margin = 50;

                // Header
                content.setNonStrokingColor(37 / 255f, 99 / 255f, 235 / 255f); // Primary blue
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                content.newLineAtOffset(margin, y);
                content.showText("DeskWell Productivity Report");
                content.endText();
                y -= 25;

                // Subtitle
                content.setNonStrokingColor(100 / 255f, 116 / 255f, 139 / 255f);
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(margin, y);
                content.showText(report.getTitle() + " - " + report.getPeriodLabel());
                content.endText();
                y -= 35;

                // Section: Executive Summary
                content.setNonStrokingColor(15 / 255f, 23 / 255f, 42 / 255f);
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                content.newLineAtOffset(margin, y);
                content.showText("Executive Summary");
                content.endText();
                y -= 20;

                String[][] summaryRows = {
                    {"Total Screen Time:", String.format("%.1f hours", report.getTotalScreenTimeSeconds() / 3600.0)},
                    {"Productive Time:", String.format("%.1f hours", report.getProductiveTimeSeconds() / 3600.0)},
                    {"Idle Time:", String.format("%.1f hours", report.getIdleTimeSeconds() / 3600.0)},
                    {"Productivity Score:", String.format("%.1f%%", report.getProductivityScore())},
                    {"Total Sessions:", String.valueOf(report.getSessionCount())}
                };

                for (String[] row : summaryRows) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                    content.newLineAtOffset(margin + 10, y);
                    content.showText(row[0] + "  " + row[1]);
                    content.endText();
                    y -= 18;
                }
                y -= 20;

                // Section: Top Applications
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                content.newLineAtOffset(margin, y);
                content.showText("Top Applications Used");
                content.endText();
                y -= 22;

                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                content.newLineAtOffset(margin, y);
                content.showText(String.format("%-30s %-20s %-10s %-15s", "Application", "Category", "Sessions", "Duration"));
                content.endText();
                y -= 15;

                if (report.getTopApplications() != null) {
                    int count = 0;
                    for (DashboardStatsDto.AppUsageDto app : report.getTopApplications()) {
                        if (count++ >= 8 || y < 60) break;
                        String cleanName = app.getAppName().length() > 25 ? app.getAppName().substring(0, 22) + "..." : app.getAppName();
                        String durationStr = String.format("%.1f hrs", app.getTotalSeconds() / 3600.0);
                        content.beginText();
                        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                        content.newLineAtOffset(margin, y);
                        content.showText(String.format("%-30s %-20s %-10s %-15s", cleanName, app.getCategory(), app.getSessionCount(), durationStr));
                        content.endText();
                        y -= 16;
                    }
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
