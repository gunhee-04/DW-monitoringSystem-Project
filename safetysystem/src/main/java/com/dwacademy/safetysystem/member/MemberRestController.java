package com.dwacademy.safetysystem.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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

        int result = memberService.approveMember(id);

        if (result > 0) {
            return ResponseEntity.badRequest().body("회원 승인 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok("회원 승인 완료");
    }

    // 반려 처리
    @PatchMapping("/{id}/reject")
    public ResponseEntity<String> rejectMember(
            @PathVariable int id,
            @RequestBody RejectRequestDto rejectRequestDto
    ) {
        log.info("--- [MemberRestController] rejectMember() ---");

        int result = memberService.rejectMember(id, rejectRequestDto.getRejectReason());

        if (result > 0) {
            return ResponseEntity.badRequest().body("회원 반려 처리 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok("회원 반려 완료");
    }


    // 탈퇴 처리 (실제 삭제 X)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> withdrawMember(@PathVariable int id) {
        log.info("--- [MemberRestController] withdrawMember() ---");

        int result = memberService.withdrawMember(id);

        if (result > 0) {
            return ResponseEntity.badRequest().body("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok("회원 탈퇴 처리 완료");
    }
}