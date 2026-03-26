package com.dwacademy.safetysystem.dangerzone;

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
    public List<DangerZone> findByCamera(Long cameraId) {
        log.info("--- [DangerZoneService] findByCamera() --- cameraId={}", cameraId);
        validateCameraId(cameraId);
        return zoneRepository.findByCameraIdAndIsActiveTrue(cameraId);
    }

    // 상세
    public DangerZone findById(Long id) {
        log.info("--- [DangerZoneService] findById() --- id={}", id);
        validateId(id);
        return getZoneOrThrow(id);
    }

    // 등록
    @Transactional
    public void save(DangerZoneDto dto) {
        log.info("--- [DangerZoneService] save() --- cameraId={}, zoneName={}",
                dto != null ? dto.getCameraId() : null,
                dto != null ? dto.getZoneName() : null);

        validateDto(dto);

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
    public void update(Long id, DangerZoneDto dto) {
        log.info("--- [DangerZoneService] update() --- id={}, cameraId={}, zoneName={}",
                id,
                dto != null ? dto.getCameraId() : null,
                dto != null ? dto.getZoneName() : null);

        validateId(id);
        validateDto(dto);

        DangerZone zone = getZoneOrThrow(id);

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
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : zone.getIsActive())
                .createdAt(zone.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        zoneRepository.save(updated);
    }

    // 소프트삭제
    @Transactional
    public void softDelete(Long id) {
        log.info("--- [DangerZoneService] softDelete() --- id={}", id);

        validateId(id);
        DangerZone zone = getZoneOrThrow(id);

        if (Boolean.FALSE.equals(zone.getIsActive())) {
            throw new IllegalStateException("위험구역 ID " + id + "번은 이미 비활성 상태이므로 삭제할 수 없습니다.");
        }

        zone.deactivate();
    }

    // 최신 활성 구역 1건 조회
    public DangerZone findLatestActiveZoneByCameraId(Long cameraId) {
        log.info("--- [DangerZoneService] findLatestActiveZoneByCameraId() --- cameraId={}", cameraId);

        validateCameraId(cameraId);

        return zoneRepository.findFirstByCameraIdAndIsActiveTrueOrderByUpdatedAtDesc(cameraId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "카메라 ID " + cameraId + "번에 대한 활성 위험구역을 찾을 수 없습니다."
                ));
    }

    // Entity → DTO
    public DangerZoneDto toDto(DangerZone dangerZone) {
        return DangerZoneDto.builder()
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

    // =========================
    // private 검증 메서드
    // =========================

    private DangerZone getZoneOrThrow(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("위험구역 ID " + id + "번을 찾을 수 없습니다."));
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 위험구역 ID입니다. id=" + id);
        }
    }

    private void validateCameraId(Long cameraId) {
        if (cameraId == null || cameraId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 카메라 ID입니다. cameraId=" + cameraId);
        }
    }

    private void validateDto(DangerZoneDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("위험구역 데이터가 없습니다.");
        }

        validateCameraId(dto.getCameraId());

        if (dto.getZoneName() == null || dto.getZoneName().trim().isEmpty()) {
            throw new IllegalArgumentException("위험구역 이름은 필수입니다.");
        }

        if (dto.getZoneType() == null || dto.getZoneType().trim().isEmpty()) {
            throw new IllegalArgumentException("위험구역 타입은 필수입니다.");
        }

        if (dto.getX1() == null || dto.getY1() == null || dto.getX2() == null || dto.getY2() == null) {
            throw new IllegalArgumentException("위험구역 좌표값은 모두 필수입니다.");
        }

        if (dto.getX1() < 0 || dto.getY1() < 0 || dto.getX2() < 0 || dto.getY2() < 0) {
            throw new IllegalArgumentException("위험구역 좌표는 0 이상이어야 합니다.");
        }

        if (dto.getX1() >= dto.getX2() || dto.getY1() >= dto.getY2()) {
            throw new IllegalArgumentException("위험구역 좌표 범위가 올바르지 않습니다. (x1<x2, y1<y2 여야 함)");
        }

        if (dto.getIntrusionTimeSec() == null) {
            throw new IllegalArgumentException("침입 감지 시간은 필수입니다.");
        }

        if (dto.getIntrusionTimeSec() <= 0) {
            throw new IllegalArgumentException("침입 감지 시간은 1초 이상이어야 합니다.");
        }
    }
}