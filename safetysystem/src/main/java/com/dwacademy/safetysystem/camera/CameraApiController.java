package com.dwacademy.safetysystem.camera;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cameras")
public class CameraApiController {

    private final CameraApiService cameraApiService;

    @GetMapping("/{cameraCode}/config")
    public CameraApiResponseDto getCameraConfig(@PathVariable String cameraCode) {
        return cameraApiService.getCameraConfig(cameraCode);
    }
}