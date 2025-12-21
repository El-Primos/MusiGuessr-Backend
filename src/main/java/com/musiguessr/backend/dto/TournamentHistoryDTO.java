package com.musiguessr.backend.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentHistoryDTO {
    private Long tournamentId;
    private Integer userScore;
    private String status;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
}
