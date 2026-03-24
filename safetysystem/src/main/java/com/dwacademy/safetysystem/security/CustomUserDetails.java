package com.dwacademy.safetysystem.security;

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
        super(member.getName(), member.getPassword(), authorities);
        this.member = member;
    }
}