package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // 🚩 파이썬의 알 수 없는 필드 무시
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
     * 서비스에서 판단한 level과 message를 받아 엔티티로 변환
     */
    public DetectionEntity toEntity(String customMessage, EventLevel calculatedLevel) {
        // 1. EventType 안전하게 변환
        EventType type;
        try {
            type = (this.eventType != null) ? EventType.valueOf(this.eventType.toUpperCase()) : EventType.CROWD;
        } catch (Exception e) {
            type = EventType.CROWD;
        }

        // 2. [핵심 수정] 시간 데이터 보정 로직
        // 파이썬에서 보낸 시간이 null이거나, 0이거나, 너무 작은 숫자(1970년 등)인 경우 현재 서버 시간 사용
        Long finalTime = this.eventTime;
        if (finalTime == null || finalTime < 1000000000L) {
            // 현재 시간을 초 단위(Epoch Second)로 가져옵니다.
            finalTime = System.currentTimeMillis() / 1000;
        }

        // 3. 침입 여부 판단 로직
        int intrusionVal = (EventType.INTRUSION.equals(type) || EventLevel.ALERT.equals(calculatedLevel)) ? 1 : 0;

        return DetectionEntity.builder()
                .cameraId(this.cameraId != null ? this.cameraId : 1)
                .dangerZoneId(this.dangerZoneId != null ? this.dangerZoneId : 0)
                .eventType(type)
                .eventLevel(calculatedLevel)
                .objectType(this.objectType)
                .detectedCount(this.detectedCount != null ? this.detectedCount : 0)
                .stayDurationSec(this.stayDurationSec)
                .intrusionNow(intrusionVal)
                .message(customMessage)
                .imagePath(this.imagePath)
                .eventTime(finalTime) // 🚩 보정된 시간이 저장됩니다.
                .build();
    }
}