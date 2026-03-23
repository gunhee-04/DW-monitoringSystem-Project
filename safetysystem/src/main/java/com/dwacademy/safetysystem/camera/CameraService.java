package com.dwacademy.safetysystem.camera;

import com.dwacademy.safetysystem.admin.AdminConfigDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CameraService {

    private final CameraRepository cameraRepository;


    // 목록 (활성 카메라만_소프트삭제)
    public List<Camera> findAll(){
        return cameraRepository.findByStatus("ACTIVE");
    }

    // 상세
    public Camera findById(Long id){
        return cameraRepository.findById(id).orElseThrow();
    }

    // 등록
    @Transactional
    public void save(AdminConfigDto dto){
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
        Camera camera = cameraRepository.findById(id).orElseThrow();

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
        Camera camera = cameraRepository.findById(id).orElseThrow();
        camera.deactivate();
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


