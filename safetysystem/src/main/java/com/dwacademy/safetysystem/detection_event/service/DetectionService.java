package com.dwacademy.safetysystem.detection_event.service;

import com.dwacademy.safetysystem.alert.AlertService;
import com.dwacademy.safetysystem.detection_event.controller.SseController;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.dto.EventResponseDto;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import com.dwacademy.safetysystem.statistics.service.StatisticsService;
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
    private final AlertService alertService;
    private final StatisticsService statisticsService;

    private static final int CROWD_WARNING_THRESHOLD = 5;
    private static final int CROWD_CRITICAL_THRESHOLD = 20;

    @Transactional
    public void processEvent(DetectionRequestDto dto) {
        // 1. 자바에서 직접 위험도 분석
        Object[] analysis = analyzeEvent(dto);
        EventLevel level = (EventLevel) analysis[0];
        String message = (String) analysis[1];

        // 2. 분석된 결과로 엔티티 생성 및 저장
        DetectionEntity entity = dto.toEntity(message, level);
        DetectionEntity saved = repository.save(entity);

        // 3. 통계 저장
        statisticsService.saveFromDetection(saved);

        // 4. 팀원 알림 로그 연동
        alertService.createAlert(
                saved.getId().longValue(),
                saved.getEventType().name(),
                saved.getEventLevel().name(),
                saved.getMessage()
        );

        // 5. SSE 실시간 브로드캐스트
        broadcast(saved);
    }

    private Object[] analyzeEvent(DetectionRequestDto dto) {
        int count = (dto.getDetectedCount() != null) ? dto.getDetectedCount() : 0;
        int hour = LocalTime.now().getHour();
        boolean isNight = (hour >= 22 || hour <= 5); // 기존 야간 로직 유지

        EventLevel level = EventLevel.NORMAL;
        String message = "실시간 정상 모니터링 중입니다.";

        // 1. 침입인 경우 (가장 높은 우선순위)
        if ("INTRUSION".equals(dto.getEventType())) {
            level = EventLevel.HIGH;
            message = "🚨 [긴급] 구역 내 침입자 감지!";
        }
        // 2. 야간에 사람이 있거나, 인원수가 임계치(20명)를 넘은 경우 -> HIGH
        else if (count >= CROWD_CRITICAL_THRESHOLD || (isNight && count > 0)) {
            level = EventLevel.HIGH;
            message = isNight ? "🌙 [야간경계] 미확인 인원 감지!" : String.format("🚨 [위험] %d명 감지!", count);
        }
        // 3. 인원수가 주의 수준(5명)을 넘은 경우 -> MEDIUM
        else if (count >= CROWD_WARNING_THRESHOLD) {
            level = EventLevel.MEDIUM;
            message = String.format("⚠️ [주의] 인원 %d명 감지!", count);
        }
        // 그 외에는 처음 설정한 NORMAL과 "정상" 메시지가 나갑니다.

        return new Object[]{level, message};
    }

    private void broadcast(DetectionEntity saved) {
        EventResponseDto response = new EventResponseDto(saved);
        SseController.emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("newDetection").data(response));
            } catch (Exception e) {
                SseController.emitters.remove(emitter);
            }
        });
    }

    // --- 조회 메서드 (MonitoringViewController에서 사용) ---

    /**
     * 전체 이벤트 엔티티 조회
     */
    public List<DetectionEntity> getAllEvents() {
        return repository.findAllByOrderByIdDesc();
    }

    /**
     * 위험도 레벨별 엔티티 조회
     */
    public List<DetectionEntity> getEventsByLevel(EventLevel level) {
        return repository.findByEventLevelOrderByIdDesc(level);
    }

    /**
     * 카메라별 엔티티 조회
     */
    public List<DetectionEntity> getEventsByCamera(Integer cameraId) {
        return repository.findByCameraIdOrderByIdDesc(cameraId);
    }

    /**
     * 대시보드 API용 (EventResponseDto 반환)
     */
    public List<EventResponseDto> getAllEventsForFront() {
        return repository.findAllByOrderByIdDesc().stream()
                .map(EventResponseDto::new)
                .toList();
    }
}