package com.dwacademy.safetysystem.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

            http
                    .csrf(AbstractHttpConfigurer::disable)

                    .authorizeHttpRequests(auth -> auth
                            // 누구나 접근 가능
                            .requestMatchers(
                                    "/**",
                                    "/",
                                    "/member/login",
                                    "/member/loginProc",
                                    "/member/create",
                                    "/css/**",
                                    "/js/**",
                                    "/images/**",
                                    "/member/list"
                            ).permitAll()

                            // 관리자만 접근
                            //.requestMatchers("/member/list").hasRole("ADMIN")
                            //.requestMatchers("/api/members/**").hasRole("ADMIN")

                            // 나머지는 로그인 필요
                            //.anyRequest().authenticated()
                    )

                    .formLogin(form -> form
                            .loginPage("/member/login")                 // 네가 만든 로그인 페이지
                            .loginProcessingUrl("/member/loginProc")        // form action이랑 맞춤
                            .usernameParameter("email")                 // input name="email"
                            .passwordParameter("password")              // input name="password"
                            .defaultSuccessUrl("/member/list", true)    // 로그인 성공 후 이동
                            .failureUrl("/member/login?error")          // 로그인 실패
                            .permitAll()
                    )

                    .logout(logout -> logout
                            .logoutUrl("/member/logout")
                            .logoutSuccessUrl("/member/login?logout")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll()
                    );

            return http.build();
        }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}