package com.dwacademy.safetysystem.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HourlyStatDto {

    private String hour;
    private Integer totalPeopleCount;
    private Double averageDensityValue;
}