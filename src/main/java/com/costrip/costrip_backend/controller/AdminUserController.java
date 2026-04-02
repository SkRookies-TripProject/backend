package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.auth.AdminUserResponseDto;
import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.entity.User;
import com.costrip.costrip_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * 전체 사용자 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserResponseDto>>> getUsers() {
        return ResponseEntity.ok(
                ApiResponse.success("전체 사용자 조회 성공", userService.getAllUsers())
        );
    }

    /**
     * 사용자 검색 (이름 or 이메일)
     * 예: /api/admin/users/search?keyword=test
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AdminUserResponseDto>>> searchUsers(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                ApiResponse.success("사용자 검색 성공", userService.searchUsers(keyword))
        );
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId) {

        userService.deleteUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("사용자 삭제 성공", null)
        );
    }
}
