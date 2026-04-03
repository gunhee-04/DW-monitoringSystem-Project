package com.dwacademy.safetysystem.alert;

import com.dwacademy.safetysystem.camera.CameraRepository;
import com.dwacademy.safetysystem.detection_event.controller.SseController;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AlertService.class);

    private final AlertLogRepository alertRepository;
    private final DetectionEventRepository detectionRepository;
    private final CameraRepository cameraRepository;

    public AlertService(
            AlertLogRepository alertRepository,
            DetectionEventRepository detectionRepository,
            CameraRepository cameraRepository
    ) {
        this.alertRepository = alertRepository;
        this.detectionRepository = detectionRepository;
        this.cameraRepository = cameraRepository;
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

    /**
     * 이벤트 발생 시 알림 생성
     * @param detectionEventId detection_event 테이블 ID
     * @param alertType 이벤트 타입 (예: INTRUSION)
     * @param severityStr 위험도 문자열 (예: NORMAL, MEDIUM, HIGH)
     * @param message 전달받은 메시지
     */
    @Transactional
    public void createAlert(Long detectionEventId, String alertType, String severityStr, String message) {

        // 지금 구조상 종 알림/팝업은 HIGH만 생성
        // 나중에 MEDIUM도 띄우고 싶으면 이 조건에서 MEDIUM 제거하면 됨
        if ("NORMAL".equalsIgnoreCase(severityStr)) {
            log.info("🚫 {} 등급은 알림 생성을 차단합니다.", severityStr);
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
        alert.setDetectionEventId(detectionEventId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setAlertMessage(message);
        alert.setIsRead(false);

        AlertLog saved = alertRepository.save(alert);

        DetectionEntity detection = detectionRepository
                .findById(saved.getDetectionEventId().intValue())
                .orElse(null);

        String location = "-";
        String droneId = "-";

        if (detection != null) {

            if (detection.getEventAddress() != null && !detection.getEventAddress().isBlank()) {
                location = detection.getEventAddress();
            }

            if (detection.getCameraId() != null) {
                var cameraOpt = cameraRepository.findById(detection.getCameraId().longValue());

                if (cameraOpt.isPresent()) {
                    var camera = cameraOpt.get();

                    if (camera.getCameraCode() != null && !camera.getCameraCode().isBlank()) {
                        droneId = camera.getCameraCode();
                    } else {
                        droneId = "DRONE-" + detection.getCameraId();
                    }
                }
            }
        }

        AlertResponseDto dto = new AlertResponseDto(saved, location, droneId);

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
}