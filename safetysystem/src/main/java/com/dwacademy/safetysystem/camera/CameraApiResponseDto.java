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
    
    private String streamUrl;  // 추가
    
    //roi객체에 순서 제대로 들어갈 수 있게 build
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
