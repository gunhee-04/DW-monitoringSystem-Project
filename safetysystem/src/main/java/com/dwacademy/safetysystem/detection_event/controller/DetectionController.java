package com.dwacademy.safetysystem.detection_event.controller;

import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.dto.EventResponseDto;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetectionController {

    private final DetectionService detectionService;

    @PostMapping("/api/events")
    public ResponseEntity<String> createEvent(@RequestBody(required = false) DetectionRequestDto dto) {
        if (dto == null) return ResponseEntity.badRequest().body("Payload is missing");
        detectionService.processEvent(dto);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/get_logs")
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        return ResponseEntity.ok(detectionService.getAllEventsForFront());
    }

    @GetMapping("/api/events/camera/{cameraId}")
    public ResponseEntity<List<EventResponseDto>> getEventsByCamera(@PathVariable("cameraId") Integer cameraId) {
        // 🚩 빨간 줄 해결: Entity 리스트를 DTO 리스트로 변환
        List<EventResponseDto> dtoList = detectionService.getEventsByCamera(cameraId)
                .stream()
                .map(EventResponseDto::new)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/api/events/mark-as-read")
    public ResponseEntity<String> markAsRead() {
        detectionService.markAllAlertsAsRead();
        return ResponseEntity.ok("모든 알림 읽음 처리 완료");
    }

}