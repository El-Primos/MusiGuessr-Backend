package com.musiguessr.backend.dto.auth;

import com.musiguessr.backend.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String message;
    private Long id;
    private String username;
    private String email;
    private String role;
    private String accessToken;
    private String refreshToken;
    private String tokenType;

    public AuthResponseDTO(
            String message,
            Long id,
            String username,
            String email,
            UserRole userRole,
            String accessToken,
            String refreshToken
    ) {
        this.message = message;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = userRole.name();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
    }
}