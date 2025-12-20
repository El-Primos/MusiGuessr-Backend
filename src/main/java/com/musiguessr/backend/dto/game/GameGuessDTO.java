package com.musiguessr.backend.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GameGuessDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long songId;
        private Long elapsedMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Boolean correct;
        private Integer gainedScore;
        private Integer totalScore;
    }
}
