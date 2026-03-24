package com.dwacademy.safetysystem.detection_event.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class SseController {
    // 접속한 클라이언트들을 관리하는 리스트
    public static final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/api/sse/connect")
    public SseEmitter connect() {
        // 30분 동안 연결 유지 (사용자 친화적 설정)
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.add(emitter);

        // 연결 종료 및 타임아웃 시 리스트에서 제거 (메모리 관리)
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        try {
            // 첫 연결 시 더미 데이터 전송 (연결 확인용)
            emitter.send(SseEmitter.event().name("connect").data("connected!"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
        return emitter;
    }
}