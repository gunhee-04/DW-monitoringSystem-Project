package com.dwacademy.safetysystem.detection_event.entity;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- 프론트엔드 통신용 가상 필드 ---

    @JsonProperty("isAlert")
    public boolean fetchIsAlert() { // 이름을 fetch...로 바꿔서 Lombok과 충돌 방지
        if (this.eventLevel == null) return false;
        return this.eventLevel == EventLevel.HIGH || this.eventLevel == EventLevel.MEDIUM;
    }

    @JsonProperty("status_text")
    public String fetchStatusText() {
        if (this.eventLevel == EventLevel.HIGH) return "위험 침입 감지";
        if (this.eventLevel == EventLevel.MEDIUM) return "밀집도 주의";
        return "정상 관제 중";
    }

    @JsonProperty("level_color")
    public String fetchLevelColor() {
        if (this.eventLevel == EventLevel.HIGH) return "#ef4444";
        if (this.eventLevel == EventLevel.MEDIUM) return "#f59e0b";
        return "#10b981";
    }

    @JsonProperty("people_count")
    public Integer fetchPeopleCount() {
        return this.detectedCount != null ? this.detectedCount : 0;
    }

    @JsonProperty("created_at")
    public String fetchFormattedCreatedAt() {
        if (this.createdAt == null) return "-";
        return this.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getFormattedEventTime() {
        if (this.eventTime == null) return "-";
        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(this.eventTime),
                ZoneId.of("Asia/Seoul")
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}