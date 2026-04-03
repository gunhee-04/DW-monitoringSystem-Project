package com.dwacademy.safetysystem.detection_event.entity;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "detection_event")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DetectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "camera_id", nullable = false)
    private Integer cameraId;

    @Column(name = "danger_zone_id")
    private Integer dangerZoneId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_level", nullable = false)
    private EventLevel eventLevel;

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "detected_count")
    private Integer detectedCount;

    @Column(name = "stay_duration_sec")
    private Integer stayDurationSec;

    @Column(name = "intrusion_now")
    private Integer intrusionNow;

    @Column(length = 500)
    private String message;

    @Lob
    @Column(name = "image_path", columnDefinition = "LONGTEXT")
    private String imagePath;

    @Column(name = "event_time", nullable = false)
    private Long eventTime;

    @Column(name = "is_read")
    @Builder.Default
    private Integer isRead = 0;

    @Column(name = "is_deleted")
    @Builder.Default
    private Integer isDeleted = 0;

    @Column(name = "event_latitude")
    private Double eventLatitude;

    @Column(name = "event_longitude")
    private Double eventLongitude;

    @Column(name = "event_address", length = 255)
    private String eventAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public String getFormattedEventTime() {
        if (this.eventTime == null) return "-";
        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(this.eventTime),
                ZoneId.of("Asia/Seoul")
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}