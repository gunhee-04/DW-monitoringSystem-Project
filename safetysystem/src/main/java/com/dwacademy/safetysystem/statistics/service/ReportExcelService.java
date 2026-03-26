package com.dwacademy.safetysystem.statistics.service;

import com.dwacademy.safetysystem.statistics.entity.CrowdStat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportExcelService {

    private final StatisticsService statisticsService;

    public ReportExcelService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    public byte[] generateCrowdStatExcel(Long cameraId, LocalDateTime start, LocalDateTime end) {
        List<CrowdStat> stats = statisticsService.findCrowdStats(cameraId, start, end);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Crowd Statistics");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // ===== 스타일 =====
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle labelStyle = workbook.createCellStyle();
            Font labelFont = workbook.createFont();
            labelFont.setBold(true);
            labelStyle.setFont(labelFont);
            labelStyle.setAlignment(HorizontalAlignment.CENTER);
            labelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            labelStyle.setBorderTop(BorderStyle.THIN);
            labelStyle.setBorderBottom(BorderStyle.THIN);
            labelStyle.setBorderLeft(BorderStyle.THIN);
            labelStyle.setBorderRight(BorderStyle.THIN);
            labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle infoValueStyle = workbook.createCellStyle();
            infoValueStyle.setAlignment(HorizontalAlignment.LEFT);
            infoValueStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            infoValueStyle.setBorderTop(BorderStyle.THIN);
            infoValueStyle.setBorderBottom(BorderStyle.THIN);
            infoValueStyle.setBorderLeft(BorderStyle.THIN);
            infoValueStyle.setBorderRight(BorderStyle.THIN);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle bodyCenterStyle = workbook.createCellStyle();
            bodyCenterStyle.setAlignment(HorizontalAlignment.CENTER);
            bodyCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bodyCenterStyle.setBorderTop(BorderStyle.THIN);
            bodyCenterStyle.setBorderBottom(BorderStyle.THIN);
            bodyCenterStyle.setBorderLeft(BorderStyle.THIN);
            bodyCenterStyle.setBorderRight(BorderStyle.THIN);

            CellStyle bodyRightStyle = workbook.createCellStyle();
            bodyRightStyle.setAlignment(HorizontalAlignment.RIGHT);
            bodyRightStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bodyRightStyle.setBorderTop(BorderStyle.THIN);
            bodyRightStyle.setBorderBottom(BorderStyle.THIN);
            bodyRightStyle.setBorderLeft(BorderStyle.THIN);
            bodyRightStyle.setBorderRight(BorderStyle.THIN);

            // ===== 제목 =====
            int rowNo = 0;

            Row titleRow = sheet.createRow(rowNo++);
            titleRow.setHeightInPoints(22);

            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Crowd Statistics Report");
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));

            rowNo++;

            // ===== 정보 영역 =====
            String cameraText = (cameraId != null) ? "CAM" + cameraId : "ALL";

            Row infoRow1 = sheet.createRow(rowNo++);
            createCell(infoRow1, 0, "Camera", labelStyle);
            createCell(infoRow1, 1, cameraText, infoValueStyle);
            createCell(infoRow1, 2, "Total Rows", labelStyle);
            createCell(infoRow1, 3, String.valueOf(stats.size()), infoValueStyle);

            Row infoRow2 = sheet.createRow(rowNo++);
            createCell(infoRow2, 0, "Start", labelStyle);
            createCell(infoRow2, 1, start != null ? start.format(formatter) : "-", infoValueStyle);
            createCell(infoRow2, 2, "End", labelStyle);
            createCell(infoRow2, 3, end != null ? end.format(formatter) : "-", infoValueStyle);

            rowNo++;

            // ===== 헤더 =====
            Row headerRow = sheet.createRow(rowNo++);
            String[] headers = {
                    "ID", "Camera", "Zone", "Measured At",
                    "People", "Density", "Increase", "Created At"
            };

            for (int i = 0; i < headers.length; i++) {
                createCell(headerRow, i, headers[i], headerStyle);
            }

            // ===== 데이터 =====
            for (CrowdStat stat : stats) {
                Row row = sheet.createRow(rowNo++);

                createCell(row, 0, valueOf(stat.getId()), bodyCenterStyle);
                createCell(row, 1, valueOf(stat.getCameraId()), bodyCenterStyle);
                createCell(row, 2, valueOf(stat.getDangerZoneId()), bodyCenterStyle);
                createCell(row, 3,
                        stat.getMeasuredAt() != null ? stat.getMeasuredAt().format(formatter) : "-",
                        bodyCenterStyle);
                createCell(row, 4, valueOf(stat.getPeopleCount()), bodyRightStyle);
                createCell(row, 5, formatDouble(stat.getDensityValue()), bodyRightStyle);
                createCell(row, 6, formatDouble(stat.getIncreaseRate()), bodyRightStyle);
                createCell(row, 7,
                        stat.getCreatedAt() != null ? stat.getCreatedAt().format(formatter) : "-",
                        bodyCenterStyle);
            }

            // ===== 컬럼 너비 =====
            sheet.setColumnWidth(0, 10 * 256); // ID
            sheet.setColumnWidth(1, 12 * 256); // Camera
            sheet.setColumnWidth(2, 12 * 256); // Zone
            sheet.setColumnWidth(3, 22 * 256); // Measured At
            sheet.setColumnWidth(4, 12 * 256); // People
            sheet.setColumnWidth(5, 12 * 256); // Density
            sheet.setColumnWidth(6, 12 * 256); // Increase
            sheet.setColumnWidth(7, 22 * 256); // Created At

            // 헤더 고정
            sheet.createFreezePane(0, 5);

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String valueOf(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatDouble(Double value) {
        return value == null ? "-" : String.format("%.2f", value);
    }
}