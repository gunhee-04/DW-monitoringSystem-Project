package com.dwacademy.safetysystem.statistics.service;

import com.dwacademy.safetysystem.statistics.dto.ChartDataDto;
import com.dwacademy.safetysystem.statistics.dto.HourlyStatDto;
import com.dwacademy.safetysystem.statistics.dto.SummaryStatDto;
import com.dwacademy.safetysystem.statistics.entity.CrowdStat;
import com.dwacademy.safetysystem.statistics.repository.CrowdStatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final CrowdStatRepository crowdStatRepository;

    public StatisticsService(CrowdStatRepository crowdStatRepository) {
        this.crowdStatRepository = crowdStatRepository;
    }

    public String test() {
        return "statistics ok";
    }

    public CrowdStat save(CrowdStat crowdStat) {
        crowdStat.setCreatedAt(LocalDateTime.now());
        return crowdStatRepository.save(crowdStat);
    }

    public List<CrowdStat> findAll() {
        return crowdStatRepository.findAll();
    }

    public List<CrowdStat> findByPeriod(LocalDateTime start, LocalDateTime end) {
        return crowdStatRepository.findByMeasuredAtBetween(start, end);
    }

    public List<CrowdStat> findByCameraId(Long cameraId) {
        return crowdStatRepository.findByCameraId(cameraId);
    }

    public List<CrowdStat> findByDangerZoneId(Long dangerZoneId) {
        return crowdStatRepository.findByDangerZoneId(dangerZoneId);
    }

    // 추가: cameraId 있으면 카메라 기준, 없으면 전체
    public List<CrowdStat> findCrowdStats(Long cameraId, LocalDateTime start, LocalDateTime end) {
        if (cameraId != null && start != null && end != null) {
            return crowdStatRepository.findByCameraIdAndMeasuredAtBetween(cameraId, start, end);
        }
        if (cameraId != null) {
            return crowdStatRepository.findByCameraId(cameraId);
        }
        if (start != null && end != null) {
            return crowdStatRepository.findByMeasuredAtBetween(start, end);
        }
        return crowdStatRepository.findAll();
    }

    public List<HourlyStatDto> getHourlyStatistics(Long cameraId, LocalDateTime start, LocalDateTime end) {
        List<CrowdStat> stats = findCrowdStats(cameraId, start, end);

        Map<Integer, List<CrowdStat>> groupedByHour = stats.stream()
                .collect(Collectors.groupingBy(
                        stat -> stat.getMeasuredAt().getHour(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<HourlyStatDto> result = new ArrayList<>();

        for (Map.Entry<Integer, List<CrowdStat>> entry : groupedByHour.entrySet()) {
            int hour = entry.getKey();
            List<CrowdStat> list = entry.getValue();

            int totalPeopleCount = list.stream()
                    .mapToInt(CrowdStat::getPeopleCount)
                    .sum();

            double averageDensityValue = list.stream()
                    .mapToDouble(CrowdStat::getDensityValue)
                    .average()
                    .orElse(0.0);

            result.add(new HourlyStatDto(
                    String.format("%02d:00", hour),
                    totalPeopleCount,
                    averageDensityValue
            ));
        }

        return result;
    }

    public List<ChartDataDto> getChartData(Long cameraId, LocalDateTime start, LocalDateTime end) {
        List<CrowdStat> stats = findCrowdStats(cameraId, start, end);

        Map<Integer, Integer> groupedByHour = stats.stream()
                .collect(Collectors.groupingBy(
                        stat -> stat.getMeasuredAt().getHour(),
                        TreeMap::new,
                        Collectors.summingInt(CrowdStat::getPeopleCount)
                ));

        List<ChartDataDto> result = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : groupedByHour.entrySet()) {
            result.add(new ChartDataDto(
                    String.format("%02d:00", entry.getKey()),
                    entry.getValue()
            ));
        }

        return result;
    }

    public SummaryStatDto getSummaryStatistics(Long cameraId, LocalDateTime start, LocalDateTime end) {
        List<CrowdStat> stats = findCrowdStats(cameraId, start, end);

        int totalPeopleCount = stats.stream()
                .mapToInt(CrowdStat::getPeopleCount)
                .sum();

        double averageDensityValue = stats.stream()
                .mapToDouble(CrowdStat::getDensityValue)
                .average()
                .orElse(0.0);

        int maxPeopleCount = stats.stream()
                .mapToInt(CrowdStat::getPeopleCount)
                .max()
                .orElse(0);

        double averageIncreaseRate = stats.stream()
                .filter(stat -> stat.getIncreaseRate() != null)
                .mapToDouble(CrowdStat::getIncreaseRate)
                .average()
                .orElse(0.0);

        return new SummaryStatDto(
                totalPeopleCount,
                averageDensityValue,
                maxPeopleCount,
                averageIncreaseRate
        );
    }
}