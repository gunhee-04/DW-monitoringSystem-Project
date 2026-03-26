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
    private Integer people_count;
    private String created_at; // 🚩 프론트의 log.created_at과 매칭

    public EventResponseDto(DetectionEntity entity) {
        this.id = entity.getId().longValue();
        this.status_text = entity.getMessage(); // "실시간 정상..." 또는 "🚨 [위험]..."
        this.people_count = entity.getDetectedCount();

        // 1. 위험도별 색상 설정 (프론트 log.level_color 대응)
        if (entity.getEventLevel() == EventLevel.HIGH) {
            this.level_color = "#ff4d4f"; // 빨강
        } else if (entity.getEventLevel() == EventLevel.MEDIUM) {
            this.level_color = "#faad14"; // 주황
        } else {
            this.level_color = "#52c41a"; // 초록
        }

        // 2. [핵심] 시간 단위 자동 감별 및 한국 시간 변환
        if (entity.getEventTime() != null && entity.getEventTime() > 0) {
            long timeVal = entity.getEventTime();
            Instant instant;

            // 값이 10^12보다 크면 밀리초(13자리), 작으면 초(10자리)로 판단
            if (timeVal > 1_000_000_000_000L) {
                instant = Instant.ofEpochMilli(timeVal);
            } else {
                instant = Instant.ofEpochSecond(timeVal);
            }

            // 한국 시간(Asia/Seoul)으로 포맷팅
            this.created_at = instant.atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            // 시간 데이터가 없으면 현재 시간 출력
            this.created_at = java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}