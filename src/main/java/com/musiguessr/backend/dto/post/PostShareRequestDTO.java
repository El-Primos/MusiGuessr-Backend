package com.musiguessr.backend.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostShareRequestDTO {

    @NotNull
    private Long gameHistoryId;
}
