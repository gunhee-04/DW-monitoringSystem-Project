package com.dwacademy.safetysystem.statistics.controller;

import com.dwacademy.safetysystem.statistics.dto.ChartDataDto;
import com.dwacademy.safetysystem.statistics.dto.HourlyStatDto;
import com.dwacademy.safetysystem.statistics.dto.SummaryStatDto;
import com.dwacademy.safetysystem.statistics.entity.CrowdStat;
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

    @GetMapping("/test")
    public String test() {
        return statisticsService.test();
    }

    @PostMapping("/crowd")
    public CrowdStat save(@RequestBody CrowdStat crowdStat) {
        return statisticsService.save(crowdStat);
    }

    @GetMapping("/crowd")
    public List<CrowdStat> findAll() {
        return statisticsService.findAll();
    }

    @GetMapping("/crowd/period")
    public List<CrowdStat> findByPeriod(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        return statisticsService.findByPeriod(start, end);
    }

    @GetMapping("/crowd/camera")
    public List<CrowdStat> findByCameraId(@RequestParam Long cameraId) {
        return statisticsService.findByCameraId(cameraId);
    }

    @GetMapping("/crowd/zone")
    public List<CrowdStat> findByZone(@RequestParam Long dangerZoneId) {
        return statisticsService.findByDangerZoneId(dangerZoneId);
    }

    @GetMapping("/crowd/hourly")
    public List<HourlyStatDto> getHourlyStatistics(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        return statisticsService.getHourlyStatistics(start, end);
    }

    @GetMapping("/crowd/chart")
    public List<ChartDataDto> getChartData(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        return statisticsService.getChartData(start, end);
    }

    @GetMapping("/crowd/summary")
    public SummaryStatDto getSummaryStatistics(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        return statisticsService.getSummaryStatistics(start, end);
    }

}