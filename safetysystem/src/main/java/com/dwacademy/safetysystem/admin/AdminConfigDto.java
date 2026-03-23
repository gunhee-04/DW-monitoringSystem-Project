package com.dwacademy.safetysystem.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminConfigDto {

    // ===== Camera =====
    private Long cameraId;
    private String cameraCode;
    private String cameraName;
    private String cameraType;
    private String ipAddress;
    private String streamUrl;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private String status;

    // ===== DangerZone =====
    private Long zoneId;
    private String zoneName;
    private String zoneType;
    private Integer x1;
    private Integer y1;
    private Integer x2;
    private Integer y2;
    private Integer intrusionTimeSec;
    private Boolean isActive;
}