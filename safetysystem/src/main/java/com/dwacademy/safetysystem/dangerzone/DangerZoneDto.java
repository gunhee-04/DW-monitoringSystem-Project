package com.dwacademy.safetysystem.dangerzone;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class DangerZoneDto {

    private Long zoneId;

    @NotNull(message = "카메라 ID가 필요합니다.")
    private Long cameraId;

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
    @Min(value = 0, message = "x2는 0 이상이어야 합니다.")
    private Integer x2;

    @NotNull(message = "y2 값을 입력하세요.")
    @Min(value = 0, message = "y2는 0 이상이어야 합니다.")
    private Integer y2;

    @NotNull(message = "침입 시간을 입력하세요.")
    @Min(value = 1, message = "침입 시간은 1초 이상이어야 합니다.")
    private Integer intrusionTimeSec;

    @NotNull(message = "활성 여부를 선택하세요.")
    private Boolean isActive;
}