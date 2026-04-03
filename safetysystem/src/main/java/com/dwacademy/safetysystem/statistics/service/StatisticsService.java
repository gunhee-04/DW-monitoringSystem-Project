package com.dwacademy.safetysystem.statistics.service;

import com.dwacademy.safetysystem.detection_event.dto.DangerHourlyResponseDto;
import com.dwacademy.safetysystem.detection_event.dto.DangerSummaryResponseDto;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import com.dwacademy.safetysystem.statistics.dto.HourlyStatDto;
import com.dwacademy.safetysystem.statistics.dto.SummaryStatDto;
import com.dwacademy.safetysystem.statistics.entity.CrowdStat;
import com.dwacademy.safetysystem.statistics.repository.CrowdStatRepository;
import org.springframework.stereotype.Service;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.domain.EventType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final CrowdStatRepository crowdStatRepository;
    private final DetectionEventRepository detectionEventRepository;

    public StatisticsService(CrowdStatRepository crowdStatRepository,
                             DetectionEventRepository detectionEventRepository) {
        this.crowdStatRepository = crowdStatRepository;
        this.detectionEventRepository = detectionEventRepository;
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start/end 필수값");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start는 end보다 늦을 수 없습니다.");
        }
    }

    private LocalDateTime toSeoulDateTime(Long epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return LocalDateTime.now();
        }

        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(epochSeconds),
                java.time.ZoneId.of("Asia/Seoul")
        );
    }

    private List<CrowdStat> getStats(Long cameraId, LocalDateTime start, LocalDateTime end) {
        validateRange(start, end);

        if (cameraId != null) {
            return crowdStatRepository.findByCameraIdAndMeasuredAtBetween(cameraId, start, end);
        }

        return crowdStatRepository.findByMeasuredAtBetween(start, end);
    }

    public List<CrowdStat> findCrowdStats(Long cameraId, LocalDateTime start, LocalDateTime end) {
        return getStats(cameraId, start, end);
    }

    public void saveFromDetection(DetectionEntity event) {
        if (event == null) return;
        if (event.getEventType() == null || event.getEventType() != EventType.CROWD) return;

        CrowdStat stat = new CrowdStat();
        stat.setCameraId(event.getCameraId() != null ? event.getCameraId().longValue() : 0L);
        stat.setDangerZoneId(event.getDangerZoneId() != null ? event.getDangerZoneId().longValue() : null);
        stat.setMeasuredAt(toSeoulDateTime(event.getEventTime()));
        stat.setPeopleCount(event.getDetectedCount() != null ? event.getDetectedCount() : 0);
        stat.setDensityValue((double) stat.getPeopleCount());
        stat.setIncreaseRate(0.0);
        stat.setCreatedAt(LocalDateTime.now());

        crowdStatRepository.save(stat);
    }

    public DangerSummaryResponseDto getDangerSummary(Long cameraId, LocalDateTime start, LocalDateTime end) {
        List<DetectionEntity> events = getDangerEvents(cameraId, start, end);

        long totalIntrusion = events.size();

        Map<Integer, Long> hourlyCount = events.stream()
                .filter(e -> e.getEventTime() != null)
                .collect(Collectors.groupingBy(
                        e -> toSeoulDateTime(e.getEventTime()).getHour(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        long maxHourlyIntrusion = hourlyCount.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        double averageStayDuration = events.stream()
                .map(DetectionEntity::getStayDurationSec)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return new DangerSummaryResponseDto(totalIntrusion, maxHourlyIntrusion, averageStayDuration);
    }

    public List<DangerHourlyResponseDto> getDangerHourly(Long cameraId, LocalDateTime start, LocalDateTime end) {
        List<DetectionEntity> events = getDangerEvents(cameraId, start, end);

        Map<Integer, List<DetectionEntity>> grouped = events.stream()
                .filter(e -> e.getEventTime() != null)
                .collect(Collectors.groupingBy(
                        e -> toSeoulDateTime(e.getEventTime()).getHour(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<DangerHourlyResponseDto> result = new ArrayList<>();
        long cumulativeIntrusionCount = 0L;

        for (Map.Entry<Integer, List<DetectionEntity>> entry : grouped.entrySet()) {
            List<DetectionEntity> list = entry.getValue();

            long intrusionCount = list.size();

            double averageStayDuration = list.stream()
                    .map(DetectionEntity::getStayDurationSec)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            cumulativeIntrusionCount += intrusionCount;

            result.add(new DangerHourlyResponseDto(
                    String.format("%02d:00", entry.getKey()),
                    intrusionCount,
                    averageStayDuration,
                    cumulativeIntrusionCount
            ));
        }

        return result;
    }

    private List<DetectionEntity> getDangerEvents(Long cameraId, LocalDateTime start, LocalDateTime end) {
        validateRange(start, end);

        long startEpoch = start.atZone(java.time.ZoneId.of("Asia/Seoul")).toEpochSecond();
        long endEpoch = end.atZone(java.time.ZoneId.of("Asia/Seoul")).toEpochSecond();

        if (cameraId != null) {
            return detectionEventRepository.findByCameraIdAndEventTypeAndEventTimeBetweenOrderByIdDesc(
                    cameraId.intValue(),
                    EventType.INTRUSION,
                    startEpoch,
                    endEpoch
            );
        }

        return detectionEventRepository.findByEventTypeAndEventTimeBetweenOrderByIdDesc(
                EventType.INTRUSION,
                startEpoch,
                endEpoch
        );
    }

    public SummaryStatDto getSummaryStatistics(Long cameraId, LocalDateTime start, LocalDateTime end) {

        List<CrowdStat> stats = getStats(cameraId, start, end);

        int totalPeopleCount = stats.stream()
                .map(CrowdStat::getPeopleCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        double avgDensity = stats.stream()
                .map(CrowdStat::getDensityValue)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        int maxPeople = stats.stream()
                .map(CrowdStat::getPeopleCount)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        List<HourlyStatDto> hourlyStats = getHourlyStatistics(cameraId, start, end);

        double increaseRate = hourlyStats.stream()
                .map(HourlyStatDto::getIncreaseRate)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return new SummaryStatDto(
                totalPeopleCount,
                avgDensity,
                maxPeople,
                increaseRate
        );
    }

    public List<HourlyStatDto> getHourlyStatistics(Long cameraId, LocalDateTime start, LocalDateTime end) {

        List<CrowdStat> stats = getStats(cameraId, start, end);

        Map<Integer, List<CrowdStat>> grouped = stats.stream()
                .filter(s -> s.getMeasuredAt() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getMeasuredAt().getHour(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<HourlyStatDto> result = new ArrayList<>();
        Double prevDensity = null;

        for (Map.Entry<Integer, List<CrowdStat>> entry : grouped.entrySet()) {

            List<CrowdStat> list = entry.getValue();

            int people = list.stream()
                    .map(CrowdStat::getPeopleCount)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);

            double density = list.stream()
                    .map(CrowdStat::getDensityValue)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            double increaseRate = 0.0;
            if (prevDensity != null && prevDensity != 0.0) {
                increaseRate = ((density - prevDensity) / prevDensity) * 100.0;
            }

            result.add(new HourlyStatDto(
                    String.format("%02d:00", entry.getKey()),
                    people,
                    density,
                    increaseRate
            ));

            prevDensity = density;
        }

        return result;
    }


}



