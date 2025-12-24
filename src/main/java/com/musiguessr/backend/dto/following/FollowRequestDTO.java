package com.musiguessr.backend.dto.following;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequestDTO {
    private Long requesterId;
    private String requesterUsername;
    private Boolean pending;
    private Boolean accepted;
}
