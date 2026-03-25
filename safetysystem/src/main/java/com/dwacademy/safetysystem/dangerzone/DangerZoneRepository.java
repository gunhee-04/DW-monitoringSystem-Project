package com.dwacademy.safetysystem.dangerzone;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DangerZoneRepository extends JpaRepository<DangerZone,Long> {

    List<DangerZone> findByCameraId(Long cameraId);

    List<DangerZone> findByCameraIdAndIsActiveTrue(Long cameraId);

    // 기존 findFirstByCameraIdAndIsActiveTrue 삭제
    Optional<DangerZone> findFirstByCameraIdAndIsActiveTrueOrderByUpdatedAtDesc(Long cameraId);
}