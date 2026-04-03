package com.dwacademy.safetysystem.detection_event.controller;

import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.dto.EventResponseDto;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetectionController {

    private final DetectionService detectionService;

    @PostMapping("/api/events")
    public ResponseEntity<String> createEvent(@RequestBody(required = false) DetectionRequestDto dto) {
        if (dto == null) return ResponseEntity.badRequest().body("Payload is missing");

        try {
            detectionService.processEvent(dto);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error("이벤트 처리 실패", e);
            return ResponseEntity.internalServerError().body("Error");
        }
    }

    @GetMapping("/get_logs")
    public ResponseEntity<Map<String, Object>> getAllEvents() {
        List<EventResponseDto> logs = detectionService.getAllEventsForFront();

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> safeLogs = logs.stream()
                .map(this::convertToSafeMap)
                .toList();

        response.put("logs", safeLogs);
        response.put("summary", null);
        response.put("hourly", Collections.emptyList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/events/camera/{cameraId}")
    public ResponseEntity<List<EventResponseDto>> getEventsByCamera(@PathVariable("cameraId") Integer cameraId) {
        List<EventResponseDto> dtoList = detectionService.getEventsByCamera(cameraId)
                .stream()
                .map(entity -> {
                    EventResponseDto dto = new EventResponseDto(entity);
                    dto.setCameraLocation(entity.getEventAddress() != null ? entity.getEventAddress() : "위치 정보 없음");
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/api/events/mark-as-read")
    public ResponseEntity<String> markAsRead() {
        detectionService.markAllAlertsAsRead();
        return ResponseEntity.ok("모든 알림 읽음 처리 완료");
    }

    private Map<String, Object> convertToSafeMap(EventResponseDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("status_text", dto.getStatus_text());
        map.put("people_count", dto.getPeople_count());
        map.put("created_at", dto.getCreated_at());
        map.put("level_color", dto.getLevel_color());
        map.put("alert", dto.isAlert());
        map.put("event_level", dto.getEvent_level());
        map.put("address", dto.getAddress());
        map.put("camera_location", dto.getCameraLocation());
        return map;
    }
}