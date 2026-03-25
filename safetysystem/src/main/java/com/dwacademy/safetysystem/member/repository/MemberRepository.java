package com.dwacademy.safetysystem.member.repository;

import com.dwacademy.safetysystem.config.RoleType;
import com.dwacademy.safetysystem.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Optional<Member> findByName(String name);

    Page<Member> findByRole(RoleType role, Pageable pageable);

    Page<Member> findByStatus(String status, Pageable pageable);

    Optional<Member> findByEmail(String email); // 이메일 데이터 조회용

    boolean existsByEmail(String email); // 중복체크를 위해 이메일이 있냐/없냐 만 보는 용도(훨씬 가벼움)

}
