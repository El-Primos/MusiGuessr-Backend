package com.musiguessr.backend.dto.post;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostShareResponseDTO {

    private Long postId;
    private Long userId;
    private Long gameHistoryId;
    private Integer gameScore;
    private OffsetDateTime playedAt;
    private List<Boolean> predictions;
    private Map<String, RoundSummaryDTO> gameHistory;
}
