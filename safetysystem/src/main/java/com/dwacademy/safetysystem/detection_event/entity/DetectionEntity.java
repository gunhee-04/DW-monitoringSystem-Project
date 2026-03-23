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

    // 🚩 DB 컬럼명 people_count에 맞춤
    @Column(name = "people_count")
    private Integer detectedCount;

    // 🚩 DB 컬럼명 stay_duration에 맞춤
    @Column(name = "stay_duration")
    private Integer stayDurationSec;

    @Column(name = "intrusion_now")
    private Integer intrusionNow;

    @Column(length = 500)
    private String message;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "event_time", nullable = false)
    private Long eventTime;

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