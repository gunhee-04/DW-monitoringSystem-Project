package com.dwacademy.safetysystem.admin;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminConfigDto {

    // ===== Camera =====

    private Long cameraId;

    @NotBlank(message = "카메라 코드를 입력하세요.")
    private String cameraCode;

    @NotBlank(message = "카메라 이름을 입력하세요.")
    private String cameraName;

    @NotBlank(message = "카메라 타입을 입력하세요. (WEBCAM / DRONE / CCTV)")
    private String cameraType;

    @NotBlank(message = "IP 주소를 입력하세요.")
    private String ipAddress;

    @NotBlank(message = "스트림 URL을 입력하세요.")
    private String streamUrl;

    @NotBlank(message = "위치명을 입력하세요.")
    private String locationName;

    @NotNull(message = "위도를 입력하세요.")
    private Double latitude;

    @NotNull(message = "경도를 입력하세요.")
    private Double longitude;

    @NotBlank(message = "상태값을 입력하세요.")
    @Pattern(regexp = "ACTIVE|INACTIVE|ERROR", message = "상태값은 ACTIVE, INACTIVE, ERROR만 가능합니다.")
    private String status;


    // ===== DangerZone =====

    private Long zoneId;

    @NotBlank(message = "구역 이름을 입력하세요.")
    private String zoneName;

    @NotBlank(message = "구역 타입을 입력하세요.")
    private String zoneType;

    @NotNull(message = "x1 값을 입력하세요.")
    @Min(value = 0, message = "x1은 0 이상이어야 합니다.")
    private Integer x1;

    @NotNull(message = "y1 값을 입력하세요.")
    @Min(value = 0, message = "y1은 0 이상이어야 합니다.")
    private Integer y1;

    @NotNull(message = "x2 값을 입력하세요.")
    @Min(value = 0, message = "x2은 0 이상이어야 합니다.")
    private Integer x2;

    @NotNull(message = "y2 값을 입력하세요.")
    @Min(value = 0, message = "y2은 0 이상이어야 합니다.")
    private Integer y2;

    @NotNull(message = "침입 시간을 입력하세요.")
    @Min(value = 1, message = "침입 시간은 1초 이상이어야 합니다.")
    private Integer intrusionTimeSec;

    @NotNull(message = "활성 여부를 선택하세요.")
    private Boolean isActive;
}