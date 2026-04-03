package com.dwacademy.safetysystem.statistics.controller;

import com.dwacademy.safetysystem.statistics.entity.ReportHistory;
import com.dwacademy.safetysystem.statistics.service.ReportHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports/history")
public class ReportHistoryController {

    private final ReportHistoryService reportHistoryService;

    public ReportHistoryController(ReportHistoryService reportHistoryService) {
        this.reportHistoryService = reportHistoryService;
    }

    @PostMapping
    public ReportHistory save(@RequestBody ReportHistory reportHistory) {
        return reportHistoryService.save(reportHistory);
    }

    @GetMapping
    public List<ReportHistory> findAll() {
        return reportHistoryService.findAll();
    }

    @GetMapping("/period")
    public List<ReportHistory> findByPeriod(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        return reportHistoryService.findByPeriod(start, end);
    }
}