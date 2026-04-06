package com.dwacademy.safetysystem.detection_event.domain;

public enum EventLevel {
    NORMAL,
    MEDIUM,  // 파이썬 밀집(CROWD) 전송용
    HIGH     // 파이썬 침입(INTRUSION) 전송용
}