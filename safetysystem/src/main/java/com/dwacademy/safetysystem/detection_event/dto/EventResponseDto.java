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

    public EventResponseDto(DetectionEntity entity) {
        this.id = entity.getId().longValue();
        this.status_text = entity.getMessage();
        this.people_count = (entity.getDetectedCount() != null) ? entity.getDetectedCount() : 0;

        // 1. 알림 여부 결정 로직 (HIGH 등급 + 미확인(0) 상태)
        if (entity.getEventLevel() == EventLevel.HIGH && entity.getIsRead() == 0) {
            this.level_color = "#ff4d4f";
            this.isAlert = true;
        } else if (entity.getEventLevel() == EventLevel.MEDIUM) {
            this.level_color = "#faad14";
            this.isAlert = false;
        } else {
            // HIGH여도 읽은 상태면 색상은 빨강 유지, isAlert만 false
            this.level_color = (entity.getEventLevel() == EventLevel.HIGH) ? "#ff4d4f" : "#52c41a";
            this.isAlert = false;
        }

        // 2. 시간 포맷팅 (빨간 줄 해결 지점)
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