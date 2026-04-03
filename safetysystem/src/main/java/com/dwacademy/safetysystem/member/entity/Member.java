package com.dwacademy.safetysystem.member.entity;

import com.dwacademy.safetysystem.config.RoleType;
import com.dwacademy.safetysystem.config.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 회원 고유 ID

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email; // 로그인 이메일

    @Column(name = "password", nullable = false)
    private String password; // 암호화된 비밀번호

    @Column(name = "name", nullable = false)
    private String name; // 사용자 이름

    @Column(name = "phone", nullable = false)
    private String phone; // 연락처

    @Column(name = "postcode", nullable = false)
    private String postcode; // 우편 번호

    @Column(name = "address", nullable = false)
    private String address; // 주소

    @Column(name = "detail_address")
    private String detailAddress; // 상세 주소 (선택)

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleType role; // USER, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status; // PENDING, ACTIVE, REJECTED, WITHDRAWN

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 가입일

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일

    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 승인일 (ACTIVE 될 때)

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt; // 탈퇴일

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt; // 마지막 로그인

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt; // 반려일

    @Column(name = "reject_reason", length = 100)
    @Size(max = 100, message = "반려 사유는 100자 이하로 입력하세요.")
    private String rejectReason; // 반려 사유
}


