package com.dwacademy.safetysystem.alert;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;

import java.time.LocalDateTime;


public class AlertResponseDto {

    private Long id;
    private String alertType;
    private EventLevel severity;
    private String alertMessage;
    private Boolean isRead;
    private LocalDateTime createdAt;

    private String location;
    private String droneId;

    public AlertResponseDto(AlertLog alert, String location, String droneId) {
        this.id = alert.getId();
        this.alertType = alert.getAlertType();
        this.severity = alert.getSeverity();
        this.alertMessage = alert.getAlertMessage();
        this.isRead = alert.getIsRead();
        this.createdAt = alert.getCreatedAt();
        this.location = location;
        this.droneId = droneId;
    }

    // getter만 있으면 됨
    public Long getId() { return id; }
    public String getAlertType() { return alertType; }
    public EventLevel getSeverity() { return severity; }
    public String getAlertMessage() { return alertMessage; }
    public Boolean getIsRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getLocation() { return location; }
    public String getDroneId() { return droneId; }
}