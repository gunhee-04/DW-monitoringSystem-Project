package com.dwacademy.safetysystem.statistics.service;

import com.dwacademy.safetysystem.statistics.entity.CrowdStat;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportPdfService {

    private final StatisticsService statisticsService;

    public ReportPdfService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    public byte[] generateCrowdStatPdf(Long cameraId, LocalDateTime start, LocalDateTime end) {
        List<CrowdStat> stats = statisticsService.findCrowdStats(cameraId, start, end);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(document, out);
            document.open();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // 1. 제목
            Paragraph title = new Paragraph("Crowd Statistics Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15f);
            document.add(title);

            // 2. 조건 정보 박스
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15f);
            infoTable.setWidths(new float[]{1.2f, 2.0f, 1.2f, 2.0f});

            addInfoCell(infoTable, "Camera", labelFont, Element.ALIGN_CENTER);
            addInfoCell(infoTable, cameraId != null ? "CAM" + cameraId : "ALL", bodyFont, Element.ALIGN_LEFT);

            addInfoCell(infoTable, "Total Rows", labelFont, Element.ALIGN_CENTER);
            addInfoCell(infoTable, String.valueOf(stats.size()), bodyFont, Element.ALIGN_LEFT);

            addInfoCell(infoTable, "Start", labelFont, Element.ALIGN_CENTER);
            addInfoCell(infoTable, start != null ? start.format(formatter) : "-", bodyFont, Element.ALIGN_LEFT);

            addInfoCell(infoTable, "End", labelFont, Element.ALIGN_CENTER);
            addInfoCell(infoTable, end != null ? end.format(formatter) : "-", bodyFont, Element.ALIGN_LEFT);

            document.add(infoTable);

            // 3. 섹션 제목
            Paragraph section = new Paragraph("Detailed Data", sectionFont);
            section.setSpacingAfter(8f);
            document.add(section);

            // 4. 데이터 표
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 1.0f, 1.1f, 1.9f, 1.0f, 1.0f, 1.0f, 1.9f});

            addHeaderCell(table, "ID");
            addHeaderCell(table, "Camera");
            addHeaderCell(table, "Zone");
            addHeaderCell(table, "Measured At");
            addHeaderCell(table, "People");
            addHeaderCell(table, "Density");
            addHeaderCell(table, "Increase");
            addHeaderCell(table, "Created At");

            for (CrowdStat stat : stats) {
                addBodyCell(table, valueOf(stat.getId()), smallFont, Element.ALIGN_CENTER);
                addBodyCell(table, valueOf(stat.getCameraId()), smallFont, Element.ALIGN_CENTER);
                addBodyCell(table, valueOf(stat.getDangerZoneId()), smallFont, Element.ALIGN_CENTER);
                addBodyCell(table,
                        stat.getMeasuredAt() != null ? stat.getMeasuredAt().format(formatter) : "",
                        smallFont, Element.ALIGN_CENTER);
                addBodyCell(table, valueOf(stat.getPeopleCount()), smallFont, Element.ALIGN_RIGHT);
                addBodyCell(table, formatDouble(stat.getDensityValue()), smallFont, Element.ALIGN_RIGHT);
                addBodyCell(table, formatDouble(stat.getIncreaseRate()), smallFont, Element.ALIGN_RIGHT);
                addBodyCell(table,
                        stat.getCreatedAt() != null ? stat.getCreatedAt().format(formatter) : "",
                        smallFont, Element.ALIGN_CENTER);
            }

            document.add(table);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(7f);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addInfoCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(7f);
        table.addCell(cell);
    }

    private String valueOf(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatDouble(Double value) {
        return value == null ? "-" : String.format("%.2f", value);
    }
}