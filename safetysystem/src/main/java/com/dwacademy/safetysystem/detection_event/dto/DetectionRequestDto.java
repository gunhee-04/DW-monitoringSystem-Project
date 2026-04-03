package com.dwacademy.safetysystem.detection_event.dto;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectionRequestDto {

    // "2", "CAM-02" 둘 다 받을 수 있게 String 유지
    @JsonAlias({"cameraId", "camera_id"})
    private String cameraId;

    @JsonAlias({"dangerZoneId", "danger_zone_id"})
    private Integer dangerZoneId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_level")
    private String eventLevel;

    @JsonAlias({"objectType", "object_type"})
    private String objectType;

    @JsonAlias({"detectedCount", "detected_count"})
    private Integer detectedCount;

    @JsonAlias({"stayDurationSec", "stay_duration_sec"})
    private Integer stayDurationSec;

    // image / imagePath / image_path 다 대응
    @JsonAlias({"image", "imagePath", "image_path"})
    private String imagePath;

    @JsonAlias({"eventTime", "event_time"})
    private String eventTime;

    @JsonAlias({"eventLatitude", "event_latitude"})
    private Double eventLatitude;

    @JsonAlias({"eventLongitude", "event_longitude"})
    private Double eventLongitude;

    @JsonAlias({"eventAddress", "event_address"})
    private String eventAddress;

    public DetectionEntity toEntity(String customMessage, EventLevel calculatedLevel) {
        EventType type = parseEventType(this.eventType);
        Long finalTime = parseEventTime(this.eventTime);
        Integer numericCameraId = parseCameraId(this.cameraId);
        int intrusionVal = EventType.INTRUSION.equals(type) ? 1 : 0;

        String finalAddress = normalizeAddress(this.eventAddress, this.eventLatitude, this.eventLongitude);

        return DetectionEntity.builder()
                .eventType(type)
                .eventLevel(calculatedLevel)
                .objectType(this.objectType)
                .detectedCount(this.detectedCount != null ? this.detectedCount : 0)
                .stayDurationSec(this.stayDurationSec != null ? this.stayDurationSec : 0)
                .intrusionNow(intrusionVal)
                .message(customMessage)
                .imagePath(this.imagePath)
                .eventTime(finalTime)
                .eventLatitude(this.eventLatitude)
                .eventLongitude(this.eventLongitude)
                .eventAddress(finalAddress)
                .build();
    }

    private EventType parseEventType(String rawEventType) {
        if (rawEventType == null || rawEventType.isBlank()) {
            return EventType.CROWD;
        }

        try {
            return EventType.valueOf(rawEventType.trim().toUpperCase());
        } catch (Exception e) {
            return EventType.CROWD;
        }
    }

    private Long parseEventTime(String rawEventTime) {
        if (rawEventTime == null || rawEventTime.isBlank()) {
            return System.currentTimeMillis() / 1000;
        }

        try {
            // "2026-04-01 10:30:00"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(rawEventTime, formatter);
            return dateTime.toEpochSecond(ZoneOffset.of("+09:00"));
        } catch (Exception ignored) {
        }

        try {
            // 이미 epoch 초로 온 경우
            return Long.parseLong(rawEventTime);
        } catch (Exception ignored) {
        }

        return System.currentTimeMillis() / 1000;
    }

    private Integer parseCameraId(String rawCameraId) {
        if (rawCameraId == null || rawCameraId.isBlank()) {
            return 1;
        }

        try {
            // "CAM-02" -> "02" -> 2
            String onlyNumber = rawCameraId.replaceAll("[^0-9]", "");
            if (onlyNumber.isBlank()) {
                return 1;
            }
            return Integer.parseInt(onlyNumber);
        } catch (Exception e) {
            return 1;
        }
    }

    private String normalizeAddress(String address, Double latitude, Double longitude) {
        if (address != null && !address.isBlank()) {
            return address.trim();
        }

        if (latitude != null && longitude != null && latitude != 0 && longitude != 0) {
            return latitude + ", " + longitude;
        }

        return "위치 정보 없음";
    }
}