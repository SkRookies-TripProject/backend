package com.costrip.costrip_backend.dto.auth;

import com.costrip.costrip_backend.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponseDto {

    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;

    public static AdminUserResponseDto from(User user) {
        return AdminUserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
