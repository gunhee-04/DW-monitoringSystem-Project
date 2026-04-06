package com.dwacademy.safetysystem.alert;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;

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

    private Integer detectedCount;
    private Double latitude;
    private Double longitude;

    public AlertResponseDto(AlertLog alert, DetectionEntity event, String location, String droneId) {
        this.id = alert.getId();
        this.alertType = alert.getAlertType();
        this.severity = alert.getSeverity();
        this.alertMessage = alert.getAlertMessage();
        this.isRead = alert.getIsRead();
        this.createdAt = alert.getCreatedAt();

        this.location = location;
        this.droneId = droneId;

        if (event != null) {
            this.detectedCount = event.getDetectedCount();
            this.latitude = event.getEventLatitude();
            this.longitude = event.getEventLongitude();
        } else {
            this.detectedCount = 0;
            this.latitude = 0.0;
            this.longitude = 0.0;
        }
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
    public Integer getDetectedCount() { return detectedCount; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}