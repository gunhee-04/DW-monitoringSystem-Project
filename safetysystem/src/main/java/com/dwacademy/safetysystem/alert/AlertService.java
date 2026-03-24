package com.dwacademy.safetysystem.alert;

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
    public void markAsRead(long id){
        AlertLog alert = alertRepository.findById(id)
                .orElseThrow();

        alert.setIsRead(true);
        alert.setReadAt(LocalDateTime.now());
    }

    //이벤트 알림 생성
    public void createAlert(Long detectionEventId, String alertType, String severity, String message) {

        AlertLog alert = new AlertLog();

        alert.setDetectionEventId(detectionEventId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setAlertMessage(message);
        alert.setIsRead(false);
        alert.setCreatedAt(LocalDateTime.now());

        alertRepository.save(alert);
    }

}
