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

/**
 * Spring Security (스프링 웹 보안) 설정을 위한 클래스입니다.
 * @Configuration: 이 클래스가 Spring의 '설정 파일'임을 나타냅니다.
 * @EnableWebSecurity: Spring Security의 웹 보안 기능을 활성화합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 비밀번호를 암호화하는 방식을 결정합니다.
     * @Bean: 이 메서드가 반환하는 객체(PasswordEncoder)를 Spring이 관리하도록 등록합니다.
     * 이렇게 등록하면 다른 Service 등에서 @Autowired로 주입받아 사용할 수 있습니다.
     * @return BCryptPasswordEncoder (현재 많이 사용되는 강력한 암호화 방식)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 요청에 대한 보안 규칙을 설정하는 핵심 메서드입니다.
     * @param http HttpSecurity 객체 (보안 설정을 구성하는 빌더)
     * @return SecurityFilterChain (설정된 보안 규칙의 체인)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CORS 설정: 아래 'corsConfigurationSource()' 메서드의 설정을 적용합니다.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. CSRF 보호 비활성화:
            //    REST API 서버는 세션 대신 JWT/토큰 등을 사용하므로, 
            //    일반적으로 CSRF 보호 기능을 비활성화(disable)합니다.
            .csrf(csrf -> csrf.disable())
            
            // 3. HTTP 요청 권한 설정:
            //    URL(주소)별로 "누가 접근할 수 있는지"를 설정합니다.
            .authorizeHttpRequests(authz -> authz
                
                // "/api/**" (모든 API 요청) 경로는 'permitAll()' (모두 허용)
                .requestMatchers("/api/**").permitAll()
                
                // 정적 리소스(HTML, CSS, JS, 이미지) 경로도 'permitAll()' (모두 허용)
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
                    "/uploads/**" // (중요) MvcConfig에서 설정한 업로드 이미지 경로
                ).permitAll()
                
                // H2 데이터베이스 콘솔 경로 'permitAll()' (모두 허용) - 개발용
                .requestMatchers("/h2-console/**").permitAll()
                
                // (중요) 위에 나열된 주소 외 "그 외 모든 요청(anyRequest)"도
                // 'permitAll()' (일단 모두 허용)
                // -> 만약 운영 서버라면 .anyRequest().authenticated() (인증된 사용자만) 등으로 변경
                .anyRequest().permitAll()  
            );
        
        // 4. H2 콘솔 사용 설정:
        //    H2 콘솔은 <frame> 태그를 사용하는데, Spring Security는 이를 기본적으로 차단합니다.
        //    'frameOptions().disable()'을 설정하여 H2 콘솔을 볼 수 있게 허용합니다.
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build(); // 설정 완료
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 정의합니다.
     * (핵심 역할): "다른 주소(도메인/포트)에서 온 요청"을 허용해주는 정책입니다.
     * 예: 프론트엔드(localhost:5500)가 백엔드(localhost:8080) API를 호출할 수 있게 해줍니다.
     * @Bean: 이 설정 객체도 Spring이 관리하도록 등록합니다.
     * @return CorsConfigurationSource (CORS 설정 소스)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // (1) 허용할 출처(Origin) 설정:
        //     프론트엔드 Live Server 주소(5500)와 백엔드 주소(8080)를 허용합니다.
        configuration.setAllowedOrigins(Arrays.asList(
            "http://127.0.0.1:5500", // 127.0.0.1 (VSCode Live Server)
            "http://localhost:5500",    // localhost (VSCode Live Server)
            "http://localhost:8080"     // 백엔드 주소
        ));
        
        // (2) 허용할 HTTP 메서드(동작) 설정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // (3) 허용할 HTTP 헤더 설정 ("*") -> 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // (4) 자격증명(쿠키 등) 허용 여부
        configuration.setAllowCredentials(true);
        
        // (5) 이 설정을 "/**" (모든 URL 경로)에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}