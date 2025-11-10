package com.javaprgraming.javaproject.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // API 엔드포인트 허용
                .requestMatchers("/api/**").permitAll()
                
                // 정적 리소스 허용 (HTML, CSS, JS, 이미지 등)
                .requestMatchers(
                    "/", 
                    "/login.html",
                    "/main.html",
                    "/auction_site_main.html",
                    "/auction_mypage.html",
                    "/auction_register_page.html",
                    "/css/**", 
                    "/js/**", 
                    "/images/**",
                    "/uploads/**"
                ).permitAll()
                
                // H2 콘솔 허용 (개발용)
                .requestMatchers("/h2-console/**").permitAll()
                
                // 나머지는 인증 필요 (현재는 사용 안 함)
                .anyRequest().permitAll()  // ← 개발 중에는 모두 허용
            );
        
        // H2 콘솔을 위한 frame 옵션 비활성화
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 모든 출처 허용 (개발 중)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://127.0.0.1:5500", 
            "http://localhost:5500",
            "http://localhost:8080"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}