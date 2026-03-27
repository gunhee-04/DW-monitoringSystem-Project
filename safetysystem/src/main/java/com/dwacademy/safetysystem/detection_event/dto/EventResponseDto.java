package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import lombok.Getter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
public class EventResponseDto {
    private Long id;
    private String status_text;
    private String level_color;
    private Integer people_count; // 🚩 프론트의 log.people_count와 매칭
    private String created_at;    // 🚩 프론트의 log.created_at과 매칭
    private boolean isAlert;      // 🚩 프론트의 log.isAlert와 매칭

    public EventResponseDto(DetectionEntity entity) {
        this.id = entity.getId().longValue();
        this.status_text = entity.getMessage();

        // 1. 인원수 데이터 담기 (null 체크 포함)
        this.people_count = (entity.getDetectedCount() != null) ? entity.getDetectedCount() : 0;

        // 2. 위험도별 색상 및 알림 여부(isAlert) 결정
        // 여기서 entity.getEventLevel()이 HIGH인지가 가장 중요합니다!
        if (entity.getEventLevel() == EventLevel.HIGH) {
            this.level_color = "#ff4d4f"; // 빨강
            this.isAlert = true;          // 🔥 종 모양 숫자를 올리는 핵심 스위치
        } else if (entity.getEventLevel() == EventLevel.MEDIUM) {
            this.level_color = "#faad14"; // 주황
            this.isAlert = false;
        } else {
            this.level_color = "#52c41a"; // 초록
            this.isAlert = false;
        }

        // 3. 시간 변환 로직
        if (entity.getEventTime() != null && entity.getEventTime() > 0) {
            long timeVal = entity.getEventTime();
            Instant instant = (timeVal > 1_000_000_000_000L)
                    ? Instant.ofEpochMilli(timeVal)
                    : Instant.ofEpochSecond(timeVal);

            this.created_at = instant.atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            this.created_at = java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}