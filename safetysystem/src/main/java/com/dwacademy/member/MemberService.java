package com.dwacademy.member;


import com.dwacademy.config.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<Member> list(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    public Page<Member> findByRole(RoleType role, Pageable pageable) {
        return memberRepository.findByRole(role, pageable);
    }

    @Transactional(readOnly = true)
    public Member getSelectOneById(int id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID(" + id + ")의 레코드를 찾을 수 없습니다."));
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username).orElse(null);
    }

    // 회원 등록
    public int createMemberProc(MemberDto memberDto) {
        Member member = new Member();
        member.setName(memberDto.getName());
        member.setPassword(passwordEncoder.encode(memberDto.getPassword())); // 암호화
        member.setEmail(memberDto.getEmail());
        member.setRole(RoleType.USER);

        int result = 0;
        try {
            memberRepository.save(member);
        } catch(Exception e) {
            result++;
        }
        return result;
    }

    // 회원 수정
    public int updateMemberProc(MemberDto memberDto) {
        Member member = memberRepository.findById(memberDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        member.setName(memberDto.getName());
        member.setEmail(memberDto.getEmail());
        member.setRole(memberDto.getRole());

        // 비밀번호 입력이 있으면 암호화 후 변경, 없으면 기존 비밀번호 유지
        if (memberDto.getPassword() != null && !memberDto.getPassword().isEmpty()) {
            member.setPassword(passwordEncoder.encode(memberDto.getPassword()));
        }

        int result = 0;
        try {
            memberRepository.save(member);
        } catch (Exception e) {
            result++;
        }
        return result;
    }

    // 회원 삭제
    public int deleteMemberProc(MemberDto memberDto) {
        Member member = memberRepository.findById(memberDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        int result = 0;
        try {
            memberRepository.delete(member);
        } catch (Exception e) {
            result++;
        }
        return result;
    }

    @Transactional
    public void changeRole(int memberId, RoleType role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        member.setRole(role);
    }

}
