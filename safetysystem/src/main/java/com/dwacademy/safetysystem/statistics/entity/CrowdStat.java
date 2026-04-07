package com.dwacademy.safetysystem.statistics.entity;

import com.dwacademy.safetysystem.camera.Camera;
import com.dwacademy.safetysystem.dangerzone.DangerZone;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "crowd_stat",
        indexes = {
                @Index(name = "idx_crowd_stat_camera_id", columnList = "camera_id"),
                @Index(name = "idx_crowd_stat_danger_zone_id", columnList = "danger_zone_id"),
                @Index(name = "idx_crowd_stat_measured_at", columnList = "measured_at")
        }
)
@Getter
@Setter
public class CrowdStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false)
    private Camera camera;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "danger_zone_id")
    private DangerZone dangerZone;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "people_count", nullable = false)
    private Integer peopleCount;

    @Column(name = "density_value", nullable = false)
    private Double densityValue;

    @Column(name = "increase_rate")
    private Double increaseRate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //  자동 시간 처리
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}