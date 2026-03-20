package com.dwacademy.member;

import com.dwacademy.config.RoleType;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    private int  id;       // 회원 고유 ID
    private String email; // 로그인 이메일
    private String password; // 암호화된 비밀번호
    private String name; // 사용자 이름
    private String phone; // 연락처
    private String address; // 주소
    private RoleType role; // USER, ADMIN
    private String status; // PENDING, ACTIVE 등
    private LocalDateTime createdAt; // 가입일
    private LocalDateTime updatedAt; // 수정일
    private LocalDateTime approvedAt; // 관리자 승인일
    private LocalDateTime withdrawnAt; // 탈퇴일
    private LocalDateTime lastLoginAt; // 마지막 로그인


}
