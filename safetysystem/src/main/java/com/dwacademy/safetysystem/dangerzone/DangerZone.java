package com.dwacademy.safetysystem.dangerzone;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "danger_zone")
@Entity
public class DangerZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long cameraId;

    @Column(nullable=false)
    private String zoneName;

    @Column(nullable=false)
    private String zoneType;  // DANGER CROWD WATCH

    private Integer x1;
    private Integer y1;
    private Integer x2;
    private Integer y2;

    private Integer intrusionTimeSec;

    @Column(nullable=false)
    private Boolean isActive;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void deactivate(){
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
