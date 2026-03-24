package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import lombok.Getter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class EventResponseDto {
    private Integer id;
    private Integer cameraId;

    // 🚩 프론트엔드 JS 변수명과 100% 일치시킴
    private Integer people_count;
    private String created_at;
    private boolean intrusion_now;

    private String eventLevel;
    private String message;

    // 🚩 추가: 위험도별 색상 필드
    private String level_color;

    public EventResponseDto(DetectionEntity entity) {
        this.id = entity.getId();
        this.cameraId = entity.getCameraId();
        this.message = entity.getMessage();
        this.eventLevel = (entity.getEventLevel() != null) ? entity.getEventLevel().name() : "NORMAL";

        // 1. people_count 매칭
        this.people_count = (entity.getDetectedCount() != null) ? entity.getDetectedCount() : 0;

        // 2. intrusion_now 판단
        this.intrusion_now = true;

        // 3. 🚩 추가: 위험도별 색상 할당 (초록, 주황, 빨강)
        if (entity.getEventLevel() != null) {
            switch (entity.getEventLevel()) {
                case ALERT -> this.level_color = "#FF4D4F";   // 빨강
                case WARNING -> this.level_color = "#FAAD14"; // 주황
                default -> this.level_color = "#52C41A";      // 초록 (NORMAL 등)
            }
        } else {
            this.level_color = "#52C41A";
        }

        // 4. created_at 시간 포맷
        if (entity.getEventTime() != null) {
            this.created_at = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(entity.getEventTime()),
                    ZoneId.systemDefault()
            ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            this.created_at = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}