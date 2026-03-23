package com.dwacademy.safetysystem.statistics.service;

import com.dwacademy.safetysystem.statistics.dto.ChartDataDto;
import com.dwacademy.safetysystem.statistics.dto.HourlyStatDto;
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

    public List<HourlyStatDto> getHourlyStatistics(LocalDateTime start, LocalDateTime end) {
        List<CrowdStat> stats = crowdStatRepository.findByMeasuredAtBetween(start, end);

        Map<Integer, List<CrowdStat>> groupedByHour = stats.stream()
                .collect(Collectors.groupingBy(stat -> stat.getMeasuredAt().getHour(), TreeMap::new, Collectors.toList()));

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

    public List<ChartDataDto> getChartData(LocalDateTime start, LocalDateTime end) {
        List<CrowdStat> stats = crowdStatRepository.findByMeasuredAtBetween(start, end);

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
}