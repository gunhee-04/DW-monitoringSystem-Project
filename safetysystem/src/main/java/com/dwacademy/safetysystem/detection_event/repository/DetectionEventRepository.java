package com.dwacademy.safetysystem.detection_event.repository;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.domain.EventType;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface DetectionEventRepository extends JpaRepository<DetectionEntity, Integer> {

    // 🚩 [추가] 가장 최근에 알림을 쏜(isRead=1) HIGH 등급 데이터 1건 가져오기
    DetectionEntity findFirstByEventLevelAndIsReadOrderByIdDesc(EventLevel eventLevel, Integer isRead);

    // 기존 메서드들
    List<DetectionEntity> findByCameraIdOrderByIdDesc(Integer cameraId);
    List<DetectionEntity> findByEventLevelOrderByIdDesc(EventLevel eventLevel);
    List<DetectionEntity> findAllByOrderByIdDesc();

    List<DetectionEntity> findByEventTypeOrderByIdDesc(EventType eventType);

    List<DetectionEntity> findByEventTypeAndEventTimeBetweenOrderByIdDesc(
            EventType eventType, Long start, Long end);

    List<DetectionEntity> findByCameraIdAndEventTypeAndEventTimeBetweenOrderByIdDesc(
            Integer cameraId, EventType eventType, Long start, Long end);

    boolean existsByEventLevelAndIsReadAndCreatedAtAfter(EventLevel level, Integer isRead, LocalDateTime time);

    List<DetectionEntity> findByIsRead(Integer isRead);

}