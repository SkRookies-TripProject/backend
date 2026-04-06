package com.costrip.costrip_backend.service;

import com.costrip.costrip_backend.dto.auth.AdminUserResponseDto;
import com.costrip.costrip_backend.dto.auth.ChangePasswordRequestDto;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 전체 사용자 조회
     */
    public List<AdminUserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponseDto::from)
                .toList();
    }

    /**
     * 사용자 검색 (이름 또는 이메일)
     */
    public List<AdminUserResponseDto> searchUsers(String keyword) {
        return userRepository
                .findByNameContainingOrEmailContaining(keyword, keyword)
                .stream()
                .map(AdminUserResponseDto::from)
                .toList();
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequestDto requestDto) {

        // 1. 유저 조회
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 현재 비밀번호 검증 (bcrypt 비교)
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 3. 새 비밀번호 해싱 후 저장
        String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());
        user.updatePassword(encodedNewPassword); // 아래 User 엔티티에 메서드 추가
        userRepository.save(user);
    }
}
