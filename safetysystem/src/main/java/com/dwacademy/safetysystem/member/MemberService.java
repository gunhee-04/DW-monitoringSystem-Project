package com.dwacademy.safetysystem.member;

import com.dwacademy.safetysystem.config.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 전체 회원 목록
    public Page<Member> getMembers(Pageable pageable) {
        log.info("--- [MemberService] getMembers() ---");
        return memberRepository.findAll(pageable);
    }

    // 상태별 조회
    public Page<Member> getMembersByStatus(String status, Pageable pageable) {
        log.info("--- [MemberService] getMembersByStatus() ---");
        return memberRepository.findByStatus(status, pageable);
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public Member getMemberById(int id) {
        log.info("--- [MemberService] getMemberById() ---");
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID(" + id + ")의 회원을 찾을 수 없습니다."));
    }

    // 회원 등록
    public int createMember(MemberDto memberDto) {
        log.info("--- [MemberService] createMember() ---");

        int result = 0;

        try {
            Member member = new Member();
            member.setName(memberDto.getName());
            member.setEmail(memberDto.getEmail());
            member.setPassword(passwordEncoder.encode(memberDto.getPassword()));
            member.setPhone(memberDto.getPhone());
            member.setAddress(memberDto.getAddress());
            member.setRole(RoleType.USER);
            member.setStatus("PENDING");
            member.setCreatedAt(LocalDateTime.now());
            member.setUpdatedAt(LocalDateTime.now());

            memberRepository.save(member);

        } catch (Exception e) {
            result++;
            log.error("회원 등록 중 오류 발생", e);
        }

        return result;
    }

    // 회원 수정
    public int updateMember(MemberDto memberDto) {
        log.info("--- [MemberService] updateMember() ---");

        int result = 0;

        try {
            Member member = memberRepository.findById(memberDto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

            member.setName(memberDto.getName());
            member.setEmail(memberDto.getEmail());
            member.setPhone(memberDto.getPhone());
            member.setAddress(memberDto.getAddress());

            if (memberDto.getRole() != null) {
                member.setRole(memberDto.getRole());
            }

            if (memberDto.getStatus() != null && !memberDto.getStatus().isBlank()) {
                member.setStatus(memberDto.getStatus());
            }

            // 비밀번호는 입력한 경우만 변경
            if (memberDto.getPassword() != null && !memberDto.getPassword().isBlank()) {
                member.setPassword(passwordEncoder.encode(memberDto.getPassword()));
            }

            member.setUpdatedAt(LocalDateTime.now());

            if ("ACTIVE".equals(memberDto.getStatus()) && member.getApprovedAt() == null) {
                member.setApprovedAt(LocalDateTime.now());
            }

            if ("WITHDRAWN".equals(memberDto.getStatus()) && member.getWithdrawnAt() == null) {
                member.setWithdrawnAt(LocalDateTime.now());
            }

            memberRepository.save(member);

        } catch (Exception e) {
            result++;
            log.error("회원 수정 중 오류 발생", e);
        }

        return result;
    }

    // 회원 승인
    public int approveMember(int memberId) {
        log.info("--- [MemberService] approveMember() ---");

        int result = 0;

        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

            member.setStatus("ACTIVE");
            member.setApprovedAt(LocalDateTime.now());
            member.setUpdatedAt(LocalDateTime.now());

            memberRepository.save(member);

        } catch (Exception e) {
            result++;
            log.error("회원 승인 중 오류 발생", e);
        }

        return result;
    }


    // 회원 반려
    public int rejectMember(int memberId, String rejectReason) {
        log.info("--- [MemberService] rejectMember() ---");

            int result = 0;

            try {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

                member.setStatus("REJECTED");
                member.setRejectedAt(LocalDateTime.now());
                member.setRejectReason(rejectReason);
                member.setUpdatedAt(LocalDateTime.now());

                memberRepository.save(member);

            } catch (Exception e) {
                result++;
                log.error("회원 반려 처리 중 오류 발생", e);
            }

            return result;
    }


    // 회원 탈퇴 처리 (실제 삭제 X)
    public int withdrawMember(int memberId) {
        log.info("--- [MemberService] withdrawMember() ---");

        int result = 0;

        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

            member.setStatus("WITHDRAWN");
            member.setWithdrawnAt(LocalDateTime.now());
            member.setUpdatedAt(LocalDateTime.now());

            memberRepository.save(member);

        } catch (Exception e) {
            result++;
            log.error("회원 탈퇴 처리 중 오류 발생", e);
        }

        return result;
    }
}