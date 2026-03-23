package com.dwacademy.safetysystem.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CameraApiResponseDto {

    private Integer intrusionSeconds;
    private RoiDto roi;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class RoiDto {
        private Integer x1;
        private Integer y1;
        private Integer x2;
        private Integer y2;
    }
}
