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
                .orElseThrow(() -> new RuntimeException("해당 알림 없음: " + id));

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
    public void createAlert(Long detectionEventId, String alertType, String severityStr, String message) {
        // 문자열 -> Enum 변환
        EventLevel severity = EventLevel.valueOf(severityStr);

        AlertLog alert = new AlertLog();
        alert.setDetectionEventId(detectionEventId);
        alert.setAlertType(alertType);
        alert.setSeverity(severityStr);
        alert.setAlertMessage(message);

        alertRepository.save(alert);
    }

}
