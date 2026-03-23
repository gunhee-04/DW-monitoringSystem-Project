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
    private String eventType;  // JSON으로 나갈 때는 문자열로 출력됨
    private String eventLevel; // JSON으로 나갈 때는 문자열로 출력됨
    private String eventTime;
    private Integer detectedCount;
    private Integer intrusionNow;
    private String message;    // 메시지 필드 추가

    public EventResponseDto(DetectionEntity entity) {
        this.id = entity.getId();
        this.cameraId = entity.getCameraId();

        // ✅ 수정 포인트: Enum 객체 뒤에 .name()을 붙여서 String으로 변환
        // 만약 entity에서 타입을 Enum으로 바꿨다면 .name()이 필요합니다.
        this.eventType = (entity.getEventType() != null) ? entity.getEventType().name() : null;
        this.eventLevel = (entity.getEventLevel() != null) ? entity.getEventLevel().name() : null;

        this.detectedCount = entity.getDetectedCount();
        this.intrusionNow = entity.getIntrusionNow();
        this.message = entity.getMessage(); // 엔티티에 추가한 메시지도 함께 전달

        // ✅ 시간 변환 로직 (기존 유지)
        if (entity.getEventTime() != null) {
            this.eventTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(entity.getEventTime()),
                    ZoneId.systemDefault()
            ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}