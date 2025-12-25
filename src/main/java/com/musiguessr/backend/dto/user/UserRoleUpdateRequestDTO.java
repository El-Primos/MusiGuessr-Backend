package com.musiguessr.backend.dto.user;

import com.musiguessr.backend.model.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateRequestDTO {

    @NotNull
    private UserRole role;
}
