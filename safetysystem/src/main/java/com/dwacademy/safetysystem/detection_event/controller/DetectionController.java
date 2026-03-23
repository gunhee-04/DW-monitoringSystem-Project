package com.dwacademy.safetysystem.detection_event.controller;

import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class DetectionController {

    private final DetectionService detectionService;

    /**
     * 1. 탐지 이벤트 수신 및 처리 (Flask AI 서버에서 호출)
     * 🚩 기존 createEvent를 processEvent 호출 방식으로 변경했습니다.
     */
    @PostMapping
    public ResponseEntity<String> createEvent(@RequestBody DetectionRequestDto dto) {
        // 서비스의 지능형 로직(판단 + 저장 + SSE 전송) 실행
        detectionService.processEvent(dto);
        return ResponseEntity.ok("Event processed and broadcasted successfully");
    }

    /**
     * 2. 모든 이벤트 목록 조회 (API 확인용)
     */
    @GetMapping
    public ResponseEntity<List<DetectionEntity>> getAllEvents() {
        return ResponseEntity.ok(detectionService.getAllEvents());
    }

    /**
     * 3. 특정 카메라별 이벤트 목록 조회
     */
    @GetMapping("/camera/{cameraId}")
    public ResponseEntity<List<DetectionEntity>> getEventsByCamera(@PathVariable Integer cameraId) {
        // 서비스에 해당 메서드가 없다면 추가가 필요할 수 있습니다.
        return ResponseEntity.ok(detectionService.getEventsByCamera(cameraId));
    }
}