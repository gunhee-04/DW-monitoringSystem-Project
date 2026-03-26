package com.dwacademy.safetysystem.member.controller;

import com.dwacademy.safetysystem.member.service.MemberService;
import com.dwacademy.safetysystem.member.dto.RejectRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Slf4j
public class MemberRestController {

    private final MemberService memberService;


    // 승인 처리
    @PatchMapping("/{id}/approve")
    public ResponseEntity<String> approveMember(@PathVariable int id) {
        log.info("--- [MemberRestController] approveMember() ---");

        try {
            memberService.approveMember(id);
            return ResponseEntity.ok("회원 승인 완료");
        } catch (IllegalArgumentException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    // 반려 처리
    @PatchMapping("/{id}/reject")
    public ResponseEntity<String> rejectMember(
            @PathVariable int id,
            @RequestBody RejectRequestDto rejectRequestDto
    ) {
        log.info("--- [MemberRestController] rejectMember() ---");

        try {
            memberService.rejectMember(id, rejectRequestDto.getRejectReason());
            return ResponseEntity.ok("회원 반려 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }

    // 탈퇴 처리 (실제 삭제 X)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> withdrawMember(@PathVariable int id) {
        log.info("--- [MemberRestController] withdrawMember() ---");

        try {
            memberService.withdrawMember(id);
            return ResponseEntity.ok("회원 탈퇴 처리 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }
}