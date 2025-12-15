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
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // H2 콘솔은 CSRF 무시
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // iframe 허용 (H2 콘솔 필요)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()                       // 메인화면은 항상 승인됨
                        .requestMatchers("/admin/**").hasRole("ADMIN")           // ADMIN 권한 필요
                        .requestMatchers("/h2-console/**").permitAll()           // H2 콘솔 허용
                        .requestMatchers("/api/myPage/**", "/mypage").authenticated()
                        //.requestMatchers("/member/**").hasAuthority("ROLE_MEMBER") // 필요시 활성화
                        .anyRequest().permitAll()
                )
                .httpBasic(withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
