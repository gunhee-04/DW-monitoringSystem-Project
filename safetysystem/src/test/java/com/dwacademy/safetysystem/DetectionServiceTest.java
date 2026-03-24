package com.dwacademy.safetysystem;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import com.dwacademy.safetysystem.detection_event.dto.DetectionRequestDto;
import com.dwacademy.safetysystem.detection_event.entity.DetectionEntity;
import com.dwacademy.safetysystem.detection_event.repository.DetectionEventRepository;
import com.dwacademy.safetysystem.detection_event.service.DetectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional // 테스트 후 DB를 자동으로 롤백하여 깨끗하게 유지합니다.
public class DetectionServiceTest {

    @Autowired
    private DetectionService detectionService;

    @Autowired
    private DetectionEventRepository repository;

    @Test
    void 야간_침입_탐지_테스트() {
        // 1. 가짜 요청 생성 및 데이터 세팅 (현재 시간 기준으로 야간 로직 테스트)
        DetectionRequestDto dto = new DetectionRequestDto();

        // 주의: DTO에 Setter가 없다면 필드를 직접 수정하거나 생성자를 활용해야 합니다.
        // 여기서는 일반적인 세팅 방식을 예시로 듭니다.
        dto.setCameraId(1);
        dto.setEventType("INTRUSION"); // 침입 상황 설정
        dto.setDetectedCount(1);
        dto.setEventLevel("NORMAL"); // 서비스에서 분석 후 ALERT로 바뀔 예정

        // 2. 서비스 실행 (판단 -> 저장 -> SSE 전송)
        detectionService.processEvent(dto);

        // 3. 마지막 저장된 데이터 확인
        DetectionEntity lastEvent = repository.findAllByOrderByIdDesc().get(0);

        // 검증: 침입(INTRUSION) 타입이므로 ALERT 등급이어야 함
        assertEquals(EventLevel.ALERT, lastEvent.getEventLevel());

        // 검증: 메시지에 '침입자' 또는 '감지' 관련 문구가 포함되어 있는지 확인
        assertTrue(lastEvent.getMessage().contains("침입자"));

        // 만약 밤 10시 ~ 새벽 5시 사이에 테스트를 돌린다면 '야간' 문구 확인 가능
        // assertTrue(lastEvent.getMessage().contains("야간"));
    }
}