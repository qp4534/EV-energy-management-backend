package com.ev_energy_management.backend.config;

import com.ev_energy_management.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // 응답 형태는 exception.ErrorResponse와 동일하게 맞춘 고정 JSON. 내용이 정적이라
    // 이 필터 체인 단계에서는 ObjectMapper 의존성 없이 문자열로 직접 내려준다.
    private static final String UNAUTHENTICATED_BODY =
            "{\"status\":401,\"error\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\",\"fieldErrors\":null}";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 도메인 API(cars, chargers, notices 등) 전부 로그인(유효한 JWT)이 있어야 호출 가능하다.
    // 관리자/관제자 role별 세분화는 아직 없음 - JwtAuthenticationFilter가 인증된 사용자 전부에게
    // 하드코딩된 ROLE_USER 하나만 부여하고 있어서, role별 제한을 하려면 그것부터 실제 role claim을
    // 반영하도록 고쳐야 한다(후속 작업).
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(UNAUTHENTICATED_BODY);
                }))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight는 인증 헤더 없이 오므로 막으면 브라우저에서 모든 API가 CORS 에러로 실패한다.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 쿠버네티스 liveness/readiness probe용 - management.endpoints.web.exposure.include=health 참고.
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login",
                                "/api/auth/email/send-code", "/api/auth/email/verify-code",
                                "/api/auth/find-email", "/api/auth/password/reset/send-code",
                                "/api/auth/password/reset").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 프론트엔드 개발 서버(Vite)에서 직접 호출 가능하도록 임시로 허용한다.
    // Vite는 5173번이 사용 중이면 5174, 5175...로 자동 변경되므로 포트를 특정하지 않고
    // localhost 전체를 패턴으로 허용한다. 배포용 프론트 도메인이 정해지면 이 패턴을 교체한다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
