package com.musiguessr.backend.dto.game;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameRoundGuessDTO {
    @NotNull
    private Long musicId;

    @NotNull
    private Long elapsedMs;
}
