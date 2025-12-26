package com.musiguessr.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PlaylistItemId implements Serializable {

    @Column(name = "playlist_id")
    private Long playlistId;

    @Column(name = "position")
    private Integer position;
}
