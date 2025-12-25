package com.musiguessr.backend.dto.user;

import com.musiguessr.backend.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {

    private String name;

    private String username;

    @Email
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private UserRole role;
}
