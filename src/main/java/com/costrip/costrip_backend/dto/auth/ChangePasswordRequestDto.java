package com.costrip.costrip_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordRequestDto {

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
    private String newPassword;
}