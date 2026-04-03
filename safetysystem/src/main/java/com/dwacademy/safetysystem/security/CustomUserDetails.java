package com.dwacademy.safetysystem.security;

import com.dwacademy.safetysystem.config.Status;
import com.dwacademy.safetysystem.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final Member member;

    public CustomUserDetails(Member member,
                             Collection<? extends GrantedAuthority> authorities) {
        super(member.getEmail(), member.getPassword(), authorities);
        this.member = member;
    }

    @Override // 이 메서드가 false면 Spring Security가 막아주고, 그 때 DisabledException 쪽으로 감
    public boolean isEnabled() {
        return member.getStatus() == Status.ACTIVE;
    }
}