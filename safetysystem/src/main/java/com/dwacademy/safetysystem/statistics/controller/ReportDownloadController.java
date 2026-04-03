package com.dwacademy.safetysystem.statistics.controller;

import com.dwacademy.safetysystem.statistics.entity.ReportHistory;
import com.dwacademy.safetysystem.statistics.service.ReportExcelService;
import com.dwacademy.safetysystem.statistics.service.ReportHistoryService;
import com.dwacademy.safetysystem.statistics.service.ReportPdfService;
import com.dwacademy.safetysystem.member.entity.Member;
import com.dwacademy.safetysystem.member.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    public ReportDownloadController(ReportPdfService reportPdfService,
                                    ReportExcelService reportExcelService,
                                    ReportHistoryService reportHistoryService,
                                    MemberRepository memberRepository
    ) {
        this.reportPdfService = reportPdfService;
        this.reportExcelService = reportExcelService;
        this.reportHistoryService = reportHistoryService;
        this.memberRepository = memberRepository;
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
        Member member = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        history.setMember(member);
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
        Member member = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        history.setMember(member);
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