package com.dwacademy.safetysystem.detection_event.service;

import com.dwacademy.safetysystem.alert.AlertService;
import com.dwacademy.safetysystem.camera.Camera;
import com.dwacademy.safetysystem.camera.CameraRepository;
import com.dwacademy.safetysystem.dangerzone.DangerZone;
import com.dwacademy.safetysystem.dangerzone.DangerZoneService;
import com.dwacademy.safetysystem.detection_event.controller.SseController;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.dto.EventResponseDto;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import com.dwacademy.safetysystem.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DetectionService {

    private final DetectionEventRepository repository;
    private final StatisticsService statisticsService;
    private final CameraRepository cameraRepository;
    private final DangerZoneService dangerZoneService;
    private final AlertService alertService;

    @Value("${kakao.rest-api-key:}")
    private String kakaoRestApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private Integer parseCameraId(String rawCameraId) {
        if (rawCameraId == null || rawCameraId.isBlank()) {
            return 1;
        }

        try {
            String onlyNumber = rawCameraId.replaceAll("[^0-9]", "");
            if (onlyNumber.isBlank()) {
                return 1;
            }
            return Integer.parseInt(onlyNumber);
        } catch (Exception e) {
            return 1;
        }
    }

    @Transactional
    public EventResponseDto processEvent(DetectionRequestDto dto) {
        Object[] analysis = analyzeEvent(dto);
        EventLevel level = (EventLevel) analysis[0];
        String message = (String) analysis[1];

        DetectionEntity entity = dto.toEntity(message, level);

        // camera 연결
        Integer numericCameraId = parseCameraId(dto.getCameraId());

        Camera camera = cameraRepository.findById(numericCameraId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("카메라 없음: " + dto.getCameraId()));
        entity.setCamera(camera);

        // dangerZone 연결 (선택)
        if (dto.getDangerZoneId() != null && dto.getDangerZoneId() > 0) {
            DangerZone zone = dangerZoneService.findById(dto.getDangerZoneId().longValue());
            entity.setDangerZone(zone);
        } else {
            entity.setDangerZone(null);
        }

        // 카메라 정보로 위치/주소 세팅
        entity.setEventLatitude(camera.getLatitude());
        entity.setEventLongitude(camera.getLongitude());
        entity.setEventAddress(buildAddressFromCamera(camera));

        log.info(">>> cameraId={} / camera.locationName={} / lat={} / lng={} / resolvedAddress={}",
                camera.getId(),
                camera.getLocationName(),
                entity.getEventLatitude(),
                entity.getEventLongitude(),
                entity.getEventAddress());



        if (level == EventLevel.HIGH || level == EventLevel.MEDIUM) {
            entity.setIsRead(0);
            log.info(">>> 🔔 [위험] 발생할 때마다 알림 전송");
        } else {
            entity.setIsRead(1);
            log.info(">>> 🗒️ [일반] 로그만 기록");
        }

        DetectionEntity saved = repository.save(entity);

        if (level == EventLevel.HIGH) {
            alertService.createAlert(saved, dto.getEventType(), level.name(), message);
        }

        try {
            statisticsService.saveFromDetection(saved);
        } catch (Exception e) {
            log.error("통계 저장 실패", e);
        }

        log.info(">>> 저장 완료: id={}, eventAddress={}, eventLatitude={}, eventLongitude={}",
                saved.getId(), saved.getEventAddress(), saved.getEventLatitude(), saved.getEventLongitude());

        broadcast(saved);

        EventResponseDto response = new EventResponseDto(saved);
        response.setCameraLocation(saved.getEventAddress() != null ? saved.getEventAddress() : "-");
        return response;
    }

    private Object[] analyzeEvent(DetectionRequestDto dto) {
        String rawType = dto.getEventType() != null ? dto.getEventType().trim().toUpperCase() : "";
        Integer detectedCount = dto.getDetectedCount() != null ? dto.getDetectedCount() : 0;
        Integer stayDurationSec = dto.getStayDurationSec() != null ? dto.getStayDurationSec() : 0;

        if ("INTRUSION".equals(rawType)) {
            return new Object[]{EventLevel.HIGH, "위험 침입 감지"};
        }

        if ("CROWD".equals(rawType)) {
            if (detectedCount >= 15) {
                return new Object[]{EventLevel.HIGH, "위험 (밀집)"};
            } else if (detectedCount >= 6) {
                return new Object[]{EventLevel.MEDIUM, "주의 (밀집)"};
            } else {
                return new Object[]{EventLevel.NORMAL, "정상 관제 중"};
            }
        }

        if ("STAY".equals(rawType)) {
            if (stayDurationSec >= 300) {
                return new Object[]{EventLevel.MEDIUM, "장시간 체류 감지"};
            } else {
                return new Object[]{EventLevel.NORMAL, "정상 관제 중"};
            }
        }

        return new Object[]{EventLevel.NORMAL, "정상 관제 중"};
    }

    private String buildAddressFromCamera(Camera camera) {
        if (camera.getLocationName() != null && !camera.getLocationName().isBlank()) {
            log.info(">>> cameraId={} 저장된 locationName 사용: {}",
                    camera.getId(), camera.getLocationName());
            return camera.getLocationName();
        }

        if (camera.getLatitude() == null || camera.getLongitude() == null) {
            log.warn(">>> 카메라 좌표가 없습니다. cameraId={}", camera.getId());
            return "-";
        }

        if (kakaoRestApiKey == null || kakaoRestApiKey.isBlank()) {
            log.warn(">>> kakao.rest-api-key 설정값이 비어 있습니다. cameraId={}", camera.getId());
            return fallbackAddress(camera);
        }

        try {
            String x = URLEncoder.encode(String.valueOf(camera.getLongitude()), StandardCharsets.UTF_8);
            String y = URLEncoder.encode(String.valueOf(camera.getLatitude()), StandardCharsets.UTF_8);

            String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json"
                    + "?x=" + x
                    + "&y=" + y
                    + "&input_coord=WGS84";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoRestApiKey.trim());

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            Map body = response.getBody();
            if (body == null) {
                log.warn(">>> 카카오 주소 변환 응답 body가 null 입니다. cameraId={}", camera.getId());
                return fallbackAddress(camera);
            }

            Object documentsObj = body.get("documents");
            if (!(documentsObj instanceof List<?> documents) || documents.isEmpty()) {
                log.warn(">>> 카카오 주소 변환 documents가 비어 있습니다. cameraId={}", camera.getId());
                return fallbackAddress(camera);
            }

            Object firstObj = documents.get(0);
            if (!(firstObj instanceof Map<?, ?> first)) {
                log.warn(">>> 카카오 주소 변환 첫 문서 형식이 올바르지 않습니다. cameraId={}", camera.getId());
                return fallbackAddress(camera);
            }

            Object roadAddressObj = first.get("road_address");
            if (roadAddressObj instanceof Map<?, ?> roadAddress) {
                Object addressName = roadAddress.get("address_name");
                if (addressName instanceof String s && !s.isBlank()) {
                    return s;
                }
            }

            Object addressObj = first.get("address");
            if (addressObj instanceof Map<?, ?> address) {
                Object addressName = address.get("address_name");
                if (addressName instanceof String s && !s.isBlank()) {
                    return s;
                }
            }

            log.warn(">>> 카카오 주소 변환 결과에서 address_name을 찾지 못했습니다. cameraId={}", camera.getId());
            return fallbackAddress(camera);

        } catch (Exception e) {
            log.warn(">>> 카카오 좌표->주소 변환 실패. cameraId={}, lat={}, lng={}, error={}",
                    camera.getId(), camera.getLatitude(), camera.getLongitude(), e.getMessage());
            return fallbackAddress(camera);
        }
    }

    private String fallbackAddress(Camera camera) {
        if (camera.getLocationName() != null && !camera.getLocationName().isBlank()) {
            return camera.getLocationName();
        }

        if (camera.getLatitude() != null && camera.getLongitude() != null) {
            return "좌표(" + camera.getLatitude() + ", " + camera.getLongitude() + ")";
        }

        return "-";
    }

    private void broadcast(DetectionEntity saved) {
        EventResponseDto response = new EventResponseDto(saved);
        response.setCameraLocation(saved.getEventAddress() != null ? saved.getEventAddress() : "-");

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : SseController.emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("newDetection")
                        .data(response));

                if (saved.getEventLevel() == EventLevel.HIGH ||
                        saved.getEventLevel() == EventLevel.MEDIUM) {
                    emitter.send(SseEmitter.event()
                            .name("danger_alert")
                            .data(response));
                    }
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }

        SseController.emitters.removeAll(deadEmitters);
    }

    public List<DetectionEntity> getAllEvents() {
        return repository.findAllByOrderByIdDesc();
    }

    public List<DetectionEntity> getEventsByLevel(EventLevel level) {
        return repository.findByEventLevelOrderByIdDesc(level);
    }

    public List<DetectionEntity> getEventsByCamera(Integer cameraId) {
        return repository.findByCamera_IdOrderByIdDesc(cameraId);
    }

    public List<EventResponseDto> getAllEventsForFront() {
        return repository.findAllByOrderByIdDesc().stream()
                .map(entity -> {
                    EventResponseDto dto = new EventResponseDto(entity);
                    dto.setCameraLocation(entity.getEventAddress() != null ? entity.getEventAddress() : "-");
                    return dto;
                })
                .toList();
    }

    @Transactional
    public void markAllAlertsAsRead() {
        List<DetectionEntity> unreadEvents = repository.findByIsRead(0);

        for (DetectionEntity entity : unreadEvents) {
            entity.setIsRead(1);
        }

        log.info(">>> ✅ {}건의 알림을 읽음 처리했습니다. 이제 숫자가 0이 됩니다.", unreadEvents.size());
    }

    public DetectionEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없음"));
    }
}