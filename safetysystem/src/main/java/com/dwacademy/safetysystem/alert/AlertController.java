package com.dwacademy.safetysystem.alert;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // 최근 알림
    @GetMapping("/recent")
    public List<AlertResponseDto> getRecentAlerts() {
        return alertService.getRecentAlerts()
                .stream()
                .map(AlertResponseDto::new)
                .toList();
    }

    // 읽음 처리
    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
    }

    //최근 경고 로그 관련 필터도 아직 안됨
    @GetMapping("/recent-warnings")
    public List<AlertLog> getRecentWarnings() {
        return alertService.getRecentWarnings();
    }

    // AlertController에 추가 요청
    @PatchMapping("/read-all")
    public void markAllAsRead() {
        alertService.markAllAsRead(); // 모든 is_read를 true로 바꾸는 기능
    }

    // AlertController에 추가 요청
    @PatchMapping("/{id}/delete")
    public void deleteAlert(@PathVariable Long id) {
        alertService.softDelete(id); // is_deleted를 true로 바꾸는 기능
    }
}
