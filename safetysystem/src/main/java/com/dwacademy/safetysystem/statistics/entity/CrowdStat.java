package com.dwacademy.safetysystem.statistics.entity;

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

    @Column(name = "camera_id", nullable = false)
    private Long cameraId;

    @Column(name = "danger_zone_id")
    private Long dangerZoneId;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "people_count", nullable = false)
    private Integer peopleCount;

    @Column(name = "density_value", nullable = false)
    private Double densityValue;

    @Column(name = "increase_rate")
    private Double increaseRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}