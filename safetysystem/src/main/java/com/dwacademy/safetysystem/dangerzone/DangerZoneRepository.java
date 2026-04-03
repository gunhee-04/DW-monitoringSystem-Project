package com.dwacademy.safetysystem.dangerzone;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DangerZoneRepository extends JpaRepository<DangerZone,Long> {

    List<DangerZone> findByCamera_Id(Long cameraId);

    List<DangerZone> findByCamera_IdAndIsActiveTrue(Long cameraId);

    Optional<DangerZone> findFirstByCamera_IdAndIsActiveTrueOrderByUpdatedAtDesc(Long cameraId);
}