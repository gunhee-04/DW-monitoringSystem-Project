package com.dwacademy.safetysystem.camera;

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
public class CameraService {

    private final CameraRepository cameraRepository;

    // 목록 (활성 카메라만_소프트삭제)
    public List<Camera> findAll() {
        log.info("--- [CameraService] findAll() ---");
        return cameraRepository.findByStatus("ACTIVE");
    }
    // 상세
    public Camera findById(Long id) {
        log.info("--- [CameraService] findById() --- id={}", id);
        validateId(id);
        return getCameraOrThrow(id);
    }

    // 등록
    @Transactional
    public void save(CameraDto dto) {
        log.info("--- [CameraService] save() --- cameraCode={}", dto.getCameraCode());

        validateDto(dto);
        validateDuplicateCameraCode(dto.getCameraCode());

        Camera camera = Camera.builder()
                .cameraCode(dto.getCameraCode())
                .cameraName(dto.getCameraName())
                .cameraType(dto.getCameraType())
                .ipAddress(dto.getIpAddress())
                .streamUrl(dto.getStreamUrl())
                .locationName(dto.getLocationName())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .status(dto.getStatus())
                .createdAt(LocalDateTime.now())
                .build();

        cameraRepository.save(camera);
    }

    // 수정
    @Transactional
    public void update(Long id, CameraDto dto) {
        log.info("--- [CameraService] update() --- id={}, cameraCode={}", id, dto.getCameraCode());

        validateId(id);
        validateDto(dto);

        Camera camera = getCameraOrThrow(id);

        // 수정 시에는 자기 자신 제외하고 코드 중복 검사
        validateDuplicateCameraCodeForUpdate(id, dto.getCameraCode());

        Camera updated = Camera.builder()
                .id(camera.getId())
                .cameraCode(dto.getCameraCode())
                .cameraName(dto.getCameraName())
                .cameraType(dto.getCameraType())
                .ipAddress(dto.getIpAddress())
                .streamUrl(dto.getStreamUrl())
                .locationName(dto.getLocationName())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .status(dto.getStatus())
                .createdAt(camera.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        cameraRepository.save(updated);
    }

    // 소프트삭제
    @Transactional
    public void softDelete(Long id) {
        log.info("--- [CameraService] softDelete() --- id={}", id);

        validateId(id);
        Camera camera = getCameraOrThrow(id);

        if (!"ACTIVE".equals(camera.getStatus())) {
            throw new IllegalStateException("카메라 ID " + id + "번은 이미 비활성 상태이므로 삭제할 수 없습니다.");
        }

        camera.deactivate();
    }

    // 카메라 상태변경
    @Transactional
    public String toggleStatus(Long id) {
        log.info("--- [CameraService] toggleStatus() --- id={}", id);

        validateId(id);
        Camera camera = getCameraOrThrow(id);

        if ("DELETED".equals(camera.getStatus())) {
            throw new IllegalStateException("삭제된 카메라는 상태를 변경할 수 없습니다. ID=" + id);
        }

        if ("ACTIVE".equals(camera.getStatus())) {
            camera.setStatus("INACTIVE");
        } else if ("INACTIVE".equals(camera.getStatus())) {
            camera.setStatus("ACTIVE");
        } else {
            throw new IllegalStateException("알 수 없는 카메라 상태입니다. status=" + camera.getStatus());
        }

        camera.setUpdatedAt(LocalDateTime.now());
        return camera.getStatus();
    }

    // Entity → DTO
    public CameraDto toDto(Camera camera) {
        return CameraDto.builder()
                .cameraId(camera.getId())
                .cameraCode(camera.getCameraCode())
                .cameraName(camera.getCameraName())
                .cameraType(camera.getCameraType())
                .ipAddress(camera.getIpAddress())
                .streamUrl(camera.getStreamUrl())
                .locationName(camera.getLocationName())
                .latitude(camera.getLatitude())
                .longitude(camera.getLongitude())
                .status(camera.getStatus())
                .build();
    }

    // =========================
    // private 검증 메서드
    // =========================

    private Camera getCameraOrThrow(Long id) {
        return cameraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카메라 ID " + id + "번을 찾을 수 없습니다."));
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 카메라 ID입니다. id=" + id);
        }
    }

    private void validateDto(CameraDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("카메라 데이터가 없습니다.");
        }
        if (dto.getCameraCode() == null || dto.getCameraCode().trim().isEmpty()) {
            throw new IllegalArgumentException("카메라 코드는 필수입니다.");
        }
        if (dto.getCameraName() == null || dto.getCameraName().trim().isEmpty()) {
            throw new IllegalArgumentException("카메라 이름은 필수입니다.");
        }
        if (dto.getStatus() == null || dto.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("카메라 상태는 필수입니다.");
        }
    }

    private void validateDuplicateCameraCode(String cameraCode) {
        boolean exists = cameraRepository.existsByCameraCode(cameraCode);
        if (exists) {
            throw new IllegalStateException("이미 사용 중인 카메라 코드입니다. cameraCode=" + cameraCode);
        }
    }

    private void validateDuplicateCameraCodeForUpdate(Long id, String cameraCode) {
        cameraRepository.findByCameraCode(cameraCode)
                .ifPresent(found -> {
                    if (!found.getId().equals(id)) {
                        throw new IllegalStateException("이미 다른 카메라가 사용 중인 코드입니다. cameraCode=" + cameraCode);
                    }
                });
    }


}