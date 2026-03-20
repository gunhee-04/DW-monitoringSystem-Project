package com.dwacademy.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .httpBasic(AbstractHttpConfigurer::disable);

        http
                //               .csrf(AbstractHttpConfigurer::disable);
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                );

        http
                .authorizeHttpRequests((authorize) -> authorize
                                //.requestMatchers("/**").permitAll() // 전부 허용한 상태로 테스트하고, 뒤에막자

                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                                .requestMatchers("/main/view/**").permitAll()
                                .requestMatchers("/member/list").hasAnyRole("ADMIN")
                                .requestMatchers("/dw202/attach/summernote/**").permitAll()
                                .requestMatchers("/dw202/attach/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated() // 인증이 필요함

                        //.anyRequest().permitAll()
                );

        http
                .formLogin(form -> form
                        .loginPage("/member/login")
                        // 기존 .defaultSuccessUrl 대신 아래 핸들러를 사용합니다.
                        .successHandler((request, response, authentication) -> {
                            // 사용자의 권한(Role) 확인
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                            if (isAdmin) {
                                response.sendRedirect("/main/list"); // 관리자면 대시보드로
                            } else {
                                response.sendRedirect("/main/list"); // 일반 유저는 게시판으로
                            }
                        })
                        .failureUrl("/member/login?error")
                        .permitAll()
                );

        http
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                );

        return http.build();
    }

    //    //import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//    //import org.springframework.security.crypto.password.PasswordEncoder;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
