package com.dwacademy.safetysystem.detection_event.domain;

public enum EventLevel {
    NORMAL,
//    WARNING,
//    ALERT,   // 과거 DB 데이터 인식용
    MEDIUM,  // 파이썬 밀집(CROWD) 전송용
    HIGH     // 파이썬 침입(INTRUSION) 전송용
}