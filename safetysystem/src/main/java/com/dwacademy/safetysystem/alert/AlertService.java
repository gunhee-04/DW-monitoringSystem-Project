package com.dwacademy.safetysystem.alert;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

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
                List.of(EventLevel.WARNING, EventLevel.ALERT)
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

        // 1. 문자열 -> Enum 변환
        if (severityStr == null || severityStr.isBlank()) {
            throw new IllegalArgumentException("severity 값은 필수입니다.");
        }

        EventLevel severity;
        try {
            severity = EventLevel.valueOf(severityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 severity 값: " + severityStr);
        }

        // 2. 메시지 기본 생성 (전달받은 메시지가 null 또는 빈 문자열이면)
        if (message == null || message.isBlank()) {
            message = switch (severity) {
                case NORMAL -> "실시간 정상 모니터링 중입니다.";
                case WARNING -> "⚠️ 주의: 위험 상태가 감지되었습니다.";
                case ALERT -> "🚨 긴급: 위험 상태가 감지되었습니다!";
            };
        }

        // 3. 알림 객체 생성 후 저장
        AlertLog alert = new AlertLog();
        alert.setDetectionEventId(detectionEventId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setAlertMessage(message);
        alert.setIsRead(false);

        alertRepository.save(alert);
    }
}
