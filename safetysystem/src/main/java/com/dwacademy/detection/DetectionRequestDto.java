package com.dwacademy.detection;

import lombok.Data;

@Data
public class DetectionRequestDto {

    private String cameraId;          // 카메라 코드 (CAM-01)
    private String eventType;         // INTRUSION, CROWD
    private String eventLevel;        // LOW, MEDIUM, HIGH, CRITICAL

    private Integer detectedCount;    // 감지된 객체 수 (밀집)
    private Integer stayDurationSec;  // 체류 시간 (침입)

    private String message;           // 이벤트 설명
    private String image;             // Base64 이미지 (선택)

    private String eventTime;         // 발생 시간 (문자열로 일단 받기)
}
