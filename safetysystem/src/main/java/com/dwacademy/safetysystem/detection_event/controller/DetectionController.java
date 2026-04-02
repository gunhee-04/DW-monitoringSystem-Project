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

    // 1. 실시간 이벤트 (SSE 전송용)
    @PostMapping("/api/events")
    public ResponseEntity<String> createEvent(@RequestBody(required = false) DetectionRequestDto dto) {
        if (dto == null) return ResponseEntity.badRequest().body("Payload is missing");

        try {
            EventResponseDto responseDto = detectionService.processEvent(dto);
            if (responseDto == null) return ResponseEntity.ok("No process");

            // 🚩 [수정 포인트] Map으로 감싸지 않고 responseDto(알맹이)만 전송
            // 이렇게 해야 프론트엔드의 state.realtimeLogs.unshift(data)가 에러 없이 작동합니다.
            SseController.emitters.forEach(emitter -> {
                try {
                    Map<String, Object> safeData = convertToSafeMap(responseDto);

                    emitter.send(SseEmitter.event()
                            .name("newDetection")
                            .data(safeData));

                    if (responseDto.isAlert()) {
                        emitter.send(SseEmitter.event()
                                .name("danger_alert")
                                .data(safeData)); // ✅ 여기 수정
                    }

                } catch (Exception e) {
                    SseController.emitters.remove(emitter);
                }
            });

            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error(">>> SSE 전송 중 예외 발생: ", e);
            return ResponseEntity.ok("Error handled");
        }
    }

    // 2. 대시보드 초기 로딩 (전체 로그 조회용)
    @GetMapping("/get_logs")
    public ResponseEntity<Map<String, Object>> getAllEvents() {
        List<EventResponseDto> logs = detectionService.getAllEventsForFront();

        // 🚩 [수정 포인트] 여기는 renderDashboard()가 d.logs와 d.summary를 찾으므로 Map 구조 유지
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> safeLogs = logs.stream()
                .map(this::convertToSafeMap)
                .toList();

        response.put("logs", safeLogs);

        Map<String, Object> summary = new HashMap<>();
        // card1에 실제 인원수를 넣어줌 (toFixed 에러 방지용 기본값 0)
        summary.put("card1", logs.isEmpty() ? 0 : logs.get(0).getPeople_count());
        summary.put("card2", 0);
        summary.put("card3", 0.0);
        response.put("summary", summary);

        // 차트나 테이블이 d.hourly를 참조할 수 있으므로 빈 리스트 추가
        response.put("hourly", new ArrayList<>());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> convertToSafeMap(EventResponseDto dto) {
        Map<String, Object> map = new HashMap<>();

        map.put("status_text", dto.getStatus_text());
        map.put("people_count", dto.getPeople_count());
        map.put("created_at", dto.getCreated_at());
        map.put("level_color", dto.getLevel_color());
        map.put("alert", dto.isAlert());

        return map;
    }
}