package com.musiguessr.backend.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeProfileDTO {
    private Long id;
    private String name;
    private String userName;
    private String email;
    private Integer totalScore;
    private Long gamesPlayed;
    private OffsetDateTime lastPlayedAt;
}
