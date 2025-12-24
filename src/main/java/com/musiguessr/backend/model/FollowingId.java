package com.musiguessr.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FollowingId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "following_id", nullable = false)
    private Long followingId;
}
