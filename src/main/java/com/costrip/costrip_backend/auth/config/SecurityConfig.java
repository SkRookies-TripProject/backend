package com.costrip.costrip_backend.auth.config;

import com.costrip.costrip_backend.auth.filter.JwtAuthenticationFilter;
import com.costrip.costrip_backend.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;
    // 요청마다 JWT 토큰을 검사하는 커스텀 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                // CORS 설정을 Security 필터 체인에서도 사용한다.
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 브라우저 preflight 요청은 인증 없이 먼저 통과시킨다.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 업로드된 이미지는 브라우저에서 직접 열 수 있도록 공개한다.
                        //개발연동 테스트용 공개 접근, 운영 시 인증 API로 전환 검토
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // 인증 실패 시 HTML 대신 401 JSON을 반환한다.
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized(인증실패)\",\"message\":\"" + e.getMessage() + "\"}");
                        })
                        // 인증은 되었지만 권한이 없을 때 403 JSON을 반환한다.
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"error\":\"Forbidden(권한없음)\",\"message\":\"" + e.getMessage() + "\"}");
                        })
                )
                // DB 기반 인증 프로바이더를 등록한다.
                .authenticationProvider(authenticationProvider())
                // JWT 필터를 로그인 필터보다 먼저 실행한다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * 이메일로 사용자를 조회하는 UserDetailsService를 등록한다.
     */
    @Bean
    public UserDetailsService userDetailsService(CustomUserDetailsService service) {
        return service;
    }

    /**
     * 사용자 정보와 비밀번호 인코더를 사용하는 인증 프로바이더를 등록한다.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    /**
     * AuthenticationManager를 빈으로 등록한다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
