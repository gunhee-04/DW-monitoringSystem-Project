package com.dwacademy.safetysystem.detection_event.controller;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MonitoringViewController {

    private final DetectionService detectionService;

    /**
     * 모니터링 메인 페이지 (초기 데이터 로딩)
     */
    @GetMapping("/monitoring")
    public String monitoringPage(@RequestParam(value = "level", required = false) EventLevel level,
                                 Model model) {

        // 1. 서비스에 위임하여 필터링된 리스트 또는 전체 리스트 확보
        List<DetectionEntity> eventList = (level != null)
                ? detectionService.getEventsByLevel(level)
                : detectionService.getAllEvents();

        // 2. 뷰(test.html)로 데이터 전달
        model.addAttribute("eventList", eventList);
        model.addAttribute("selectedLevel", level); // 현재 선택된 필터값 유지용

        return "test"; // resources/templates/test.html 호출
    }
}