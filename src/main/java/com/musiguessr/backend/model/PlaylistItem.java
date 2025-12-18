package com.musiguessr.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "playlist_items")
public class PlaylistItem {

    @EmbeddedId
    private PlaylistItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(
            name = "playlist_id",
            foreignKey = @ForeignKey(name = "fk_playlist_item_playlist")
    )
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(
            name = "song_id",
            foreignKey = @ForeignKey(name = "fk_playlist_item_music")
    )
    private Music song;

    // Needed for POST /playlists/:id/songs position? and reorder endpoint
    @Column
    private Integer position;
}
