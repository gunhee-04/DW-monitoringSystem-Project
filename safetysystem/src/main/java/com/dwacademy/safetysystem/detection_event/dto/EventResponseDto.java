package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class EventResponseDto {
    private Long id;
    private String status_text;
    private String level_color;
    private Integer people_count;
    private String created_at;
    private String address;

    @JsonProperty("camera_location")
    private String cameraLocation;

    @JsonProperty("alert")
    private boolean alert;

    @JsonProperty("event_level")
    private String event_level;

    public EventResponseDto(DetectionEntity entity) {
        this.id = entity.getId().longValue();
        this.status_text = entity.getMessage() != null ? entity.getMessage() : "정상 관제 중";
        this.people_count = (entity.getDetectedCount() != null) ? entity.getDetectedCount() : 0;

        EventLevel level = entity.getEventLevel();

        this.event_level = (level != null) ? level.name() : "NORMAL";

        if (entity.getEventAddress() != null && !entity.getEventAddress().isBlank()) {
            this.address = entity.getEventAddress();
            this.cameraLocation = entity.getEventAddress();
        } else if (entity.getEventLatitude() != null
                && entity.getEventLongitude() != null
                && entity.getEventLatitude() != 0
                && entity.getEventLongitude() != 0) {
            String fallbackLocation = entity.getEventLatitude() + ", " + entity.getEventLongitude();
            this.address = fallbackLocation;
            this.cameraLocation = fallbackLocation;
        } else {
            this.address = "위치 정보 없음";
            this.cameraLocation = "위치 정보 없음";
        }

        if (level == EventLevel.HIGH && entity.getIsRead() != null && entity.getIsRead() == 0) {
            this.level_color = "#ff4d4f";
            this.alert = true;
        } else if (level == EventLevel.MEDIUM) {
            this.level_color = "#faad14";
            this.alert = false;
        } else if (level == EventLevel.HIGH) {
            this.level_color = "#ff4d4f";
            this.alert = false;
        } else {
            this.level_color = "#52c41a";
            this.alert = false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (entity.getCreatedAt() != null) {
            this.created_at = entity.getCreatedAt().format(formatter);
        } else {
            this.created_at = LocalDateTime.now().format(formatter);
        }
    }
}