package com.dwacademy.safetysystem.camera;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraDto {

    private Long cameraId;

    @NotBlank(message = "카메라 코드를 입력하세요.")
    private String cameraCode;

    @NotBlank(message = "카메라 이름을 입력하세요.")
    private String cameraName;

    @NotBlank(message = "카메라 타입을 입력하세요. (WEBCAM / DRONE / CCTV)")
    @Pattern(
            regexp = "WEBCAM|DRONE|CCTV",
            message = "카메라 타입은 WEBCAM, DRONE, CCTV만 가능합니다."
    )
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
    @Pattern(
            regexp = "ACTIVE|INACTIVE|ERROR",
            message = "상태값은 ACTIVE, INACTIVE, ERROR만 가능합니다."
    )
    private String status;
}