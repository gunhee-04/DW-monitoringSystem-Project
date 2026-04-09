package com.dwacademy.safetysystem.camera;

import com.dwacademy.safetysystem.dangerzone.DangerZone;
import com.dwacademy.safetysystem.dangerzone.DangerZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CameraApiService {

    private final CameraRepository cameraRepository;
    private final DangerZoneRepository dangerZoneRepository;

    public CameraApiResponseDto getCameraConfig(String cameraCode) {

        // 1. cameraCode로 카메라 조회
        Camera camera = cameraRepository.findByCameraCode(cameraCode)
                .orElseThrow(() -> new IllegalArgumentException("카메라를 찾을 수 없습니다."));

        // 2. 해당 카메라의 활성 danger zone 1개 조회
        DangerZone zone = dangerZoneRepository.findFirstByCamera_IdAndIsActiveTrueOrderByUpdatedAtDesc(camera.getId())
                .orElseThrow(() -> new IllegalArgumentException("활성 위험구역을 찾을 수 없습니다."));

        // 3. JSON 응답 DTO 생성
        return CameraApiResponseDto.builder()
                .intrusionSeconds(zone.getIntrusionTimeSec())
                .roi(
                        CameraApiResponseDto.RoiDto.builder()
                                .x1(zone.getX1())
                                .y1(zone.getY1())
                                .x2(zone.getX2())
                                .y2(zone.getY2())
                                .build()
                )
                .streamUrl(camera.getStreamUrl())  //추가
                .build();
    }
}