package com.dwacademy.safetysystem.statistics.repository;

import com.dwacademy.safetysystem.statistics.entity.CrowdStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CrowdStatRepository extends JpaRepository<CrowdStat, Long> {

    // 전체 기간 조회
    List<CrowdStat> findByMeasuredAtBetween(LocalDateTime start, LocalDateTime end);

    // 카메라별 조회
    List<CrowdStat> findByCameraId(Long cameraId);

    // 구역별 조회
    List<CrowdStat> findByDangerZoneId(Long dangerZoneId);

    // 기간 + 카메라별 조회 추가
    List<CrowdStat> findByCameraIdAndMeasuredAtBetween(Long cameraId, LocalDateTime start, LocalDateTime end);

    // 기간 + 구역별 조회도 나중에 쓸 수 있어서 있으면 좋음
    List<CrowdStat> findByDangerZoneIdAndMeasuredAtBetween(Long dangerZoneId, LocalDateTime start, LocalDateTime end);
}