package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectionRequestDto {

    @JsonProperty("cameraId")
    private String cameraId;

    @JsonProperty("dangerZoneId")
    private Integer dangerZoneId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_level")
    private String eventLevel;

    @JsonProperty("objectType")
    private String objectType;

    @JsonProperty("detectedCount")
    private Integer detectedCount;

    @JsonProperty("stayDurationSec")
    private Integer stayDurationSec;

    @JsonProperty("image")
    private String imagePath;

    @JsonProperty("eventTime")
    private Long eventTime;

    public DetectionEntity toEntity(String customMessage, EventLevel calculatedLevel) {

        EventType type = EventType.NORMAL;
        try {
            if (this.eventType != null && !this.eventType.isBlank()) {
                type = EventType.valueOf(this.eventType.toUpperCase());
            }
        } catch (Exception e) {
            type = EventType.NORMAL;
        }

        Long finalTime = (this.eventTime != null) ? this.eventTime : System.currentTimeMillis() / 1000;

        Integer numericCameraId = 1;
        try {
            if (this.cameraId != null && !this.cameraId.isBlank()) {
                String onlyNumber = this.cameraId.replaceAll("[^0-9]", "");
                if (!onlyNumber.isBlank()) {
                    numericCameraId = Integer.parseInt(onlyNumber);
                }
            }
        } catch (Exception e) {
            numericCameraId = 1;
        }

        int intrusionVal = EventType.INTRUSION.equals(type) ? 1 : 0;

        return DetectionEntity.builder()
                .cameraId(numericCameraId)
                .dangerZoneId(this.dangerZoneId != null ? this.dangerZoneId : 0)
                .eventType(type)
                .eventLevel(calculatedLevel)
                .objectType(this.objectType)
                .detectedCount(this.detectedCount != null ? this.detectedCount : 0)
                .stayDurationSec(this.stayDurationSec != null ? this.stayDurationSec : 0)
                .intrusionNow(intrusionVal)
                .message(customMessage)
                .imagePath(this.imagePath)
                .eventTime(finalTime)
                .build();
    }
}