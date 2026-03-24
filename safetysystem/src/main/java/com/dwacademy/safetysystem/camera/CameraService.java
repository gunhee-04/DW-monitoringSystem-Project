package com.dwacademy.safetysystem.camera;

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
public class CameraService {

    private final CameraRepository cameraRepository;

    // 목록 (활성 카메라만_소프트삭제)
    public List<Camera> findAll(){
        log.info("--- [CameraService] findAll() ---");
        return cameraRepository.findByStatus("ACTIVE");
    }

    // 상세
    public Camera findById(Long id){
        log.info("--- [CameraService] findById() ---");
        return cameraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카메라를 찾을 수 없습니다."));
    }

    // 등록
    @Transactional
    public void save(AdminConfigDto dto){
        log.info("--- [CameraService] save() ---");
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
    public void update(Long id, AdminConfigDto dto){
        log.info("--- [CameraService] update() ---");
        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 카메라를 찾을 수 없습니다."));

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
    public void softDelete(Long id){
        log.info("--- [CameraService] softDelete() ---");
        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 카메라를 찾을 수 없습니다."));
        camera.deactivate();
    }

    //카메라 상태변경
    @Transactional
    public String toggleStatus(Long id){
        Camera camera = cameraRepository.findById(id)
                .orElseThrow();

        if ("ACTIVE".equals(camera.getStatus())) {
            camera.setStatus("INACTIVE");
        } else {
            camera.setStatus("ACTIVE");
        }

        camera.setUpdatedAt(LocalDateTime.now());

        return camera.getStatus();
    }

    // Entity → DTO
    public AdminConfigDto toDto(Camera camera){
        return AdminConfigDto.builder()
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
}