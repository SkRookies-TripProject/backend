package com.costrip.service;

import com.costrip.dto.auth.LoginRequestDto;
import com.costrip.dto.auth.LoginResponseDto;
import com.costrip.dto.auth.RegisterRequestDto;
import com.costrip.entity.User;
import com.costrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 회원가입
     * - 이메일 중복 확인 후 비밀번호 암호화하여 저장
     */
    @Transactional
    public void register(RegisterRequestDto dto) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + dto.getEmail());
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인
     * - 이메일/비밀번호 검증 후 JWT 발급
     */
    public LoginResponseDto login(LoginRequestDto dto) {
        // 사용자 조회
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // JWT 발급
        String accessToken = jwtService.generateToken(user.getEmail());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
