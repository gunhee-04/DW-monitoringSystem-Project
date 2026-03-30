package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class EventResponseDto {
    private Long id;
    private String status_text;
    private String level_color;
    private Integer people_count;
    private String created_at;
    private boolean isAlert;
    private int isRead; // 🚩 컨트롤러의 getIsRead() 빨간 줄 해결을 위해 추가

    public EventResponseDto(DetectionEntity entity) {
        this.id = entity.getId().longValue();
        this.status_text = entity.getMessage();
        this.people_count = (entity.getDetectedCount() != null) ? entity.getDetectedCount() : 0;
        this.isRead = entity.getIsRead(); // Entity의 값을 그대로 담음

        // 1. 알림 여부 및 색상 결정
        if (entity.getEventLevel() == EventLevel.HIGH) {
            this.level_color = "#ff4d4f"; // 빨강
            // 신규 알림(isRead == 1)일 때만 프론트 팝업 트리거
            this.isAlert = (entity.getIsRead() == 0);
        } else if (entity.getEventLevel() == EventLevel.MEDIUM) {
            this.level_color = "#faad14"; // 주황
            this.isAlert = false;
        } else {
            this.level_color = "#52c41a"; // 초록
            this.isAlert = false;
        }

        // 2. 시간 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (entity.getEventTime() != null && entity.getEventTime() > 0) {
            long timeVal = entity.getEventTime();
            Instant instant = (timeVal > 1_000_000_000_000L)
                    ? Instant.ofEpochMilli(timeVal)
                    : Instant.ofEpochSecond(timeVal);
            this.created_at = instant.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
        } else {
            this.created_at = LocalDateTime.now().format(formatter);
        }
    }
}