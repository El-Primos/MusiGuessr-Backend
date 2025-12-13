package com.musiguessr.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank
    private String userName;

    @NotBlank
    private String password;
}
