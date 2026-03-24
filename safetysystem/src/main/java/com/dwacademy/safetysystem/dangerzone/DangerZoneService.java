package com.dwacademy.safetysystem.dangerzone;

import com.dwacademy.safetysystem.admin.AdminConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DangerZoneService {

    private final DangerZoneRepository zoneRepository;

    // 목록 (활성 구역만)
    public List<DangerZone> findByCamera(Long cameraId){
        log.info("--- [DangerZoneService] findByCamera() ---");
        return zoneRepository.findByCameraIdAndIsActiveTrue(cameraId);
    }

    // 상세
    public DangerZone findById(Long id){
        log.info("--- [DangerZoneService] findById() ---");
        return zoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("위험구역을 찾을 수 없습니다."));
    }

    // 등록
    @Transactional
    public void save(AdminConfigDto dto){
        log.info("--- [DangerZoneService] save() ---");
        DangerZone zone = DangerZone.builder()
                .cameraId(dto.getCameraId())
                .zoneName(dto.getZoneName())
                .zoneType(dto.getZoneType())
                .x1(dto.getX1())
                .y1(dto.getY1())
                .x2(dto.getX2())
                .y2(dto.getY2())
                .intrusionTimeSec(dto.getIntrusionTimeSec())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        zoneRepository.save(zone);
    }

    // 수정
    @Transactional
    public void update(Long id, AdminConfigDto dto){
        log.info("--- [DangerZoneService] update() ---");
        DangerZone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 위험구역을 찾을 수 없습니다."));

        DangerZone updated = DangerZone.builder()
                .id(zone.getId())
                .cameraId(dto.getCameraId())
                .zoneName(dto.getZoneName())
                .zoneType(dto.getZoneType())
                .x1(dto.getX1())
                .y1(dto.getY1())
                .x2(dto.getX2())
                .y2(dto.getY2())
                .intrusionTimeSec(dto.getIntrusionTimeSec())
                .isActive(dto.getIsActive())
                .createdAt(zone.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        zoneRepository.save(updated);
    }

    // 소프트삭제
    @Transactional
    public void softDelete(Long id){
        log.info("--- [DangerZoneService] softDelete() ---");
        DangerZone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 위험구역을 찾을 수 없습니다."));
        zone.deactivate();
    }

    // Entity → DTO
    public AdminConfigDto toDto(DangerZone dangerZone){
        return AdminConfigDto.builder()
                .zoneId(dangerZone.getId())
                .cameraId(dangerZone.getCameraId())
                .zoneName(dangerZone.getZoneName())
                .zoneType(dangerZone.getZoneType())
                .x1(dangerZone.getX1())
                .y1(dangerZone.getY1())
                .x2(dangerZone.getX2())
                .y2(dangerZone.getY2())
                .intrusionTimeSec(dangerZone.getIntrusionTimeSec())
                .isActive(dangerZone.getIsActive())
                .build();
    }
}