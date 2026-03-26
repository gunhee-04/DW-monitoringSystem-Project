package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectionRequestDto {

    // 1. 파이썬에서 "CAM-02"라고 보내므로 String으로 받아야 400 에러가 안 납니다.
    @JsonProperty("cameraId")
    private String cameraId;

    private Integer dangerZoneId;

    // 2. 파이썬의 snake_case 대응 (@JsonProperty 사용)
    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_level")
    private String eventLevel;

    private String objectType;
    private Integer detectedCount;
    private Integer stayDurationSec;

    // 3. 파이썬은 image라는 키로 보냅니다.
    @JsonProperty("image")
    private String imagePath;

    private String eventTime;

    /**
     * 엔티티 변환 로직 (파이썬 데이터를 자바 규격에 맞게 보정)
     */
    public DetectionEntity toEntity(String customMessage, EventLevel calculatedLevel) {

        // [타입 변환] EventType
        EventType type = EventType.CROWD;
        try {
            if (this.eventType != null) {
                type = EventType.valueOf(this.eventType.toUpperCase());
            }
        } catch (Exception e) {
            // 변환 실패 시 기본값 유지
        }

        // [시간 변환] 문자열 -> Long (Epoch Second)
        Long finalTime;
        try {
            if (this.eventTime != null && this.eventTime.contains("-")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(this.eventTime, formatter);
                finalTime = dateTime.toEpochSecond(java.time.ZoneOffset.of("+09:00"));
            } else {
                finalTime = System.currentTimeMillis() / 1000;
            }
        } catch (Exception e) {
            finalTime = System.currentTimeMillis() / 1000;
        }

        // [카메라 ID 변환] "CAM-02" -> 2 (숫자만 추출)
        Integer numericCameraId = 1;
        try {
            if (this.cameraId != null) {
                numericCameraId = Integer.parseInt(this.cameraId.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            numericCameraId = 1;
        }

        int intrusionVal = (EventType.INTRUSION.equals(type) || EventLevel.MEDIUM.equals(calculatedLevel)) ? 1 : 0;

        return DetectionEntity.builder()
                .cameraId(numericCameraId)
                .dangerZoneId(this.dangerZoneId != null ? this.dangerZoneId : 0)
                .eventType(type)
                .eventLevel(calculatedLevel)
                .objectType(this.objectType)
                .detectedCount(this.detectedCount != null ? this.detectedCount : 0)
                .stayDurationSec(this.stayDurationSec)
                .intrusionNow(intrusionVal)
                .message(customMessage)
                .imagePath(this.imagePath)
                .eventTime(finalTime)
                .build();
    }
}