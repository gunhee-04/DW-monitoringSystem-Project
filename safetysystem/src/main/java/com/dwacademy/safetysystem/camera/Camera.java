package com.dwacademy.safetysystem.camera;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "camera")
@Entity
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cameraCode;

    @Column(nullable = false)
    private String cameraName;

    private String cameraType;   // WEBCAM DRONE CCTV

    private String ipAddress;

    private String streamUrl;

    private String locationName;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private String status; // ACTIVE INACTIVE ERROR

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void deactivate() {
        this.status = "INACTIVE";
        this.updatedAt = LocalDateTime.now();
    }


    }


