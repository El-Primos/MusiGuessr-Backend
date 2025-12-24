package com.musiguessr.backend.dto.tournament;

import com.musiguessr.backend.model.TournamentState;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Long playlistId;
    private Long creatorId;
    private String creatorUsername;
    private TournamentState status;
    private OffsetDateTime createDate;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Integer participantCount;
}
