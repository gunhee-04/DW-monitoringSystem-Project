package com.dwacademy.safetysystem.security;

import com.dwacademy.safetysystem.member.entity.Member;
import com.dwacademy.safetysystem.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class MemberRefreshFilter extends OncePerRequestFilter {

    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof CustomUserDetails userDetails) {

            Member sessionMember = userDetails.getMember();
            Member dbMember = memberRepository.findById(sessionMember.getId()).orElse(null);

            // 1. 탈퇴 처리되었으면 즉시 로그아웃
            if (dbMember == null || "WITHDRAWN".equals(dbMember.getStatus())) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
                response.sendRedirect("/member/login?withdraw");
                return;
            }

            // 2. 권한이 바뀌었으면 세션 권한 갱신
            if (!Objects.equals(dbMember.getRole(), sessionMember.getRole())) {

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + dbMember.getRole())
                );

                CustomUserDetails newUserDetails =
                        new CustomUserDetails(dbMember, authorities);

                Authentication newAuth =
                        new UsernamePasswordAuthenticationToken(
                                newUserDetails,
                                auth.getCredentials(),
                                authorities
                        );

                // 새 SecurityContext 생성 후 Authentication 교체
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(newAuth);
                SecurityContextHolder.setContext(context);

                // 세션에도 반영
                request.getSession(true).setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        context
                );
            }
        }

        filterChain.doFilter(request, response);
    }
}