package com.dwacademy.safetysystem.camera;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cameras")
public class CameraApiController {

    private final CameraApiService cameraApiService;
    private final CameraService cameraService;

    //flask에 json객체로 전달(Responsebody)
    @GetMapping("/{cameraCode}/config")
    public CameraApiResponseDto getCameraConfig(@PathVariable String cameraCode) {
        return cameraApiService.getCameraConfig(cameraCode);
    }


    //카메라 상태변경
    @PatchMapping("/{id}/status")
    public Map<String, Object> toggleStatus(@PathVariable Long id){
        String status = cameraService.toggleStatus(id);
        return Map.of(
                "success", true,
                "status", status
        );
    }
}