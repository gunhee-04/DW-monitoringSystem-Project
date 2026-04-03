package com.dwacademy.safetysystem.detection_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DangerHourlyResponseDto {

    private String hour;
    private long intrusionCount;
    private double averageStayDuration;
    private long cumulativeIntrusionCount;
}