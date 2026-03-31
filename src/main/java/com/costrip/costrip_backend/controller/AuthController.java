package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.auth.LoginRequestDto;
import com.costrip.costrip_backend.dto.auth.LoginResponseDto;
import com.costrip.costrip_backend.dto.auth.RegisterRequestDto;
import com.costrip.costrip_backend.dto.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * 회원가입
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequestDto requestDto) {

        authService.register(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", null));
    }

    /**
     * POST /api/auth/login
     * 로그인 (JWT 발급)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto) {

        LoginResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity
                .ok(ApiResponse.success("로그인 성공", responseDto));
    }

    /**
     * POST /api/auth/logout
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT 방식은 클라이언트 토큰 삭제로 처리
        // 추후 RefreshToken DB 삭제 로직 추가 가능
        return ResponseEntity
                .ok(ApiResponse.success("로그아웃 되었습니다.", null));
    }
}
