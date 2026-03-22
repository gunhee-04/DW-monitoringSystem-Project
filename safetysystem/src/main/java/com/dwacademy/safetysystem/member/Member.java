package com.dwacademy.safetysystem.member;

import com.dwacademy.safetysystem.config.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="MEMBER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @Column(name="NO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int  id;       // 회원 고유 ID

    @Column(nullable = false, unique = true)
    private String email; // 로그인 이메일

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호

    @Column(nullable = false)
    private String name; // 사용자 이름

    private String phone; // 연락처

    private String address; // 주소

    @Column(nullable = false)
    private RoleType role; // USER, ADMIN

    @Column(nullable = false)
    private String status; // PENDING, ACTIVE 등

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 가입일

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일

    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 관리자 승인일

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt; // 탈퇴일

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt; // 마지막 로그인


}
