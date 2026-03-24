package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    /**
     * DTO를 엔티티로 변환합니다.
     */
    public DetectionEntity toEntity(String customMessage) {
        String typeStr = (this.eventType != null) ? this.eventType.toUpperCase() : "CROWD";

        // 🚩 [수정 포인트] 프론트엔드 알림창(intrusion_now)과 연동하기 위해
        // 이벤트 타입이 INTRUSION이거나 레벨이 ALERT인 경우 intrusionNow를 1로 설정합니다.
        int intrusionVal = ("INTRUSION".equals(typeStr) || "ALERT".equals(this.eventLevel)) ? 1 : 0;

        return DetectionEntity.builder()
                .cameraId(this.cameraId)
                .dangerZoneId(this.dangerZoneId)
                .eventType(EventType.valueOf(typeStr))
                // eventLevel이 null이면 기본값 NORMAL 사용
                .eventLevel(EventLevel.valueOf(this.eventLevel != null ? this.eventLevel.toUpperCase() : "NORMAL"))
                .objectType(this.objectType)
                .detectedCount(this.detectedCount != null ? this.detectedCount : 0)
                .stayDurationSec(this.stayDurationSec)
                .intrusionNow(intrusionVal) // 🚩 가공된 값이 엔티티에 저장됨
                .message(customMessage)
                .imagePath(this.imagePath)
                .eventTime(this.eventTime != null ? this.eventTime : LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .build();
    }
}