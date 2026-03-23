package com.dwacademy.safetysystem.statistics.repository;

import com.dwacademy.safetysystem.statistics.entity.CrowdStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CrowdStatRepository extends JpaRepository<CrowdStat, Long> {

    // 🔥 핵심 (기간별)
    List<CrowdStat> findByMeasuredAtBetween(LocalDateTime start, LocalDateTime end);

    // 카메라별
    List<CrowdStat> findByCameraId(Long cameraId);

    // 구역별
    List<CrowdStat> findByDangerZoneId(Long dangerZoneId);
}