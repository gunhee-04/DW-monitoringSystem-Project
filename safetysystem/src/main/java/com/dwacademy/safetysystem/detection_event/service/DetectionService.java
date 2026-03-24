package com.dwacademy.safetysystem.detection_event.service;

import com.dwacademy.safetysystem.alert.AlertService;
import com.dwacademy.safetysystem.detection_event.controller.SseController;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.dto.EventResponseDto;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DetectionService {

    private final DetectionEventRepository repository;
    private final AlertService alertService; // 주입된 서비스 활용

    // 판단 기준 상수
    private static final int CROWD_WARNING_THRESHOLD = 5;
    private static final int CROWD_CRITICAL_THRESHOLD = 20;

    @Transactional
    public void processEvent(DetectionRequestDto dto) {
        // 1. 위험도 및 메시지 판단 로직 실행
        Object[] analysis = analyzeEvent(dto);
        EventLevel level = (EventLevel) analysis[0];
        String message = (String) analysis[1];

        // 2. 엔티티 생성 및 DB 저장 (detection_event 테이블)
        DetectionEntity entity = dto.toEntity(message);
        entity.setEventLevel(level);
        DetectionEntity saved = repository.save(entity);

        // 🚩 3. [팀원 코드 연동] 알림 로그 생성 (alert_log 테이블)
        // 저장된 엔티티의 정보를 팀원이 만든 createAlert 메서드에 전달합니다.
        alertService.createAlert(
                saved.getId().longValue(),    // detectionEventId (Long 타입 변환)
                saved.getEventType().name(),         // alertType
                saved.getEventLevel().name(), // severity (위험 등급 문자열)
                saved.getMessage()            // alertMessage
        );

        // 4. 생성된 데이터를 DTO로 변환하여 실시간 전송 (SSE)
        EventResponseDto response = new EventResponseDto(saved);

        for (SseEmitter emitter : SseController.emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("newDetection")
                        .data(response));
            } catch (Exception e) {
                SseController.emitters.remove(emitter);
            }
        }
    }

    /**
     * 상황별 위험도와 메시지를 결정하는 내부 로직
     */
    private Object[] analyzeEvent(DetectionRequestDto dto) {
        int count = (dto.getDetectedCount() != null) ? dto.getDetectedCount() : 0;
        int hour = LocalTime.now().getHour();
        boolean isNight = (hour >= 22 || hour <= 5);

        EventLevel level = EventLevel.NORMAL;
        String message = "실시간 정상 모니터링 중입니다.";

        if ("INTRUSION".equals(dto.getEventType())) {
            level = EventLevel.ALERT;
            message = "🚨 [긴급] 허가되지 않은 구역에 침입자가 감지되었습니다!";
        } else if (count >= CROWD_CRITICAL_THRESHOLD || (isNight && count > 0)) {
            level = EventLevel.ALERT;
            message = isNight ? "🌙 [야간경계] 심야 구역 내 미확인 인원 감지!"
                    : String.format("🚨 [위험] 현재 %d명 감지! 밀집도가 매우 높습니다.", count);
        } else if (count >= CROWD_WARNING_THRESHOLD) {
            level = EventLevel.WARNING;
            message = String.format("⚠️ [주의] 인원이 %d명으로 증가했습니다. 모니터링을 강화하세요.", count);
        }

        return new Object[]{level, message};
    }

    // --- 조회 메서드 ---
    public List<DetectionEntity> getAllEvents() {
        return repository.findAllByOrderByIdDesc();
    }

    public List<EventResponseDto> getAllEventsForFront() {
        return repository.findAllByOrderByIdDesc().stream()
                .map(EventResponseDto::new)
                .toList();
    }

    public List<DetectionEntity> getEventsByLevel(EventLevel level) {
        return repository.findByEventLevelOrderByIdDesc(level);
    }

    public List<DetectionEntity> getEventsByCamera(Integer cameraId) {
        return repository.findByCameraIdOrderByIdDesc(cameraId);
    }
}