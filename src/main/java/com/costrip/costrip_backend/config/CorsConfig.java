package com.costrip.costrip_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 설정 (prod 프로파일)
 *
 * CorsConfigurationSource 빈을 등록하면 SecurityConfig의
 * .cors(Customizer.withDefaults())가 이 빈을 자동으로 참조합니다.
 *
 * 기존 FilterRegistrationBean 방식의 문제:
 *   - FilterRegistrationBean order(0)은 Spring Security(-100)보다 늦게 실행
 *   - preflight(OPTIONS) 요청이 Security 필터에서 먼저 차단됨
 * 해결:
 *   - CorsConfigurationSource 빈 등록 → Security 필터 체인 내부에서 CORS 처리
 */
@Configuration
//@Profile("prod")
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of("*")); // 🔥 이거 꼭 추가
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
