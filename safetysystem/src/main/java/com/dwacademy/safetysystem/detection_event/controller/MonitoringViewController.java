package com.dwacademy.safetysystem.detection_event.controller;

import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MonitoringViewController {

    // 🚩 1. 변수명을 detectionService로 변경
    private final DetectionService detectionService;

    @GetMapping("/monitoring")
    public String monitoringPage(@RequestParam(value = "level", required = false) String level, Model model) {
        List<DetectionEntity> eventList;

        if (level != null && !level.isEmpty()) {
            // 이제 아래의 detectionService와 이름이 일치합니다!
            eventList = detectionService.getEventsByLevel(EventLevel.valueOf(level));
        } else {
            eventList = detectionService.getAllEvents();
        }

        model.addAttribute("eventList", eventList);
        model.addAttribute("selectedLevel", level);
        return "test";
    }
}