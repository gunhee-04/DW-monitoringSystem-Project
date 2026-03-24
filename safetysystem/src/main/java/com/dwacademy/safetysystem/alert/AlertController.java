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
    public List<AlertLog> getRecentAlerts() {
        return alertService.getRecentAlerts();
    }

    // 읽음 처리
    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
    }


}
