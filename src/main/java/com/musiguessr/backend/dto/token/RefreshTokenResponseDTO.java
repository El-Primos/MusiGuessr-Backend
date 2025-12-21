package com.musiguessr.backend.dto.token;

import lombok.Data;

@Data
public class RefreshTokenResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType;

    public RefreshTokenResponseDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
    }
}