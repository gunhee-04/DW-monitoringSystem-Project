package com.dwacademy.safetysystem.alert;

import com.dwacademy.safetysystem.detection_event.controller.SseController;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AlertService.class);

    private final AlertLogRepository alertRepository;


    public AlertService(
            AlertLogRepository alertRepository
    ) {
        this.alertRepository = alertRepository;
    }

    // 최근 알림 조회
    public List<AlertLog> getRecentAlerts() {
        return alertRepository.findTop10ByOrderByCreatedAtDesc();
    }

    // 개별 읽음 처리
    @Transactional
    public void markAsRead(long id) {
        AlertLog alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림 없음: " + id));

        if (Boolean.TRUE.equals(alert.getIsRead())) {
            return;
        }

        alert.setIsRead(true);
        alert.setReadAt(LocalDateTime.now());
    }

    // 최근 경고 로그 조회
    public List<AlertLog> getRecentWarnings() {
        return alertRepository.findTop10BySeverityInOrderByCreatedAtDesc(
                List.of(EventLevel.MEDIUM, EventLevel.HIGH)
        );
    }

    @Transactional
    public void createAlert(DetectionEntity detectionEntity, String alertType, String severityStr, String message) {

        // 지금 구조상 종 알림/팝업은 HIGH만 생성
        // 나중에 MEDIUM도 띄우고 싶으면 이 조건에서 MEDIUM 제거하면 됨
        if ("NORMAL".equalsIgnoreCase(severityStr)) {
            return;
        }

        EventLevel severity;
        try {
            severity = EventLevel.valueOf(severityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("잘못된 severity 값 들어옴: {}. HIGH로 대체합니다.", severityStr);
            severity = EventLevel.HIGH;
        }

        if (message == null || message.isBlank()) {
            message = switch (severity) {
                case HIGH -> "🚨 [위험] 긴급 상황 발생!";
                case MEDIUM -> "⚠️ [주의] 이상 징후 감지";
                case NORMAL -> "✅ 정상 상태";
            };
        }

        AlertLog alert = new AlertLog();
        alert.setDetectionEntity(detectionEntity);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setAlertMessage(message);
        alert.setIsRead(false);

        AlertLog saved = alertRepository.save(alert);

        AlertResponseDto dto = toDto(saved);
        sendAlertSse(dto);
    }

    // Alert 전용 SSE
    public void sendAlertSse(AlertResponseDto dto) {
        SseController.emitters.forEach(emitter -> {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("alert")
                                .data(dto)
                );
            } catch (Exception e) {
                SseController.emitters.remove(emitter);
            }
        });
    }

    // 모든 알림 읽음 처리
    @Transactional
    public void markAllAsRead() {
        List<AlertLog> unreadAlerts = alertRepository.findAllByIsReadFalse();

        for (AlertLog alert : unreadAlerts) {
            alert.setIsRead(true);
            alert.setReadAt(LocalDateTime.now());
        }
    }

    // 알림 삭제
    @Transactional
    public void softDelete(Long id) {
        AlertLog alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림 없음: " + id));

        alertRepository.delete(alert);
        log.info("알림 삭제 완료: id={}", id);
    }

    public List<AlertResponseDto> getRecentAlertDtos() {
        return alertRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public AlertResponseDto toDto(AlertLog alert) {
        String location = "-";
        String droneId = "-";

        if (alert.getDetectionEntity() != null) {
            DetectionEntity detection = alert.getDetectionEntity();

            if (detection.getEventAddress() != null && !detection.getEventAddress().isBlank()) {
                location = detection.getEventAddress();
            }

            if (detection.getCamera() != null) {
                var camera = detection.getCamera();

                if (camera.getCameraCode() != null && !camera.getCameraCode().isBlank()) {
                    droneId = camera.getCameraCode();
                } else {
                    droneId = "DRONE-" + camera.getId();
                }
            }

            if (
                    "-".equals(location)
                            || location.contains("좌표(")
                            || location.contains("0.0")
                            || "위치 정보 없음".equals(location)
            ) {
                if ("CAM-01".equals(droneId)) {
                    location = "서울특별시 중구 세종대로 110 서울시청";
                } else if ("CAM-02".equals(droneId)) {
                    location = "서울특별시 중구 을지로 281 DDP";
                } else if ("CAM-03".equals(droneId)) {
                    location = "서울특별시 종로구 세종대로 일대";
                }
            }
        }

        return new AlertResponseDto(alert, location, droneId);
    }

}