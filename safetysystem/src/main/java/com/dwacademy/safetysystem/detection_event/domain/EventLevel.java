package com.dwacademy.safetysystem.detection_event.domain;

public enum EventLevel {
    NORMAL,   // 정상
    WARNING,  // 주의 (인원 증가 등)
    ALERT     // 위험 (침입, 심야 감지, 밀집)
}