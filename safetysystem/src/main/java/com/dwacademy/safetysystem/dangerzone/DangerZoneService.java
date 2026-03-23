package com.dwacademy.safetysystem.dangerzone;

import com.dwacademy.safetysystem.camera.CameraRepository;

import com.dwacademy.safetysystem.admin.AdminConfigDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DangerZoneService {

    private final DangerZoneRepository zoneRepository;

    // 목록 (활성 구역만)
    public List<DangerZone> findByCamera(Long cameraId){
        return zoneRepository.findByCameraIdAndIsActiveTrue(cameraId);
    }

    // 상세
    public DangerZone findById(Long id){
        return zoneRepository.findById(id).orElseThrow();
    }

    // 등록
    @Transactional
    public void save(AdminConfigDto dto){
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
        DangerZone zone = zoneRepository.findById(id).orElseThrow();

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
        DangerZone zone = zoneRepository.findById(id).orElseThrow();
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
