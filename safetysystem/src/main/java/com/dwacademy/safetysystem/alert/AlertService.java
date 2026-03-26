package com.dwacademy.safetysystem.alert;

import com.dwacademy.safetysystem.detection_event.controller.SseController;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AlertService.class);
    private final AlertLogRepository alertRepository;

    public AlertService(AlertLogRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    //조회 작업할 때
    public List<AlertLog> getRecentAlerts(){
        return alertRepository.findTop10ByOrderByCreatedAtDesc();
    }

    //읽음 처리 할 때
    @Transactional
    public void markAsRead(long id){
        AlertLog alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림 없음: " + id));

        if (Boolean.TRUE.equals(alert.getIsRead())) {
            return;
        }

        alert.setIsRead(true);
        alert.setReadAt(LocalDateTime.now());
    }

    //최근 경고 로그 관련 필터도 아직 안됨
    public List<AlertLog> getRecentWarnings(){
        return alertRepository.findTop10BySeverityInOrderByCreatedAtDesc(
                List.of(EventLevel.MEDIUM, EventLevel.HIGH)
        );
    }

    //이벤트 알림 생성
    /**
     * 이벤트 발생 시 알림 생성
     * @param detectionEventId detection_event 테이블 ID
     * @param alertType 이벤트 타입 (예: INTRUSION)
     * @param severityStr 위험도 문자열 (예: "NORMAL", "WARNING", "ALERT")
     * @param message 전달받은 메시지 (null이면 기본 메시지 생성)
     */
    @Transactional
    public void createAlert(Long detectionEventId, String alertType, String severityStr, String message) {

        if (severityStr == null || severityStr.isBlank()) {
            severityStr = "NORMAL"; // 기본값 설정
        }

        EventLevel severity;
        try {
            severity = EventLevel.valueOf(severityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 🚩 핵심: 코드에 없는 ALERT 같은 값이 들어와도 HIGH나 NORMAL로 치환해서 서버가 안 죽게 방어
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
        // ... 이하 저장 로직 동일 ...

        // 3. 알림 객체 생성 후 저장
        AlertLog alert = new AlertLog();
        alert.setDetectionEventId(detectionEventId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setAlertMessage(message);
        alert.setIsRead(false);

        AlertLog saved = alertRepository.save(alert);

        // 🔥 2. 저장 직후 SSE 전송
        sendAlertSse(saved);
    }

    // 🔥 Alert 전용 SSE (핵심)
    public void sendAlertSse(AlertLog alert) {
        SseController.emitters.forEach(emitter -> {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("alert")   // 이벤트 이름 명확히
                                .data(new AlertResponseDto(alert))     // 🔥 전체 객체 전송
                );
            } catch (Exception e) {
                SseController.emitters.remove(emitter);
            }
        });
    }

}
