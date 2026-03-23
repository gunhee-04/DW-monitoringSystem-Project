package com.dwacademy.safetysystem.detection_event.repository;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetectionEventRepository extends JpaRepository<DetectionEntity, Integer> {

    // 🚩 이 줄을 추가하세요!
    List<DetectionEntity> findByCameraIdOrderByIdDesc(Integer cameraId);

    List<DetectionEntity> findByEventLevelOrderByIdDesc(EventLevel eventLevel);
    List<DetectionEntity> findAllByOrderByIdDesc();
}