package com.dwacademy.safetysystem.detection_event.service;

import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.controller.SseController;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DetectionService {

    private final DetectionEventRepository repository;

    // 1. 판단 기준 상수 설정
    private static final int CROWD_WARNING_THRESHOLD = 5;
    private static final int CROWD_CRITICAL_THRESHOLD = 20;

    /**
     * 실시간 탐지 이벤트를 처리하고 DB 저장 및 SSE 알림을 전송합니다.
     */
    @Transactional
    public void processEvent(DetectionRequestDto dto) {
        int count = (dto.getDetectedCount() != null) ? dto.getDetectedCount() : 0;

        // 1. 기본값은 NORMAL
        EventLevel determinedLevel = EventLevel.NORMAL;
        String detailMessage = String.format("카메라 %d번 정상 모니터링 중", dto.getCameraId());

        // 2. 3단계 판단 로직
        // [ALERT] - 가장 위험한 상황 (인파 밀집 또는 야간 감지)
        int hour = LocalTime.now().getHour();
        boolean isNight = (hour >= 22 || hour <= 5);

        if (count >= CROWD_CRITICAL_THRESHOLD || (isNight && count > 0)) {
            determinedLevel = EventLevel.ALERT;
            detailMessage = isNight ? "🌙 [야간경계] 심야 구역 내 인원 감지!"
                    : String.format("🚨 [위험] %d명 감지! 즉시 확인 요망.", count);
        }
        // [WARNING] - 주의 단계
        else if (count >= CROWD_WARNING_THRESHOLD) {
            determinedLevel = EventLevel.WARNING;
            detailMessage = String.format("⚠️ [주의] %d명 감지. 혼잡도가 증가하고 있습니다.", count);
        }

        // [D] 엔티티 생성 및 저장 (변수명을 entity로 선언하여 하단 SSE와 맞춤)
        DetectionEntity entity = dto.toEntity(detailMessage);
        entity.setEventLevel(determinedLevel); // 최종 판단된 등급 세팅

        DetectionEntity saved = repository.save(entity);

        // [E] 실시간 SSE 알림 전송 (가장 중요한 하이라이트!)
        for (SseEmitter emitter : SseController.emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("newDetection")
                        .data(saved)); // 저장된 최신 데이터를 실시간으로 전송
            } catch (Exception e) {
                SseController.emitters.remove(emitter); // 연결 끊긴 클라이언트 제거
            }
        }
    }

    // 모든 이벤트 조회 (최신순)
    public List<DetectionEntity> getAllEvents() {
        return repository.findAllByOrderByIdDesc();
    }

    // 특정 등급별로 최신순 조회
    public List<DetectionEntity> getEventsByLevel(EventLevel level) {
        return repository.findByEventLevelOrderByIdDesc(level);
    }

    // 특정 카메라별 이벤트 목록을 최신순으로 조회
    public List<DetectionEntity> getEventsByCamera(Integer cameraId) {
        // 🚩 레포지토리에 이 메서드가 있어야 합니다.
        return repository.findByCameraIdOrderByIdDesc(cameraId);
    }
}