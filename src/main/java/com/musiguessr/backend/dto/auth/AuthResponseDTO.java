package com.musiguessr.backend.dto.auth;

import com.musiguessr.backend.model.Role;
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

    public AuthResponseDTO(String message, Long id, String username, String email, Role role) {
        this.message = message;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role.name();
    }
}