package com.dwacademy.safetysystem.detection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detection")
public class MembersController {

    @PostMapping
    public ResponseEntity<String> receiveDetection(@RequestBody MembersRequestDto request) {

        System.out.println("===== Detection API 호출됨 =====");

        System.out.println("카메라 ID: " + request.getCameraId());
        System.out.println("이벤트 타입: " + request.getEventType());
        System.out.println("위험도: " + request.getEventLevel());

        System.out.println("감지 수: " + request.getDetectedCount());
        System.out.println("체류 시간: " + request.getStayDurationSec());

        System.out.println("메시지: " + request.getMessage());
        System.out.println("이벤트 시간: " + request.getEventTime());

        System.out.println("===============================");

        return ResponseEntity.ok("success");
    }
}
