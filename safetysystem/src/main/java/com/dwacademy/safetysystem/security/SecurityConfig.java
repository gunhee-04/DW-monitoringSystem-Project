package com.dwacademy.safetysystem.security;

import com.dwacademy.safetysystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 활성화
@RequiredArgsConstructor // 생성자 자동 주입
public class SecurityConfig {

    private final MemberRepository memberRepository;
    private final CustomLoginFailureHandler customLoginFailureHandler;

    // 필터를 Bean으로 등록 (이게 핵심)
    @Bean
    public MemberRefreshFilter memberRefreshFilter() {
        return new MemberRefreshFilter(memberRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // POST 요청(YOLO 데이터 전송)을 위해 필수

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/members/**",
                                "/member/login",
                                "/member/loginProc",
                                "/member/create",
                                "/api/members/check-email",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/api/**",       // 모든 API 경로 (통계, SSE 등)
                                "/get_logs",     // 로그 조회 경로
                                "/video/**"      // 혹시 모를 비디오 스트리밍 경로
                        ).permitAll()

                        .requestMatchers("/member/list").hasRole("ADMIN")
                        .requestMatchers("/api/members/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/loginProc")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(customLoginFailureHandler)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/member/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Bean으로 등록된 필터 사용 (이게 진짜 중요)
                .addFilterAfter(
                        memberRefreshFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}