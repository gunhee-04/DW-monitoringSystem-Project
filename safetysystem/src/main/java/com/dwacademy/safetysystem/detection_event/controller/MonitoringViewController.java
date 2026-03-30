package com.dwacademy.safetysystem.detection_event.controller;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.dto.EventResponseDto;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MonitoringViewController {

    private final DetectionService detectionService;

    @GetMapping("/monitoring")
    public String monitoringPage(@RequestParam(value = "level", required = false) EventLevel level,
                                 Model model) {

        // 서비스에서 가져온 Entity 리스트를 DTO 리스트로 변환
        List<EventResponseDto> eventList = (level != null)
                ? detectionService.getEventsByLevel(level).stream().map(EventResponseDto::new).collect(Collectors.toList())
                : detectionService.getAllEvents().stream().map(EventResponseDto::new).collect(Collectors.toList());

        model.addAttribute("eventList", eventList);
        model.addAttribute("selectedLevel", level);

        return "test";
    }
}