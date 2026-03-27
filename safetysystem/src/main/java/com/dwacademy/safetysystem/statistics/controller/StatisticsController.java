package com.dwacademy.safetysystem.statistics.controller;

import com.dwacademy.safetysystem.detection_event.dto.DangerHourlyResponseDto;
import com.dwacademy.safetysystem.detection_event.dto.DangerSummaryResponseDto;
import com.dwacademy.safetysystem.statistics.dto.HourlyStatDto;
import com.dwacademy.safetysystem.statistics.dto.SummaryStatDto;
import com.dwacademy.safetysystem.statistics.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/crowd/summary")
    public SummaryStatDto getSummaryStatistics(
            @RequestParam(required = false) Long cameraId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        return statisticsService.getSummaryStatistics(cameraId, start, end);
    }

    @GetMapping("/crowd/hourly")
    public List<HourlyStatDto> getHourlyStatistics(
            @RequestParam(required = false) Long cameraId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        return statisticsService.getHourlyStatistics(cameraId, start, end);
    }

    @GetMapping("/danger/summary")
    public DangerSummaryResponseDto getDangerSummary(
            @RequestParam(required = false) Long cameraId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        return statisticsService.getDangerSummary(cameraId, start, end);
    }

    @GetMapping("/danger/hourly")
    public List<DangerHourlyResponseDto> getDangerHourly(
            @RequestParam(required = false) Long cameraId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        return statisticsService.getDangerHourly(cameraId, start, end);
    }
}