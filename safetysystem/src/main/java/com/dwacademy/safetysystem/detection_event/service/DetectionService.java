package com.dwacademy.safetysystem.detection_event.service;

import com.dwacademy.safetysystem.alert.AlertService;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.dto.EventResponseDto;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import com.dwacademy.safetysystem.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DetectionService {

    private final DetectionEventRepository repository;
    private final AlertService alertService;
    private final StatisticsService statisticsService;

    @Transactional
    public EventResponseDto processEvent(DetectionRequestDto dto) {
        Object[] analysis = analyzeEvent(dto);
        EventLevel level = (EventLevel) analysis[0];
        String message = (String) analysis[1];

        DetectionEntity entity = dto.toEntity(message, level);

        // 권장 규칙:
        // 0 = unread(새 알림)
        // 1 = read(읽음)
        if (level == EventLevel.HIGH) {
            entity.setIsRead(0);
        } else {
            entity.setIsRead(1);
        }

        DetectionEntity saved = repository.save(entity);

        try {
            statisticsService.saveFromDetection(saved);
        } catch (Exception e) {
            log.error("통계 저장 실패", e);
        }

        if (level == EventLevel.HIGH) {
            try {
                alertService.createAlert(
                        saved.getId().longValue(),
                        saved.getEventType().name(),
                        level.name(),
                        message
                );
            } catch (Exception e) {
                log.error("알림 생성 실패", e);
            }
        }

        return new EventResponseDto(saved);
    }

    private Object[] analyzeEvent(DetectionRequestDto dto) {
        String rawType = dto.getEventType() != null ? dto.getEventType().trim().toUpperCase() : "";
        Integer detectedCount = dto.getDetectedCount() != null ? dto.getDetectedCount() : 0;
        Integer stayDurationSec = dto.getStayDurationSec() != null ? dto.getStayDurationSec() : 0;

        // 🔥 침입은 그대로 위험 (이건 보안이라 유지하는 게 맞음)
        if ("INTRUSION".equals(rawType)) {
            return new Object[]{EventLevel.HIGH, "위험 침입 감지"};
        }

        // 🔥 여기만 수정하면 됨
        if ("CROWD".equals(rawType)) {
            if (detectedCount >= 15) {
                return new Object[]{EventLevel.HIGH, "위험 (밀집)"};
            } else if (detectedCount >= 6) {
                return new Object[]{EventLevel.MEDIUM, "주의 (밀집)"};
            } else {
                return new Object[]{EventLevel.NORMAL, "정상 관제 중"};
            }
        }

        // 기존 유지
        if ("STAY".equals(rawType)) {
            if (stayDurationSec >= 300) {
                return new Object[]{EventLevel.MEDIUM, "장시간 체류 감지"};
            } else {
                return new Object[]{EventLevel.NORMAL, "정상 관제 중"};
            }
        }

        return new Object[]{EventLevel.NORMAL, "정상 관제 중"};
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

    @Transactional
    public void markAllAlertsAsRead() {
        List<DetectionEntity> unreadEvents = repository.findByIsRead(0);
        for (DetectionEntity entity : unreadEvents) {
            entity.setIsRead(1);
        }
    }
}