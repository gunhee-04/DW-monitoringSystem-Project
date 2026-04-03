package com.dwacademy.safetysystem.statistics.controller;

import com.dwacademy.safetysystem.statistics.entity.ReportHistory;
import com.dwacademy.safetysystem.statistics.service.ReportExcelService;
import com.dwacademy.safetysystem.statistics.service.ReportHistoryService;
import com.dwacademy.safetysystem.statistics.service.ReportPdfService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
public class ReportDownloadController {

    private final ReportPdfService reportPdfService;
    private final ReportExcelService reportExcelService;
    private final ReportHistoryService reportHistoryService;

    public ReportDownloadController(ReportPdfService reportPdfService,
                                    ReportExcelService reportExcelService,
                                    ReportHistoryService reportHistoryService) {
        this.reportPdfService = reportPdfService;
        this.reportExcelService = reportExcelService;
        this.reportHistoryService = reportHistoryService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam(required = false) Long cameraId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        byte[] pdfBytes = reportPdfService.generateCrowdStatPdf(cameraId, start, end);

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String camera = (cameraId != null) ? "CAM" + cameraId : "ALL";
        String fileName = "crowd_report_" + camera + "_" + date + ".pdf";

        ReportHistory history = new ReportHistory();
        history.setMemberId(1L); // 로그인 연동 전까지 임시
        history.setReportType("PDF");
        history.setCategory("CROWD_STAT");
        history.setStartDate(start);
        history.setEndDate(end);
        history.setFileName(fileName);
        reportHistoryService.save(history);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam(required = false) Long cameraId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        byte[] excelBytes = reportExcelService.generateCrowdStatExcel(cameraId, start, end);

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String camera = (cameraId != null) ? "CAM" + cameraId : "ALL";
        String fileName = "crowd_report_" + camera + "_" + date + ".xlsx";

        ReportHistory history = new ReportHistory();
        history.setMemberId(1L);
        history.setReportType("EXCEL");
        history.setCategory("CROWD_STAT");
        history.setStartDate(start);
        history.setEndDate(end);
        history.setFileName(fileName);
        reportHistoryService.save(history);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }
}