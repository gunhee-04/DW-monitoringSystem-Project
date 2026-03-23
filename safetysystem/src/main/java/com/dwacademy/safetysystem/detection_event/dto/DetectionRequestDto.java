package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DetectionRequestDto {
    private Integer cameraId;
    private Integer dangerZoneId;
    private String eventType;
    private String eventLevel;
    private String objectType;
    private Integer detectedCount;
    private Integer stayDurationSec;
    private String imagePath;
    private Long eventTime;

    // 🚩 Getter 메서드 직접 추가 (Lombok 대신)
    public Integer getCameraId() { return cameraId; }
    public Integer getDetectedCount() { return detectedCount; }
    public String getEventType() { return eventType; }
    public String getEventLevel() { return eventLevel; }

    // 🚩 toEntity 메서드 (이미 있으시겠지만 다시 확인)
    public DetectionEntity toEntity(String customMessage) {
        return DetectionEntity.builder()
                .cameraId(this.cameraId)
                .dangerZoneId(this.dangerZoneId)
                .eventType(EventType.valueOf(this.eventType))
                .eventLevel(EventLevel.valueOf(this.eventLevel))
                .objectType(this.objectType)
                .detectedCount(this.detectedCount)
                .stayDurationSec(this.stayDurationSec)
                .intrusionNow(this.eventType.equals("INTRUSION") ? 1 : 0)
                .message(customMessage)
                .imagePath(this.imagePath)
                .eventTime(this.eventTime != null ? this.eventTime : LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .build();
    }

    // Setter 등 다른 메서드들도 필요하면 Alt+Insert 버튼으로 생성 가능합니다.
}