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

import java.time.LocalDateTime;
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

    private static final int CROWD_WARNING_THRESHOLD = 3;
    private static final int CROWD_CRITICAL_THRESHOLD = 5;

    @Transactional
    public void processEvent(DetectionRequestDto dto) {
        Object[] analysis = analyzeEvent(dto);
        EventLevel level = (EventLevel) analysis[0];
        String message = (String) analysis[1];

        // 🚩 [수정] 시간 비교 대신, 'isRead=1'로 저장된 가장 최근 데이터를 가져옴
        DetectionEntity lastAlert = repository.findFirstByEventLevelAndIsReadOrderByIdDesc(EventLevel.HIGH, 1);

        boolean alreadySent = false;
        if (lastAlert != null) {
            // 마지막 알림(isRead=1)이 1분 이내에 생성되었다면 중복으로 판단
            // (단순히 시간 차이가 1분 미만인지 체크)
            if (lastAlert.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
                alreadySent = true;
            }
        }

        DetectionEntity entity = dto.toEntity(message, level);

        // 위험 상황이고 + 최근 1분 내에 팝업용(isRead=1) 데이터를 만든 적이 없을 때만 1로 설정
        if (level == EventLevel.HIGH && !alreadySent) {
            entity.setIsRead(1);
            log.info(">>> 🔔 [신규] 팝업 발생 신호를 보냅니다.");
        } else {
            entity.setIsRead(0);
            log.info(">>> 🗒️ [중복] 로그만 기록하고 팝업은 스킵합니다.");
        }

        DetectionEntity saved = repository.save(entity);
        statisticsService.saveFromDetection(saved);

        // 알림 로그 생성 (여기는 HIGH면 다 생성해도 됨)
        if (level == EventLevel.HIGH) {
            alertService.createAlert(
                    saved.getId().longValue(),
                    saved.getEventType().name(),
                    saved.getEventLevel().name(),
                    saved.getMessage()
            );
        }

        broadcast(saved);
    }

    private Object[] analyzeEvent(DetectionRequestDto dto) {
        int count = (dto.getDetectedCount() != null) ? dto.getDetectedCount() : 0;
        int hour = LocalTime.now().getHour();
        boolean isNight = (hour >= 22 || hour <= 5);

        EventLevel level = EventLevel.NORMAL;
        String message = "실시간 정상 모니터링 중입니다.";

        if ("INTRUSION".equals(dto.getEventType())) {
            level = EventLevel.HIGH;
            message = "🚨 [긴급] 구역 내 침입자 감지!";
        } else if (count >= CROWD_CRITICAL_THRESHOLD || (isNight && count > 0)) {
            level = EventLevel.HIGH;
            message = isNight ? "🌙 [야간경계] 미확인 인원 감지!" : String.format("🚨 [위험] %d명 감지!", count);
        } else if (count >= CROWD_WARNING_THRESHOLD) {
            level = EventLevel.MEDIUM;
            message = String.format("⚠️ [주의] 인원 %d명 감지!", count);
        }

        return new Object[]{level, message};
    }

    private void broadcast(DetectionEntity saved) {
        EventResponseDto response = new EventResponseDto(saved);

        SseController.emitters.forEach(emitter -> {
            try {
                // [이벤트 1] 실시간 로그창 업데이트용 (항상 전송)
                emitter.send(SseEmitter.event()
                        .name("newDetection")
                        .data(response));

                // [이벤트 2] 🚩 팝업 알림용 (오직 1분 이내 첫 발생시에만 전송!)
                // 9명->7명으로 바뀔 때, 7명 데이터는 isRead가 0이므로 이 'alert' 이벤트가 안 나갑니다.
                if (saved.getIsRead() == 1) {
                    emitter.send(SseEmitter.event()
                            .name("alert")
                            .data(response));
                }
            } catch (Exception e) {
                SseController.emitters.remove(emitter);
            }
        });
    }

    public List<DetectionEntity> getAllEvents() {
        return repository.findAllByOrderByIdDesc();
    }

    public List<DetectionEntity> getEventsByLevel(EventLevel level) {
        return repository.findByEventLevelOrderByIdDesc(level);
    }

    public List<DetectionEntity> getEventsByCamera(Integer cameraId) {
        return repository.findByCameraIdOrderByIdDesc(cameraId);
    }

    public List<EventResponseDto> getAllEventsForFront() {
        return repository.findAllByOrderByIdDesc().stream()
                .map(EventResponseDto::new)
                .toList();
    }
}