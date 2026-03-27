package com.dwacademy.safetysystem.detection_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DangerSummaryResponseDto {

    private long totalIntrusion;        // 총 침입 수
    private long maxHourlyIntrusion;    // 시간당 최대 침입 횟수
    private double averageStayDuration; // 평균 체류 시간(초)
}