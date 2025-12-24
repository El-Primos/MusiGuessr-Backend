package com.musiguessr.backend.dto.following;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowFriendDTO {
    private Long userId;
    private String username;
}
