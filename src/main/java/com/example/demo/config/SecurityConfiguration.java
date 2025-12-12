package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.
                csrf(csrf -> csrf.disable())    // 개발단계라 disable 처리, 사용시 withDefaults() 사용
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/").permitAll()                                   // 메인화면은 항상 승인됨
                        .requestMatchers("/admin/**").hasRole("ADMIN")                      // 권한에 'ADMIN' 이 포함되어 있을때 만 /admin 실행 가능
//                        .requestMatchers("/member/**").hasAuthority("ROLE_MEMBER")          권한이 'ROLE_MEMBER' 일때 만 /member 실행 가능(수정, 마이페이지 예정)
                        .requestMatchers("/signup").permitAll()                             // 회원가입은 항상 승인됨
                        .anyRequest().authenticated())                                        // 이외의 mapping은 로그인상태만 접근 가능
                .httpBasic(withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)                  // 로그인 성공 시 메인화면으로 이동, 요청한 URL이 있다면 거기로 이동
                        .permitAll())
                .logout(logout -> logout                            // 로그아웃 성공시 메인화면으로 이동
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
