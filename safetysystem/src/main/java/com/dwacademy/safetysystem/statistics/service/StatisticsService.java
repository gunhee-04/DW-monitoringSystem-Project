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

    // =========================
    // 공통 조회
    // =========================
    private List<CrowdStat> getStats(Long cameraId, LocalDateTime start, LocalDateTime end) {

        if (start == null || end == null) {
            throw new IllegalArgumentException("start/end 필수값");
        }

        if (cameraId != null) {
            return crowdStatRepository.findByCameraIdAndMeasuredAtBetween(cameraId, start, end);
        }

        return crowdStatRepository.findByMeasuredAtBetween(start, end);
    }

    public List<CrowdStat> findCrowdStats(Long cameraId, LocalDateTime start, LocalDateTime end) {
        return getStats(cameraId, start, end);
    }

    // =========================
    // 시간별 통계
    // =========================
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

            result.add(new HourlyStatDto(
                    String.format("%02d:00", entry.getKey()),
                    people,
                    density
            ));
        }

        return result;
    }

    // =========================
    // 요약 통계
    // =========================
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

        double increaseRate = stats.stream()
                .filter(s -> s.getIncreaseRate() != null)
                .mapToDouble(CrowdStat::getIncreaseRate)
                .average()
                .orElse(0.0);

        return new SummaryStatDto(
                totalPeopleCount,
                avgDensity,
                maxPeople,
                increaseRate
        );
    }

    // =========================
    // YOLO → 통계 자동 저장
    // =========================
    public void saveFromDetection(DetectionEntity event) {

        if (event == null) return;

        if (event.getEventType() == null || event.getEventType() != EventType.CROWD) {
            return;
        }

        CrowdStat stat = new CrowdStat();

        stat.setCameraId(event.getCameraId() != null ? event.getCameraId().longValue() : 0L);
        stat.setDangerZoneId(event.getDangerZoneId() != null ? event.getDangerZoneId().longValue() : null);

        stat.setMeasuredAt(
                event.getEventTime() != null
                        ? LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(event.getEventTime()),
                        java.time.ZoneId.of("Asia/Seoul")
                )
                        : LocalDateTime.now()
        );

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
                        e -> LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochSecond(e.getEventTime()),
                                java.time.ZoneId.of("Asia/Seoul")
                        ).getHour(),
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
                        e -> LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochSecond(e.getEventTime()),
                                java.time.ZoneId.of("Asia/Seoul")
                        ).getHour(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<DangerHourlyResponseDto> result = new ArrayList<>();

        for (Map.Entry<Integer, List<DetectionEntity>> entry : grouped.entrySet()) {
            List<DetectionEntity> list = entry.getValue();

            long intrusionCount = list.size();

            double averageStayDuration = list.stream()
                    .map(DetectionEntity::getStayDurationSec)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            result.add(new DangerHourlyResponseDto(
                    String.format("%02d:00", entry.getKey()),
                    intrusionCount,
                    averageStayDuration
            ));
        }

        return result;
    }

    private List<DetectionEntity> getDangerEvents(Long cameraId, LocalDateTime start, LocalDateTime end) {
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

}