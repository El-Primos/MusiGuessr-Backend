package com.musiguessr.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "followings")
public class Following {

    @EmbeddedId
    private FollowingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followingId")
    @JoinColumn(name = "following_id")
    private User following;

    @Column(name = "pending", nullable = false)
    private Boolean pending = false;

    @Column(name = "is_accepted", nullable = false)
    private Boolean accepted = false;
}
