package com.musiguessr.backend.dto.tournament;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentUpdateRequestDTO {
    private String name;
    private String description;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}

