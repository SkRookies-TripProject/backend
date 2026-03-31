package com.costrip.costrip_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;
    private String tokenType;   // "Bearer"
    private String name;
    private String email;
}
