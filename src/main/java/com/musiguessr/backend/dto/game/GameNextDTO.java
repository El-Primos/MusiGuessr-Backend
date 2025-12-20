package com.musiguessr.backend.dto.game;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameNextDTO {

    private Integer round;
    private String previewUrl;
    private OffsetDateTime deadlineAt;

    private List<Candidate> candidates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private Long songId;
        private String name;
        private String artistName;
    }
}
