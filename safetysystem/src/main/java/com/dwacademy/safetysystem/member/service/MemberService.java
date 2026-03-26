package com.dwacademy.safetysystem.member.service;

import com.dwacademy.safetysystem.config.RoleType;
import com.dwacademy.safetysystem.member.dto.MemberDto;
import com.dwacademy.safetysystem.member.entity.Member;
import com.dwacademy.safetysystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 전체 회원 목록
    @Transactional(readOnly = true)
    public Page<Member> getMembers(Pageable pageable) {
        log.info("--- [MemberService] getMembers() ---");
        return memberRepository.findAll(pageable);
    }

    // 상태별 조회
    @Transactional(readOnly = true)
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

    // 회원 등록 형식 정해주기
    private static final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern phonePattern = Pattern.compile("^010-\\d{4}-\\d{4}$");

    private static final Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    // 유효성 검사
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()){
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        if (!passwordPattern.matcher(password).matches()) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다.");
        }

    }

    private void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("연락처를 입력해주세요.");
        }

        if (!phonePattern.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException("연락처 형식은 010-0000-0000 이어야 합니다.");
        }
    }

    private void validatePostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            throw new IllegalArgumentException("우편번호를 입력해주세요.");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소를 입력해주세요.");
        }
    }

    private void validateDuplicatedActiveEmail(String email) {
        if (memberRepository.existsByEmailAndStatusIn(
                email.trim(),
                List.of("PENDING", "ACTIVE", "WITHDRAWN")
        )){
            throw new IllegalArgumentException("이미 사용 중인 이메일 입니다.");
        }
    }

    // 회원 등록
    public void createMember(MemberDto memberDto) {
        log.info("--- [MemberService] createMember() ---");
        
        // 검증 : memberDto에서 값을 빼와서 문제가 없는지 확인!
        validateName(memberDto.getName());
        validateEmail(memberDto.getEmail());
        validatePassword(memberDto.getPassword());
        validatePhone(memberDto.getPhone());
        validatePostcode(memberDto.getPostcode());
        validateAddress(memberDto.getAddress());
        validateDuplicatedActiveEmail(memberDto.getEmail());

        Member member = new Member();
        member.setName(memberDto.getName().trim());
        member.setEmail(memberDto.getEmail().trim());
        member.setPassword(passwordEncoder.encode(memberDto.getPassword()));
        member.setPhone(memberDto.getPhone().trim());
        member.setPostcode(memberDto.getPostcode().trim());
        member.setAddress(memberDto.getAddress().trim());
        member.setDetailAddress(memberDto.getDetailAddress() != null ? memberDto.getDetailAddress().trim() : null);
        member.setRole(RoleType.USER);
        member.setStatus("PENDING");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        memberRepository.save(member);

    }



    // 회원 수정
    public void updateMember(MemberDto memberDto) {
        log.info("--- [MemberService] updateMember() ---");

        Member member = memberRepository.findById(memberDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 수정 가능 필드 검증
        validatePhone(memberDto.getPhone());
        validatePostcode(memberDto.getPostcode());
        validateAddress(memberDto.getAddress());

        // 수정 가능 필드만 변경
        member.setPhone(memberDto.getPhone().trim());
        member.setPostcode(memberDto.getPostcode().trim());
        member.setAddress(memberDto.getAddress().trim());
        member.setDetailAddress(
                memberDto.getDetailAddress() != null ? memberDto.getDetailAddress().trim() : null
        );
        member.setRole(memberDto.getRole());
        member.setStatus(memberDto.getStatus());

        member.setUpdatedAt(LocalDateTime.now());

        if ("ACTIVE".equals(memberDto.getStatus()) && member.getApprovedAt() == null) {
            member.setApprovedAt(LocalDateTime.now());
        }

        if ("WITHDRAWN".equals(memberDto.getStatus()) && member.getWithdrawnAt() == null) {
            member.setWithdrawnAt(LocalDateTime.now());
        }

        memberRepository.save(member);

    }

    // 회원 승인
    public void approveMember(int memberId) {
        log.info("--- [MemberService] approveMember() ---");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        if ("ACTIVE".equals(member.getStatus())) {
            throw new IllegalArgumentException("이미 승인된 회원입니다.");
        }

        if ("WITHDRAWN".equals(member.getStatus())) {
            throw new IllegalArgumentException("탈퇴한 회원은 승인할 수 없습니다.");
        }

        member.setStatus("ACTIVE");
        member.setApprovedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        memberRepository.save(member);
    }


    // 회원 반려
    public void rejectMember(int memberId, String rejectReason) {
        log.info("--- [MemberService] rejectMember() ---");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다.")   );

        if (rejectReason == null || rejectReason.isBlank()) {
            throw new IllegalArgumentException("반려 사유를 입력해주세요.");
        }

        if ("WITHDRAWN".equals(member.getStatus())) {
            throw new IllegalArgumentException("탈퇴한 회원은 반려할 수 없습니다.");
        }

        member.setStatus("REJECTED");
        member.setRejectedAt(LocalDateTime.now());
        member.setRejectReason(rejectReason);
        member.setUpdatedAt(LocalDateTime.now());

        memberRepository.save(member);

    }

    // 회원 탈퇴 처리 (실제 삭제 X)
    public void withdrawMember(int memberId) {
        log.info("--- [MemberService] withdrawMember() ---");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new IllegalArgumentException("회원이 존재하지 않습니다."));

        if ("WITHDRAWN".equals(member.getStatus())) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }

        member.setStatus("WITHDRAWN");
        member.setWithdrawnAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        memberRepository.save(member);
    }
}