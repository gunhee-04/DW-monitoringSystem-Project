package com.dwacademy.safetysystem.detection_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DangerHourlyResponseDto {

    private String hour;                // 09:00
    private long intrusionCount;        // 해당 시간 침입 횟수
    private double averageStayDuration; // 해당 시간 평균 체류 시간
}