package com.dwacademy.safetysystem.detection_event.controller;

import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.dto.EventResponseDto;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetectionController { // 🚩 @RequestMapping("/api/events")를 제거했습니다.

    private final DetectionService detectionService;

    /**
     * 1. 데이터 수신 (YOLO AI 서버용)
     * 이 주소는 외부에서 던지는 주소이므로 그대로 둡니다.
     */
    @PostMapping("/api/events")
    public ResponseEntity<String> createEvent(@RequestBody DetectionRequestDto dto) {
        detectionService.processEvent(dto);
        return ResponseEntity.ok("Event processed and broadcasted successfully");
    }

    /**
     * 2. 모든 이벤트 목록 조회 (대시보드 home.html용)
     * 🚩 프론트엔드가 fetch("/get_logs")를 호출하므로 주소를 똑같이 맞춥니다.
     */
    @GetMapping("/get_logs")
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        // 방금 만드신 필드명(people_count 등)이 담긴 DTO 리스트를 반환합니다.
        return ResponseEntity.ok(detectionService.getAllEventsForFront());
    }

    /**
     * 3. 특정 카메라별 조회 (필요한 경우 사용)
     */
    @GetMapping("/api/events/camera/{cameraId}")
    public ResponseEntity<List<EventResponseDto>> getEventsByCamera(@PathVariable Integer cameraId) {
        List<EventResponseDto> dtoList = detectionService.getEventsByCamera(cameraId)
                .stream()
                .map(EventResponseDto::new)
                .toList();
        return ResponseEntity.ok(dtoList);
    }
}