package com.costrip.costrip_backend.controller;

import com.costrip.costrip_backend.dto.auth.ChangePasswordRequestDto;
import com.costrip.costrip_backend.dto.common.ApiResponse;
import com.costrip.costrip_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * PATCH /api/users/change-password
     * 비밀번호 변경
     */
    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDto requestDto) {

        userService.changePassword(userDetails.getUsername(), requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }
}