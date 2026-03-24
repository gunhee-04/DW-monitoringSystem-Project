package com.dwacademy.safetysystem.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SummaryStatDto {

    private Integer totalPeopleCount;     // 총 사람 수 합계
    private Double averageDensityValue;   // 평균 밀집도
    private Integer maxPeopleCount;       // 최대 사람 수
    private Double averageIncreaseRate;   // 평균 증가율
}