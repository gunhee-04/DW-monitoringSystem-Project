package com.dwacademy.safetysystem.alert;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {

    List<AlertLog> findTop10ByOrderByCreatedAtDesc();


    List<AlertLog> findTop10BySeverityInOrderByCreatedAtDesc(List<EventLevel> severities);

    List<AlertLog> findAllByIsReadFalse();
}